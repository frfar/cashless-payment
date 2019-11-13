import mifare.*;
import org.nfctools.mf.MfCardListener;
import org.nfctools.mf.MfReaderWriter;
import org.nfctools.mf.card.MfCard;
import transaction.PlainTransaction;
import transaction.TransactionManager;

import javax.smartcardio.CardException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

public class MiFareCardTest {

    private static File privatekeyFile = new File("src/test/resources/ec256-key-pair-pkcs8.pem");
    private static File publickeyFile = new File("src/test/resources/ec256-public.pem");

    public static void main(String[] args) {

        System.out.println("Testing the MiFare Card Utility!!");
        
        Acr122Device acr122;
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
                        System.out.println("The amount in card is: " + amount);

                        if(amount < 5) {
                            System.out.println("You don't have Sufficient balance!");
                            return;
                        }

                        double newAmount = amount - 5;

                        writeTrasaction(mfCard, mfReaderWriter, newAmount);

                        System.out.println("The remaining amount in card is: " + newAmount);

                    } catch (CardException | GeneralSecurityException e) {
                        e.printStackTrace();
                    }

                }
            });
            System.out.println("Press ENTER to exit");
            System.in.read();

            acr122.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeTrasaction(MfCard mfCard, MfReaderWriter mfReaderWriter, double amount) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[8];
        secureRandom.nextBytes(key);

        PlainTransaction transaction = new PlainTransaction("12345678","1234567890ABCDEF",amount,"1234", key);

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

    private static void writeInitialTransaction(MfCard mfCard, MfReaderWriter mfReaderWriter) {
        writeTrasaction(mfCard, mfReaderWriter, 100);
    }
}
