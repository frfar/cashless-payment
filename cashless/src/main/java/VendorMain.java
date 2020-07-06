import config.Config;
import keypad.UIKeypad;
import mifare.Acr122Device;
import mifare.MifareManager;
import org.nfctools.mf.MfCardListener;
import org.nfctools.mf.MfReaderWriter;
import org.nfctools.mf.card.MfCard;
import security.SHA256;
import transaction.PlainTransaction;
import transaction.TransactionManager;
import web.AuthenticationService;
import web.TransactionUploadThread;
import web.structures.OfflineTransaction;

import javax.smartcardio.CardException;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class VendorMain {

    private static final String NAME = "authorized-vendor";
    private static File privatekeyFile = new File(Config.privateKeyFileName);
    private static File publickeyFile = new File(Config.publicKeyFileName);
    private static SwingUI swingUI = new SwingUI(false);
    private static LoginForm loginForm = new LoginForm();
    private static TransactionUploadThread transactionUploadThread;

    public static void main(String[] args) throws InterruptedException {

        Acr122Device acr122;
        transactionUploadThread = TransactionUploadThread.getInstance();

        // Require Internet connection
        if (!transactionUploadThread.isConnected()){
            System.out.println("Internet is not connected. Please try again later");
            return;
        }

        JFrame jFrame = new JFrame();
        jFrame.add(loginForm);
        jFrame.setSize(300, 300);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);

        while (!AuthenticationService.isLoggedIn()){
            Thread.sleep(1000);
        }

        jFrame.setVisible(false);
        if (AuthenticationService.isLoggedIn()) {
            vendorMain();
        }
    }

    private static void vendorMain() {
        JFrame frame = new JFrame();
        Acr122Device acr122;
        try {
            acr122 = new Acr122Device();
            SwingUtilities.invokeLater(() -> {
                frame.add(swingUI);
                frame.setSize(300,300);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            });
        } catch (RuntimeException re) {
            System.out.println("No ACR122 reader found.");
            return;
        }
        try {
            acr122.open();
            acr122.listen(new MfCardListener() {
                @Override
                public void cardDetected(MfCard mfCard, MfReaderWriter mfReaderWriter)  {
                    System.out.println("Card Detected!!");

                    try {
                        PlainTransaction transaction = retrieveTransaction(mfCard, mfReaderWriter);
                        double amount = transaction.getAmount();
                        short sequenceNumber = transaction.getSequenceNumber();
                        byte[] passcodeHash = transaction.getPasscode();
                        byte[] passcodeSecret = transaction.getHashkey();

                        UIKeypad keypad = UIKeypad.getKeypadInstance();

                        System.out.println("The amount in card is: " + amount);
                        updateMessasge("Current amount in card is: " + amount);
                        Thread.sleep(1000);
                        updateMessasge("Enter the amount you want to add");
                        keypad.flushBuffer();
                        String addedAmount = keypad.readChars(3);
                        int intAddedAmount = Integer.parseInt(addedAmount);

                        if(amount + intAddedAmount > 100) {
                            System.out.println("You can't add more than $100!");

                            Thread.sleep(1000);
                            updateMessasge("Please Swipe the Card:");
                            return;
                        }

                        double newAmount = amount + intAddedAmount;

                        updateMessasge("Adding amount of $" + intAddedAmount);

                        long timestamp =  System.currentTimeMillis();
                        writeTrasaction(mfCard, mfReaderWriter, "12345679","1234567890ABCDEF", newAmount, passcodeHash, passcodeSecret, timestamp, (short)(sequenceNumber + 1));

                        PlainTransaction retrievedTransaction = retrieveTransaction(mfCard, mfReaderWriter);
                        double retrievedAmount = retrievedTransaction.getAmount();

                        if(newAmount == retrievedAmount) {
                            System.out.println("The final amount in card is: " + newAmount);
                            updateMessasge("The final amount in card is: " + newAmount);

                            String prevVmId = transaction.getVmId();
                            String prevVmIntId = "";
                            if(prevVmId.equals("12345678")) {
                                prevVmIntId = "1";
                            } else if(prevVmId.equals("12345679")) {
                                prevVmIntId = "2";
                            } else {
                                prevVmIntId = "3";
                            }

                            OfflineTransaction offlineTransaction = new OfflineTransaction("1","2", retrievedAmount, retrievedTransaction.getTimestamp(),prevVmIntId,amount, transaction.getTimestamp(), retrievedTransaction.getSequenceNumber());
                            transactionUploadThread.addOfflineTransaction(offlineTransaction);
                        } else {
                            System.out.println("Transaction failed!!");
                            updateMessasge("Transaction failed!!");
                        }

                        Thread.sleep(1000);
                        updateMessasge("Please Swipe the Card:");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Failed! Please re-swipe the card!!");
                        updateMessasge("Failed! Please re-swipe the card!!");
                    }

                }
            });
            while(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateMessasge(String message) {
        SwingUtilities.invokeLater(() -> {
            swingUI.setMessageLabel(message);
        });
    }

    private static void writeTrasaction(MfCard mfCard, MfReaderWriter mfReaderWriter, String vmId, String cardId, double amount, String passcode, long timestamp, short sequenceNumber) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[8];
        secureRandom.nextBytes(key);
        byte[] passcodeHash = SHA256.getHMAC(passcode, key);
        writeTrasaction(mfCard, mfReaderWriter, vmId, cardId, amount, passcodeHash, key, timestamp, sequenceNumber);
    }

    private static void writeTrasaction(MfCard mfCard, MfReaderWriter mfReaderWriter, String vmId, String cardId, double amount, byte[] passcodeHash, byte[] passcodeKey, long timestamp, short sequenceNumber) {
        PlainTransaction transaction = new PlainTransaction(vmId, cardId,amount,passcodeHash, passcodeKey, timestamp, sequenceNumber);

        try {
            byte[] transactionBytes = TransactionManager.encryptAndSignTransaction(transaction,privatekeyFile, publickeyFile);

            MifareManager.writeTransaction(transactionBytes, mfReaderWriter, mfCard);

        } catch (CardException | GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    private static PlainTransaction retrieveTransaction(MfCard mfCard, MfReaderWriter mfReaderWriter) throws CardException, GeneralSecurityException, FileNotFoundException {
        byte[] retrievedTransactionBytes = MifareManager.readTransaction(mfReaderWriter,mfCard);

        PlainTransaction retrievedTransaction = TransactionManager.verifyAndDecrypt(retrievedTransactionBytes, publickeyFile);

        return retrievedTransaction;

    }
}
