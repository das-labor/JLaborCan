package org.labor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        InputStream in = new ByteArrayInputStream(new byte[0]);
        MessageInputStream<CANTCPMessage> tcpin = new MessageReader<CANTCPMessage>(CANTCPMessage.factory, in);
        MessageInputStream<CANMessage> canin = new MessageInputAdapter<CANTCPMessage, CANMessage>(CANMessage.factory, tcpin);
        MessageInputStream<LAPMessage> lapin = new MessageInputAdapter<CANMessage, LAPMessage>(LAPMessage.factory, canin);
        LAPMessage msg = lapin.read();
    }
}
