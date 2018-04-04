package soundcloud.server.event;

import java.nio.channels.SocketChannel;
import soundcloud.server.ServerSocket;

/**
 * @author akt.
 */
public class NewClientEvent implements ServerEvent {

	private final SocketChannel socketChannel;
	private final ServerSocket server;

	public NewClientEvent(ServerSocket server, SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
		this.server = server;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	@Override
	public ServerSocket getServerSocket() {
		return server;
	}
}
