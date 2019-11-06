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
    private int amount;

    /**
     * passcode of the card.
     */
    private String passcode;

    /**
     * hashkey of the transaction.
     */
    private byte[] hashkey;

    /**
     *
     * @param vmId
     * @param cardId
     * @param amount
     * @param passcode
     * @param hashkey
     */
    public PlainTransaction(String vmId, String cardId, int amount, String passcode, byte[] hashkey) {
        this.vmId = vmId;
        this.cardId = cardId;
        this.amount = amount;
        this.passcode = passcode;
        this.hashkey = hashkey;
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
        int amount = (int) (transaction[12] + transaction[13] * 0.01);
        StringBuilder passcode = new StringBuilder();
        passcode.append((transaction[14] & 0xF0) >> 4);
        passcode.append((transaction[14] & 0x0F));
        passcode.append((transaction[15] & 0xF0) >> 4);
        passcode.append((transaction[15] & 0x0F));
        byte[] hashkey = Arrays.copyOfRange(transaction, 16, 24);

        return new PlainTransaction(vmId, atmId, amount, passcode.toString(), hashkey);
    }

    /**
     * provides byte equivalent of the transaction.
     * @return
     */
    public byte[] getBytes() {
        byte[] vmIdBytes = DatatypeConverter.parseHexBinary(vmId);
        byte[] cardIdBytes = DatatypeConverter.parseHexBinary(cardId);
        byte amountDollars = (byte) Math.floor(amount);
        byte amountCents = (byte) ((byte) (amount - amountDollars) * 100);
        byte passcodeLSB = (byte) ((((passcode.charAt(0) - '0') & 0x0F) << 4) + ((passcode.charAt(1) - '0') & 0x0F));
        byte passcodeMSB = (byte) ((((passcode.charAt(2) - '0') & 0x0F) << 4) + ((passcode.charAt(3) - '0') & 0x0F));
        ByteBuffer buffer = ByteBuffer.allocate(vmIdBytes.length + cardIdBytes.length + 4 + hashkey.length);
        buffer.put(vmIdBytes).put(cardIdBytes).put(amountDollars).put(amountCents).put(passcodeLSB).put(passcodeMSB).put(hashkey);

        return buffer.array();
    }

    public String getVmId() {
        return vmId;
    }

    public String getCardId() {
        return cardId;
    }

    public int getAmount() {
        return amount;
    }

    public String getPasscode() {
        return passcode;
    }

    public byte[] getHashkey() {
        return hashkey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlainTransaction that = (PlainTransaction) o;
        return amount == that.amount &&
                Objects.equals(vmId, that.vmId) &&
                Objects.equals(cardId, that.cardId) &&
                Objects.equals(passcode, that.passcode) &&
                Arrays.equals(hashkey, that.hashkey);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(vmId, cardId, amount, passcode);
        result = 31 * result + Arrays.hashCode(hashkey);
        return result;
    }

    @Override
    public String toString() {
        return "PlainTransaction{" +
                "vmId='" + vmId + '\'' +
                ", cardId='" + cardId + '\'' +
                ", amount=" + amount +
                ", passcode='" + passcode + '\'' +
                ", hashkey=" + Arrays.toString(hashkey) +
                '}';
    }
}
