import mifare.*;
import org.nfctools.mf.MfCardListener;
import org.nfctools.mf.MfReaderWriter;
import org.nfctools.mf.card.MfCard;
import security.SHA256;
import transaction.PlainTransaction;
import transaction.TransactionManager;
import web.TransactionUploadThread;
import web.structures.OfflineTransaction;

import javax.smartcardio.CardException;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Scanner;

public class MiFareCardTest {

    private static File privatekeyFile = new File("src/test/resources/ec256-key-pair-pkcs8.pem");
    private static File publickeyFile = new File("src/test/resources/ec256-public.pem");

    public static void main(String[] args) {

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

                    try {
                        PlainTransaction transaction = retrieveTransaction(mfCard, mfReaderWriter);
                        double amount = transaction.getAmount();
                        short sequenceNumber = transaction.getSequenceNumber();
                        byte[] passcodeHash = transaction.getPasscode();
                        byte[] passcodeSecret = transaction.getHashkey();

                        System.out.println("Enter Passcode:");
                        Scanner scanner = new Scanner(System.in);
                        String userPasscode = scanner.next();

                        byte[] userPasscodeHash = SHA256.getHMAC(userPasscode,passcodeSecret);

                        System.out.println(Arrays.toString(userPasscodeHash));
                        if(!Arrays.equals(userPasscodeHash,passcodeHash)) {
                            System.out.println("Passcode is not valid!");
                            return;
                        }

                        System.out.println("The amount in card is: " + amount);

                        System.out.println("Enter the amount you want to add");
                        double addedAmount = scanner.nextDouble();

                        if(amount + addedAmount > 100) {
                            System.out.println("You can't add more than $100!");
                            return;
                        }

                        double newAmount = amount + addedAmount;

                        long timestamp =  System.currentTimeMillis();
                        writeTrasaction(mfCard, mfReaderWriter, "12345679","1234567890ABCDEF", newAmount, userPasscode, timestamp, (short) (sequenceNumber + 1));

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


                            OfflineTransaction offlineTransaction = new OfflineTransaction("1","2", retrievedAmount, retrievedTransaction.getTimestamp(),prevVmIntId,amount, transaction.getTimestamp(), retrievedTransaction.getSequenceNumber());
                            transactionUploadThread.addOfflineTransaction(offlineTransaction);
                        } else {
                            System.out.println("Transaction failed!!");
                        }

                    } catch (CardException | GeneralSecurityException e) {
                        e.printStackTrace();
                    }

                }
            });
            //System.out.println("Press ENTER to exit");
            //System.in.read();
            while(true);
            //acr122.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
