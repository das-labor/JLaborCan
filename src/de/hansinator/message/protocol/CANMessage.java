package de.hansinator.message.protocol;

import java.io.IOException;
import java.io.InputStream;

import de.hansinator.message.MessageObject;
import de.hansinator.message.io.MessageFactory;

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
	public final int id;
	public final boolean remote;

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
		byte[] rawData = new byte[data.length + HEADER_LEN];

		rawData[BYTE_POS_ID] = (byte) (id & 0xFF);
		rawData[BYTE_POS_ID + 1] = (byte) ((id >> 8) & 0xFF);
		rawData[BYTE_POS_ID + 2] = (byte) ((id >> 16) & 0xFF);
		rawData[BYTE_POS_ID + 3] = (byte) ((id >> 24) & 0xFF);
		rawData[BYTE_POS_LEN] = (byte) data.length;
		System.arraycopy(data, 0, rawData, HEADER_LEN, data.length);
		return rawData;
	}

	public final static MessageFactory<CANMessage> factory = new MessageFactory<CANMessage>() {

		public CANMessage assemble(InputStream in) throws IOException {
			int id, ret, off = 0, len;
			byte[] rawData = new byte[DATA_MAX_LENGTH + HEADER_LEN], payload;

			while (off < HEADER_LEN) {
				ret = in.read(rawData, off, HEADER_LEN - off);
				if (ret < 0) {
					throw new IOException("unexpected end of stream");
				}
				off += ret;
			}

			len = ((int) rawData[BYTE_POS_LEN]) & 0xFF;

			if (len > DATA_MAX_LENGTH || len < 0) {
				throw new IOException("invalid packet length");
			}

			if (len != 0 && in.read(rawData, off, len) != len) {
				throw new IOException("unexpected end of stream");
			}

			id = ((int) rawData[BYTE_POS_ID] & 0xFF) | (((int) rawData[BYTE_POS_ID + 1] & 0xFF) << 8)
					| (((int) rawData[BYTE_POS_ID + 2] & 0xFF) << 16) | (((int) rawData[BYTE_POS_ID + 3] & 0xFF) << 24);

			if (len > 0) {
				payload = new byte[len];
				System.arraycopy(rawData, HEADER_LEN, payload, 0, len);
			} else {
				payload = null;
			}

			return new CANMessage(id, payload, false);
		}
	};
}
