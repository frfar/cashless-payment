package transaction;

import javax.xml.bind.DatatypeConverter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the transaction.
 */
public class PlainTransaction {
    /**
     * vending machine id.
     */
    private String vmId;

    /**
     * card id.
     */
    private String cardId;

    /**
     * amount for the transaction.
     */
    private double amount;

    /**
     * passcode of the card.
     */
    private byte[] passcode;

    /**
     * hashkey of the transaction.
     */
    private byte[] hashkey;

    /**
     * timestamp for the transaction.
     */
    private long timestamp;

    /**
     *
     * @param vmId
     * @param cardId
     * @param amount
     * @param passcode
     * @param hashkey
     * @param timestamp
     */
    public PlainTransaction(String vmId, String cardId, double amount, byte[] passcode, byte[] hashkey, long timestamp) {
        this.vmId = vmId;
        this.cardId = cardId;
        this.amount = amount;
        this.passcode = passcode;
        this.hashkey = hashkey;
        this.timestamp = timestamp;
    }

    /**
     * parse the transaction from the byte array.
     *
     * @param transaction
     * @return
     */
    public static PlainTransaction parse(byte[] transaction) {
        String vmId = DatatypeConverter.printHexBinary(Arrays.copyOfRange(transaction,0,4));
        String atmId = DatatypeConverter.printHexBinary(Arrays.copyOfRange(transaction,4,12));
        double amount = (transaction[12] + transaction[13] * 0.01);
        byte[] passcode = Arrays.copyOfRange(transaction,14,46);
        byte[] hashkey = Arrays.copyOfRange(transaction, 46, 54);

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(Arrays.copyOfRange(transaction,54,62));
        buffer.flip();
        long timestamp = buffer.getLong();

        return new PlainTransaction(vmId, atmId, amount, passcode, hashkey, timestamp);
    }

    /**
     * provides byte equivalent of the transaction.
     * @return
     */
    public byte[] getBytes() {
        byte[] vmIdBytes = DatatypeConverter.parseHexBinary(vmId);
        byte[] cardIdBytes = DatatypeConverter.parseHexBinary(cardId);
        byte amountDollars = (byte) Math.floor(amount);
        byte amountCents =  (byte) ((amount - amountDollars) * 100);
        ByteBuffer buffer = ByteBuffer.allocate(vmIdBytes.length + cardIdBytes.length + 2 + passcode.length + hashkey.length + 8);
        buffer.put(vmIdBytes).put(cardIdBytes).put(amountDollars).put(amountCents).put(passcode).put(hashkey).putLong(timestamp);

        return buffer.array();
    }

    public String getVmId() {
        return vmId;
    }

    public String getCardId() {
        return cardId;
    }

    public double getAmount() {
        return amount;
    }

    public byte[] getPasscode() {
        return passcode;
    }

    public byte[] getHashkey() {
        return hashkey;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlainTransaction that = (PlainTransaction) o;
        return Double.compare(that.amount, amount) == 0 &&
                timestamp == that.timestamp &&
                Objects.equals(vmId, that.vmId) &&
                Objects.equals(cardId, that.cardId) &&
                Arrays.equals(passcode, that.passcode) &&
                Arrays.equals(hashkey, that.hashkey);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(vmId, cardId, amount, timestamp);
        result = 31 * result + Arrays.hashCode(passcode);
        result = 31 * result + Arrays.hashCode(hashkey);
        return result;
    }

    @Override
    public String toString() {
        return "PlainTransaction{" +
                "vmId='" + vmId + '\'' +
                ", cardId='" + cardId + '\'' +
                ", amount=" + amount +
                ", passcode=" + Arrays.toString(passcode) +
                ", hashkey=" + Arrays.toString(hashkey) +
                ", timestamp=" + timestamp +
                '}';
    }
}
