package soundcloud.server;

import java.util.Arrays;
import java.util.Objects;
import rx.Observable;
import rx.Subscription;
import soundcloud.action.entity.EventEntity;
import soundcloud.action.executor.EventExecutor;
import soundcloud.parser.Parser;
import soundcloud.util.RxUtil;

/**
 * @author akt.
 */
public class MessageListener {

	private final static String CRLF = "\r\n";
	private final Parser<EventEntity> sourceEventParser;
	private final Parser<String> clientEventPaser;
	private final Observable<String> eventMessage;
	private final Observable<String> clientMessage;
	private final EventExecutor eventExecutor;
	private Subscription eventMessageSubscription;
	private Subscription clientEventSubscription;

	public MessageListener(Observable<String> eventMessages, Observable<String> clientMessages,
		Parser<EventEntity> sourceEventParser, Parser<String> clientEventParser, EventExecutor eventExecutor) {
		this.eventMessage = eventMessages;
		this.clientMessage = clientMessages;
		this.sourceEventParser = sourceEventParser;
		this.eventExecutor = eventExecutor;
		this.clientEventPaser = clientEventParser;
	}

	public void startListen() {
		this.eventMessageSubscription = eventMessage.subscribe(eventMessage ->
			Arrays.stream(eventMessage.split(CRLF))
				.map(sourceEventParser::parse)
				.filter(Objects::nonNull)
				.forEach(eventExecutor::execute)
		);

		this.clientEventSubscription = clientMessage.subscribe(clientMessage -> {
			String userCode = clientEventPaser.parse(clientMessage);
		});
	}

	public void stopListen() {
		RxUtil.unsubscribe(eventMessageSubscription);
		RxUtil.unsubscribe(clientEventSubscription);
	}
}
