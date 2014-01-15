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

package org.gogoup.dddutils.misc;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class RemoteAPISecurityHelper {
    
    // private static final String CERTIFICATE_FILE =
    // "BuzzSpot_Certificates.p12";
    // private static final String CERTIFICATE_FILE_PASSCODE = "123456";
    
    private static final TrustManager[] TrustManagers = new TrustManager[] { new RemoteAPISecurityTrustManager() };
    private static final String PROTOCOL = "TLS";
    private static SSLContext APNServerSSLContext;
    private static SSLContext ServerSSLContext;
    private static SSLContext ClientSSLContext;
    
    public static SSLContext getServerSSLContext() {
        if (null == ServerSSLContext) {
            try {
                String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
                if (algorithm == null) {
                    algorithm = "SunX509";
                }
                
                /*
                 * Dummy Server SSL Context initialization
                 */
                
                KeyStore ks = KeyStore.getInstance("JKS");
                ks.load(BogusKeyStore.asInputStream(), BogusKeyStore.getKeyStorePassword());
                
                // Set up key manager factory to use our key store
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
                kmf.init(ks, BogusKeyStore.getCertificatePassword());
                
                // Initialize the SSLContext to work with our key managers.
                ServerSSLContext = SSLContext.getInstance(PROTOCOL);
                ServerSSLContext.init(kmf.getKeyManagers(), null, null);
                
            } catch (Exception e) {
                throw new Error("Failed to initialize the server-side SSLContext", e);
            }
        }
        return ServerSSLContext;
    }
    
    public static SSLContext getClientSSLContext() {
        if (null == ClientSSLContext) {
            try {
                /*
                 * Generic client SSL Context initialization
                 */
                ClientSSLContext = SSLContext.getInstance(PROTOCOL);
                ClientSSLContext.init(null, TrustManagers, null);
            } catch (Exception e) {
                throw new Error("Failed to initialize the client-side SSLContext", e);
            }
        }
        return ClientSSLContext;
    }
    
    public static SSLContext getAPNServerSSLContext(String cert, String password) {
        if (null == APNServerSSLContext) {
            try {
                /*
                 * APN Server SSL Context initialization
                 */
                String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
                if (algorithm == null) {
                    algorithm = "SunX509";
                }
                
                File certFile = new File(cert);
                if (!certFile.exists())
                    throw new RuntimeException("No Cert File Exist.");
                
                char[] passwKey = password.toCharArray();
                KeyStore apnKS = KeyStore.getInstance("PKCS12");
                apnKS.load(new FileInputStream(certFile), passwKey);
                /*
                 * Enumeration<String> aliases=apnKS.aliases();
                 * while(aliases.hasMoreElements()) {
                 * System.out.println("HERE======>KEY STORE ALIAS: "
                 * +aliases.nextElement()); }
                 */
                // Set up key manager factory to use our key store
                KeyManagerFactory apnKMF = KeyManagerFactory.getInstance(algorithm);
                apnKMF.init(apnKS, passwKey);
                // Initialize the SSLContext to work with our key managers.
                APNServerSSLContext = SSLContext.getInstance(PROTOCOL);
                APNServerSSLContext.init(apnKMF.getKeyManagers(), null, null);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
        return APNServerSSLContext;
    }
    
    private RemoteAPISecurityHelper() {
    }
    
    private static class RemoteAPISecurityTrustManager implements X509TrustManager {
        
        // X509Certificate[] certs;
        
        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            System.out.println("HERE======>RemoteAPISecurityTrustManager.checkClientTrusted()");
            System.err.println("UNKNOWN CLIENT CERTIFICATE: " + arg0[0].getSubjectDN() + "<Thread: "
                    + Thread.currentThread() + "; Time: " + System.currentTimeMillis() + ">");
        }
        
        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            System.out.println("HERE======>RemoteAPISecurityTrustManager.checkServerTrusted()");
            System.err.println("UNKNOWN SERVER CERTIFICATE: " + arg0[0].getSubjectDN() + "<Thread: "
                    + Thread.currentThread() + "; Time: " + System.currentTimeMillis() + ">");
        }
        
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            System.out.println("HERE======>RemoteAPISecurityTrustManager.getAcceptedIssuers()");
            return new X509Certificate[0];
        }
    }
    
}
