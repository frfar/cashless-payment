package web.structures;

public class TransactionMessage {
    public String encryptedAmount;
    public String signature;

    @Override
    public String toString() {
        return "TransactionMessage{" +
                "encryptedAmount='" + encryptedAmount + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}

