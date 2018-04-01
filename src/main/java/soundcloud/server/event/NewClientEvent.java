package soundcloud.server.event;

import java.nio.channels.SocketChannel;

/**
 * @author akt.
 */
public class NewClientEvent implements ServerDataEvent {

	private final SocketChannel socketChannel;

	public NewClientEvent(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}
}