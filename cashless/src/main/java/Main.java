
import com.pi4j.io.serial.Serial;
import keypad.Keypad;
import mifare.Acr122Device;
import mifare.MifareManager;
import org.nfctools.mf.MfCardListener;
import org.nfctools.mf.MfReaderWriter;
import org.nfctools.mf.card.MfCard;
import security.AES;
import security.SHA256;
import security.utils.Utils;

import transaction.PlainTransaction;
import transaction.TransactionManager;
import web.TransactionUploadThread;
import web.structures.OfflineTransaction;

import javax.smartcardio.CardException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

public class Main {

    //here is the Name which specifies the vm which the work gets implemented to
    private static final String NAME = "vm123456";

    //private key that is later used for encryption
    private static File privatekeyFile = new File("ec256-key-pair-pkcs8.pem");

    //public key that is sent to the other side for decryption
    private static File publickeyFile = new File("ec256-public.pem");

    // TODO: Implement Logging
    // TODO: Implement save upload queue to file on abrupt termination

    public static void main(String[] args) {

	     //here we are asking the user from the command line to provide either the vm or atm as their argument
        if(args.length == 0) {
            System.out.println("Provide either vm or atm as first argument!");
            return;
        }

	       //here we have the vending machine option
        if (args[0].equals("vm")) {
            System.out.println("Welcome to the Cashless Payment System Vending Machine!");

	      //here we have the atm option
        } else if (args[0].equals("atm")) {
            System.out.println("Welcome to the Cashless Payment System ATM!");

	      //this is the case that the user didn't provide either of the options and some other input was given
        } else {
            System.out.println("Provide either vm or atm as first argument!");
            return;
        }

        System.out.println("Testing the MiFare Card Utility!!");

	      //here we are instanciating a Arc122 device
        Acr122Device acr122;

	      //we are creating an instance for the thread that is going to upload our transaction
        TransactionUploadThread transactionUploadThread = TransactionUploadThread.getInstance();

	      //this try-catch loop will find if the acr122 card was read or not
        try {
            acr122 = new Acr122Device();
        } catch (RuntimeException re) {
            System.out.println("No ACR122 reader found.");
            return;
        }

 	      //we are listening to the MfCardListener() here and finding if there is an issue with the card detector
        try {
            acr122.open();

	           //acr122's listener function and its first input is the cardListener
            acr122.listen(new MfCardListener() {
		            //we are seeing if there was a card detected with the inputs mfCard, MfReaderWriter and if it is successful it will print card detected.
                @Override
                public void cardDetected(MfCard mfCard, MfReaderWriter mfReaderWriter) throws IOException {
                    System.out.println("Card Detected!!");
                    //writeInitialTrasaction(mfCard, mfReaderWriter);
		                //now if the card was detected we will try to retrieve the transaction information
                    try {
			                   //we get the transaction from the mfcard via the mfReaderWriter
                        PlainTransaction transaction = retrieveTransaction(mfCard, mfReaderWriter);

			                  //we get the amount from the transaction class, the sequence number, passcode, and the Hashkey
                        double amount = transaction.getAmount();
                        short sequenceNumber = transaction.getSequenceNumber();
                        byte[] passcodeHash = transaction.getPasscode();
                        byte[] passcodeSecret = transaction.getHashkey();

			                  //getting the keypad instance
                        Keypad keypad = Keypad.getKeypadInstance();
                        System.out.println("Enter your passcode:");
                        keypad.flushBuffer();
                        String userPasscode = keypad.readPassword();

			                  //this will hash the user password
                        byte[] userPasscodeHash = SHA256.getHMAC(userPasscode,passcodeSecret);

			                  //we compare the passcodes to see whether it is valid or not
                        if(!Arrays.equals(userPasscodeHash,passcodeHash)) {
                            System.out.println("Passcode is not valid!");
                            return;
                        }

                        System.out.println("The amount in card is: " + amount);

                        if(amount < 5) {
                            System.out.println("You don't have Sufficient balance!");
                            return;
                        }
			                  //this will automatically do a transaction worth of 5
                        double newAmount = amount - 5;

			                  //printing it
                        System.out.println("Making a purchase of $5");

			                  //we get a timestamp using the system function of currentTimeMillis
                        long timestamp =  System.currentTimeMillis();

			                  //we are writing the transaction to the mf card
			                  //the thing that is most importantly written is the newAmount, the timestamp and the sequence number that is added one
                        writeTrasaction(mfCard, mfReaderWriter, "12345677","1234567890ABCDEF", newAmount, userPasscode, timestamp, (short)(sequenceNumber + 1));

			                  //Here by retrieving the transaction we check the amount was updated by the two lines below that we have here
                        PlainTransaction retrievedTransaction = retrieveTransaction(mfCard, mfReaderWriter);
                        double retrievedAmount = retrievedTransaction.getAmount();

			                  //this will check if the amount was correctly updated, and if so it will
                        if(newAmount == retrievedAmount) {
                            System.out.println("The final amount in card is: " + newAmount);

                            //assigning a vmID to the transaction
                            String prevVmId = transaction.getVmId();

                            //if the vm id was the 12345678 them prev vm ID is 0ne else if 12345679 then the id is 2
                            String prevVmIntId = "";
                            if(prevVmId.equals("12345678")) {
                                prevVmIntId = "1";
                            } else if(prevVmId.equals("12345679")) {
                                prevVmIntId = "2";
                            } else {
                                prevVmIntId = "3";
                            }

                            //we are instantiating an offline transaction sending the amount retrieved from the card, timestamp, prevVMId, timestamp, and sequenceNumber.
                            OfflineTransaction offlineTransaction = new OfflineTransaction("1","3", retrievedAmount, retrievedTransaction.getTimestamp(),prevVmIntId,amount, transaction.getTimestamp(), retrievedTransaction.getSequenceNumber());

                            //on the transaction thread we add an offline transaction so it can be saved for later retrieval
                            transactionUploadThread.addOfflineTransaction(offlineTransaction);
                        } else {
                            System.out.println("Transaction failed!!");
                        }

                    //this is a catch all exception
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Failed! Please re-swipe the card!!");
                    }

                }
            });
//            System.out.println("Press ENTER to exit");
//            System.in.read();
//
//            acr122.close();
            // this will wait
            while(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true);
    }

    //function that is the thing that writes bacl the money, to the card serial
    private static void writeBackMoney(Serial serial, String[] cardStrings, byte newMoney) throws IOException, InterruptedException {
        // this encripts the money object
        byte[] encryptNewMoney = AES.encrypt(Utils.hexStringToBytes("2b7e151628aed2a6abf7158809cf4f3c"), Utils.hexStringToBytes("000102030405060708090a0b0c0d0e0f"),new byte[]{newMoney});

        //converting the encoded to a string object
        String encodedNewMoney = Base64.getEncoder().encodeToString(encryptNewMoney);

        //we get the cardstrings and the encoded new money all in the same string
        String newCardString = cardStrings[0] + " " + cardStrings[1] + " " + encodedNewMoney;

        //we write it to the serial
        serial.write(newCardString + "\r\n");

        //telling the thread to sleep
        Thread.sleep(1000);

        System.out.println("Transaction successfully finished");
    }

    //function that takes the mf card, mfreaderwriter, vmID, cardId, amount in the card, password, timestamp and the sequence number
    private static void writeTrasaction(MfCard mfCard, MfReaderWriter mfReaderWriter, String vmId, String cardId, double amount, String passcode, long timestamp, short sequenceNumber) {

        //object sequence random for the purpose of verification later
        SecureRandom secureRandom = new SecureRandom();

        //getting the key from the secure random object
        byte[] key = new byte[8];
        secureRandom.nextBytes(key);

        //we get the pascode hash by the SHA256 class
        byte[] passcodeHash = SHA256.getHMAC(passcode, key);

        //getting the plainTransaction here and inputing all of the transaction specifiers
        PlainTransaction transaction = new PlainTransaction(vmId, cardId,amount,passcodeHash, key, timestamp, sequenceNumber);


        try {
            //encrypting and signing our transaction
            byte[] transactionBytes = TransactionManager.encryptAndSignTransaction(transaction,privatekeyFile, publickeyFile);

            //we write our transaction to the mfare card
            MifareManager.writeTransaction(transactionBytes, mfReaderWriter, mfCard);

        //we check whether the card was working correctly, security was working, and any file reads and writes working
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
