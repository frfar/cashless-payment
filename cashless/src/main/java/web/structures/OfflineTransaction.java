package web.structures;

public class OfflineTransaction {

    public String cardId;
    public String vmId;
    public double remainingAmount;
    public long timestamp;
    public String prevVmId;
    public double prevRemainingAmount;
    public long prev_timestamp;

    public OfflineTransaction(String cardId, String vmId, double remainingAmount, long timestamp, String prevVmId, double prevRemainingAmount, long prev_timestamp) {
        this.cardId = cardId;
        this.vmId = vmId;
        this.remainingAmount = remainingAmount;
        this.timestamp = timestamp;
        this.prevVmId = prevVmId;
        this.prevRemainingAmount = prevRemainingAmount;
        this.prev_timestamp = prev_timestamp;
    }

    @Override
    public String toString() {
        return "OfflineTransaction{" +
                "cardId='" + cardId + '\'' +
                ", vmId='" + vmId + '\'' +
                ", remainingAmount=" + remainingAmount +
                ", timestamp=" + timestamp +
                ", prevVmId='" + prevVmId + '\'' +
                ", prevRemainingAmount=" + prevRemainingAmount +
                ", prev_timestamp=" + prev_timestamp +
                '}';
    }
}
