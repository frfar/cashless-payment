package felica;

import java.math.BigInteger;

public class Utils {
    public static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1,data));
    }
}
