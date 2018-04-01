package soundcloud.server;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Objects;
import rx.Observable;
import rx.Subscription;
import soundcloud.event.entity.EventEntity;
import soundcloud.event.executor.EventExecutor;
import soundcloud.parser.Parser;
import soundcloud.server.event.ClientDisconnectEvent;
import soundcloud.server.event.NewMessageEvent;
import soundcloud.server.event.ServerSocketEvent;
import soundcloud.user.UserCache;
import soundcloud.user.UserEntity;
import soundcloud.util.RxUtil;

/**
 * @author akt.
 */
public class MessageListener {

	private final static String CRLF = "\\n";
	private final Parser<EventEntity> sourceEventParser;
	private final Parser<String> clientEventParser;
	private final Observable<ServerSocketEvent> sourceEventMessages;
	private final Observable<ServerSocketEvent> clientMessage;
	private final EventExecutor eventExecutor;
	private final UserCache userCache;
	private Subscription eventMessageSubscription;
	private Subscription clientEventSubscription;

	public MessageListener(Observable<ServerSocketEvent> sourceEventMessages,
		Observable<ServerSocketEvent> clientMessages, Parser<EventEntity> sourceEventParser,
		Parser<String> clientEventParser, EventExecutor eventExecutor, UserCache userCache) {
		this.sourceEventMessages = sourceEventMessages;
		this.clientMessage = clientMessages;
		this.sourceEventParser = sourceEventParser;
		this.eventExecutor = eventExecutor;
		this.clientEventParser = clientEventParser;
		this.userCache = userCache;
	}

	public void startListen() {
		this.eventMessageSubscription = sourceEventMessages.filter(event -> event instanceof NewMessageEvent)
			.map(event -> (NewMessageEvent) event)
			.subscribe(eventMessage ->
				Arrays.stream(eventMessage.getMessage().split(CRLF))
					.map(sourceEventParser::parse)
					.filter(Objects::nonNull)
					.forEach(eventExecutor::execute)
			);

		this.clientEventSubscription = clientMessage
			.subscribe(clientMessage -> {
				if (clientMessage instanceof NewMessageEvent) {
					NewMessageEvent newMessageEvent = (NewMessageEvent) clientMessage;
					String userCode = clientEventParser.parse(newMessageEvent.getMessage());
					userCache.addUser(userCode, new UserEntity(userCode, newMessageEvent.getSocketChannel()));
				} else if (clientMessage instanceof ClientDisconnectEvent) {
					ClientDisconnectEvent disconnectEvent = (ClientDisconnectEvent) clientMessage;
					SocketChannel socketChannel = disconnectEvent.getSocketChannel();
					userCache.removeUser(socketChannel);
				}
			});
	}

	public void stopListen() {
		RxUtil.unsubscribe(eventMessageSubscription);
		RxUtil.unsubscribe(clientEventSubscription);
	}
}
