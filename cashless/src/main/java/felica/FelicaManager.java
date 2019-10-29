package felica;

import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.nio.ByteBuffer;

public class FelicaManager {

    private static FelicaManager felicaManager;
    private CardChannel channel;

    private FelicaManager(CardChannel channel) {
        this.channel = channel;
    }

    public static FelicaManager getInstance(CardChannel channel) {
        if(felicaManager == null) {
            felicaManager = new FelicaManager(channel);
        }

        return felicaManager;
    }

    public byte[] executeCommand(byte commandCode, byte[] idm, byte[] payload) {
        FelicaCommand felicaCommand = new FelicaCommand(commandCode, idm, payload);

        try {
            ResponseAPDU response = channel.transmit(new CommandAPDU(new Command(felicaCommand.getBytes(), true).getBytes()));
            return response.getData();
        } catch (CardException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public byte[] writeWithoutAuthentication(byte[] idm, byte block, byte[] data) {
        byte[] payload = new byte[]{ONE_SERVICE, SERVICE_CODE[0], SERVICE_CODE[1], ONE_BLOCK, BLOCK_IDENTIFIER, block};

        ByteBuffer bytes = ByteBuffer.allocate(payload.length + data.length);
        bytes.put(payload).put(data);

        return executeCommand(WRITE_COMMAND_CODE, idm, bytes.array());
    }

    public byte[] readWithoutAuthentication(byte[] idm, byte block) {
        byte[] payload = new byte[]{ONE_SERVICE, SERVICE_CODE[0],SERVICE_CODE[1], ONE_BLOCK, BLOCK_IDENTIFIER, block};

        return executeCommand(READ_COMMAND_CODE, idm, payload);
    }

    public byte[] requestSystemCode(byte[] idm) {
        return executeCommand(REQUEST_SYSTEM_CODE, idm, new byte[0]);
    }

    public byte[] requestSystemMode(byte[] idm) {
        return executeCommand(REQUEST_SYSTEM_MODE_CODE, idm, new byte[0]);
    }

    public byte[] requestService(byte[] idm, byte[] nodeList) {
        byte[] payload = new byte[] {ONE_NODE, nodeList[0], nodeList[1]};

        return executeCommand(REQUEST_SERVICE_CODE, idm, payload);
    }

    public byte[] requestService(byte[] idm, byte block) {
        byte[] payload = new byte[] {ONE_NODE,  block, (byte) (0x80)};
        return executeCommand(REQUEST_SERVICE_CODE, idm, payload);

    }
    public byte[] polling() {
        byte[] payload = new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0x01, (byte) 0x0F};

        return executeCommand(POLLING_COMMAND_CODE, new byte[0], payload);
    }
    private static final byte ONE_SERVICE = 0x01;
    private static final byte ONE_BLOCK = 0x01;
    private static final byte ONE_NODE = 0x01;
    private static final byte BLOCK_IDENTIFIER = (byte) 0x80;
    private static final byte[] SERVICE_CODE = new byte[] {(byte) 0x00, (byte) 0x09};
    private static final byte WRITE_COMMAND_CODE = 0x08;
    private static final byte READ_COMMAND_CODE = 0x06;
    private static final byte REQUEST_SYSTEM_CODE = 0x0C;
    private static final byte REQUEST_SYSTEM_MODE_CODE = 0x04;
    private static final byte REQUEST_SERVICE_CODE = 0x02;
    private static final byte POLLING_COMMAND_CODE = 0x00;

}
