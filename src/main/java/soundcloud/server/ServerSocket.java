package soundcloud.server;

import java.nio.channels.SocketChannel;
import soundcloud.server.event.ServerType;

/**
 * @author akt.
 */
public interface ServerSocket {

	void send(SocketChannel socket, byte[] data);

	ServerType getType();

}
