package soundcloud.server;

import java.nio.channels.SocketChannel;


class ChangeRequest {

	static final int CHANGEOPS = 2;

	SocketChannel socket;
	int type;
	int ops;

	ChangeRequest(SocketChannel socket, int type, int ops) {
		this.socket = socket;
		this.type = type;
		this.ops = ops;
	}
}
