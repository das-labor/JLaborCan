package de.hansinator;

import java.io.IOException;
import java.net.Socket;

import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageInputAdapter;
import de.hansinator.message.io.MessageInputFilter;
import de.hansinator.message.io.MessageOutput;
import de.hansinator.message.io.MessageStreamReader;
import de.hansinator.message.io.MessageStreamWriter;
import de.hansinator.message.protocol.CANTCPMessage;
import de.hansinator.message.protocol.LAPMessage;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        //chained message read/write example
        Socket sock = new Socket("kvm", 2342);
        MessageInput<CANTCPMessage> tcpin = new MessageStreamReader<CANTCPMessage>(CANTCPMessage.factory, sock.getInputStream());
        final MessageOutput<CANTCPMessage> tcpout = new MessageStreamWriter<CANTCPMessage>(sock.getOutputStream());
		final MessageInput<CANTCPMessage> lapfilter = new MessageInputFilter<CANTCPMessage>(tcpin) {

			@Override
			public boolean accept(CANTCPMessage message) {
				
				return message.getCommand() == 0x11;
			}

		};
        MessageInput<LAPMessage> lapin = new MessageInputAdapter<CANTCPMessage, LAPMessage>(LAPMessage.factory, lapfilter);
        MessageOutput<LAPMessage> lapout = new MessageOutput<LAPMessage>() {

            public void write(LAPMessage message) throws IOException {
                tcpout.write(new CANTCPMessage((byte) 0x11, message.encode()));
            }
        };
        LAPMessage msg, msg2;
        
        //klingel
        msg2 = new LAPMessage((byte)0x04, (byte)0x00, (byte)0x00, (byte)0x01, new byte[]{5,0});
        //lapout.write(msg2);
        //System.exit(1);

        while ((msg = lapin.read()) != null) {
            System.out.println("SRC (ADDR, PORT):\t" + String.format("%02X, %02X", msg.getSrcAddr(), msg.getSrcPort()));
            System.out.println("DST (ADDR, PORT):\t" + String.format("%02X, %02X", msg.getDstAddr(), msg.getDstPort()));
            System.out.println("DATA LENGTH:\t" + String.format("%02X", msg.getLength()));
            System.out.println("");
        }

        sock.close();
    }
}
