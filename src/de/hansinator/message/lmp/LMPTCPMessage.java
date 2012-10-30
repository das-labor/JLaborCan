package de.hansinator.message.lmp;

import java.io.IOException;
import java.io.InputStream;

import de.hansinator.message.MessageObject;
import de.hansinator.message.io.MessageFactory;

/**
 * A LMP message variant for tcp tunnels.
 * 
 * @author hansinator
 */
public class LMPTCPMessage extends LMPMessage {

	/**
	 * Construct an LMP-TCP message.
	 * 
	 * @param cmd
	 *            command byte
	 * @param data
	 *            payload
	 */
	public LMPTCPMessage(byte cmd, byte data[]) {
		super(cmd, data);
	}

	@Override
	public byte[] encode() {
		byte[] rawData = new byte[data.length + HEADER_LEN];
		rawData[0] = (byte) (data.length & 0xFF);
		rawData[1] = cmd;
		System.arraycopy(data, 0, rawData, HEADER_LEN, data.length);
		return rawData;
	}

	/**
	 * A factory to assemble LMP-TCP messages
	 */
	public final static MessageFactory<LMPTCPMessage> factory = new MessageFactory<LMPTCPMessage>() {

		public LMPTCPMessage assemble(InputStream in) throws IOException {
			byte cmd;
			byte[] rawData = new byte[DATA_MAX_LENGTH + HEADER_LEN], payload;
			int len, ret, off = 0;

			while (off < HEADER_LEN) {
				ret = in.read(rawData, off, HEADER_LEN - off);
				if (ret < 0) {
					throw new IOException("unexpected end of stream");
				}
				off += ret;
			}

			len = ((int) rawData[0]) & 0xFF;

			if (len > DATA_MAX_LENGTH || len < 0) {
				throw new IOException("invalid packet length :" + len);
			}

			while (len != 0) {
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

			return new LMPTCPMessage(cmd, payload);
		}
	};
}
