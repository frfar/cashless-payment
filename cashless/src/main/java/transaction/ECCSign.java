package transaction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * Class to sign a message using ECC signature.
 */
public class ECCSign {

    /**
     * private key used for signing the message.
     */
    private PrivateKey privateKey;

    /**
     *
     * @param privateKey private key used.
     */
    private ECCSign(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * creates the ECCSign instance.
     * Make sure file doesn't contain start key and end key messages.
     * @param str string containing the private key.
     * @return
     * @throws FileNotFoundException
     * @throws GeneralSecurityException
     */
    public static ECCSign getInstance(String str) throws GeneralSecurityException {

        byte[] rawArray = Base64.getDecoder().decode(str);
        PrivateKey privateKey = extractKey(rawArray);

        return new ECCSign(privateKey);
    }

    /**
     * creates the ECCSign instance.
     * Make sure file doesn't contain start key and end key messages.
     * @param file file containing the private key.
     * @return
     * @throws FileNotFoundException
     * @throws GeneralSecurityException
     */
    public static ECCSign getInstance(File file) throws FileNotFoundException, GeneralSecurityException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String keyString = reader.lines().collect(Collectors.joining());

        return getInstance(keyString);
    }

    /**
     * creates the private key from the byte array.
     *
     * @param pkcs8key
     * @return
     * @throws GeneralSecurityException
     */
    private static PrivateKey extractKey(byte[] pkcs8key) throws GeneralSecurityException {

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(pkcs8key);
        KeyFactory factory = KeyFactory.getInstance("EC");
        PrivateKey privateKey = factory.generatePrivate(spec);
        return privateKey;
    }

    /**
     * signs the message using the private key.
     * @param message
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public byte[] sign(byte[] message) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature ecdsa = Signature.getInstance("SHA256withECDSA");

        ecdsa.initSign(privateKey);

        ecdsa.update(message);

        return ecdsa.sign();
    }
}