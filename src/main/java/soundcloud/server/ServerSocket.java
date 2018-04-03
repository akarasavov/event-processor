package soundcloud.server;

import java.nio.channels.SocketChannel;

/**
 * @author akt.
 */
public interface ServerSocket {

	void send(SocketChannel socket, byte[] data);

	int getType();

}
