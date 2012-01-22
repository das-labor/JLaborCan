package org.labor.can;

import java.io.IOException;
import java.io.InputStream;
import org.labor.message.MessageFactory;
import org.labor.message.MessageObject;

/**
 *
 * @author hansinator
 */
public class CANTCPMessage extends MessageObject {

    public static final int DATA_MAX_LENGTH = CANMessage.DATA_MAX_LENGTH + 5;
    private final byte cmd;

    public CANTCPMessage(byte cmd, byte data[]) {
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
    
    public final static MessageFactory<CANTCPMessage> factory = new MessageFactory<CANTCPMessage>() {

        public CANTCPMessage assemble(InputStream in) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    };
}
