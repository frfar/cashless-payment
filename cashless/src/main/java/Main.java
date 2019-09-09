import keypad.Keypad;
import security.AES;
import security.utils.Utils;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the Cashless Payment System!");

        byte[] encrypted = AES.encrypt(Utils.hexStringToByte("2b7e151628aed2a6abf7158809cf4f3c"), Utils.hexStringToByte("000102030405060708090a0b0c0d0e0f"), Utils.hexStringToByte("6bc1bee22e409f96"));
        System.out.println(new String(encrypted));
//        try {
//            Keypad keypad = Keypad.getKeypadInstance();
//            while(true) {
//                System.out.println(keypad.readPassword());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}