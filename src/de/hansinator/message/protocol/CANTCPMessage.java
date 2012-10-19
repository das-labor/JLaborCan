package de.hansinator.message.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import de.hansinator.message.io.MessageFactory;
import de.hansinator.message.MessageObject;

/**
 *
 * @author hansinator
 */
public class CANTCPMessage extends MessageObject {

    public static final int DATA_MAX_LENGTH = 20;
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
        byte[] rawData = new byte[data.length + HEADER_LEN];
        rawData[0] = (byte) (data.length & 0xFF);
        rawData[1] = cmd;
        System.arraycopy(data, 0, rawData, HEADER_LEN, data.length);
        return rawData;
    }
    public final static MessageFactory<CANTCPMessage> factory = new MessageFactory<CANTCPMessage>() {

        public CANTCPMessage assemble(InputStream in) throws IOException {
            byte cmd;
            byte[] rawData = new byte[DATA_MAX_LENGTH + HEADER_LEN], payload;
            int len, ret, off = 0;

            while (off < HEADER_LEN)
            {
                ret = in.read(rawData, off, HEADER_LEN - off);
                if (ret < 0) {
                    throw new IOException("unexpected end of stream");
                }
                off += ret;
            }

            len = ((int)rawData[0]) & 0xFF;

            if (len > DATA_MAX_LENGTH || len < 0) {
                throw new IOException("invalid packet length :" + len);
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
            	payload = new byte[len];
            	System.arraycopy(rawData, HEADER_LEN, payload, 0, len);
            } else {
            	payload = null;
            }

            return new CANTCPMessage(cmd, payload);
        }
    };
}
