/*
 * Corg.gogoup.dddutils.appsession.implda@gmail.com)
 *
CLAppSessionFactory2ct licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.gogoup.dddutils.appsession.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.gogoup.dddutils.appsession.AppServiceResolver;
import org.gogoup.dddutils.appsession.AppSession;
import org.gogoup.dddutils.appsession.AppSessionFactory;
import org.gogoup.dddutils.appsession.AppSessionFactoryDelegate;
import org.gogoup.dddutils.pool.MappedPool;
import org.gogoup.dddutils.pool.MappedPoolDelegate;
import org.gogoup.dddutils.pool.ParameterResolver;


public class CLAppSessionFactory extends DefaultAppSessionFactory {
   
    private CLAppSessionFactoryConfig config;
    private ClassLoader classLoader;
    
    public CLAppSessionFactory(CLAppSessionFactoryConfig config) {
        super(config, null, new CLAppSessionPoolDelegate());
        this.config = config;        
        this.classLoader = createClassLoader(this.config);
        this.setDelegate(new CLAppSessionFactoryDeletage(this.config, this.classLoader));
    }
            
    @Override
    public void close() {
        super.close();
    }
    
    private static ClassLoader createClassLoader(CLAppSessionFactoryConfig config) {
        String base = config.getApplicationBase() + "/";
        String[] locations = config.getLocations();
        URL[] urls = new URL[locations.length];
        try {
            for (int i=0; i<urls.length; i++) {
                String location = base + locations[i];
                if (location.endsWith(".jar")) {
                    urls[i] = new URL("jar:file:" + location + "!/");
                } else {
                    urls[i] = new URL("file:" + location);
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return new ParentLastURLClassLoader(urls);
    }
    
    private static class CLAppSessionFactoryDeletage implements AppSessionFactoryDelegate {
        
        private CLAppSessionFactoryConfig config;
        private ClassLoader classLoader;
        
        public CLAppSessionFactoryDeletage(CLAppSessionFactoryConfig config, ClassLoader classLoader) {
            this.config = config;
            this.classLoader = classLoader;
        }
        
        @Override
        public ParameterResolver[] registerParameterResolvers(AppSessionFactory factory, AppSession session) {
            
            return new ParameterResolver[] { 
                    new CLParameterResolver(this.config.getParameterResolvers(), this.classLoader)};
        }
        
        @Override
        public AppServiceResolver[] registerAppServiceResolvers(AppSessionFactory factory, AppSession session) {
            
            return new AppServiceResolver[] {
                    new CLAppServiceResolver(this.config.getAppServiceResolvers(), this.classLoader) };
        }

        @Override
        public ParameterResolver[] registerApplicationParameterResolvers(AppSessionFactory factory) {
            return new ParameterResolver[] { 
                    new CLParameterResolver(this.config.getApplicationParameterResolvers(), this.classLoader)};
        }
    }
    
    private static class CLAppSessionPoolDelegate implements MappedPoolDelegate<String, DefaultAppSession> {

        public CLAppSessionPoolDelegate() { }
        
        @Override
        public boolean willBorrow(MappedPool<String, DefaultAppSession> pool, String key, DefaultAppSession item) {
            return true;
        }
        
        @Override
        public boolean willReturn(MappedPool<String, DefaultAppSession> pool, String key, DefaultAppSession item) {
            return true;
        }
    }
    
    
    /**
     * A parent-last classloader that will try the child classloader first and then the parent.
     * This takes a fair bit of doing because java really prefers parent-first.
     * 
     * For those not familiar with class loading trickery, be wary
     */
    private static class ParentLastURLClassLoader extends ClassLoader 
    {
        private ChildURLClassLoader childClassLoader;

        public ParentLastURLClassLoader(URL[] urls) {
            super(Thread.currentThread().getContextClassLoader());
        
            childClassLoader = new ChildURLClassLoader(urls, ParentLastURLClassLoader.class.getClassLoader());
        }

        @Override
        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            
            return childClassLoader.findClass(name);
//            try {
//                // first we try to find a class inside the child classloader
//                return childClassLoader.findClass(name);
//            } catch( ClassNotFoundException e ) {
//                // didn't find it, try the parent
//                return super.loadClass(name, resolve);
//            }
        }

        /**
         * This class delegates (child then parent) for the findClass method for a URLClassLoader.
         * We need this because findClass is protected in URLClassLoader
         */
        private static class ChildURLClassLoader extends URLClassLoader
        {
            private ClassLoader realParent;

            public ChildURLClassLoader(URL[] urls, ClassLoader realParent) {
                super(urls, null);

                this.realParent = realParent;
            }

            @Override
            public Class<?> findClass(String name) throws ClassNotFoundException {
                
                Class<?> loadedClass = this.findLoadedClass(name);
                if (null != loadedClass) {
                    return loadedClass;
                }
                
                try {
                    // first try to use the URLClassLoader findClass
                    return super.findClass(name);
                } catch( ClassNotFoundException e ) {                    
                    // if that fails, we ask our real parent classloader to load the class (we give up)
                    return realParent.loadClass(name);
                }
            }
        }

        /**
         * This class allows me to call findClass on a classloader
         */
//        private static class ParentClassLoader extends ClassLoader
//        {
//            public ParentClassLoader(ClassLoader parent) {
//                super(parent);
//            }
//        
//            @Override
//            public Class<?> findClass(String name) throws ClassNotFoundException {
//                return super.findClass(name);
//            }
//        }
    }
}
