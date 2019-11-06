package transaction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * Class to verify a signed message.
 */
public class ECCVerify {

    /**
     * public key used for signing the message.
     */
    private PublicKey publicKey;

    /**
     *
     * @param publicKey public key used to verify.
     */
    private ECCVerify(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * creates the ECCVerify instance.
     * Make sure file doesn't contain start key and end key messages.
     * @param file file containing the public key.
     * @return
     * @throws FileNotFoundException
     * @throws GeneralSecurityException
     */
    public static ECCVerify getInstance(File file) throws FileNotFoundException, GeneralSecurityException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String keyString = reader.lines().collect(Collectors.joining());

        byte[] rawArray = Base64.getDecoder().decode(keyString);
        PublicKey publicKey = extractKey(rawArray);

        return new ECCVerify(publicKey);
    }

    /**
     * creates the ECCVerify instance.
     * Make sure file doesn't contain start key and end key messages.
     * @param str string containing the public key.
     * @return
     * @throws FileNotFoundException
     * @throws GeneralSecurityException
     */
    public static ECCVerify getInstance(String str) throws GeneralSecurityException {
        byte[] rawArray = Base64.getDecoder().decode(str);

        return getInstance(rawArray);
    }

    /**
     * creates the ECCVerify instance.
     * Make sure file doesn't contain start key and end key messages.
     * @param bytes bytes containing the public key.
     * @return
     * @throws FileNotFoundException
     * @throws GeneralSecurityException
     */
    public static ECCVerify getInstance(byte[] bytes) throws GeneralSecurityException {
        PublicKey publicKey = extractKey(bytes);

        return new ECCVerify(publicKey);
    }

    /**
     * creates the public key from the byte array.
     *
     * @param pkcs8key
     * @return
     * @throws GeneralSecurityException
     */
    private static PublicKey extractKey(byte[] pkcs8key) throws GeneralSecurityException {

        X509EncodedKeySpec spec = new X509EncodedKeySpec(pkcs8key);
        KeyFactory factory = KeyFactory.getInstance("EC");
        PublicKey publicKey = factory.generatePublic(spec);
        return publicKey;
    }

    /**
     * verifies the signature of the message.
     * @param input
     * @param signature
     * @return
     * @throws SignatureException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    public boolean verify(byte[] input, byte[] signature) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        Signature verifyEcdsa = Signature.getInstance("SHA256withECDSA");

        verifyEcdsa.initVerify(publicKey);

        verifyEcdsa.update(input);

        return verifyEcdsa.verify(signature);
    }


    public static byte[] getRawPublicKey(String encodedPublickey) throws GeneralSecurityException {
        byte[] rawArray = Base64.getDecoder().decode(encodedPublickey);

        ECPublicKey publicKey = (ECPublicKey) extractKey(rawArray);
        return publicKey.getEncoded();
    }
}