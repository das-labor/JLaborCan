package de.hansinator.incubator;

import java.io.IOException;
import java.net.Socket;

import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageInputAdapter;
import de.hansinator.message.io.MessageInputFilter;
import de.hansinator.message.io.MessageStreamReader;
import de.hansinator.message.protocol.CANTCPMessage;
import de.hansinator.message.protocol.LAPMessage;

public class Dump {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws IOException {
		// chained message read/write example
		Socket sock = new Socket("kvm", 2342);
		MessageInput<CANTCPMessage> tcpin = new MessageStreamReader<CANTCPMessage>(CANTCPMessage.factory, sock.getInputStream());
		final MessageInput<CANTCPMessage> lapfilter = new MessageInputFilter<CANTCPMessage>(tcpin) {

			@Override
			public boolean accept(CANTCPMessage message) {

				return message.getCommand() == 0x11;
			}

		};
		MessageInput<LAPMessage> lapin = new MessageInputAdapter<CANTCPMessage, LAPMessage>(LAPMessage.factory, lapfilter);
		LAPMessage msg;

		while ((msg = lapin.read()) != null) {
			StringBuilder sb = new StringBuilder();
			String from, to;
			from = LAPAddressBook.names.get(msg.getSrcAddr());
			to = LAPAddressBook.names.get(msg.getDstAddr());
			if (from == null)
				from = "unknwown";
			if (to == null)
				from = "unknwown";

			sb.append("'" + from + " -> '" + to + "'\n");
			sb.append(String.format("%02X:%02X -> ", msg.getSrcAddr(), msg.getSrcPort())
					+ String.format("%02X:%02X", msg.getDstAddr(), msg.getDstPort()) + ", DLC: "
					+ String.format("%02X", msg.getLength()));
			sb.append("\nData: ");
			for (int i = 0; i < msg.getLength(); i++)
			{
				sb.append(String.format("%02X", msg.getLength()));
				if(i != msg.getLength() -1)
					sb.append(", ");
			}
			sb.append("\n");
			
			System.out.println(sb);
		}

		sock.close();
	}
}
