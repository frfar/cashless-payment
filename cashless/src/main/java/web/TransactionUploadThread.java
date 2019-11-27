package web;

import web.structures.OfflineTransaction;

import java.io.IOException;
import java.util.PriorityQueue;

public class TransactionUploadThread implements Runnable {

    private static TransactionUploadThread transactionUploadThread;
    private static Thread thread;
    private static PriorityQueue<OfflineTransaction> offlineTransactions = new PriorityQueue<>();

    private TransactionUploadThread() {
    }

    public static TransactionUploadThread getInstance() {
        if(transactionUploadThread == null) {
            transactionUploadThread = new TransactionUploadThread();
            thread = new Thread(transactionUploadThread);
            thread.start();
        }

        return transactionUploadThread;
    }

    public void addOfflineTransaction(OfflineTransaction offlineTransaction) {
        offlineTransactions.add(offlineTransaction);
    }

    @Override
    public void run() {
        while(true) {
            if (!offlineTransactions.isEmpty()) {
                OfflineTransaction offlineTransaction = offlineTransactions.peek();
                String ret = TransactionService.sendOfflineTransaction(offlineTransaction);

                if (ret != null) {
                    offlineTransactions.poll();
                }
            }

            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
