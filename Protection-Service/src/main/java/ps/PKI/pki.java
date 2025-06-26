/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ps.PKI;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author alumne
 */
public class pki {

    public static Pair<String, String> genRSAKeys(){
        PrivateKey privkey = null;
        PublicKey pubkey = null;
        if(pubkey == null && privkey == null){
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
                kpg.initialize(2048,random);
                KeyPair keyPair = kpg.generateKeyPair();
                pubkey = keyPair.getPublic();
                privkey = keyPair.getPrivate();

                Base64.Encoder encoder = Base64.getEncoder();
                System.out.println("privateKey: " + encoder.encodeToString(privkey.getEncoded()));
                System.out.println("publicKey: " + encoder.encodeToString(pubkey.getEncoded()));

                Pair<String, String> p = new Pair<>(encoder.encodeToString(privkey.getEncoded()),encoder.encodeToString(pubkey.getEncoded()));
                return p;
            } catch (NoSuchAlgorithmException e) {
                Logger.getLogger(pki.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return new Pair<>(null, null);
    }
    public static String genSymKey(String cipher, int keySize){
        byte[] secureRandomKeyBytes = new byte[keySize / 8];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(secureRandomKeyBytes);
//        String aux = Base64.getEncoder().encodeToString(new SecretKeySpec(secureRandomKeyBytes, cipher).getEncoded());
//        byte[] aux2 = Base64.getDecoder().decode(aux);
//        System.out.println(Base64.getEncoder().encodeToString(aux2));
//        System.out.println(Base64.getEncoder().encodeToString(new SecretKeySpec(secureRandomKeyBytes, cipher).getEncoded()));
        return Base64.getEncoder().encodeToString(new SecretKeySpec(secureRandomKeyBytes, cipher).getEncoded());
    }

    public static String encrypt(String data, PublicKey publickey) {
	Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publickey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException ex) {
            Logger.getLogger(pki.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    public static String decrypt(byte[] data, PrivateKey privkey) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privkey);
            return new String(cipher.doFinal(data));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(pki.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    private static PublicKey stringToPK(String pk){
        String[] byteValues = pk.substring(1, pk.length() - 1).split(",");
        try {
            byte[] bytes = new byte[byteValues.length];
            for (int i=0, len=bytes.length; i<len; i++) {
               bytes[i] = Byte.parseByte(byteValues[i].trim());     
            }
            X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(pki.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
