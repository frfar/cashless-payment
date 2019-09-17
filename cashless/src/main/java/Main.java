import com.pi4j.io.serial.*;
import keypad.Keypad;
import security.AES;
import security.SHA256;
import security.utils.Utils;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        if (args[0].equals("vm")) {
            System.out.println("Welcome to the Cashless Payment System Vending Machine!");
        } else if (args[0].equals("atm")) {
            System.out.println("Welcome to the Cashless Payment System ATM!");
        }

        final Serial serial = SerialFactory.createInstance();

        SerialConfig config = new SerialConfig();
        config.device("/dev/" + "ttyACM0")
                .baud(Baud._57600)
                .dataBits(DataBits._8)
                .parity(Parity.NONE)
                .stopBits(StopBits._1)
                .flowControl(FlowControl.NONE);

        try {
            System.out.println("Initializing the connection!!");
            serial.open(config);
            Thread.sleep(3000);
            System.out.println("Connection setup successfully!!");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

//        String id = "8223372036851235807";
//        String password = "1991";
//        byte[] passwordBytes = password.getBytes();
//
//        byte[] hmac = SHA256.getHMAC(id, passwordBytes);
//        String encodedHmac = new String(Base64.getEncoder().encode(hmac));
//
//        byte[] encryptedMoney = AES.encrypt(Utils.hexStringToBytes("2b7e151628aed2a6abf7158809cf4f3c"), Utils.hexStringToBytes("000102030405060708090a0b0c0d0e0f"), Utils.hexStringToBytes("10"));
//        String encodedMoney = new String(Base64.getEncoder().encode(encryptedMoney));
//        System.out.println(id + " " + encodedHmac + " " + encodedMoney);
//        try {
//            serial.write(id + " " + encodedHmac + " " + encodedMoney +"\r\n");
//            Thread.sleep(1000);
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }

        serial.addListener(event -> {
            try {
                System.out.println(event.getAsciiString().trim());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serial.addListener(event -> {
            try {
                String cardString = event.getAsciiString().trim();
                System.out.println(cardString);

                String[] cardStrings = cardString.split(" ");

                String cardId = cardStrings[0];
                String encodedCardHmac = cardStrings[1];
                String encodedCardMoney = cardStrings[2];

                System.out.println("Enter your 4-digit passcode:");
                Keypad keypad = Keypad.getKeypadInstance();
                String userPassword = keypad.readPassword();
                byte[] userPasswordBytes = userPassword.getBytes();

                byte[] userHmac = SHA256.getHMAC(cardId, userPasswordBytes);
                String encodedUserHmac = new String(Base64.getEncoder().encode(userHmac));

                if(encodedCardHmac.equals(encodedUserHmac)) {
                    System.out.println("You have entered the right passcode!!");

                    System.out.println("Encoded card money is: " + encodedCardMoney);
                    byte[] encryptCardMoney = Base64.getDecoder().decode(encodedCardMoney);
                    byte[] cardMoney = AES.decrypt(Utils.hexStringToBytes("2b7e151628aed2a6abf7158809cf4f3c"), Utils.hexStringToBytes("000102030405060708090a0b0c0d0e0f"),encryptCardMoney);

                    int money = cardMoney[0];
                    System.out.println("You have $" + money + " in your card!!");

                    if (args[0].equals("vm")) {
                        System.out.println("Coke $1: 101, Lays $2: 102, Pizza $3: 103. Or enter 000 to cancel");
                        Keypad keypadItem = Keypad.getKeypadInstance();
                        String selectedItemCode = keypadItem.readChars(3, true);
                        if (selectedItemCode.equals("000")) {
                            System.out.println("Transaction canceled");
                        } else {
                            System.out.println("Enter the amount of selected item from 1 to 9");
                            int selectedAmount = -1;
                            do {
                                if (selectedAmount == 0) {
                                    System.out.println("Please enter amount > 0");
                                }
                                Keypad keypadAmount = Keypad.getKeypadInstance();
                                selectedAmount = Integer.parseInt(keypadAmount.readChars(1, true));
                            } while (selectedAmount < 1);

                            HashMap<String, Integer> mapItems = new HashMap<>();
                            mapItems.put("101", 1);
                            mapItems.put("102", 2);
                            mapItems.put("103", 3);
                            int totalPrice = mapItems.get(selectedItemCode) * selectedAmount;

                            int newMoney = money - totalPrice;

                            if(newMoney < 0) {
                                System.out.println("Insufficient amount in the card!!");
                            } else {
                                System.out.println("You spent: $" + totalPrice);
                                System.out.println("You have $" + newMoney + " left in your card");
                                byte[] encryptNewMoney = AES.encrypt(Utils.hexStringToBytes("2b7e151628aed2a6abf7158809cf4f3c"), Utils.hexStringToBytes("000102030405060708090a0b0c0d0e0f"),new byte[]{(byte)newMoney});
                                String encodedNewMoney = Base64.getEncoder().encodeToString(encryptNewMoney);

                                String newCardString = cardStrings[0] + " " + cardStrings[1] + " " + encodedNewMoney;

                                serial.write(newCardString + "\r\n");

                                Thread.sleep(1000);

                                System.out.println("Transaction successfully finished");
                            }
                        }
                    } else if (args[0].equals("atm")) {
                        System.out.println("Reminder: your card limit is $30");
                        System.out.println("Enter 1 for $5, 2 for $10, 3 for $30, or 0 to cancel");
                        Keypad keypadMoney = Keypad.getKeypadInstance();
                        String selectedMoneyCode = keypadMoney.readChars(1, true);

                        if (selectedMoneyCode.equals("0")) {
                            System.out.println("Transaction canceled");
                        } else {
                            HashMap<String, Integer> mapMoney = new HashMap<>();
                            mapMoney.put("1", 5);
                            mapMoney.put("2", 10);
                            mapMoney.put("3", 30);

                            int selectedMoney = mapMoney.get(selectedMoneyCode);

                            int newMoney = money + selectedMoney;

                            if(newMoney > 30) {
                                System.out.println("Maximum amount reached in the card!!");
                            } else {
                                System.out.println("You added: $" + selectedMoney);
                                System.out.println("You now have $" + newMoney + " in your card");
                                byte[] encryptNewMoney = AES.encrypt(Utils.hexStringToBytes("2b7e151628aed2a6abf7158809cf4f3c"), Utils.hexStringToBytes("000102030405060708090a0b0c0d0e0f"),new byte[]{(byte)newMoney});
                                String encodedNewMoney = Base64.getEncoder().encodeToString(encryptNewMoney);

                                String newCardString = cardStrings[0] + " " + cardStrings[1] + " " + encodedNewMoney;

                                serial.write(newCardString + "\r\n");

                                Thread.sleep(1000);

                                System.out.println("Transaction successfully finished");
                            }
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });

//        serial.addListener((SerialDataEventListener) event -> {
//            try {
//                String cardString = event.getAsciiString().trim();
//                System.out.println("Messaged Received from Card:");
//                System.out.print(event.getAsciiString());
//
//                byte[] encryptedMoney = AES.encrypt(Utils.hexStringToBytes("2b7e151628aed2a6abf7158809cf4f3c"), Utils.hexStringToBytes("000102030405060708090a0b0c0d0e0f"), Utils.hexStringToBytes("6bc1bee22e409f96"));
//
//                System.out.println("Please Enter your 4 digit pin:");
//
//                Keypad keypad = Keypad.getKeypadInstance();
//                String password = keypad.readPassword();
//                byte[] passwordBytes = password.getBytes();
//                System.out.println("You have entered: " + password);
//
//                byte[] hmac = SHA256.getHMAC(encryptedMoney, passwordBytes);
//
//                String encryptedString = new String(Base64.getEncoder().encode(hmac));
//                System.out.println("The encryptedMoney String is:" + encryptedString);
//
//                if(!encryptedString.equals(cardString)) {
//                    System.out.println("You have entered incorrect pin!!");
//                } else {
//                    System.out.println("You have entered correct pin!!");
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });

//        Keypad keypad = null;
//        try {
//            keypad = Keypad.getKeypadInstance();
//            String line = keypad.readLine();
//
//            System.out.println(line);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
        while (true);
    }
}