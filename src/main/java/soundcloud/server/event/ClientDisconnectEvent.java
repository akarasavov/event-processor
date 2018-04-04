package soundcloud.server.event;

import java.nio.channels.SocketChannel;
import soundcloud.server.ServerSocket;

/**
 * @author akt
 */
public class ClientDisconnectEvent implements ServerEvent {

	private final SocketChannel socketChannel;

	public ClientDisconnectEvent(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	@Override
	public ServerSocket getServerSocket() {
		return null;
	}
}
