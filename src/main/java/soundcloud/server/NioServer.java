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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioServer implements Runnable, ServerSocket {

	private Logger logger = LoggerFactory.getLogger(NioServer.class);
	private final EchoWorker echoWorker;
	private final int type;
	private Selector selector;
	private ByteBuffer readBuffer = ByteBuffer.allocate(8196);
	private final List<ChangeRequest> pendingChanges = new LinkedList<>();
	private final Map<SocketChannel, List<ByteBuffer>> pendingData = new HashMap<>();

	public NioServer(String hostAddress, int port, int type, EchoWorker echoWorker) throws IOException {
		this.selector = initSelector(hostAddress, port);
		this.echoWorker = echoWorker;
		this.type = type;
	}

	public void send(SocketChannel socket, byte[] data) {
		synchronized (this.pendingChanges) {
			this.pendingChanges.add(new ChangeRequest(socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));

			synchronized (this.pendingData) {
				List<ByteBuffer> queue = this.pendingData.get(socket);
				if (queue == null) {
					queue = new ArrayList<>();
					this.pendingData.put(socket, queue);
				}
				queue.add(ByteBuffer.wrap(data));
			}
		}
		this.selector.wakeup();
	}

	@Override
	public int getType() {
		return type;
	}

	public void run() {
		while (true) {
			try {
				synchronized (this.pendingChanges) {
					Iterator changes = this.pendingChanges.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = (ChangeRequest) changes.next();
						switch (change.type) {
							case ChangeRequest.CHANGEOPS:
								SelectionKey key = change.socket.keyFor(this.selector);
								if (key != null) {
									key.interestOps(change.ops);
								} else {
									logger.warn("SelectionKey for socket={} is null", change.socket);
								}
						}
					}
					this.pendingChanges.clear();
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
						this.accept(key);
					} else if (key.isReadable()) {
						this.read(key);
					} else if (key.isWritable()) {
						this.write(key);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void accept(SelectionKey key) throws IOException {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		socketChannel.register(this.selector, SelectionKey.OP_READ);
		echoWorker.newClient(this, socketChannel);
	}

	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		this.readBuffer.clear();

		try {
			int readBytes = socketChannel.read(this.readBuffer);
			if (readBytes == -1) {
				key.channel().close();
				key.cancel();
			} else {
				echoWorker.newMessage(this, socketChannel, readBuffer.array(), readBytes);
			}
		} catch (IOException e) {
			key.cancel();
			socketChannel.close();
		}
	}

	private void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (this.pendingData) {
			List queue = (List) this.pendingData.get(socketChannel);

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

	private Selector initSelector(String host, int port) throws IOException {
		Selector socketSelector = SelectorProvider.provider().openSelector();

		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		InetSocketAddress isa = new InetSocketAddress(host, port);
		serverChannel.socket().bind(isa);
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

		return socketSelector;
	}

}

