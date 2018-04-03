package soundcloud.user;

import java.nio.channels.SocketChannel;

/**
 * @author akt.
 */
public class ConnectedUser extends User {

	private final SocketChannel socketChannel;

	public ConnectedUser(String userCode, SocketChannel socketChannel) {
		super(userCode);
		this.socketChannel = socketChannel;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	@Override
	public boolean isConnected() {
		return true;
	}
}