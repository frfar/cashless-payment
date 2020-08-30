package web;

import com.google.gson.*;
import config.Config;
import web.structures.OfflineTransaction;
import web.structures.SendTransactionErrorResponse;
import web.structures.SendTransactionResponse;
import web.structures.SendTransactionSuccessResponse;

import java.io.FileNotFoundException;

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
        String baseURL = "";
        try {
            baseURL = Config.getInstance().getServer();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        String res = req.sendGet(baseURL + "/transaction", query);
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
        query.addParameter("prev_timestamp", Long.toString(offlineTransaction.prevTimestamp));
        query.addParameter("transaction_sequence", Short.toString(offlineTransaction.transactionSequence));

        String baseURL = "";
        try {
            baseURL = Config.getInstance().getServer();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        String res = WebRequest.sendPost(baseURL + "/offline_transaction/incomplete", query);

//        System.out.println(res);
        if (res != null && offlineTransaction.hasVendor()) {
            QueryParameter vendorTransact =  new QueryParameter();
            vendorTransact.addParameter("vendor_id", offlineTransaction.getVendorId());
            vendorTransact.addParameter("card_id", offlineTransaction.cardId);
            vendorTransact.addParameter("timestamp", Long.toString(offlineTransaction.timestamp));
            double amount = offlineTransaction.remainingAmount - offlineTransaction.prevRemainingAmount;
            vendorTransact.addParameter("amount", String.format("%.02f", amount));

            res = WebRequest.sendPost(baseURL + "/vendor_transaction", vendorTransact);
        }

        return res;
    }
}
