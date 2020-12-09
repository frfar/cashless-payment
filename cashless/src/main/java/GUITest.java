import config.Config;
import mifare.Acr122Device;
import mifare.MifareManager;
import org.nfctools.mf.MfCardListener;
import org.nfctools.mf.MfReaderWriter;
import org.nfctools.mf.card.MfCard;
import security.SHA256;
import transaction.PlainTransaction;
import transaction.TransactionManager;
import web.TransactionUploadThread;
import web.structures.OfflineTransaction;

import javax.smartcardio.CardException;
import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import keypad.UIKeypad;

public class GUITest {

    //NAME is the vm name which isn't used
    private static final String NAME = "vm123456";

    //this is for the https private key
    private static File privatekeyFile = new File(Config.privateKeyFileName);

    //this is the public key file
    private static File publickeyFile = new File(Config.publicKeyFileName);

    //this is the UI class
    private static SwingUI swingUI = new SwingUI();

    //main function
    public static void main(String[] args) {
	
	JFrame frame = new JFrame();
	
	//here we are setting the setting options
        SwingUtilities.invokeLater(() -> {
            frame.add(swingUI);
            frame.setSize(500,300);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });

        Acr122Device acr122;

        TransactionUploadThread transactionUploadThread = TransactionUploadThread.getInstance();

        try {
            acr122 = new Acr122Device();
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
                        System.out.println("Enter your passcode:");
                        updateMessasge("Enter your passcode:");
                        keypad.flushBuffer();
                        String userPasscode = keypad.readPassword();

                        byte[] userPasscodeHash = SHA256.getHMAC(userPasscode,passcodeSecret);

                        if(!Arrays.equals(userPasscodeHash,passcodeHash)) {
                            System.out.println("Passcode is not valid!");
                            updateMessasge("Passcode is not valid!");
                            return;
                        }


                        System.out.println("The amount in card is: " + amount);
                        updateMessasge("The amount in card is: " + amount);

                        Thread.sleep(1000);
                        updateMessasge("Please select an item");
                        keypad.flushBuffer();
                        String item = keypad.readChars(3);

                        double itemPrice = 0;

                        if(item.equals("101")) {
                            itemPrice = 2;
                        } else if(item.equals("102")) {
                            itemPrice = 20;
                        } else if(item.equals("103")) {
                            itemPrice = 3;
                        }

                        if(itemPrice == 0) {
                            System.out.println("Please select a proper item!");
                            updateMessasge("Please select a proper item!");

                            Thread.sleep(1000);
                            updateMessasge("Please Swipe the Card:");
                            return;
                        }

                        if(amount < itemPrice) {
                            System.out.println("You don't have Sufficient balance!");
                            updateMessasge("You don't have Sufficient balance!");

                            Thread.sleep(1000);
                            updateMessasge("Please Swipe the Card:");
                            return;
                        }

                        double newAmount = amount - itemPrice;

                        System.out.println("Making a purchase of $" + itemPrice);
                        updateMessasge("Making a purchase of $" + itemPrice);

                        long timestamp =  System.currentTimeMillis();
                        writeTrasaction(mfCard, mfReaderWriter, "12345677","1234567890ABCDEF", newAmount, userPasscode, timestamp, (short)(sequenceNumber + 1));

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

                            OfflineTransaction offlineTransaction = new OfflineTransaction("1","3", retrievedAmount, retrievedTransaction.getTimestamp(),prevVmIntId,amount, transaction.getTimestamp(), retrievedTransaction.getSequenceNumber());
                            transactionUploadThread.addOfflineTransaction(offlineTransaction);
                            dispenseAction();
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
//            System.out.println("Press ENTER to exit");
//            System.in.read();
//
//            acr122.close();
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

    private static void dispenseAction(){
        // Stub for dispensing action
        JOptionPane.showMessageDialog(null, "Dispense action complete", "Dispensing Item! " , JOptionPane.INFORMATION_MESSAGE);
    }

    private static void writeTrasaction(MfCard mfCard, MfReaderWriter mfReaderWriter, String vmId, String cardId, double amount, String passcode, long timestamp, short sequenceNumber) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[8];
        secureRandom.nextBytes(key);

        byte[] passcodeHash = SHA256.getHMAC(passcode, key);
        PlainTransaction transaction = new PlainTransaction(vmId, cardId,amount,passcodeHash, key, timestamp, sequenceNumber);

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
