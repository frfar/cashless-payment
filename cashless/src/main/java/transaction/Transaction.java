package transaction;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Transaction {

    private byte[] transaction;
    private byte[] signature;
    private byte[] encryption;
    private static ECCSignature eccSignature;
    private static final byte[] aesKey = DatatypeConverter.parseHexBinary("2b7e151628aed2a6abf7158809cf4f3c");
    private byte[] aesIV;

    private Transaction(byte[] transaction, byte[] encryption, byte[] signature, byte[] aesIV) {
        this.transaction = transaction;
        this.encryption = encryption;
        this.signature = signature;
        this.aesIV = aesIV;
    }

    public static void setEccSignature(ECCSignature eccSignature) {
        Transaction.eccSignature = eccSignature;
    }

    private static byte[] getTransactionBytes(String atmID, String cardId, int amount, String passcode,long timestamp, byte[] hashKey) {
        byte[] atmIDBytes = DatatypeConverter.parseHexBinary(atmID);
        byte[] cardIDBytes = DatatypeConverter.parseHexBinary(cardId);
        byte[] amountBytes = new byte[] {(byte) amount};
        byte[] passcodeBytes = passcode.getBytes();

        // String timestampString = String.format("%08ld", timestamp);
       // byte[] timestampBytes = timestampString.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(atmIDBytes.length + cardIDBytes.length + amountBytes.length + passcodeBytes.length + Long.BYTES + hashKey.length);
        System.out.println("size of the buffer " + buffer.array().length);
        buffer.put(atmIDBytes).put(cardIDBytes).put(amountBytes).put(passcodeBytes).putLong(timestamp).put(hashKey);

        return buffer.array();
    }

    public static Transaction create(String atmID, String cardId, int amount, String passcode,long timestamp ,byte[] hashKey, byte[] aesIV) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {

        byte[] transactionBytes = getTransactionBytes(atmID, cardId, amount, passcode,timestamp, hashKey);
        byte[] encryption = AES.encrypt(aesKey, aesIV, transactionBytes);

        return new Transaction(transactionBytes, encryption, eccSignature.sign(encryption), aesIV);
    }

    public byte[] getValue() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        if(!eccSignature.verify(encryption, signature)) {
            return new byte[0];
        }

        return Objects.requireNonNull(AES.decrypt(aesKey, aesIV, encryption));
    }

    public byte getAmount() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        byte[] rawTransaction = getValue();

        if(rawTransaction.length == 0) {
            return -1;
        }

        return rawTransaction[16];
    }

    public static byte[] getValue(byte[] encryption, byte[] signature, byte[] aesIV) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        if(!eccSignature.verify(encryption, signature)) {
            return new byte[0];
        }

        return Objects.requireNonNull(AES.decrypt(aesKey, aesIV, encryption));
    }

    public static byte getAmount(byte[] encryption, byte[] signature, byte[] aesIV) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        byte[] rawTransaction = getValue(encryption, signature, aesIV);

        if(rawTransaction.length == 0) {
            return -1;
        }

        return rawTransaction[16];
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(encryption.length + signature.length + 2);

        return buffer.put((byte)encryption.length).put((byte)signature.length).put(encryption).put(signature).array();
    }

     public void printTransaction() {
        Transaction.printTransaction(transaction);
    }

    public static void printTransaction(byte[] transaction) {
        System.out.println("Printing the values of Transaction:");
        System.out.println("atm id : " + DatatypeConverter.printHexBinary(Arrays.copyOfRange(transaction,0,8)));
        System.out.println("card id : " + DatatypeConverter.printHexBinary(Arrays.copyOfRange(transaction,8,16)));
        System.out.println("amount : " + DatatypeConverter.printHexBinary(Arrays.copyOfRange(transaction,16,17)));
        System.out.println("passcode : " + new String(Arrays.copyOfRange(transaction,17,21)));
        System.out.println("timestamp: " + DatatypeConverter.printHexBinary(Arrays.copyOfRange(transaction,21,29)));
        System.out.println("hash key : " + DatatypeConverter.printHexBinary(Arrays.copyOfRange(transaction,29,37)));
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "signature=" + Arrays.toString(signature) +
                ", encryption=" + Arrays.toString(encryption) +
                '}';
    }
}
