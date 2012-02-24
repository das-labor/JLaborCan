package org.labor.can;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import org.labor.message.MessageFactory;
import org.labor.message.MessageObject;

/**
 * Represents a CAN 2.0B Extended Frame
 * 
 * @author hansinator
 */
public class CANMessage extends MessageObject {

    public static int DATA_MAX_LENGTH = 8;
    private final static int HEADER_LEN = 5;
    private final static int BYTE_POS_LEN = 4;
    private final static int BYTE_POS_ID = 0;
    protected final int id;
    protected final boolean remote;

    public CANMessage(int id, byte data[], boolean remote) {
        super(data);
        this.id = id;
        this.remote = remote;
    }

    public int getId() {
        return id;
    }

    public boolean isRemoteFrame() {
        return remote;
    }

    @Override
    public byte[] encode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public final static MessageFactory<CANMessage> factory = new MessageFactory<CANMessage>() {

        public CANMessage assemble(InputStream in) throws IOException {
            int id, ret, off = 0;
            byte length;
            byte[] rawData = new byte[DATA_MAX_LENGTH + HEADER_LEN];

            while (off < HEADER_LEN)
            {
                ret = in.read(rawData, off, HEADER_LEN - off);
                if (ret < 0) {
                    throw new IOException("unexpected end of stream");
                }
                off += ret;
            }

            length = rawData[BYTE_POS_LEN];

            if (length > DATA_MAX_LENGTH || length < 0) {
                throw new IOException("invalid packet length");
            }

            if (length != 0 && in.read(rawData, off, length) != length) {
                throw new IOException("unexpected end of stream");
            }

            id = (int) rawData[BYTE_POS_ID]
                    | ((int) rawData[BYTE_POS_ID + 1] << 8)
                    | ((int) rawData[BYTE_POS_ID + 2] << 16)
                    | ((int) rawData[BYTE_POS_ID + 3] << 24);

            if (length > 0) {
                rawData = Arrays.copyOfRange(rawData, HEADER_LEN, HEADER_LEN + length);
            } else {
                rawData = null;
            }

            return new CANMessage(id, rawData, false);
        }
    };
}
