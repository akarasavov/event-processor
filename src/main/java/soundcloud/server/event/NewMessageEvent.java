package soundcloud.server.event;

import java.nio.channels.SocketChannel;
import soundcloud.server.ServerSocket;

/**
 * @author akt.
 */
public class NewMessageEvent implements ServerEvent {

	private final SocketChannel socketChannel;
	private final byte[] data;
	private final ServerSocket serverSocket;

	public NewMessageEvent(ServerSocket serverSocket, SocketChannel socketChannel, byte[] data) {
		this.socketChannel = socketChannel;
		this.data = data;
		this.serverSocket = serverSocket;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public ServerSocket getServerSocket() {
		return serverSocket;
	}
}
