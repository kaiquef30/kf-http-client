package org.httpclient.kf.http;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public final class KfSslUtils {
    
    private KfSslUtils() {
    
    }
    
    public static SSLContext createSslContextFromP12(String path, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(path)) {
            keyStore.load(fis, password.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, password.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }
    
    /**
     * AVISO: Não use em produção. É apenas para cenários de desenvolvimento/teste
     * onde você precisa “ignorar” a verificação de CA/chain.
     */
    @Deprecated(since = "1.0.0", forRemoval = false)
    public static SSLContext trustAllSslContext() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                        validateCertificateChain(chain, authType, "client");
                    }
                    
                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType)
                            throws CertificateException {
                        validateCertificateChain(chain, authType, "server");
                    }
                    
              
                    private void validateCertificateChain(X509Certificate[] chain,
                                                          String authType,
                                                          String peerType) throws CertificateException {
                        if (chain == null || chain.length == 0) {
                            throw new CertificateException("Empty " + peerType + " certificate chain");
                        }
                        
                        if (authType == null || authType.isBlank()) {
                            throw new CertificateException("Missing authType for " + peerType + " certificate chain");
                        }
                        
                        for (X509Certificate cert : chain) {
                            if (cert == null) {
                                throw new CertificateException("Null certificate in " + peerType + " certificate chain");
                            }
                            cert.checkValidity();
                        }
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        return sslContext;
    }

}
