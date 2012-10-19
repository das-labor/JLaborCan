package de.hansinator.message.net;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.hansinator.message.MessageObject;
import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageOutput;

public class AsyncMessageEndpoint<T extends MessageObject> implements MessageEndpoint<T> {

	private volatile boolean running = false;

	private final MessageEndpoint<T> endpoint;

	private final BlockingQueue<T> messageWriteQueue;
	
	private final BlockingQueue<T> messageReadQueue;

	private Thread worker = null;

	private boolean autoRestart;

	private int lastTimeout = 0;

	public AsyncMessageEndpoint(MessageEndpoint<T> endpoint, boolean autoRestart) {
		if (endpoint == null)
			throw new NullPointerException("endpoint is null");

		this.endpoint = endpoint;
		//XXX make queue sizes configurable
		this.messageWriteQueue = new LinkedBlockingQueue<T>(64);
		this.messageReadQueue = new LinkedBlockingQueue<T>(64);
		this.autoRestart = autoRestart;
	}

	private synchronized boolean start(final int timeout) {
		// Log.v(LTHRD, "Starting worker thread ");

		if (!running && (worker == null || !worker.isAlive())) {
			running = true;
			worker = new Thread(new Runnable() {
				@Override
				public void run() {
					// Log.v(LTHRD, "Worker running");
					
					if (!endpoint.isConnected())
						try {
							endpoint.connect(timeout);
						} catch (IOException e) {
							// Log.d(LNET, "Connect failed, disabling worker", e);
							running = false;
							autoRestart = false;
							return;
						}

					// Log.v(LTHRD, "Entering main loop");
					try {
						// setup message pipe and enter main loop
						final MessageOutput<T> out = endpoint.getMessageOutput();
						final MessageInput<T> in = endpoint.getMessageInput();
						T readMsg = null;
						while (running || !messageWriteQueue.isEmpty()) {
							out.write(messageWriteQueue.take());
							Thread.interrupted();
							if(readMsg == null)
							{
								readMsg = in.read();
								Thread.interrupted();
							}
							if((readMsg != null) && messageReadQueue.offer(readMsg))
								readMsg = null;
						}
					} catch (IOException e) {
						if (e instanceof SocketException) {
							// if this happens, we may try a reconnect and re-enqueue the message,
							// so
							// that it isn't lost
							// Log.d(LNET, "Socket error broke worker main loop", e);
						}
						// Log.d(LNET, "IO error broke worker main loop", e);
					} catch (InterruptedException e) {
					} finally {
						running = false;

						if (endpoint.isConnected()) {
							// Log.v(LNET, "Shutting down connection");
							try {
								endpoint.close();
							} catch (IOException e) {
								// Log.d(LNET, "Connection shutdown failed", e);
							}
							// Log.v(LTHRD, "Mainloop exited");
						}
					}
					// Log.v(LTHRD, "Gracefully exiting worker");
				}
			}, "LAPMessageSenderThread");
			worker.start();

			// Log.v(LTHRD, "Worker started");
		} else
			return false;

		return true;
	}

	private synchronized void stop() {
		running = false;
		if (worker != null && worker.isAlive())
			worker.interrupt();
	}

	public boolean isRunning() {
		return running;
	}

	synchronized private boolean up() {
		// if not running, try auto restarting the worker
		if (!running && autoRestart) {
			// Log.v(LTHRD, "Attempting worker restart");

			// wake up neo
			if (worker != null && worker.isAlive())
				worker.interrupt();

			// wait for thread to shut down
			while (worker.isAlive())
				Thread.yield();

			// restart & wait
			start(lastTimeout);
			while (!running && autoRestart)
				Thread.yield();
		}

		return autoRestart;
	}
	
	synchronized private T readMessageQueue() throws InterruptedException {
		if (!up())
			return null;

		if (running)
			return messageReadQueue.take();

		return null;
	}

	synchronized private boolean enqueueMessageWrite(T msg) throws InterruptedException {
		if (!up())
			return false;

		if (running) {
			messageWriteQueue.put(msg);
			return true;
		}

		return false;
	}

	@Override
	public MessageInput<T> getMessageInput() {
		return input;
	}

	@Override
	public MessageOutput<T> getMessageOutput() {
		return output;
	}

	private final MessageOutput<T> output = new MessageOutput<T>() {
		@Override
		public void write(T message) throws IOException {
			try {
				if (!enqueueMessageWrite(message))
					throw new IOException("Not connected and auto connect failed or is disabled");
			} catch (InterruptedException e) {
				throw new IOException("Message delivery has been interrupted", e);
			}
		}
	};
	
	private final MessageInput<T> input = new MessageInput<T>() {
		@Override
		public T read() throws IOException {
			try {
				T msg = readMessageQueue();
				if(msg == null)
					throw new IOException("Not connected and auto connect failed or is disabled");
				return msg;
			} catch (InterruptedException e) {
				throw new IOException("Message reception has been interrupted", e);
			}
		}
	};

	public boolean isAutoRestart() {
		return autoRestart;
	}

	@Override
	public void connect(int timeout) throws IOException {
		start(timeout);
	}

	@Override
	public void connect() throws IOException {
		start(0);
	}

	@Override
	public void close() throws IOException {
		stop();
	}

	@Override
	public boolean isConnected() {
		return isRunning() && endpoint.isConnected();
	}
}
