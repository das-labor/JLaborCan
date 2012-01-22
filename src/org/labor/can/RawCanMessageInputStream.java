package org.labor.can;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 *
 * @author hansinator
 */
public class RawCanMessageInputStream extends InputStream {

    private final static int HEADER_LEN = 5;
    private final static int BYTE_POS_LEN = 4;
    private final static int BYTE_POS_ID = 0;

    InputStream sourceStream;

    public RawCanMessageInputStream(InputStream in) {
        sourceStream = in;
    }

    //TODO: something needs to be done about this method, as it'll de-sync the stream
    @Override
    public int read() throws IOException {
        return sourceStream.read();
    }

    RawCanMessage readRawCanMessage() throws IOException {
        int id;
        byte length;
        byte[] rawData = new byte[RawCanMessage.DATA_MAX_LENGTH + HEADER_LEN];

        if(sourceStream.read(rawData, 0, HEADER_LEN) != HEADER_LEN){
            throw new IOException("Unexpected end of stream.");
        }

        length = rawData[BYTE_POS_LEN];

        if(length > RawCanMessage.DATA_MAX_LENGTH || length < 0) {
            throw new IOException("Invalid packet length.");
        }

        if(read(rawData, HEADER_LEN, length) != length) {
            throw new IOException("Unexpected end of stream.");
        }

        id = (int)rawData[BYTE_POS_ID] |
                ((int)rawData[BYTE_POS_ID + 1] << 8) |
                ((int)rawData[BYTE_POS_ID + 2] << 16) |
                ((int)rawData[BYTE_POS_ID + 3] << 24);

        rawData = Arrays.copyOfRange(rawData, HEADER_LEN, rawData.length);
        
        return new RawCanMessage(id, length, rawData);
    }
}
