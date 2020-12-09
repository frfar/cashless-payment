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

    //instantiation the name of the vendor, private key file, public key file and SwingUI which is the UI help, and the transaction upload thread
    private static final String NAME = "authorized-vendor";
    private static File privatekeyFile = new File(Config.privateKeyFileName);
    private static File publickeyFile = new File(Config.publicKeyFileName);
    private static SwingUI swingUI = new SwingUI(false);
    private static LoginForm loginForm = new LoginForm();
    private static TransactionUploadThread transactionUploadThread;

    public static void main(String[] args) throws InterruptedException {

        //instantiating our cardreader device
        Acr122Device acr122;

        //creating a thread for the uploading of the transaction
        transactionUploadThread = TransactionUploadThread.getInstance();

        // Require Internet connection or else it will snap out of the device
        if (!transactionUploadThread.isConnected()){
            System.out.println("Internet is not connected. Please try again later");
            return;
        }

        //the Jframe is the class that instantiates the form
        JFrame jFrame = new JFrame();
        jFrame.add(loginForm);
        jFrame.setSize(300, 300);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);

        //if the authentication service is logged in the sleep our thread
        while (!AuthenticationService.isLoggedIn()){
          Thread.sleep(1000);
        }
        //our frame is set to not being visible
        jFrame.setVisible(false);

        //showing the vendorMain if the authentication service is in fact logged in
        if (AuthenticationService.isLoggedIn()) {
            vendorMain();
        }
    }

    //this is the function for the vendormain
    private static void vendorMain() {
        //again we are showing the screen
        JFrame frame = new JFrame();
        Acr122Device acr122;
        try {
            //instantiating the device
            acr122 = new Acr122Device();
            //this is the screen
            SwingUtilities.invokeLater(() -> {
                frame.add(swingUI);
                frame.setSize(300,300);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            });

        //catching any runtime errors
        } catch (RuntimeException re) {
            System.out.println("No ACR122 reader found.");
            return;
        }
        try {
            //opeining the card reader device
            acr122.open();

            //we are listening to the mf card
            acr122.listen(new MfCardListener() {
                //when the card is swipped then this here will take place
                @Override
                public void cardDetected(MfCard mfCard, MfReaderWriter mfReaderWriter)  {
                    System.out.println("Card Detected!!");

                    try {
                        //get the transaction and amount sequenceNumber and passcode and secrect for the passcode from the class
                        PlainTransaction transaction = retrieveTransaction(mfCard, mfReaderWriter);
                        double amount = transaction.getAmount();
                        short sequenceNumber = transaction.getSequenceNumber();
                        byte[] passcodeHash = transaction.getPasscode();
                        byte[] passcodeSecret = transaction.getHashkey();

                        // instantiating the keypad
                        UIKeypad keypad = UIKeypad.getKeypadInstance();

                        //here we display the amount and enter the amount we want to add
                        System.out.println("The amount in card is: " + amount);
                        updateMessasge("Current amount in card is: " + amount);
                        Thread.sleep(1000);
                        updateMessasge("Enter the amount you want to add");

                        //here we do the operations so the user can enter the amount
                        keypad.flushBuffer();
                        String addedAmount = keypad.readChars(3);
                        int intAddedAmount = Integer.parseInt(addedAmount);

                        //we can't add more than a 100
                        if(amount + intAddedAmount > 100) {
                            System.out.println("You can't add more than $100!");
                            //waits for a second the we swipe it again
                            Thread.sleep(1000);
                            updateMessasge("Please Swipe the Card:");
                            return;
                        } else if (intAddedAmount <= 0) {

                            //we also can't add zero for the edge cases
                            System.out.println("Amount must be greater than $0!");
                            Thread.sleep(1000);

                            //letting us we can swipe the card
                            updateMessasge("Please Swipe the Card:");
                            return;
                        }

                        //updating the amount
                        double newAmount = amount + intAddedAmount;

                        //sending updated message to the screen
                        updateMessasge("Adding amount of $" + intAddedAmount);

                        //we are getting the transaction
                        long timestamp =  System.currentTimeMillis();

                        //we are writing the transaction to the mf card
			                  //the thing that is most importantly written is the newAmount, the timestamp and the sequence number that is added one
                        writeTrasaction(mfCard, mfReaderWriter, "12345679","1234567890ABCDEF", newAmount, passcodeHash, passcodeSecret, timestamp, (short)(sequenceNumber + 1));

                        PlainTransaction retrievedTransaction = retrieveTransaction(mfCard, mfReaderWriter);
                        double retrievedAmount = retrievedTransaction.getAmount();

                        //checking if update indeed happened in the card
                        if(newAmount == retrievedAmount) {
                            System.out.println("The final amount in card is: " + newAmount);
                            updateMessasge("The final amount in card is: " + newAmount);

                            String prevVmId = transaction.getVmId();
                            String prevVmIntId = "";

                            //if the vm id was the 12345678 them prev vm ID is 0ne else if 12345679 then the id is 2
                            if(prevVmId.equals("12345678")) {
                                prevVmIntId = "1";
                            } else if(prevVmId.equals("12345679")) {
                                prevVmIntId = "2";
                            } else {
                                prevVmIntId = "3";
                            }

                            //we are instantiating an offline transaction sending the amount retrieved from the card, timestamp, prevVMId, timestamp, and sequenceNumber.
                            OfflineTransaction offlineTransaction = new OfflineTransaction("1","2", retrievedAmount, retrievedTransaction.getTimestamp(),prevVmIntId,amount, transaction.getTimestamp(), retrievedTransaction.getSequenceNumber());

                            //we set the vendor id to the transaction that we have here
                            offlineTransaction.setVendorId(AuthenticationService.getAuthUser().getId());

                            //we add the offline transaction to the thread
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
    //this function updates the message in the screen of our choosing
    private static void updateMessasge(String message) {
        SwingUtilities.invokeLater(() -> {
            swingUI.setMessageLabel(message);
        });
    }
    //we write our transaction to the mfare card with the below things including, vmID, cardId, amount, passcode, timestamp, and sequenceId
    private static void writeTrasaction(MfCard mfCard, MfReaderWriter mfReaderWriter, String vmId, String cardId, double amount, String passcode, long timestamp, short sequenceNumber) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[8];
        secureRandom.nextBytes(key);
        byte[] passcodeHash = SHA256.getHMAC(passcode, key);

        //writing the transaction with its password hash
        writeTrasaction(mfCard, mfReaderWriter, vmId, cardId, amount, passcodeHash, key, timestamp, sequenceNumber);
    }
    //this is the other write transaction function which this time actually writes it to the online db
    private static void writeTrasaction(MfCard mfCard, MfReaderWriter mfReaderWriter, String vmId, String cardId, double amount, byte[] passcodeHash, byte[] passcodeKey, long timestamp, short sequenceNumber) {
        PlainTransaction transaction = new PlainTransaction(vmId, cardId,amount,passcodeHash, passcodeKey, timestamp, sequenceNumber);


        try {
            byte[] transactionBytes = TransactionManager.encryptAndSignTransaction(transaction,privatekeyFile, publickeyFile);

            //the line that does the writing
            MifareManager.writeTransaction(transactionBytes, mfReaderWriter, mfCard);

        } catch (CardException | GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    //this in a way will retrieve a transaction data and will throw an error if the transaction was faulty including: CardException, GeneralSecurityException, FileNotFoundException
    private static PlainTransaction retrieveTransaction(MfCard mfCard, MfReaderWriter mfReaderWriter) throws CardException, GeneralSecurityException, FileNotFoundException {

      //this is the bytes of the transaction
        byte[] retrievedTransactionBytes = MifareManager.readTransaction(mfReaderWriter,mfCard);

        //verifies and decrypts the transaction in there
        PlainTransaction retrievedTransaction = TransactionManager.verifyAndDecrypt(retrievedTransactionBytes, publickeyFile);

        return retrievedTransaction;

    }
}
