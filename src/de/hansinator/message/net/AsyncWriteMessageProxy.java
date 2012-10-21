package de.hansinator.message.net;

import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import de.hansinator.message.MessageObject;
import de.hansinator.message.io.MessageInput;
import de.hansinator.message.io.MessageOutput;

/**
 * A proxying message endpoint to asynchronously write and synchronously read messages.
 * 
 * Arriving messages are queued for delivery and delivered to the underlying endpoint by a worker
 * thread. The message input is just passed through. If autoRestart is true, the worker and
 * underlying endpoint are automatically started if no active connection exists.
 * 
 * @author hansinator
 * @param <T>
 *            The message type
 */
public class AsyncWriteMessageProxy<T extends MessageObject> implements MessageEndpoint<T> {

	private volatile boolean running = false;

	private final MessageEndpoint<T> endpoint;

	private final BlockingQueue<T> messageQueue;

	private MessageInput<T> realInput;

	private final Object inputLock = new Object();

	private Thread worker = null;

	private boolean autoRestart;

	private int lastTimeout = 0;

	public AsyncWriteMessageProxy(MessageEndpoint<T> endpoint, boolean autoRestart) {
		this(endpoint, 64, autoRestart);
	}

	public AsyncWriteMessageProxy(MessageEndpoint<T> endpoint, int queueSize, boolean autoRestart) {
		if (endpoint == null)
			throw new NullPointerException("endpoint is null");

		this.endpoint = endpoint;
		this.messageQueue = new LinkedBlockingQueue<T>(queueSize);
		this.autoRestart = autoRestart;
	}

	private synchronized boolean start(final int timeout) {
		if (!running && (worker == null || !worker.isAlive())) {
			worker = new Thread(new Runnable() {
				@Override
				public void run() {
					// setup connection
					if (!endpoint.isConnected())
						try {
							endpoint.connect(timeout);
						} catch (IOException e) {
							autoRestart = false;
							return;
						}

					// setup synchronous input
					synchronized (inputLock) {
						realInput = endpoint.getMessageInput();
					}

					// setup output and enter main loop
					running = true;
					final MessageOutput<T> out = endpoint.getMessageOutput();
					try {
						while (running || !messageQueue.isEmpty()) {
							// write out messages
							out.write(messageQueue.take());
							if (Thread.interrupted())
								running = false;
						}
					} catch (IOException e) {
						if (e instanceof SocketException) {
							// if this happens, we may try a reconnect and re-enqueue the message,
							// so that it isn't lost
							// Log.d(LNET, "Socket error broke worker main loop", e);
						}
						// Log.d(LNET, "IO error broke worker main loop", e);
					} catch (InterruptedException e) {
					} finally {
						running = false;

						// release input
						synchronized (inputLock) {
							realInput = null;
						}

						// disconnect
						if (endpoint.isConnected()) {
							try {
								endpoint.close();
							} catch (IOException e) {
								// connection shutdown failed
							}
						}
					}
				}
			}, "AsyncMessageWriteWorker");
			worker.start();
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

	private boolean enqueueMessageWrite(T msg) throws InterruptedException {
		return up() && running && messageQueue.offer(msg);
	}

	@Override
	public synchronized MessageInput<T> getMessageInput() {
		if (up())
			synchronized (inputLock) {
				if (isConnected() && (realInput != null))
					return input;
			}
		throw new IllegalStateException("Not connected");

	}

	@Override
	public MessageOutput<T> getMessageOutput() {
		return output;
	}

	public boolean isAutoRestart() {
		return autoRestart;
	}

	public void setAutoRestart(boolean autoRestart) {
		this.autoRestart = autoRestart;
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

	private final MessageInput<T> input = new MessageInput<T>() {
		@Override
		public T read() throws IOException {
			if (up())
			{
				MessageInput<T> in = null;
				synchronized (inputLock) {
					if (isConnected())
						in = realInput;
				}
				if(in != null)
					return in.read();
			}
			throw new IOException("Input not connected");
		}
	};

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
}
