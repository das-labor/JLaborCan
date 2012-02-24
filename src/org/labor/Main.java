package org.labor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import org.labor.can.CANMessage;
import org.labor.can.CANTCPMessage;
import org.labor.can.LAPMessage;
import org.labor.message.MessageInputAdapter;
import org.labor.message.MessageInputStream;
import org.labor.message.MessageReader;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        //chained message read example
        Socket sock = new Socket("kvm", 2342);
        MessageInputStream<CANTCPMessage> tcpin = new MessageReader<CANTCPMessage>(CANTCPMessage.factory, sock.getInputStream());
        MessageInputStream<LAPMessage> lapin = new MessageInputAdapter<CANTCPMessage, LAPMessage>(LAPMessage.factory, tcpin);
        LAPMessage msg;
        
        while((msg = lapin.read()) != null)
        {
            System.out.println("SRC (ADDR, PORT):\t" + String.format("%02X, %02X", msg.getSrcAddr(), msg.getSrcPort()));
            System.out.println("DST (ADDR, PORT):\t" + String.format("%02X, %02X", msg.getDstAddr(), msg.getDstPort()));
            System.out.println("DATA LENGTH:\t" + String.format("%02X", msg.getLength()));
            System.out.println("");
        }
        
        sock.close();
    }
}
