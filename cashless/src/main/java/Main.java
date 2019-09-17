import com.pi4j.io.serial.*;
import keypad.Keypad;
import security.AES;
import security.SHA256;
import security.utils.Utils;

import java.io.IOException;
import java.util.Base64;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the Cashless Payment System!");

        final Serial serial = SerialFactory.createInstance();

        SerialConfig config = new SerialConfig();
        config.device("/dev/" + "ttyACM0")
                .baud(Baud._57600)
                .dataBits(DataBits._8)
                .parity(Parity.NONE)
                .stopBits(StopBits._1)
                .flowControl(FlowControl.NONE);

        try {
            System.out.println("Initializing the connections!!");
            serial.open(config);
            Thread.sleep(5000);
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

//        serial.addListener(event -> {
//            try {
//                System.out.println(event.getAsciiString().trim());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
        serial.addListener(event -> {
            try {
                String cardString = event.getAsciiString().trim();
                System.out.println(cardString);

                String[] cardStrings = cardString.split(" ");

                String cardId = cardStrings[0];
                String encodedCardHmac = cardStrings[1];
                String encodedCardMoney = cardStrings[2];

                System.out.println("Enter the passcode:");
                Keypad keypad = Keypad.getKeypadInstance();
                String userPassword = keypad.readPassword();
                byte[] userPasswordBytes = userPassword.getBytes();
                System.out.println("You have entered: " + userPassword);

                byte[] userHmac = SHA256.getHMAC(cardId, userPasswordBytes);
                String encodedUserHmac = new String(Base64.getEncoder().encode(userHmac));

                if(encodedCardHmac.equals(encodedUserHmac)) {
                    System.out.println("You have entered the right passcode!!");

                    System.out.println("encoded card money is: " + encodedCardMoney);
                    byte[] encryptCardMoney = Base64.getDecoder().decode(encodedCardMoney);
                    byte[] cardMoney = AES.decrypt(Utils.hexStringToBytes("2b7e151628aed2a6abf7158809cf4f3c"), Utils.hexStringToBytes("000102030405060708090a0b0c0d0e0f"),encryptCardMoney);

                    System.out.println((int) cardMoney[0]);
                }
            } catch (IOException e) {
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