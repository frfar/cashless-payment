package web;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TransactionService {

    private static Gson gson = new Gson();
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

    public static SendTransactionResponse sendTransaction(String uniqueId, Double amount, String vendingMachineName, Type type) {
        QueryParameter query = new QueryParameter();
        query.addParameter("unique_Id", uniqueId);
        query.addParameter("type", type.getType());
        query.addParameter("amount", Double.toString(amount));
        query.addParameter("vendingMachineName", vendingMachineName);

        WebRequest req = new WebRequest();
        String res = req.sendGet("http://localhost:3000/transaction", query);
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
}
