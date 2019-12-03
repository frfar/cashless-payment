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

public class Main {

    private static final String NAME = "vm123456";
    private static File privatekeyFile = new File("ec256-key-pair-pkcs8.pem");
    private static File publickeyFile = new File("ec256-public.pem");

    public static void main(String[] args) {

        if(args.length == 0) {
            System.out.println("Provide either vm or atm as first argument!");
            return;
        }
        if (args[0].equals("vm")) {
            System.out.println("Welcome to the Cashless Payment System Vending Machine!");
        } else if (args[0].equals("atm")) {
            System.out.println("Welcome to the Cashless Payment System ATM!");
        } else {
            System.out.println("Provide either vm or atm as first argument!");
            return;
        }

        System.out.println("Testing the MiFare Card Utility!!");

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
                public void cardDetected(MfCard mfCard, MfReaderWriter mfReaderWriter) throws IOException {
                    System.out.println("Card Detected!!");
                    //writeInitialTrasaction(mfCard, mfReaderWriter);

                    try {
                        PlainTransaction transaction = retrieveTransaction(mfCard, mfReaderWriter);
                        double amount = transaction.getAmount();
                        short sequenceNumber = transaction.getSequenceNumber();
                        byte[] passcodeHash = transaction.getPasscode();
                        byte[] passcodeSecret = transaction.getHashkey();

                        Keypad keypad = Keypad.getKeypadInstance();
                        System.out.println("Enter your passcode:");
                        keypad.flushBuffer();
                        String userPasscode = keypad.readPassword();
s
                        byte[] userPasscodeHash = SHA256.getHMAC(userPasscode,passcodeSecret);

                        if(!Arrays.equals(userPasscodeHash,passcodeHash)) {
                            System.out.println("Passcode is not valid!");
                            return;
                        }

                        System.out.println("The amount in card is: " + amount);

                        if(amount < 5) {
                            System.out.println("You don't have Sufficient balance!");
                            return;
                        }

                        double newAmount = amount - 5;

                        System.out.println("Making a purchase of $5");

                        long timestamp =  System.currentTimeMillis();
                        writeTrasaction(mfCard, mfReaderWriter, "12345677","1234567890ABCDEF", newAmount, userPasscode, timestamp, (short)(sequenceNumber + 1));

                        PlainTransaction retrievedTransaction = retrieveTransaction(mfCard, mfReaderWriter);
                        double retrievedAmount = retrievedTransaction.getAmount();

                        if(newAmount == retrievedAmount) {
                            System.out.println("The final amount in card is: " + newAmount);

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
                        } else {
                            System.out.println("Transaction failed!!");
                        }

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
            while(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true);
    }

    private static void writeBackMoney(Serial serial, String[] cardStrings, byte newMoney) throws IOException, InterruptedException {
        byte[] encryptNewMoney = AES.encrypt(Utils.hexStringToBytes("2b7e151628aed2a6abf7158809cf4f3c"), Utils.hexStringToBytes("000102030405060708090a0b0c0d0e0f"),new byte[]{newMoney});
        String encodedNewMoney = Base64.getEncoder().encodeToString(encryptNewMoney);

        String newCardString = cardStrings[0] + " " + cardStrings[1] + " " + encodedNewMoney;

        serial.write(newCardString + "\r\n");

        Thread.sleep(1000);

        System.out.println("Transaction successfully finished");
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