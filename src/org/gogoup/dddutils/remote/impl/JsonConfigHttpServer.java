package org.gogoup.dddutils.remote.impl;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.gogoup.dddutils.appsession.AppSessionFactory;
import org.gogoup.dddutils.appsession.impl.CLAppSessionFactoryConfig;
import org.gogoup.dddutils.appsession.impl.CLAppSessionFactory;
import org.gogoup.dddutils.misc.CodingHelper;
import org.gogoup.dddutils.misc.Helper;
import org.gogoup.dddutils.remote.RemoteHttpRequest;
import org.gogoup.dddutils.remote.RemoteHttpServerConfig;
import org.gogoup.dddutils.remote.RemoteHttpServerDelegate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

public class JsonConfigHttpServer extends DefaultHttpServer{
    
    public static final String CONFIG_FILE_SYSTEM_PROPERTY_NAME = "org.gogoup.dddutils.remote.cfg.conf"; 
    
    private volatile Map<String, AppSessionFactory> applications;
    
    public JsonConfigHttpServer() {
        this(System.getProperty(CONFIG_FILE_SYSTEM_PROPERTY_NAME), Executors.newCachedThreadPool());        
    }    
    
    public JsonConfigHttpServer(String configFile, ExecutorService sessionExecutorService) {
        super(readJsonServerConfigWithSystemProperty(configFile), null, sessionExecutorService, null);
        this.setDelegate(new DefaultServerDelegate(
                this.getConfig().getSessionIdField(), new HashMap<String, String>()));
        this.applications = new LinkedHashMap<String, AppSessionFactory>(0);
    }
    
    @Override
    public AppSessionFactory getApplication(String applicationId) {
        if (this.getConfig().isDebugMode()) {
            debugModeUpdateApplications();
        }
        return this.applications.get(applicationId);
    }

    @Override
    public void startup() {
        if (!this.getConfig().isDebugMode()) {
            new Thread(new AgentThread(this)).start();
        } else {
            debugModeUpdateApplications();
        }
        super.startup();
    }
    
    @Override 
    public void shutdown() {
        super.shutdown();
        this.applications.clear();
        this.applications = null;
    }
    
    public void replaceApplications(Map<String, AppSessionFactory> applications) {
        Map<String, AppSessionFactory> oldApplications = this.applications;
        this.applications = applications;
        oldApplications.clear();
        oldApplications = null;
    }
    
    private void debugModeUpdateApplications() {
        synchronized(this) {
            File configFile = getRepositoryJsonConfigFile(this.getConfig());
            updateApplications(this, configFile);
        }
    }

    private static void updateApplications(JsonConfigHttpServer server, File configFile) {
        
        String json = Helper.readFileAsJson(configFile);
    
        Map<String, CLAppSessionFactoryConfig> configs = readJsonAppSessionFactoryConfig(
                server.getConfig().getApplications(), json);
        
        Map<String, AppSessionFactory> applications = new LinkedHashMap<String, AppSessionFactory>(
                configs.size());
        
        for (Iterator<CLAppSessionFactoryConfig> iter=configs.values().iterator(); iter.hasNext();) {
            CLAppSessionFactoryConfig config = iter.next();
            AppSessionFactory application = new CLAppSessionFactory(config);
            applications.put(application.getApplicationId(), application);
        }
        
        server.replaceApplications(applications);
        
        Map<String, String> applicationMapping = readJsonApplicationMappingConfig(json);
        
        server.setDelegate(
                new DefaultServerDelegate(server.getConfig().getSessionIdField(), applicationMapping));
    }
    
    private static File getRepositoryJsonConfigFile(RemoteHttpServerConfig config) {
        File configFile = new File(config.getApplications());
        if (!configFile.exists()) {
            throw new IllegalArgumentException(config.getApplications() + " doesn't exist.");
        }
        return configFile;
    }

    private static RemoteHttpServerConfig readJsonServerConfigWithSystemProperty(String path) {        
        String json = Helper.readFileAsJson(path);
        return DefaultHttpServerConfigCodec.fromJson(json, RemoteHttpServerConfig.class);
    }

    private static Map<String, CLAppSessionFactoryConfig> readJsonAppSessionFactoryConfig(
            String repositoryPath, String json) {
        
        Map<String, JsonElement> configJson = new Gson().fromJson(json, 
                new TypeToken<Map<String, JsonElement>>() {}.getType());
        
        JsonArray appsJson = (JsonArray) configJson.get("Applications");
        Map<String, CLAppSessionFactoryConfig> configs = new HashMap<String, CLAppSessionFactoryConfig>();
        for (int i=0; i<appsJson.size(); i++) {
            
            JsonObject jsonObj = appsJson.get(i).getAsJsonObject();         
            CLAppSessionFactoryConfig factoryConfig = AppSessionFactoryConfigCodec.fromJson(
                    jsonObj, CLAppSessionFactoryConfig.class);

            configs.put(factoryConfig.getApplicationId(), factoryConfig);                       
        }
        
        return configs;
    }
    
    private static Map<String, String> readJsonApplicationMappingConfig(String json) {
        
        Map<String, JsonElement> configJson = new Gson().fromJson(json, 
                new TypeToken<Map<String, JsonElement>>() {}.getType());
        
        Map<String, String> appMapping = new LinkedHashMap<String, String>();
        
        JsonArray jsonArray = (JsonArray) configJson.get("ApplicationMapping");        
        for (int i=0; i<jsonArray.size(); i++) {
            
            JsonObject jsonObj = jsonArray.get(i).getAsJsonObject();         
            
            String applicationId = jsonObj.get("ApplicationId").getAsString();
            String[] urlPatterns = new Gson().fromJson(jsonObj.get("UrlPatterns"), String[].class);
            for (String pattern: urlPatterns) {
                appMapping.put(pattern.trim().toLowerCase(), applicationId);
            }
        }
        
        return appMapping;
    }
    
    /**
     * This class will keep checking the configure file (repository.conf) under the
     * specified repository path at an interval time.
     * 
     * If the version file has been modified -- the version file's last modify
     * time is different with the saved one, then reading the version number,
     * and read the configuration information and class files from the
     * corresponding version directory.
     * 
     * 
     * @author sunr
     * 
     */
    private static class AgentThread implements Runnable {
        
        private boolean isRunning;
        private JsonConfigHttpServer server;
        private RemoteHttpServerConfig serverConfig;
        private long lastReadTime;
        private File repositoryConfigFile;        
        
        public AgentThread(JsonConfigHttpServer server) {
            
            this.isRunning = true;
            this.server = server;
            this.serverConfig = server.getConfig();
            this.lastReadTime = 0;
            this.repositoryConfigFile = getRepositoryJsonConfigFile(server.getConfig());
        }
        
        @Override
        public void run() {            
            long autoDeplyDelayInSec = this.serverConfig.getAutoDeployDelay() * 1000;
            while (this.isRunning) {
                if ((this.lastReadTime == 0 
                        || this.repositoryConfigFile.lastModified() > this.lastReadTime)
                        || this.serverConfig.isDebugMode()) {
                  
                    this.lastReadTime = this.repositoryConfigFile.lastModified();
                    updateApplications(this.server, this.repositoryConfigFile);
              }
              
              try {
                  Thread.sleep(autoDeplyDelayInSec);
              } catch (InterruptedException e) {                  
                  e.printStackTrace();
              }
            }
            System.out.println("AgentThread has stopped.");
        }  
    }
    
    private static class DefaultServerDelegate implements RemoteHttpServerDelegate {
        private String sessionIdField;
        private Map<String, String> applicationMap;
        
        public DefaultServerDelegate(String sessionIdField, Map<String, String> applicationMap) {
            this.sessionIdField = sessionIdField;
            this.applicationMap = applicationMap;            
        }
    
        @Override
        public String getApplicationId(RemoteHttpRequest request) {
            
            return applicationMap.get(request.getPath().toLowerCase());
        }
    
        @Override
        public String getSessionKey(RemoteHttpRequest request,
                String applicationId) {
            
            String sessionKey = request.getQueryParameter(this.sessionIdField);
            if (null == sessionKey
                    || sessionKey.trim().length() == 0) {
                sessionKey = CodingHelper.nextUUIDString();
            }
//            return "ABC1234567890";
            return sessionKey;
        }
        
    }
    
    public static final Gson DefaultHttpServerConfigCodec = new GsonBuilder().
            registerTypeAdapter(RemoteHttpServerConfig.class, new DefaultHttpServerConfigAdapter()).create();
    
    public static final Gson AppSessionFactoryConfigCodec = new GsonBuilder().
            registerTypeAdapter(CLAppSessionFactoryConfig.class, new AppSessionFactoryConfigAdapter()).create();
        
    private static class DefaultHttpServerConfigAdapter implements JsonDeserializer<RemoteHttpServerConfig> {
    
        public RemoteHttpServerConfig deserialize(JsonElement json, Type typeOfT, 
                JsonDeserializationContext context) throws JsonParseException {
            
            JsonObject jobj = json.getAsJsonObject();
                        
            return new RemoteHttpServerConfig(
                    jobj.getAsJsonPrimitive("Port").getAsInt(),
                    (jobj.getAsJsonPrimitive("SslCert").getAsString().trim().length()>0?true:false),
                    jobj.getAsJsonPrimitive("ReaderIdleTime").getAsLong(),
                    jobj.getAsJsonPrimitive("WriterIdleTime").getAsLong(),
                    jobj.getAsJsonPrimitive("ApplicationVendor").getAsString(),
                    jobj.getAsJsonPrimitive("Applications").getAsString(),
                    jobj.getAsJsonPrimitive("AutoDeployDelay").getAsInt(),
                    CodingHelper.getSizeInBytes(jobj.getAsJsonPrimitive("MaxPostSize").getAsString()),
                    jobj.getAsJsonPrimitive("SessionIdParameterName").getAsString(),
                    jobj.getAsJsonPrimitive("UploadTemp").getAsString(),
                    CodingHelper.getSizeInBytes(jobj.getAsJsonPrimitive("UploadCacheSize").getAsString()),
                    jobj.getAsJsonPrimitive("DebugMode").getAsBoolean()
                    );
        }
    }
   
    private static class AppSessionFactoryConfigAdapter implements JsonDeserializer<CLAppSessionFactoryConfig> {
    
        public CLAppSessionFactoryConfig deserialize(JsonElement json, Type typeOfT, 
                JsonDeserializationContext context) throws JsonParseException {
            
            JsonObject jobj = json.getAsJsonObject();
            //String repPath = jobj.getAsJsonPrimitive("RepositoryPath").getAsString();
            String base = jobj.getAsJsonPrimitive("ApplicationBase").getAsString();
            //base = repPath + System.getProperty("file.separator") + base;
            
            Map<String, Object> applicationParameters = new LinkedHashMap<String, Object>();
            JsonObject paramObj = jobj.getAsJsonObject("ApplicationParameters");
            for (Map.Entry<String,JsonElement> paramEntry : paramObj.entrySet()) {
                String paramName = paramEntry.getKey();
                JsonPrimitive param = paramEntry.getValue().getAsJsonPrimitive();               
                applicationParameters.put(paramName, CodingHelper.getObjectFromJson(param));
            }
            
            return new CLAppSessionFactoryConfig(
                    jobj.getAsJsonPrimitive("ApplicationId").getAsString(),
                    base,
                    jobj.getAsJsonPrimitive("ConcurrentSessionSize").getAsInt(),
                    jobj.getAsJsonPrimitive("SessionCacheSize").getAsInt(),
                    applicationParameters,
                    new Gson().fromJson(jobj.get("Locations"), String[].class),
                    new Gson().fromJson(jobj.get("ParameterResolvers"), String[].class),
                    new Gson().fromJson(jobj.get("AppServiceResolvers"), String[].class),
                    new Gson().fromJson(jobj.get("ApplicationParameterResolvers"), String[].class)
                    );
        }
    }
    
}
