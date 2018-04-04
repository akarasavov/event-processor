package soundcloud;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class SocketClient {

	private final PrintWriter out;
	private final BufferedReader in;
	private final Socket socket;

	public SocketClient(String host, int port) throws IOException {
		this.socket = new Socket(host, port);
		this.out = new PrintWriter(socket.getOutputStream(), true);
		this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	public void writeMessage(List<String> message) {
		message.forEach(out::write);
		out.flush();
	}

	public String readMessage() throws IOException {
		return in.readLine() + "\n";
	}

	public void close() throws IOException {
		out.close();
		in.close();
		socket.close();
	}


}
