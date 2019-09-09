package security;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SHA256 {

    public static byte[] getMessageDigest(String message){

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(message.getBytes());

            return md.digest();
        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }

        return null;
    }

    public static byte[] getHMAC(String message,byte[] secret){

        Mac sha256_HMAC;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret, "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return sha256_HMAC.doFinal(message.getBytes());

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static byte[] getMessageDigest(byte[] message){

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(message);

            return md.digest();
        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }

        return null;
    }

    public static byte[] getHMAC(byte[] message,byte[] secret){

        Mac sha256_HMAC;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret, "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return sha256_HMAC.doFinal(message);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }

}
