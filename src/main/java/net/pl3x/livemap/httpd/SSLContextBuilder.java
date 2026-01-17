package net.pl3x.livemap.httpd;

import net.pl3x.livemap.LiveMap;
import net.pl3x.livemap.configuration.Config;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

abstract class SSLContextBuilder {
    private SSLContextBuilder() {
        // Empty constructor to pacify javadoc lint
    }

    @NotNull
    static SSLContext build() throws IOException {
        // how to convert pem to pfx
        // https://community.letsencrypt.org/t/creating-a-pkcs-12/157883
        try {
            SSLContext context = SSLContext.getInstance(Config.HTTPD_HTTPS_ALGORITHM);
            context.init(
                buildKeyManagers(),
                buildTrustManagers(),
                SecureRandom.getInstanceStrong()
            );
            return context;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException("Unable to create and initialise the SSLContext", e);
        }
    }

    private static KeyStore loadKeyStore(char[] password) throws IOException {
        Path path = LiveMap.instance().getDataDirectory().resolve(Config.HTTPD_HTTPS_KEYSTORE_FILE);
        try (InputStream stream = path.toUri().toURL().openStream()) {
            KeyStore keyStore = KeyStore.getInstance(Config.HTTPD_HTTPS_KEYSTORE_TYPE);
            keyStore.load(stream, password);
            return keyStore;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new IOException(String.format("Unable to load KeyStore %s", path), e);
        }
    }

    private static KeyManager[] buildKeyManagers() throws IOException {
        try {
            char[] password = Config.HTTPD_HTTPS_KEYSTORE_PASSWORD.toCharArray();
            KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            factory.init(loadKeyStore(password), password);
            return factory.getKeyManagers();
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new IOException("Unable to initialise KeyManager[]", e);
        }
    }

    private static TrustManager[] buildTrustManagers() throws IOException {
        try {
            TrustManagerFactory factory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            factory.init((KeyStore) null);
            return factory.getTrustManagers();
        } catch (KeyStoreException | NoSuchAlgorithmException e) {
            throw new IOException("Unable to initialise TrustManager[]", e);
        }
    }
}
