package de.hansinator.message.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import de.hansinator.message.MessageObject;
import de.hansinator.message.io.MessageFactory;
import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageOutput;
import de.hansinator.message.io.MessageStreamReader;
import de.hansinator.message.io.MessageStreamWriter;

public class TCPMessageEndpoint<T extends MessageObject> implements MessageEndpoint<T> {
	private final MessageFactory<T> factory;
	
	private Socket socket = null;

	private SocketAddress address;
	
	private InputStream in;
	
	private OutputStream out;

	public TCPMessageEndpoint(String host, int port, MessageFactory<T> factory) {
		if(factory == null)
			throw new NullPointerException("No factory");
		this.factory = factory;
		this.address = new InetSocketAddress(host, port);
	}

	@Override
	public void connect(int timeout) throws IOException {
		if (address == null)
			throw new NullPointerException("No address");
		if (isConnected())
			throw new IllegalStateException("Already connected");

		socket = new Socket();
		socket.connect(address, timeout);
		
		try{
			out = socket.getOutputStream();
			in = socket.getInputStream();
		} catch (IOException e) {
			socket.close();
			throw e;
		}
	}

	@Override
	public void connect() throws IOException {
		connect(0);
	}

	public void connect(SocketAddress address) throws IOException {
		this.address = address;
		connect(0);
	}

	public void connect(SocketAddress address, int timeout) throws IOException {
		this.address = address;
		connect(timeout);
	}

	@Override
	public void close() throws IOException {
		if(socket != null)
			socket.close();
	}

	@Override
	public boolean isConnected() {
		return socket != null && socket.isConnected() && !socket.isClosed();
	}

	public MessageInput<T> getMessageInput() {
		if(!isConnected())
			throw new IllegalStateException("No connection");
		
		return new MessageStreamReader<T>(factory, in);
	}

	public MessageOutput<T> getMessageOutput() {
		if(!isConnected())
			throw new IllegalStateException("No connection");
		
		return new MessageStreamWriter<T>(out);
	}
}
