/*
 * Copyright 2013 Rui Sun (SteveSunCanada@gmail.com)
 *
 * The Netty Project licenses this file to you under the Apache License,
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CLAppSession extends DefaultAppSession {
	
	private AppSessionClassLoader classLoader;
	
	public CLAppSession(DefaultAppSessionFactory factory, String sessionKey, String[] locations) {
		this(factory, sessionKey, locations, new String[0]);
	}
	
	public CLAppSession(DefaultAppSessionFactory factory, String sessionKey, String[] locations, String[] jarFiles) {
		super(factory, sessionKey);
		//this.classLoader=new AppSessionClassLoader(locations);
		
		this.classLoader=new AppSessionClassLoader(locations);
	}

	public Class<?> loadClassByName(String location, String name) throws ClassNotFoundException 
	{
		//System.out.println("HERE======>CLAppSession.loadClassByName() rootPath: "+location);
		//System.out.println("HERE======>CLAppSession.loadClassByName() LOAD CLASS BY NAME: "+name);
				
		return this.classLoader.loadClass(name);		
	}
	
	@Override
	public boolean isExpiry() {
		//System.out.println("HERE======>CLAppSession.isExpiry() #1");
		boolean isExpiry = false;
		//isExpiry = this.classLoader.needToReload();
		//System.out.println("HERE======>CLAppSession.isExpiry() #2 "+isExpiry);
		return isExpiry;
		
	}
	
	private static class AppSessionClassLoader extends ClassLoader {
		
		private String currentLocation;
//		private String clazzName;
		private Map<String, Date> locationUpdates;
		private Map<String, Class<?>> classCache;
		
		public AppSessionClassLoader(String[] locations, ClassLoader parent) {
			super(parent);
			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader() #1");			//
			this.locationUpdates = new HashMap<String, Date>();
			this.classCache = new HashMap<String, Class<?>>();
			
			for(String path: locations)
				this.addRootPath(path.toLowerCase());
			
		}
		
		public AppSessionClassLoader(String[] locations) {			
			this(locations, AppSessionClassLoader.class.getClassLoader());
			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader() #2");
		}
		
//		public boolean needToReload() {
//			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.needToReload() #1");
//			for(Iterator<String> iter = this.locationUpdates.keySet().iterator(); iter.hasNext();)
//			{
//				String root = iter.next();
//				File rootDir = new File(root);
//				if(!rootDir.exists())
//					throw new IllegalArgumentException("Class root path, "+currentLocation+" does not exist!");
//				if(!rootDir.isDirectory())
//					throw new IllegalArgumentException("Class root path, "+currentLocation+" is not a directory!");
//				Date lastModified = this.locationUpdates.get(root);
//				if(lastModified.getTime() != rootDir.lastModified())
//					return true;
//			}
//			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.needToReload() #2");
//			return false;
//		}
//		
//		public Class<?> loadClassByName(String location, String clazzName) throws ClassNotFoundException {
//			
//			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.loadClassByName() rootPath: "+location);
//			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.loadClassByName() clazzName: "+clazzName);
//			this.currentLocation = location.toLowerCase();
//			if(!this.locationUpdates.containsKey(this.currentLocation))
//				this.addRootPath(this.currentLocation);
//			this.clazzName=clazzName;
//			return this.loadClassByName(clazzName);
//		}
		
//		private Class<?> loadClassByName(String name) throws ClassNotFoundException {
//			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.loadClassByName() #1 NAME: "+name);
//			
//			Class<?> loadedClass = null;
//			
//			if(this.clazzName.equals(name))
//			{
//				loadedClass = this.findLoadedClass(name);
//				if(null != loadedClass) return loadedClass;
//				//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.loadClassByName() #1.1 NAME: "+name);			
//				loadedClass = this.findClass(name);
//				//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.loadClassByName() #1.2 loadedClass: "+loadedClass);			
//			}
//			else
//			{
//				loadedClass = super.loadClass(name);
//			}
//			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.loadClassByName() #2 loadedClass: "+loadedClass);
//			return loadedClass;
//		}
		
		public Class<?> loadClass(String className) throws ClassNotFoundException { 
			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.loadClass() #1 NAME: "+className);		
			Class<?> loadedClass = super.loadClass(className);
			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.loadClass() #2 CLASS: "+className+" "+(loadedClass != null?" HAS BEEN LOADED":"HAS NOT BEEN LOADED YET!"));	
			if(loadedClass != null) return loadedClass;
			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.loadClass() #3 NAME: "+className);	
			return this.findClass(className);
	    } 
		
		public Class<?> findClass(String name) throws ClassNotFoundException{
			
			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #1 NAME: "+name);
			
			Class<?> foundClass = null;
			//foundClass = this.classCache.get(name);
			foundClass = this.findLoadedClass(name);
			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #2 CLASS CACHE: "+this.classCache);
			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #2 CLASS: "+name+" "+(foundClass != null?" HAS BEEN FOUND":"HAS NOT BEEN FOUND YET!"));
			if(null != foundClass) return foundClass;
			
			
			
			Iterator<String> iter = this.locationUpdates.keySet().iterator();
			while(null == foundClass && iter.hasNext()) 
			{
				String root = iter.next();
				//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #2.1 root: "+root);
				//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #2.2 rootPath: "+this.currentLocation);
				//if(!root.equalsIgnoreCase(this.currentLocation.toLowerCase()))
					this.currentLocation = root;
				//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #2.3 rootPath: "+this.currentLocation);
				foundClass = this.loadClassFromLocation(this.currentLocation, name);
				//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #2.4 loadedClass: "+loadedClass);
			};
			
			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #3 foundClass: "+foundClass);	
			if(null == foundClass) throw new ClassNotFoundException();
			
			this.classCache.put(name, foundClass);
			
			return foundClass;
		}
		
		
//		public Class<?> findClass(String name) throws ClassNotFoundException{
//			
//			System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #1 NAME: "+name);		
//			
//			Class<?> loadedClass = this.loadClassFromLocation(this.currentLocation, name);
//			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #2 loadedClass: "+loadedClass);
//			if(null!=loadedClass) return loadedClass;
//			
//			Iterator<String> iter = this.locationUpdates.keySet().iterator();
//			while(null == loadedClass && iter.hasNext()) 
//			{
//				String root = iter.next();
//				//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #2.1 root: "+root);
//				//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #2.2 rootPath: "+this.currentLocation);
//				if(!root.equalsIgnoreCase(this.currentLocation.toLowerCase()))
//					this.currentLocation = root;
//				//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #2.3 rootPath: "+this.currentLocation);
//				loadedClass = this.loadClassFromLocation(this.currentLocation, name);
//				//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #2.4 loadedClass: "+loadedClass);
//			};
//			
//			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.findClass() #3 loadedClass: "+loadedClass);	
//			if(null == loadedClass) throw new ClassNotFoundException();
//			
//			return loadedClass;
//			
//	    }
		
		private Class<?> loadClassFromLocation(String location, String clazzName) {
			
			File clazzFile = new File(getClassFilePath(location, clazzName));
			//System.out.println("HERE======>CLAppSession$AppSessionClassLoader.loadClassFromRemote() CLASS ABS PATH: "+clazzFile.getAbsolutePath());
			if(!clazzFile.exists()) return null;
			
			FileInputStream fileInput = null;
			ByteArrayOutputStream dataOutput = null;
			byte[] classData = null;
			try {						
				fileInput = new FileInputStream(clazzFile);
				dataOutput = new ByteArrayOutputStream();
		        byte[] buffer = new byte[1024];
		        int count = fileInput.read(buffer);
		        while(count != -1)
		        {
		        	dataOutput.write(buffer, 0, count);
		            count = fileInput.read(buffer);
		        }
		        
		        classData = dataOutput.toByteArray();
		        
		        return this.defineClass(clazzName, classData, 0, classData.length);
		        
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				
				try {
					if(null!=fileInput) fileInput.close();
					if(null!=dataOutput) dataOutput.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return null;
		}

		private void addRootPath(String rootPath) {
			
			File rootDir = new File(rootPath);
			if(!rootDir.exists())
				throw new IllegalArgumentException("Class root path, "+rootPath+" does not exist!");
			if(!rootDir.isDirectory())
				throw new IllegalArgumentException("Class root path, "+rootPath+" is not a directory!");
			
			this.locationUpdates.put(rootPath, new Date(rootDir.lastModified()));
		}
		
		private static String getClassFilePath(String rootPath, String clazz) {
			StringBuilder strBuilder = new StringBuilder(rootPath);
			strBuilder.append(clazz.replace(".", "/"));
			strBuilder.append(".class");
			return strBuilder.toString();
		}
	}

}
