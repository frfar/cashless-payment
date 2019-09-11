package keypad;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Keypad {

    private static Keypad keypad;

    private static ProcessBuilder pb;
    private static Process p;
    private static BufferedReader br;

    private static final String[] command = new String[] {"sudo","usbhid-dump","-m","04d9:1203","-es"};
    private static final int numberOfLinePerKeyStroke = 6;
    private static final int lineNumberWithKeyStroke = 1;
    private static final int numberOfKeyStrokesInPassword = 4;

    private Keypad() {
        pb = new ProcessBuilder(Arrays.asList(command));
    }

    public static Keypad getKeypadInstance() throws IOException {

        if(keypad == null || p == null || br == null) {
            keypad = new Keypad();
            p = pb.start();
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        }
        return keypad;
    }

    private static String readLine() throws IOException {
        return br.readLine();
    }

    public String readKeyPressed() throws IOException {
        String lineWithKeyStroke = "";
        for(int i = 0; i < numberOfLinePerKeyStroke; i++) {
            String line = br.readLine();

            if(i == lineNumberWithKeyStroke) {
                lineWithKeyStroke = line;
            }
        }
        return Converter.parse(lineWithKeyStroke);
    }

    public String readPassword() throws IOException {
        StringBuilder password = new StringBuilder();
        for(int i = 0; i < numberOfKeyStrokesInPassword; i++) {
            password.append(readKeyPressed());
        }

        return password.toString();
    }

    public String readKeyLine() throws IOException {
        StringBuilder line = new StringBuilder();
        String key;
        while(!(key = readKeyPressed()).equals("Enter")) {
            line.append(key);
        }

        return line.toString();
    }
}
