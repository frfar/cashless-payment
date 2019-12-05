package web.structures;

public class SendTransactionResponse {
    private SendTransactionSuccessResponse sendTransactionSuccessResponse;
    private SendTransactionErrorResponse sendTransactionErrorResponse;

    public SendTransactionResponse(SendTransactionSuccessResponse sendTransactionSuccessResponse, SendTransactionErrorResponse sendTransactionErrorResponse) {
        this.sendTransactionSuccessResponse = sendTransactionSuccessResponse;
        this.sendTransactionErrorResponse = sendTransactionErrorResponse;
    }

    public SendTransactionSuccessResponse getSendTransactionSuccessResponse() {
        return sendTransactionSuccessResponse;
    }

    public SendTransactionErrorResponse getSendTransactionErrorResponse() {
        return sendTransactionErrorResponse;
    }

    @Override
    public String toString() {
        return "SendTransactionResponse{" +
                "sendTransactionSuccessResponse=" + sendTransactionSuccessResponse +
                ", sendTransactionErrorResponse=" + sendTransactionErrorResponse +
                '}';
    }
}
