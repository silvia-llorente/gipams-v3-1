/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gcs.Utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.RFC3394WrapEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author alumne
 */
public class EncryptionUtil {
    static{
        try{
            Security.addProvider(new BouncyCastleProvider());
        }catch(Exception e){
            Logger.getLogger(ProtectionUtil.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    public static String genIV(){
        byte[] secureRandomKeyBytes = new byte[12];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(secureRandomKeyBytes);
        return Utils.encode(new SecretKeySpec(secureRandomKeyBytes, "AES").getEncoded());
    }
    
    public static String PBKDF2( final char[] password, final String PRF, final String saltS, final int iterations, final int keyLength ) {
        try {
            byte[] salt = Utils.decode(saltS);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA"+PRF.substring(PRF.length()-3));
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength*8);
            SecretKey key = skf.generateSecret( spec );
            byte[] res = key.getEncoded( );
            return Utils.encode(res);
        } catch ( NoSuchAlgorithmException | InvalidKeySpecException e ) {
            throw new RuntimeException( e );
        }
    }
    
    public static String wrapAES(String kek, String bkey){
        byte[] keyEncryptionKey = Utils.decode(kek);
        byte[] key = Utils.decode(bkey);
        RFC3394WrapEngine engine = new RFC3394WrapEngine(new AESEngine());
        engine.init(true, new KeyParameter(keyEncryptionKey));
        byte[] res = engine.wrap(key, 0, key.length);
        return Utils.encode(res);
    }
    
    public static String unwrapAES(String kek, String wrappedKeyS) {
        try {
            byte[] keyEncryptionKey = Utils.decode(kek);
            byte[] wrappedKey = Utils.decode(wrappedKeyS);
            RFC3394WrapEngine engine = new RFC3394WrapEngine(new AESEngine());
            engine.init(false, new KeyParameter(keyEncryptionKey));
            byte[] res = engine.unwrap(wrappedKey, 0, wrappedKey.length);
            return Utils.encode(res);
        } catch (InvalidCipherTextException ex) {
            Logger.getLogger(EncryptionUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static String wrapRSA(String hFunc, String MGF, Key pub, String plain){
        try {
            Cipher oaepFromInit = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING","SunJCE");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-384", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
            oaepFromInit.init(Cipher.ENCRYPT_MODE, pub, oaepParams);
            byte[] pt = oaepFromInit.doFinal(Utils.decode(plain));
            return Utils.encode(pt);
        } catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | NoSuchProviderException | InvalidAlgorithmParameterException ex) {
            Logger.getLogger(EncryptionUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static String unwrapRSA(String hashFuncS, String MGFS, PrivateKey priv, byte[] wrappedKey){
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());            
            Cipher oaepFromInit = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING","SunJCE");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-384", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);
            oaepFromInit.init(Cipher.DECRYPT_MODE, priv, oaepParams);
            byte[] pt = oaepFromInit.doFinal(wrappedKey);
            return Utils.encode(pt);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | NoSuchProviderException   ex) {
            Logger.getLogger(EncryptionUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static byte[] EncryptAES(byte[] key, byte[] IV, String tag, String plain, String type){
        System.out.println("TAG: " + tag);
        switch(type.substring(type.length() - 3)){
            case "ctr":
                return EncryptAESCTR(key, IV, plain);
            case "gcm":
                return EncryptAESGCM(key, IV, tag, plain);
        }
        return null;
    }
    
    public static byte[] DecryptAES(byte[] key, byte[] IV, String tag, byte[] cipherText, String type){
        System.out.println("TAG: " + tag);
        switch(type.substring(type.length() - 3)){
            case "ctr":
                return DecryptAESCTR(key, IV, cipherText);
            case "gcm":
                return DecryptAESGCM(key, IV, tag, cipherText);
        }
        return null;
    }
    
    private static byte[] EncryptAESCTR(byte[] key, byte[] IV, String plain){
        try {
            Key keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");          
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(ProtectionUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static byte[] EncryptAESGCM(byte[] key, byte[] IV, String tagBase64, String plain) {
        try {
            SecretKey keySpec = new SecretKeySpec(key, "AES");
            // Decodifica el tag para obtener su tamaño real en bytes
            byte[] tagBytes = Base64.getDecoder().decode(tagBase64);
            int tLenBits = tagBytes.length * 8;  

            GCMParameterSpec parameterSpec = new GCMParameterSpec(tLenBits, IV);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

            //cipher.updateAAD(Utils.decode(tagBase64));
            // Pero no uses el tag como AAD.
            byte [] result = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return result;
        } catch (Exception ex) {
            Logger.getLogger(ProtectionUtil.class.getName())
                  .log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    private static byte[] DecryptAESCTR(byte[] key, byte[] IV, byte[] cipherText){
        try {
            Key keySpec = new SecretKeySpec(key, "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV);
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");          
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            return cipher.doFinal(cipherText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(ProtectionUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static byte[] DecryptAESGCM(byte[] key, byte[] IV, String tagBase64, byte[] cipherTextWithTag) {
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        byte[] tagBytes = Base64.getDecoder().decode(tagBase64);
        int tLenBits = tagBytes.length * 8;
        GCMParameterSpec spec = new GCMParameterSpec(tLenBits, IV);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            // aquí pasamos directamente el ciphertext+tag
            return cipher.doFinal(cipherTextWithTag);

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ProtectionUtil.class.getName())
                  .log(Level.SEVERE, "Algoritmo AES/GCM no disponible", ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(ProtectionUtil.class.getName())
                  .log(Level.SEVERE, "Padding no soportado en AES/GCM", ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(ProtectionUtil.class.getName())
                  .log(Level.SEVERE, "Clave inválida para AES/GCM", ex);
        } catch (InvalidAlgorithmParameterException ex) {
            Logger.getLogger(ProtectionUtil.class.getName())
                  .log(Level.SEVERE, "Parámetros inválidos (IV/tLen) para AES/GCM", ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(ProtectionUtil.class.getName())
                  .log(Level.SEVERE, "Bloque de datos de tamaño ilegal en AES/GCM", ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(ProtectionUtil.class.getName())
                  .log(Level.SEVERE, "Error de autenticación (bad tag) en AES/GCM", ex);
        }

        // Si llega aquí, devolvemos null o podrías lanzar una RuntimeException
        return null;
    }





}
