package soundcloud.user;

import java.nio.channels.SocketChannel;


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

	@Override
	public String toString() {
		return "ConnectedUser{" +
			"socketChannel=" + socketChannel +
			"userCode=" + getUserCode() +
			'}';
	}
}
