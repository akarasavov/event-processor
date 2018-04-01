package soundcloud.server.event;

import java.nio.channels.SocketChannel;

/**
 * @author akt
 */
public class NewMessageEvent implements ServerDataEvent {

	private final SocketChannel channel;
	private byte[] bytes;

	public NewMessageEvent(SocketChannel channel, byte[] bytes) {
		this.channel = channel;
		this.bytes = bytes;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public SocketChannel getChannel() {
		return channel;
	}

}
