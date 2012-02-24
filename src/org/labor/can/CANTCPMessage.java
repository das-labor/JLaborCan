package org.labor.can;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.labor.message.MessageFactory;
import org.labor.message.MessageObject;

/**
 *
 * @author hansinator
 */
public class CANTCPMessage extends MessageObject {

    public static final int DATA_MAX_LENGTH = 18;
    private final static int HEADER_LEN = 2;
    private final byte cmd;

    public CANTCPMessage(byte cmd, byte data[]) {
        super(data);
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
            byte len, cmd;
            byte[] rawData = new byte[DATA_MAX_LENGTH + HEADER_LEN];
            int ret, off = 0;

            while (off < HEADER_LEN)
            {
                ret = in.read(rawData, off, HEADER_LEN - off);
                if (ret < 0) {
                    throw new IOException("unexpected end of stream");
                }
                off += ret;
            }

            len = rawData[0];

            if (len > DATA_MAX_LENGTH || len < 0) {
                throw new IOException("invalid packet length");
            }

            while (len != 0)
            {
                ret = in.read(rawData, off, len);
                if (ret < 0) {
                    throw new IOException("unexpected end of stream");
                }
                len -= ret;
                off += ret;
            }

            len = rawData[0];
            cmd = rawData[1];
            if (len > 0 && cmd != 0) {
                rawData = Arrays.copyOfRange(rawData, HEADER_LEN, HEADER_LEN + len);
            } else {
                rawData = null;
            }

            return new CANTCPMessage(cmd, rawData);
        }
    };
}
