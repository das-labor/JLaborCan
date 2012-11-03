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
 * thread. The message input is just passed through. The supplied underlying endpoint is started by
 * the worker thread and may be left unconnected initially. If auto connect is true, the worker and
 * underlying endpoint are automatically started if no active connection exists and a read or write
 * is issued. The message delivery queue has a maximum size. If the queue is full, message write
 * will stall.
 * 
 * @author hansinator
 * @param <T>
 *            The message type
 */
public class AsyncWriteMessageProxy<T extends MessageObject> implements MessageEndpoint<T> {

	private volatile boolean running = false, starting = false, autoRestart;

	private final MessageEndpoint<T> endpoint;

	private final BlockingQueue<T> messageQueue;

	private MessageInput<T> realInput;

	private final Object inputLock = new Object();

	private Thread worker = null;

	private int lastTimeout = 0;

	/**
	 * Construct an unconnected asynchronous endpoint with a default maximum send queue size of 64.
	 * 
	 * @param endpoint
	 *            The underlying endpoint
	 * @param autoConnect
	 *            If true, this endpoint will automatically connect when a message input is read or
	 *            output is written. This will also restore a broken connection, except when there
	 *            is an error during connection setup.
	 */
	public AsyncWriteMessageProxy(MessageEndpoint<T> endpoint, boolean autoConnect) {
		this(endpoint, autoConnect, 64);
	}

	/**
	 * Construct an unconnected asynchronous endpoint with a maximum send queue size.
	 * 
	 * @param endpoint
	 *            The underlying endpoint
	 * @param queueSize
	 *            The maximum send queue size
	 * @param autoConnect
	 *            If true, this endpoint will automatically connect when a message input is read or
	 *            output is written. This will also restore a broken connection, except when there
	 *            is an error during connection setup.
	 */
	public AsyncWriteMessageProxy(MessageEndpoint<T> endpoint, boolean autoConnect, int queueSize) {
		if (endpoint == null)
			throw new NullPointerException("endpoint is null");

		this.endpoint = endpoint;
		this.messageQueue = new LinkedBlockingQueue<T>(queueSize);
		this.autoRestart = autoConnect;
	}

	/**
	 * Query the auto connection feature status
	 * 
	 * @see #connect(int)
	 * @return true is auto connect is enabled
	 */
	public boolean isAutoConnect() {
		return autoRestart;
	}

	/**
	 * @see #connect(int)
	 * @see #isAutoConnect()
	 * @param autoConnect enable or disable auto connection status
	 */
	public void setAutoConnect(boolean autoConnect) {
		this.autoRestart = autoConnect;
	}

	/**
	 * Indicates if the worker thread starting and is in it's connection phase.
	 * 
	 * @see #connect(int)
	 * @return true if connection is being set-up
	 */
	public boolean isConnecting() {
		return starting;
	}

	/**
	 * Start the worker thread and connect the underlying endpoint with the given timeout.
	 * 
	 * The method returns immediately and does not wait for the underlying endpoint connection setup
	 * to complete. isConnecting() indicates whether connection setup is active. When the connection
	 * is setup successfully, isConnected() will return true. If an IOException is encountered
	 * during the connect phase of the worker, startup is aborted , auto connect will be disabled
	 * and isAutoConnect() returns false. isConnecting() and isConnected() may be used to detect
	 * connection setup and distinguish success or failure. When isConnecting becomes false after
	 * issuing a connect and isConnected() returns true, the connection is okay. If isConnected()
	 * evaluates to false, the connection has failed.
	 * 
	 * @throws IOException
	 *             Passed through from the underyling endpoint
	 */
	@Override
	public void connect(int timeout) throws IOException {
		start(timeout);
	}

	/**
	 * Start the worker thread and connect the underlying endpoint.
	 * 
	 * @see #connect(int)
	 * @throws IOException
	 *             Passed through from the underyling endpoint
	 */
	@Override
	public void connect() throws IOException {
		start(0);
	}

	/**
	 * Stop the worker thread and close the underlying endpoint. However, when auto connect is
	 * enabled, this endpoint may reconnect on its own again.
	 * 
	 * @see #connect(int)
	 * @throws never
	 *             thrown by this endpoint
	 */
	@Override
	public void close() throws IOException {
		stop();
	}

	/**
	 * See if this endpoint is connected and ready for I/O operations.
	 * 
	 * @see #connect(int)
	 * @return conenction state
	 */
	@Override
	public boolean isConnected() {
		return running && endpoint.isConnected();
	}

	/**
	 * Get a message input and auto start the connection is auto connect is enabled.
	 * 
	 * @see #connect(int)
	 * @return the synchronous message input
	 * @throws IOException
	 *             thrown when the input is not connected and auto connect is disabled
	 */
	@Override
	public synchronized MessageInput<T> getMessageInput() throws IOException {
		if (up())
			synchronized (inputLock) {
				if (isConnected() && (realInput != null))
					return input;
			}
		throw new IOException("Not connected");

	}

	/**
	 * @return returns the queuing asynchronous message output
	 */
	@Override
	public MessageOutput<T> getMessageOutput() {
		return output;
	}

	private synchronized boolean start(final int timeout) {
		if (!running && !starting && (worker == null || !worker.isAlive())) {
			worker = new Thread(new Runnable() {
				@Override
				public void run() {
					// setup connection
					MessageOutput<T> out = null;
					if (!endpoint.isConnected())
						try {
							endpoint.connect(timeout);
							// setup synchronous input
							synchronized (inputLock) {
								realInput = endpoint.getMessageInput();
							}
							// fetch output
							out = endpoint.getMessageOutput();

							// set to running
							running = true;
						} catch (IOException e) {
							autoRestart = false;
							starting = false;
							return;
						} finally {
							starting = false;
						}

					// enter main loop
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
			starting = true;
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

	synchronized private boolean up() {
		// if not running, try auto restarting the worker
		if (!running && autoRestart && !starting) {
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
		} else if (starting)
			while (starting)
				Thread.yield();

		return running;
	}

	private boolean enqueueMessageWrite(T msg) throws InterruptedException {
		return up() && running && messageQueue.offer(msg);
	}

	private final MessageInput<T> input = new MessageInput<T>() {
		@Override
		public T read() throws IOException {
			if (up()) {
				MessageInput<T> in = null;
				synchronized (inputLock) {
					if (isConnected())
						in = realInput;
				}
				if (in != null)
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
