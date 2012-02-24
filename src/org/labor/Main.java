package org.labor;

import java.io.IOException;
import java.net.Socket;
import org.labor.can.CANTCPMessage;
import org.labor.can.LAPMessage;
import org.labor.message.MessageInputAdapter;
import org.labor.message.MessageInputStream;
import org.labor.message.MessageOutputStream;
import org.labor.message.MessageReader;
import org.labor.message.MessageWriter;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        //chained message read/write example
        Socket sock = new Socket("kvm", 2342);
        MessageInputStream<CANTCPMessage> tcpin = new MessageReader<CANTCPMessage>(CANTCPMessage.factory, sock.getInputStream());
        final MessageOutputStream<CANTCPMessage> tcpout = new MessageWriter<CANTCPMessage>(sock.getOutputStream());
        MessageInputStream<LAPMessage> lapin = new MessageInputAdapter<CANTCPMessage, LAPMessage>(LAPMessage.factory, tcpin);
        MessageOutputStream<LAPMessage> lapout = new MessageOutputStream<LAPMessage>() {

            public void write(LAPMessage message) throws IOException {
                tcpout.write(new CANTCPMessage((byte) 0x11, message.encode()));
            }
        };
        LAPMessage msg, msg2;
        
        //klingel
        msg2 = new LAPMessage((byte)0x04, (byte)0x00, (byte)0x00, (byte)0x01, new byte[]{0,0}, false);
        lapout.write(msg2);
        System.exit(1);

        while ((msg = lapin.read()) != null) {
            System.out.println("SRC (ADDR, PORT):\t" + String.format("%02X, %02X", msg.getSrcAddr(), msg.getSrcPort()));
            System.out.println("DST (ADDR, PORT):\t" + String.format("%02X, %02X", msg.getDstAddr(), msg.getDstPort()));
            System.out.println("DATA LENGTH:\t" + String.format("%02X", msg.getLength()));
            System.out.println("");
        }

        sock.close();
    }
}
