package web;

public class SendTransactionSuccessResponse {
    public String status;
    public TransactionMessage message;
    public String amount;

    @Override
    public String toString() {
        return "SendTransactionSuccessResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }
}

class TransactionMessage {
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
