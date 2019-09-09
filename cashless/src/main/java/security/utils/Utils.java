package security.utils;

public class Utils {
    public static byte[] hexStringToByte(String hex) {
        byte[] ret = new byte[hex.length()/2];
        for (int i = 0; i < hex.length()/2; i++) {
            int index = i * 2;
            int j = Integer.parseInt(hex.substring(index, index + 2), 16);
            ret[i] = (byte) j;
        }

        return ret;
    }
}
