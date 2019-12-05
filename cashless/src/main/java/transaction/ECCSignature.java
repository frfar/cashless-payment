package transaction;

import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;

public class ECCSignature {

    private KeyPair keypair;

    private static ECCSignature eccSignature;

    private ECCSignature(KeyPair keyPair) {
        this.keypair = keyPair;
    }

    public static ECCSignature getInstance() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {

        if(eccSignature == null) {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");

            keyGen.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());

            KeyPair pair = keyGen.generateKeyPair();

            eccSignature = new ECCSignature(pair);
        }

        return eccSignature;
    }

    public ECPrivateKey getPrivateKey() {
        return (ECPrivateKey) keypair.getPrivate();
    }

    public ECPublicKey getPublicKey() {
        return (ECPublicKey) keypair.getPublic();
    }

    public byte[] sign(byte[] input) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return sign(input, (ECPrivateKey) keypair.getPrivate());
    }

    public boolean verify(byte[] input, byte[] signature) throws SignatureException, InvalidKeyException, NoSuchAlgorithmException {
        Signature verifyEcdsa = Signature.getInstance("SHA256withECDSA");

        verifyEcdsa.initVerify(keypair.getPublic());

        verifyEcdsa.update(input);

        return verifyEcdsa.verify(signature);
    }

    public static byte[] sign(byte[] input, ECPrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature ecdsa = Signature.getInstance("SHA256withECDSA");

        ecdsa.initSign(privateKey);

        ecdsa.update(input);

        return ecdsa.sign();
    }
}
