package felica;

import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        CardReader cardReader = new CardReader();

        cardReader.addCardReaderCallback(new CardReaderCallback() {
            @Override
            public void isCardPresent(FelicaManager felicaManager) {
                byte[] response = felicaManager.polling();
                byte[] idm = Arrays.copyOfRange(response,5,13);
                System.out.println("idm is: " + Utils.bin2hex(idm));
            }
        });

               // byte[] writeResult = felicaManager.writeWithoutAuthentication(idm, (byte) 0x20, new byte[]{(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0xAA});
               // System.out.println("response is: " + com.utd.dslab.cashless.felica.Utils.bin2hex(writeResult));
//
//                byte[] readResult = com.utd.dslab.cashless.felica.FelicaManager.readWithoutAuthentication(idm, (byte) 0x81);
//                System.out.println("response is: " + com.utd.dslab.cashless.felica.Utils.bin2hex(readResult));

//                for(int i = 1; i < 0xFFFF; i++) {
//                    byte[] requestSystemCodeResult = felicaManager.requestService(idm, new byte[] {(byte)(i & 0xFF), (byte) ((i >> 8) & 0xFF)});
//                    System.out.println("response is: " + com.utd.dslab.cashless.felica.Utils.bin2hex(requestSystemCodeResult));
//
//                    byte[] serviceCode = Arrays.copyOfRange(requestSystemCodeResult, 14, requestSystemCodeResult.length);
//                    System.out.println("service code is: " + com.utd.dslab.cashless.felica.Utils.bin2hex(serviceCode));
//
//                    if(serviceCode[0] != (byte)0xFF || serviceCode[1] != (byte)0xFF) {
//                        System.out.println(i);
//                        break;
//                    }
//
//                }
               // System.out.println("Wait for 5 seconds before initializing next reading!!");
    }
}
