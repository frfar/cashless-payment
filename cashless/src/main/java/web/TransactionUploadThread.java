package web;

import web.structures.OfflineTransaction;

import java.util.PriorityQueue;

public class TransactionUploadThread implements Runnable {

    private static TransactionUploadThread transactionUploadThread;
    private static Thread thread;
    private static PriorityQueue<OfflineTransaction> offlineTransactions = new PriorityQueue<>();

    private boolean isConnected = true;

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

                    if(!isConnected) {
                        System.out.println("Internet is connected now!");
                    }
                    System.out.println(ret);
                    isConnected = true;
                } else {
                    if(isConnected) {
                        System.out.println("Internet is down!");
                        isConnected = false;
                    }
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
