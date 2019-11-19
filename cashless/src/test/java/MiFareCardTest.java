import mifare.*;
import org.nfctools.mf.MfCardListener;
import org.nfctools.mf.MfReaderWriter;
import org.nfctools.mf.card.MfCard;
import security.SHA256;
import transaction.PlainTransaction;
import transaction.TransactionManager;

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
                        byte[] passcodeHash = transaction.getPasscode();
                        byte[] passcodeSecret = transaction.getHashkey();

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

                        writeTrasaction(mfCard, mfReaderWriter, newAmount, userPasscode);

                        PlainTransaction retrievedTransaction = retrieveTransaction(mfCard, mfReaderWriter);
                        double retrievedAmount = retrievedTransaction.getAmount();

                        if(newAmount == retrievedAmount) {
                            System.out.println("The final amount in card is: " + newAmount);
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

    private static void writeTrasaction(MfCard mfCard, MfReaderWriter mfReaderWriter, double amount, String passcode) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[8];
        secureRandom.nextBytes(key);

        byte[] passcodeHash = SHA256.getHMAC(passcode, key);
        PlainTransaction transaction = new PlainTransaction("12345678","1234567890ABCDEF",amount,passcodeHash, key, System.currentTimeMillis());

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
