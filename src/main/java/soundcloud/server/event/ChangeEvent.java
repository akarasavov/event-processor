package soundcloud.server.event;

import java.nio.channels.SocketChannel;


public class ChangeEvent {

	public static final int REGISTER = 1;
	public static final int CHANGEOPS = 2;
	private final SocketChannel socketChannel;
	private final int operationType;
	private final int eventType;

	public ChangeEvent(SocketChannel socketChannel, int eventType, int operationType) {
		this.socketChannel = socketChannel;
		this.operationType = operationType;
		this.eventType = eventType;
	}

	public static int getREGISTER() {
		return REGISTER;
	}

	public static int getCHANGEOPS() {
		return CHANGEOPS;
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public int getOperationType() {
		return operationType;
	}

	public int getEventType() {
		return eventType;
	}
}
