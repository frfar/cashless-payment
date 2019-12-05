package web.structures;

public class SendTransactionErrorResponse {

    public String status;
    public String message;
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
