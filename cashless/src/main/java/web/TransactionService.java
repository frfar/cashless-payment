package web;

import com.google.gson.*;
import web.structures.OfflineTransaction;
import web.structures.SendTransactionErrorResponse;
import web.structures.SendTransactionResponse;
import web.structures.SendTransactionSuccessResponse;

public class TransactionService {

    public enum Type {

        CREDIT("credit"),
        DEBIT("debit");

        private String type;

        Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    public static SendTransactionResponse sendTransaction(String uniqueId, Double amount, String passcode, String vendingMachineName, Type type) {
        Gson gson = new Gson();

        QueryParameter query = new QueryParameter();
        query.addParameter("unique_Id", uniqueId);
        query.addParameter("type", type.getType());
        query.addParameter("amount", Double.toString(amount));
        query.addParameter("vendingMachineName", vendingMachineName);
        query.addParameter("passcode", passcode);

        WebRequest req = new WebRequest();
        String res = req.sendGet("http://19d69285.ngrok.io/transaction", query);
        System.out.println(res);

        JsonObject objectJson = new JsonParser().parse(res).getAsJsonObject();
        JsonElement status = objectJson.get("status");

        if(status.getAsString().equals("Success")) {
            SendTransactionSuccessResponse ret = gson.fromJson(res, SendTransactionSuccessResponse.class);
            return new SendTransactionResponse(ret, null);
        } else {
            SendTransactionErrorResponse ret = gson.fromJson(res, SendTransactionErrorResponse.class);
            return new SendTransactionResponse(null, ret);
        }
    }

    public static String sendOfflineTransaction(OfflineTransaction offlineTransaction) {
        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

        QueryParameter query = new QueryParameter();
        query.addParameter("card_id", offlineTransaction.cardId);
        query.addParameter("vm_id", offlineTransaction.vmId);
        query.addParameter("remaining_amount", String.format("%.02f", offlineTransaction.remainingAmount));
        query.addParameter("timestamp", Long.toString(offlineTransaction.timestamp));
        query.addParameter("prev_vm_id", offlineTransaction.prevVmId);
        query.addParameter("prev_remaining_amount", String.format("%.02f", offlineTransaction.prevRemainingAmount));
        query.addParameter("prev_timestamp", Long.toString(offlineTransaction.prev_timestamp));

        String res = WebRequest.sendPost("http://e4421a4a.ngrok.io/offline_transaction/incomplete", query);

        System.out.println(res);

        return res;
    }
}
