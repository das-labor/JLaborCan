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
    private final int id;
    private final boolean remote;

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
            int id;
            byte length;
            byte[] rawData = new byte[DATA_MAX_LENGTH + HEADER_LEN];

            if (in.read(rawData, 0, HEADER_LEN) != HEADER_LEN) {
                throw new IOException("enexpected end of stream");
            }

            length = rawData[BYTE_POS_LEN];

            if (length > CANMessage.DATA_MAX_LENGTH || length < 0) {
                throw new IOException("invalid packet length");
            }

            if (in.read(rawData, HEADER_LEN, length) != length) {
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
