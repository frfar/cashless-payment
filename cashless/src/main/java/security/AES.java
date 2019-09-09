package security;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

    public static byte[] encrypt(byte[] key,byte[] iv, byte[] plainText){

        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, generateKey(key),new IvParameterSpec(iv));
            return cipher.doFinal(plainText);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] decrypt(byte[] key,byte[] iv, byte[] plainText){

        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, generateKey(key),new IvParameterSpec(iv));
            return cipher.doFinal(plainText);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] generateRandomIV(){

        SecureRandom secureRandom = new SecureRandom();

        byte [] iv = new byte[16];

        secureRandom.nextBytes(iv);

        return iv;
    }

    public static byte[] generateRandomAESKey(){
        SecureRandom secureRandom = new SecureRandom();

        byte[] key = new byte[16];

        secureRandom.nextBytes(key);

        return key;
    }

    private static Key generateKey(byte[] keyBytes){

        return new SecretKeySpec(keyBytes, "AES");

    }

}