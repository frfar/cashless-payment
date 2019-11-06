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