package soundcloud.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import soundcloud.server.event.NewMessageEvent;
import soundcloud.server.event.ServerType;


public class NioServer implements ServerSocket {

	private Logger logger = LoggerFactory.getLogger(NioServer.class);
	private final EventProcessor eventProcessor;
	private final ServerType serverType;
	private final Selector selector;
	private final ByteBuffer readBuffer = ByteBuffer.allocate(8196);
	private final List<ChangeRequest> changeEvents = new LinkedList<>();
	private final Map<SocketChannel, List<ByteBuffer>> channelDataMap = new HashMap<>();
	private volatile boolean shutdown;
	private volatile boolean killed;
	private ServerSocketChannel serverChannel;

	public NioServer(String hostAddress, int port, ServerType serverType, EventProcessor eventProcessor)
		throws IOException {
		this.selector = initializeSelector(hostAddress, port);
		this.eventProcessor = eventProcessor;
		this.serverType = serverType;
	}

	public void send(SocketChannel socket, byte[] data) {
		synchronized (this.changeEvents) {
			this.changeEvents.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			synchronized (this.channelDataMap) {
				List<ByteBuffer> queue = this.channelDataMap.get(socket);
				if (queue == null) {
					queue = new ArrayList<>();
					this.channelDataMap.put(socket, queue);
				}
				queue.add(ByteBuffer.wrap(data));
			}
		}
		this.selector.wakeup();
	}

	public ServerType getServerType() {
		return serverType;
	}

	public void run() {
		try {
			logger.info("ServerType={} started", serverType);
			loop();
		} catch (IOException e) {
			logger.error("Error in loop function", e);
		}
	}

	private void loop() throws IOException {
		while (!shutdown) {
			synchronized (this.changeEvents) {
				for (ChangeRequest change : this.changeEvents) {
					switch (change.type) {
						case ChangeRequest.CHANGEOPS:
							SelectionKey key = change.socket.keyFor(this.selector);
							if (key != null && key.isValid()) {
								key.interestOps(change.ops);
							} else {
								logger.warn("SelectionKey for socket={} is null or invalid", change.socket);
							}
					}
				}
				this.changeEvents.clear();
			}
			this.selector.select();
			Iterator selectedKeys = this.selector.selectedKeys().iterator();
			while (selectedKeys.hasNext()) {
				SelectionKey key = (SelectionKey) selectedKeys.next();
				selectedKeys.remove();

				if (!key.isValid()) {
					continue;
				}

				if (key.isAcceptable()) {
					this.acceptConnection(key);
				} else if (key.isReadable()) {
					this.readData(key);
				} else if (key.isWritable()) {
					this.writeData(key);
				}
			}
		}
		close();
	}

	private void close() throws IOException {
		killed = true;
		selector.close();
		serverChannel.close();
		logger.info("ServerType={} shutdown", serverType);
	}

	private void acceptConnection(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		socketChannel.register(this.selector, SelectionKey.OP_READ);
	}

	private void readData(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		this.readBuffer.clear();

		try {
			int readBytes = socketChannel.read(this.readBuffer);
			if (readBytes == -1) {
				key.channel().close();
				key.cancel();
			} else {
				byte[] dataCopy = new byte[readBytes];
				System.arraycopy(readBuffer.array(), 0, dataCopy, 0, readBytes);
				eventProcessor.newEvent(new NewMessageEvent(this, socketChannel, dataCopy));
			}
		} catch (IOException e) {
			key.cancel();
			socketChannel.close();
		}
	}

	private void writeData(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		logger.info("Send message to client={}", socketChannel);

		synchronized (this.channelDataMap) {
			List queue = (List) this.channelDataMap.get(socketChannel);

			while (!queue.isEmpty()) {
				ByteBuffer buffer = (ByteBuffer) queue.get(0);
				socketChannel.write(buffer);
				if (buffer.remaining() > 0) {
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				key.interestOps(SelectionKey.OP_READ);
			}
		}
	}

	private Selector initializeSelector(String host, int port) throws IOException {
		Selector socketSelector = SelectorProvider.provider().openSelector();

		this.serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		InetSocketAddress isa = new InetSocketAddress(host, port);
		serverChannel.socket().bind(isa);
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}

	@Override
	public Callable<Boolean> shutdown() {
		shutdown = true;
		return () -> {
			while (!killed) {
				selector.wakeup();
			}
			return true;
		};
	}
}

