package soundcloud.server.event;

import java.nio.channels.SocketChannel;

/**
 * @author akt.
 */
public class NewMessageEvent implements ServerSocketEvent {

	private final SocketChannel socketChannel;
	private final String message;

	public NewMessageEvent(SocketChannel socketChannel, String message) {
		this.socketChannel = socketChannel;
		this.message = message;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "NewMessageEvent{" +
			"socketChannel=" + socketChannel +
			", message='" + message + '\'' +
			'}';
	}
}
