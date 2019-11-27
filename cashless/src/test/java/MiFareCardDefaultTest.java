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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class MiFareCardDefaultTest {

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

                        int random = (int) (Math.random() * 8999 + 1000);

                        System.out.println("Your passcode is: " + random);
                        writeTrasaction(mfCard, mfReaderWriter,"12345678","1234567890ABCDEF", 0, Integer.toString(random), System.currentTimeMillis(), (short)0);

                        PlainTransaction retrievedTransaction = retrieveTransaction(mfCard, mfReaderWriter);
                        double retrievedAmount = retrievedTransaction.getAmount();

                        if(0 == retrievedAmount) {
                            System.out.println("The final amount in card is: " + 0);

                            OfflineTransaction offlineTransaction = new OfflineTransaction("1","1", retrievedAmount, retrievedTransaction.getTimestamp(),"1",0, retrievedTransaction.getTimestamp(), retrievedTransaction.getSequenceNumber());

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
