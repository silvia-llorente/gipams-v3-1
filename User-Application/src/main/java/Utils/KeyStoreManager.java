package Utils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Properties;

public class KeyStoreManager {

    protected static String GetSecureKey() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {
        Properties props = new Properties();
        props.load(KeyStoreManager.class.getClassLoader().getResourceAsStream("app.properties"));
        char[] pwdArray = props.getProperty("keystorePassword").toCharArray();
        String csrfKeyID = props.getProperty("csrfKeyAlias");
        String keystorePath = props.getProperty("keystorePath");

        KeyStore ks = KeyStore.getInstance("PKCS12");
        Path path = Paths.get(keystorePath, "serverSecrets.p12");

        boolean newFile = Files.notExists(path);
        if (newFile) {
            ks.load(null, pwdArray);
        } else {
            ks.load(Files.newInputStream(path), pwdArray);
        }

        KeyStore.ProtectionParameter password = new KeyStore.PasswordProtection(pwdArray);
        if (!ks.containsAlias(csrfKeyID)) {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();
            byte[] data = new byte[16];
            secureRandom.nextBytes(data);
            SecretKey secretKey = new SecretKeySpec(data, "AES");
            KeyStore.SecretKeyEntry inputSecret = new KeyStore.SecretKeyEntry(secretKey);
            ks.setEntry(csrfKeyID, inputSecret, password);
        }
        
        System.out.println("Path: "+ path);
        if(newFile){
            try (OutputStream file = Files.newOutputStream(path)) {
                ks.store(file, pwdArray);
            }
        }

        KeyStore.SecretKeyEntry secret = (KeyStore.SecretKeyEntry) ks.getEntry(csrfKeyID, password);
        SecretKey secretKey = secret.getSecretKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

}
