package de.hansinator.message.protocol;

import java.io.IOException;
import java.io.InputStream;
import de.hansinator.message.io.MessageFactory;
import de.hansinator.message.MessageObject;

/**
 *
 * @author hansinator
 */
public class CANUARTMessage extends MessageObject {

    public static final int DATA_MAX_LENGTH = CANMessage.DATA_MAX_LENGTH + 5;
    private final byte cmd;

    public CANUARTMessage(byte cmd, byte data[]) {
        super(data);
        if (data.length > DATA_MAX_LENGTH || data.length < 0) {
            throw new IllegalArgumentException("Packet either too long or too short.");
        }
        this.cmd = cmd;
    }

    public byte getCommand() {
        return cmd;
    }

    @Override
    public byte[] encode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public final static MessageFactory<CANUARTMessage> factory = new MessageFactory<CANUARTMessage>() {

        public CANUARTMessage assemble(InputStream in) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    };
}
