import security.SHA256;
import transaction.*;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

public class TransactionBlockTest {

    private static File privatekeyFile = new File("src/test/resources/ec256-key-pair-pkcs8.pem");
    private static File publickeyFile = new File("src/test/resources/ec256-public.pem");

    public static void main(String[] args) throws IOException, GeneralSecurityException {

        writeTransaction();
        BufferedReader reader = new BufferedReader(new FileReader("card.txt"));
        String fileContent = reader.lines().collect(Collectors.joining());

        String[] separateFileTransactions = fileContent.split(" ");

        for (String fileTransaction: separateFileTransactions) {
            byte[] fileContentBytes = Base64.getDecoder().decode(fileTransaction);
            PlainTransaction retrievedTransaction = TransactionManager.verifyAndDecrypt(fileContentBytes, publickeyFile);

            if(retrievedTransaction == null) {
                System.out.println("error in verification!!");
            }
            System.out.println(retrievedTransaction);
        }

        //       System.out.println(retrievedTransaction.equals(transaction1));
        //     System.out.println(retrievedTransaction);

        //byte[] hmac = SHA256.getHMAC(retrievedTransaction.getHashkey(),retrievedTransaction.getHashkey());
        //PlainTransaction transaction2 = new PlainTransaction("12345678","1234567890ABCDEF",4,"1234", Arrays.copyOfRange(hmac,0,8));

    }

    private static void writeTransaction() throws IOException, GeneralSecurityException {
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[8];
        secureRandom.nextBytes(key);

        PlainTransaction transaction = new PlainTransaction("12345678","1234567890ABCDEF",3.5,SHA256.getHMAC("1234", key), key, System.currentTimeMillis(),(short)0);

        byte[] transactionBytes = TransactionManager.encryptAndSignTransaction(transaction,privatekeyFile, publickeyFile);

        PrintWriter writer = new PrintWriter(new FileWriter("card.txt",true));
        writer.println(new String(Base64.getEncoder().encode(transactionBytes)));
        writer.print(" ");
        writer.close();
    }
}
