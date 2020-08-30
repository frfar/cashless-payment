package web;

import config.Config;
import web.structures.OfflineTransaction;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.PriorityQueue;

public class TransactionUploadThread implements Runnable {

    private static TransactionUploadThread transactionUploadThread;
    private static Thread thread;
    private static PriorityQueue<OfflineTransaction> offlineTransactions = new PriorityQueue<>();

    private boolean isConnected = true;

    public boolean isConnected() {
        URL obj = null;
        try {
            Config config = Config.getInstance();
            obj = new URL(Config.getInstance().getServer() + new QueryParameter().getQueryString(QueryParameter.CallType.GET));
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept-Language","en-US,en;q=0.5");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            int responseCode = con.getResponseCode();
            isConnected = (responseCode / 100) == 2;
        } catch (IOException e) {
            e.printStackTrace();
            isConnected = false;
        }
        return isConnected;
    }

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
