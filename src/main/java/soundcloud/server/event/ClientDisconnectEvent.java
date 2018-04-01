package soundcloud.server.event;

import java.nio.channels.SocketChannel;

/**
 * @author akt
 */
public class ClientDisconnectEvent implements ServerSocketEvent {

	private final SocketChannel socketChannel;

	public ClientDisconnectEvent(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}
}
