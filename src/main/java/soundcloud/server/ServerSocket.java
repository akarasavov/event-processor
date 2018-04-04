package soundcloud.server;

import java.nio.channels.SocketChannel;
import soundcloud.server.event.ServerType;


public interface ServerSocket extends CancelableRunnable {

	void send(SocketChannel socket, byte[] data);

	ServerType getServerType();

}
