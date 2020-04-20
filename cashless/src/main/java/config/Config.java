package config;

import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class Config {
    // JSON Variables
    private String keypadId = null;
    private String server = null;

    // Static variables
    public static String privateKeyFileName = "ec256-key-pair-pkcs8.pem";
    public static String publicKeyFileName = "ec256-public.pem";
    public static final String configFileName = "config.json";
//    public static String privateKeyFileName = "src/main/resources/ec256-key-pair-pkcs8.pem";
//    public static String publicKeyFileName = "src/main/resources/ec256-public.pem";
//    public static final String configFileName = "src/main/resources/config.json";

    // Singleton pattern
    private static Config config = null;

    private Config(){}

    public static Config getInstance() throws FileNotFoundException {
        if (config == null) {
            config = readConfig();
        }
        return config;
    }

    private static Config readConfig() throws FileNotFoundException {
        return new Gson().fromJson(new FileReader(configFileName), Config.class);
    }

    public String getPrivateKeyFileName() {
        return privateKeyFileName;
    }

    public String getPublicKeyFileName() {
        return publicKeyFileName;
    }

    public String getKeypadId() {
        return keypadId;
    }

    public void setKeypadId(String keypadId) {
        this.keypadId = keypadId;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}