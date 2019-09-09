import keypad.Keypad;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to the Cashless Payment System!");

        try {
            Keypad keypad = Keypad.getKeypadInstance();
            while(true) {
                System.out.println(keypad.readKeyPressed());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}