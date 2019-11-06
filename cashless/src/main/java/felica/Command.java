package felica;

import java.nio.ByteBuffer;

public class Command {

    // represents all the bytes of the command.
    private byte[] command;

    // represents the felica command
    private byte[] felicaCommand;

    private static final byte[] IntialBytes = {(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    private static final byte[] readWriteBytes = {(byte) 0xD4, (byte) 0x40, (byte) 0x01};

    public Command(byte[] command, boolean isFelicaCommand) {
        if(isFelicaCommand) {
            this.felicaCommand = command;
        } else {
            this.command = command;
        }
    }

    public byte[] getBytes() {
        if(command != null) {
            return command;
        }
        if(felicaCommand == null) {
            return new byte[]{};
        }

        ByteBuffer bytes = ByteBuffer.allocate(IntialBytes.length + 1 + readWriteBytes.length + felicaCommand.length);
        bytes.put(IntialBytes).put((byte) (felicaCommand.length + readWriteBytes.length)).put(readWriteBytes).put(felicaCommand);

        return bytes.array();
    }
}
