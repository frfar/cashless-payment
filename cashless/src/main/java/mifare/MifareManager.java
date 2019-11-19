package mifare;

import org.nfctools.mf.MfCardListener;
import org.nfctools.mf.MfReaderWriter;
import org.nfctools.mf.card.MfCard;

import javax.smartcardio.CardException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class MifareManager {

    private MfCardListener listener;

    private static final String key = "FFFFFFFFFFFF";
    private static final ArrayList<String> keys = new ArrayList<String>() {
        {
            add(key);
            add("FF00A1A0B000");
            add("FF00A1A0B001");
            add("FF00A1A0B099");
            addAll(MifareUtils.COMMON_MIFARE_CLASSIC_1K_KEYS);
        }
    };

    private static void printCardInfo (MfCard card){
        System.out.println("Card detected: "
                + card.getTagType().toString() + " "
                + card.toString());
    }

    public static void writeTransaction(byte[] trasactionBytes, MfReaderWriter reader, MfCard card) throws CardException {

        int noOfBlocks = (int) Math.ceil((double)trasactionBytes.length / 16.0);

        ByteBuffer buffer = ByteBuffer.allocate(noOfBlocks * 16);
        byte[] bytesToPad = new byte[buffer.capacity() - trasactionBytes.length];
        Arrays.fill(bytesToPad, (byte) 0x00);

        byte[] bytesToWrite = buffer.put(trasactionBytes).put(bytesToPad).array();

        for(int i = 0; i < noOfBlocks; i++) {
            MifareFileSystem fileSystem = MifareFileSystem.fromFileIndex(i);

            byte[] blockBytes = Arrays.copyOfRange(bytesToWrite,i * 16, i * 16 + 16);

            MifareUtils.writeToMifareClassic1KCard(reader, card, fileSystem.getSection(), fileSystem.getBlock(),key, HexUtils.bytesToHexString(blockBytes));
        }
    }

    public static byte[] readTransaction(MfReaderWriter reader, MfCard card) throws CardException {

        int noOfBlocks = 10;
        ByteBuffer buffer = ByteBuffer.allocate(16 * noOfBlocks);

        for(int i = 0; i < noOfBlocks; i++) {
            MifareFileSystem fileSystem = MifareFileSystem.fromFileIndex(i);
            String blockData = MifareUtils.readMifareClassic1KBlock(reader, card, fileSystem.getSection(), fileSystem.getBlock(),keys);
            byte[] blockBytes = HexUtils.hexStringToBytes(blockData);

            buffer.put(blockBytes);
        }

        return buffer.array();
    }

    private static String[] splitData(byte[] input){ //splits byte array input into Strings of block size (16 bytes)

        if(input.length % 16 != 0) {
            return new String[0];
        }

        int size = input.length/16;
        String[] output = new String[size];

        byte[] temp = new byte[16];
        for(int i=0; i < size; i++){
            for(int j=0;j<16;j+=1){
                temp[j] = input[j+i*16];
            }
            output[i] = HexUtils.bytesToHexString(temp);
        }

        return output;
    }

    //start with writing max of three blocks, all in same sector (start at block 0)
    public static void writeData(byte[] input, MfReaderWriter reader, MfCard card, int sectorId, String key) throws CardException {
        String[] toWrite = splitData(input);
        int blockCounter = 0;
        for(String s : toWrite){
            MifareUtils.writeToMifareClassic1KCard(reader, card, sectorId, blockCounter, key, s);
            System.out.println("Wrote " + s + " to block " + blockCounter);
            blockCounter++;
        }
    }
}
