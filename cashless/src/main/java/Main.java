import com.pi4j.io.serial.Serial;
import felica.CardReader;
import felica.CardReaderCallback;
import felica.FelicaManager;
import keypad.Keypad;
import security.AES;
import security.utils.Utils;
import web.SendTransactionErrorResponse;
import web.SendTransactionResponse;
import web.SendTransactionSuccessResponse;
import web.TransactionService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Base64;

public class Main {

    private static final String NAME = "vm123456";
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

        CardReader cardReader = new CardReader();

        cardReader.addCardReaderCallback(new CardReaderCallback() {
            @Override
            public void isCardPresent(FelicaManager felicaManager) {
                try {
                    byte[] felicaResponse = felicaManager.polling();
                    String idm = felica.Utils.bin2hex(Arrays.copyOfRange(felicaResponse, 5, 13));
                    System.out.println("idm is: " + idm);

                    System.out.println("Enter your passcode");

                    Keypad keypad = Keypad.getKeypadInstance();
                    String passcode = keypad.readPassword();

//                    Transaction.setEccSignature(ECCSignature.getInstance());
//
//                    File file = new File("/home/pi/Desktop/cards/" + idm);
//                    if(file.exists()) {
//                        FileInputStream inputStream = new FileInputStream(file);
//                        byte[] previousTransaction = new byte[(int) file.length()];
//                        inputStream.read(previousTransaction);
//
//                        int encryptionSize = previousTransaction[0];
//                        int signatureSize = previousTransaction[1];
//                        byte[] encryptionBytes = Arrays.copyOfRange(previousTransaction,2,2 + encryptionSize);
//
//                        byte[] signatureBytes = Arrays.copyOfRange(previousTransaction,2 + encryptionSize, 2 + encryptionSize + signatureSize);
//
//                        byte[] t = Transaction.getValue(encryptionBytes,signatureBytes,DatatypeConverter.parseHexBinary("000102030405060708090a0b0c0d0e0f"));
//                        Transaction.printTransaction(t);
//                    }
//
//                    Transaction transaction = Transaction.create(NAME,idm,3, passcode, DatatypeConverter.parseHexBinary("1122334455667788"), DatatypeConverter.parseHexBinary("000102030405060708090a0b0c0d0e0f"));
//
//                    byte[] transactionBytes = transaction.getBytes();
//                    System.out.println("Size of the transaction is: " + transactionBytes.length);
//                    file = new File("/home/pi/Desktop/cards/" + idm);
//                    FileOutputStream outputStream = new FileOutputStream(file, true);
//                    outputStream.write(transactionBytes);
//                    outputStream.close();

                    SendTransactionResponse response = TransactionService.sendTransaction(idm, 2.0, passcode, NAME, TransactionService.Type.DEBIT);
                    SendTransactionErrorResponse error = response.getSendTransactionErrorResponse();
                    SendTransactionSuccessResponse success = response.getSendTransactionSuccessResponse();

                    if (error != null) {
                        System.out.println("There is an error!!");
                        System.out.println(error.message);
                    } else {
                        System.out.println("You have " + success.amount + " left in your account!!");

                        File file = new File(idm);

                        PrintWriter writer = new PrintWriter(new FileWriter(file));
                        writer.println(success.message.encryptedAmount + " " + success.message.signature);
                        writer.close();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

//        serial.addListener(event -> {
//            try {
//                String cardString = event.getAsciiString().trim();
//                System.out.println(cardString);
//
//                String[] cardStrings = cardString.split(" ");
//
//                String cardId = cardStrings[0];
//                String encodedCardHmac = cardStrings[1];
//                String encodedCardMoney = cardStrings[2];
//
//                System.out.println("Enter your 4-digit passcode:");
//                Keypad keypad = Keypad.getKeypadInstance();
//                String userPassword = keypad.readPassword();
//                byte[] userPasswordBytes = userPassword.getBytes();
//
//                byte[] userHmac = SHA256.getHMAC(cardId, userPasswordBytes);
//                String encodedUserHmac = new String(Base64.getEncoder().encode(userHmac));
//
//                if(encodedCardHmac.equals(encodedUserHmac)) {
//                    System.out.println("You have entered the right passcode!!");
//
//                    System.out.println("Encoded card money is: " + encodedCardMoney);
//                    byte[] encryptCardMoney = Base64.getDecoder().decode(encodedCardMoney);
//                    byte[] cardMoney = AES.decrypt(Utils.hexStringToBytes("2b7e151628aed2a6abf7158809cf4f3c"), Utils.hexStringToBytes("000102030405060708090a0b0c0d0e0f"),encryptCardMoney);
//
//                    int money = cardMoney[0];
//                    System.out.println("You have $" + money + " in your card!!");
//
//                    if (args[0].equals("vm")) {
//                        System.out.println("Coke $1: 101, Lays $2: 102, Pizza $3: 103. Or enter 000 to cancel");
//                        Keypad keypadItem = Keypad.getKeypadInstance();
//                        String selectedItemCode = keypadItem.readChars(3, true);
//                        if (selectedItemCode.equals("000")  || Integer.parseInt(selectedItemCode) > 103 || Integer.parseInt(selectedItemCode) < 101) {
//                            System.out.println("Transaction canceled");
//                        } else {
//                            System.out.println("Enter the amount of selected item from 1 to 9");
//                            int selectedAmount = -1;
//                            do {
//                                if (selectedAmount == 0) {
//                                    System.out.println("Please enter amount > 0");
//                                }
//                                Keypad keypadAmount = Keypad.getKeypadInstance();
//                                selectedAmount = Integer.parseInt(keypadAmount.readChars(1, true));
//                            } while (selectedAmount < 1);
//
//                            HashMap<String, Integer> mapItems = new HashMap<>();
//                            mapItems.put("101", 1);
//                            mapItems.put("102", 2);
//                            mapItems.put("103", 3);
//                            int totalPrice = mapItems.get(selectedItemCode) * selectedAmount;
//
//                            int newMoney = money - totalPrice;
//
//                            if(newMoney < 0) {
//                                System.out.println("Insufficient amount in the card!!");
//                            } else {
//                                System.out.println("You spent: $" + totalPrice);
//                                System.out.println("You have $" + newMoney + " left in your card");
//                                writeBackMoney(serial, cardStrings, (byte) newMoney);
//                            }
//                        }
//                    } else if (args[0].equals("atm")) {
//                        System.out.println("Reminder: your card limit is $30");
//                        System.out.println("Enter 1 for $5, 2 for $10, 3 for $30, or 0 to cancel");
//                        Keypad keypadMoney = Keypad.getKeypadInstance();
//                        String selectedMoneyCode = keypadMoney.readChars(1, true);
//
//                        if (selectedMoneyCode.equals("0") || Integer.parseInt(selectedMoneyCode) > 3) {
//                            System.out.println("Transaction canceled");
//                        } else {
//                            HashMap<String, Integer> mapMoney = new HashMap<>();
//                            mapMoney.put("1", 5);
//                            mapMoney.put("2", 10);
//                            mapMoney.put("3", 30);
//
//                            int selectedMoney = mapMoney.get(selectedMoneyCode);
//
//                            int newMoney = money + selectedMoney;
//
//                            if(newMoney > 30) {
//                                System.out.println("Maximum amount reached in the card!!");
//                            } else {
//                                System.out.println("You added: $" + selectedMoney);
//                                System.out.println("You now have $" + newMoney + " in your card");
//                                writeBackMoney(serial, cardStrings, (byte) newMoney);
//                            }
//                        }
//                    }
//                }
//            } catch (IOException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        });

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
}