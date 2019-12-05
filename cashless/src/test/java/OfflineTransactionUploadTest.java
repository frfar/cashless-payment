import web.TransactionService;
import web.structures.OfflineTransaction;

public class OfflineTransactionUploadTest {

    public static void main(String[] args) {
        OfflineTransaction offlineTransaction = new OfflineTransaction("1","1",95,2,"1",100,1, (short) 0);

        TransactionService.sendOfflineTransaction(offlineTransaction);
    }
}
