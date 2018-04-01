package soundcloud.server.event;

import java.nio.channels.SocketChannel;

/**
 * @author akt.
 */
public class NewMessageEvent {

	public final SocketChannel socketChannel;
	public final String message;

	public NewMessageEvent(SocketChannel socketChannel, String message) {
		this.socketChannel = socketChannel;
		this.message = message;
	}

	@Override
	public String toString() {
		return "NewMessageEvent{" +
			"socketChannel=" + socketChannel +
			", message='" + message + '\'' +
			'}';
	}
}
