package soundcloud.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author akarasavov
 */
public class SocketChannelUtil {

	private SocketChannelUtil() {
	}

	public static String readMessage(SocketChannel socketChannel) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		while (true) {
			byte[] bytes = readBytes(socketChannel, 256);
			if (bytes.length == 0) {
				return stringBuilder.toString();
			} else {
				stringBuilder.append(new String(bytes, StandardCharsets.UTF_8));
			}
		}
	}

	public static byte[] readBytes(SocketChannel channel, int length) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(length);
		int readBytes = 0;
		int prevReadBytes;
		for (int i = 0; buffer.hasRemaining(); i++) {
			prevReadBytes = readBytes;
			readBytes += channel.read(buffer);
			if (i > 1 && readBytes == prevReadBytes) {
				break;
			}
		}

		byte[] data = new byte[readBytes];
		System.arraycopy(buffer.array(), 0, data, 0, readBytes);
		return data;
	}
}
