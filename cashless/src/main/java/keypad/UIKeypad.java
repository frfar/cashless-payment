package keypad;

import java.util.LinkedList;
import java.util.Queue;

public class UIKeypad {

    private static UIKeypad keypad;

    private static Queue<String> lines = new LinkedList<>();

    private static final int numberOfKeyStrokesInPassword = 4;

    private UIKeypad() {

    }

    public static UIKeypad getKeypadInstance() {

        if(keypad == null) {
            keypad = new UIKeypad();
        }

        return keypad;
    }

    public static void addKeyStroke(String line) {
        lines.add(line);
    }

    public String readKeyPressed() {
        while(lines.isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return lines.poll();
    }

    public String readPassword() {
        StringBuilder password = new StringBuilder();
        for(int i = 0; i < numberOfKeyStrokesInPassword; i++) {
            password.append(readKeyPressed());
        }

        return password.toString();
    }

    public String readChars(int n, boolean... printChar) {
        StringBuilder password = new StringBuilder();
        boolean flag = (printChar.length >= 1) && printChar[0];

        for(int i = 0; i < n; i++) {
            String key = readKeyPressed();
            if (key.equals("\n")){
                break;
            }
            password.append(key);
            if (flag) {
                System.out.print(key);
            }
        }

        return password.toString();
    }

    public String readLine() {
        StringBuilder line = new StringBuilder();
        String key;
        while(!(key = readKeyPressed()).equals("Enter")) {
            line.append(key);
        }

        return line.toString();
    }

    /** This is the method reads all lines in the buffer */
    public void flushBuffer() {
        lines.clear();
    }
}

