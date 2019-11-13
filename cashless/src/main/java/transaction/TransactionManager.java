package transaction;

import security.AES;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * manages encryption and signing of a single transaction.
 */
public class TransactionManager {

    private static final byte[] aesKey = DatatypeConverter.parseHexBinary("2b7e151628aed2a6abf7158809cf4f3c");

    /**
     * encrypts the transaction and sign it.
     * @param transaction
     * @param privatekeyFile
     * @param publickeyFile
     * @return
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static byte[] encryptAndSignTransaction(PlainTransaction transaction, File privatekeyFile, File publickeyFile) throws IOException, GeneralSecurityException {

        byte[] transcationBytes = transaction.getBytes();
        byte[] iv = AES.generateRandomIV();
        byte[] encryption = AES.encrypt(aesKey,iv,transcationBytes);
        ECCSign eccSign = ECCSign.getInstance(privatekeyFile);
        byte[] signature = eccSign.sign(encryption);

        ByteBuffer buffer = ByteBuffer.allocate(encryption.length + signature.length + iv.length + 2);

        buffer.put((byte)encryption.length).put((byte) signature.length).put(encryption).put(signature).put(iv);

        return buffer.array();

    }

    /**
     * verifies the signed transaction and decrypts it.
     * @param transactionBytes
     * @return
     * @throws GeneralSecurityException
     */
    public static PlainTransaction verifyAndDecrypt(byte[] transactionBytes, File publickeyFile) throws GeneralSecurityException, FileNotFoundException {

        int encryptionSize = transactionBytes[0];
        int signatureSize = transactionBytes[1];

        byte[] encryptionBytes = Arrays.copyOfRange(transactionBytes,2,2 + encryptionSize);
        byte[] signatureBytes = Arrays.copyOfRange(transactionBytes,2 + encryptionSize, 2 + encryptionSize + signatureSize);
        byte[] iv = Arrays.copyOfRange(transactionBytes, 2 + encryptionSize + signatureSize, 2 + encryptionSize + signatureSize + 16);

        ECCVerify eccVerify = ECCVerify.getInstance(publickeyFile);
        boolean verification = eccVerify.verify(encryptionBytes,signatureBytes);

        if(!verification) {
            return null;
        }

        byte[] plainTransactionBytes = AES.decrypt(aesKey,iv,encryptionBytes);

        return PlainTransaction.parse(plainTransactionBytes);
    }




}
