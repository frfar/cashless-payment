package felica;

import java.nio.ByteBuffer;

public class FelicaCommand {

    private byte commandCode;
    private byte[] idm;
    private byte[] payload;

    public FelicaCommand(byte commandCode, byte[] idm, byte[] payload) {
        this.commandCode = commandCode;
        this.idm = idm;
        this.payload = payload;
    }

    public byte[] getBytes() {
        int size = idm.length + payload.length + 2;
        ByteBuffer bytes = ByteBuffer.allocate(size);
        bytes.put((byte)size).put(commandCode).put(idm).put(payload);

        return bytes.array();
    }
}
