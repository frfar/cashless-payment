package keypad;

import com.google.gson.Gson;
import config.Config;

import java.io.*;
import java.util.Arrays;

public class Keypad {

    private static Keypad keypad;

    private static ProcessBuilder pb;
    private static Process p;
    private static BufferedReader br;

    private static Gson gson = new Gson();

    private static String[] command;
    private static final int numberOfLinePerKeyStroke = 6;
    private static final int lineNumberWithKeyStroke1 = 1;
    private static final int lineNumberWithKeyStroke2 = 4;
    private static final int numberOfKeyStrokesInPassword = 4;

    private Keypad() {
        Config config = null;
        try {
            config = gson.fromJson(new FileReader("config.json"),Config.class);
            command = new String[] {"sudo","usbhid-dump","-m",config.keypadId,"-es"};
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Keypad getKeypadInstance() throws IOException {

        if(keypad == null || p == null || br == null) {
            keypad = new Keypad();
        }
        pb = new ProcessBuilder(Arrays.asList(command));
        p = pb.start();
        br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        keypad.flushBuffer();
        return keypad;
    }

    public String readKeyPressed() throws IOException {
        String lineWithKeyStroke1 = "";
        String lineWithKeyStroke2 = "";

        for(int i = 0; i < numberOfLinePerKeyStroke; i++) {
            String line = br.readLine();

            if(i == lineNumberWithKeyStroke1) {
                lineWithKeyStroke1 = line;
            }

            if(i == lineNumberWithKeyStroke2) {
                lineWithKeyStroke2 = line;
            }
        }

        String keyPressed = Converter.parse(lineWithKeyStroke1);

        if(keyPressed.equals("")) {
            keyPressed = Converter.parse(lineWithKeyStroke2);
        }

        return keyPressed;
    }

    public String readPassword() throws IOException {
        StringBuilder password = new StringBuilder();
        for(int i = 0; i < numberOfKeyStrokesInPassword; i++) {
            password.append(readKeyPressed());
            System.out.print("*");
        }
        System.lineSeparator();

        return password.toString();
    }

    public String readChars(int n, boolean... printChar) throws IOException {
        StringBuilder password = new StringBuilder();
        boolean flag = (printChar.length >= 1) ? printChar[0] : false;

        for(int i = 0; i < n; i++) {
            String key = readKeyPressed();
            password.append(key);
            if (flag) {
                System.out.print(key);
            }
        }
        System.lineSeparator();

        return password.toString();
    }

    public String readLine() throws IOException {
        StringBuilder line = new StringBuilder();
        String key;
        while(!(key = readKeyPressed()).equals("Enter")) {
            line.append(key);
        }

        return line.toString();
    }
    
    /** This is the method reads all lines in the buffer */
    public void flushBuffer() throws IOException {

        if(!br.ready()) {
            return;
        }
    	String line = br.readLine();
    	while (line != null)
    		line = br.readLine();
    }

    public void close() throws IOException {
        br.close();
        p.destroy();
    }
}
