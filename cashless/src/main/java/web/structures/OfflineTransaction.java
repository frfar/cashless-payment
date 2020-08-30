package web.structures;

public class OfflineTransaction implements Comparable <OfflineTransaction> {

    public String cardId;
    public String vmId;
    public double remainingAmount;
    public long timestamp;
    public String prevVmId;
    public double prevRemainingAmount;
    public long prevTimestamp;
    public short transactionSequence;
    private String vendorId;

    public OfflineTransaction(String cardId, String vmId, double remainingAmount, long timestamp, String prevVmId, double prevRemainingAmount, long prevTimestamp, short transactionSequence) {
        this.cardId = cardId;
        this.vmId = vmId;
        this.remainingAmount = remainingAmount;
        this.timestamp = timestamp;
        this.prevVmId = prevVmId;
        this.prevRemainingAmount = prevRemainingAmount;
        this.prevTimestamp = prevTimestamp;
        this.transactionSequence = transactionSequence;
        this.vendorId = null;
    }

    public String getVendorId(){
        return this.vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = String.valueOf(vendorId);
    }

    public boolean hasVendor(){
        return vendorId != null;
    }

    @Override
    public int compareTo(OfflineTransaction o) {
        return Double.compare(this.timestamp, o.timestamp);
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
                ", prevTimestamp=" + prevTimestamp +
                '}';
    }
}
