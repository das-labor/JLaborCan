package de.hansinator.message.lmp;

import java.io.IOException;
import java.io.InputStream;

import de.hansinator.message.io.MessageFactory;

/**
 * A LMP message variant for UART connections.
 * 
 * @author hansinator
 */
public class LMPUARTMessage extends LMPMessage {

	private final static int CRC_LEN = 2;

	public LMPUARTMessage(byte cmd, byte data[]) {
		super(cmd, data);
	}

	@Override
	public byte[] encode() {
		byte[] rawData = new byte[data.length + HEADER_LEN + CRC_LEN];
		int crc = crc16(data);
		rawData[0] = cmd;
		rawData[1] = (byte) (data.length & 0xFF);
		System.arraycopy(data, 0, rawData, HEADER_LEN, data.length);
		rawData[data.length + HEADER_LEN + 0] = (byte)((crc >> 8) & 0xFF);
		rawData[data.length + HEADER_LEN + 1] = (byte)(crc & 0xFF);
		return rawData;
	}

	/**
	 * A factory that assembles LMP-UART messages
	 */
	public final static MessageFactory<LMPUARTMessage> factory = new MessageFactory<LMPUARTMessage>() {

		public LMPUARTMessage assemble(InputStream in) throws IOException {
			throw new UnsupportedOperationException("Not supported yet.");
			/* cand receive state machine 
	static char *uartpkt_data;
	static unsigned int	crc;
	unsigned char c;

	while (uart_getc_nb(&c))
	{
		debug(10, "canu_get_nb received: %02x\n", c);
		switch (canu_rcvstate)
		{
			case STATE_START:
				if (c)
				{
					canu_rcvstate = STATE_LEN;
					canu_rcvpkt.cmd = c;
				}
				else
					canu_failcnt = 0;
				break;
			case STATE_LEN:
				canu_rcvlen       = c;
				if(canu_rcvlen > RS232CAN_MAXLENGTH)
				{
					canu_rcvstate = STATE_START;
					break;
				}
				canu_rcvstate     = STATE_PAYLOAD;
				canu_rcvpkt.len   = c;
				uartpkt_data      = &canu_rcvpkt.data[0];
				break;
			case STATE_PAYLOAD:
				if(canu_rcvlen--)
					*(uartpkt_data++) = c;
				else
				{
					canu_rcvstate = STATE_CRC;
					crc = c;
				}
				break;
			case STATE_CRC:
				canu_rcvstate = STATE_START;
				crc = (crc << 8) | c;

				debug(10, "canu_get_nb crc: 0x%04x, 0x%04x\n", crc, crc16(&canu_rcvpkt.cmd, canu_rcvpkt.len + 2));
				if(crc == crc16(&canu_rcvpkt.cmd, canu_rcvpkt.len + 2))
				{
					canu_failcnt = 0;
					return &canu_rcvpkt;
				}
				canu_failcnt++;

				break;
		}
	}
			 */
		}

	};

	private int crc16Update(int crc, byte a) {
		crc ^= (int) a & 0xFF;
		for (int i = 0; i < 8; ++i) {
			if ((crc & 1) == 1)
				crc = (crc >> 1) ^ 0xA001;
			else
				crc = (crc >> 1);
		}

		return crc & 0xFFFF;
	}

	private int crc16(byte[] buf) {
		int crc = 0;

		for (int i = 0; i < buf.length; i++)
			crc = crc16Update(crc, buf[i]);

		return crc;
	}
}
