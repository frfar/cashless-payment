package keypad;

import com.google.gson.Gson;
import config.Config;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class Keypad {

    private static Keypad keypad;

    private static Process p;
    private static Queue<String> lines = new LinkedList<>();

    private static Gson gson = new Gson();

    private static String[] command;
    private static final int numberOfLinePerKeyStroke = 6;
    private static final int lineNumberWithKeyStroke = 1;
    private static final int numberOfKeyStrokesInPassword = 4;

    private static class KeypadStream extends Thread {
        private BufferedReader br;
        private KeypadStreamCallback callback;

        KeypadStream(BufferedReader br, KeypadStreamCallback callback) {
            this.br = br;
            this.callback = callback;
        }

        @Override
        public void run() {
            super.run();

            while(true) {
                try {
                    String line = br.readLine();

                    if(line != null) {
                        callback.newLineInStream(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    interface KeypadStreamCallback {
        void newLineInStream(String str);
    }

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

        if(keypad == null || p == null) {
            keypad = new Keypad();
        }
        ProcessBuilder pb = new ProcessBuilder(Arrays.asList(command));
        p = pb.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

        KeypadStream stream = new KeypadStream(br, str -> lines.add(str));
        stream.start();

        return keypad;
    }

    public String readKeyPressed() {

        while(lines.size() < numberOfLinePerKeyStroke) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        String lineWithKeyStroke = "";

        for(int i = 0; i < numberOfLinePerKeyStroke; i++) {
            String line = lines.poll();

            if(i == lineNumberWithKeyStroke) {
                lineWithKeyStroke = line;
            }
            System.out.println(line);
        }

        System.out.println(lineWithKeyStroke);

        return Converter.parse(lineWithKeyStroke);
    }

    public String readPassword() {
        StringBuilder password = new StringBuilder();
        for(int i = 0; i < numberOfKeyStrokesInPassword; i++) {
            password.append(readKeyPressed());
            System.out.print("*");
        }
        System.out.println();

        return password.toString();
    }

    public String readChars(int n, boolean... printChar) {
        StringBuilder password = new StringBuilder();
        boolean flag = (printChar.length >= 1) && printChar[0];

        for(int i = 0; i < n; i++) {
            String key = readKeyPressed();
            password.append(key);
            if (flag) {
                System.out.print(key);
            }
        }
        System.out.println();

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

    public void close() {
        p.destroy();
    }
}

