package soundcloud.parser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import soundcloud.event.entity.EventEntity;
import soundcloud.event.entity.EventType;


public class SourceEventParserImplTest {

	private Parser<EventEntity> actionParser;

	@Before
	public void setUp() throws Exception {
		this.actionParser = SourceEventParserImpl.DEFAULT;
	}

	@Test
	public void parseBrokenPrivateMsg() {
		String message = "578194|P|88";
		Assert.assertNull(actionParser.parse(message));
	}

	@Test
	public void parseBroadcast() {
		String message = "542532|B";
		EventEntity eventEntity = actionParser.parse(message);
		Assert.assertTrue(eventEntity.getEventType() == EventType.BROADCAST);
		Assert.assertTrue(eventEntity.getMessage().equals(message + "\n"));
	}

	@Test
	public void parseFollow() {
		String message = "666|F|60|50";
		EventEntity eventEntity = actionParser.parse(message);
		Assert.assertTrue(eventEntity.getEventType() == EventType.FOLLOW);
		Assert.assertTrue(eventEntity.getMessage().equals(message + "\n"));
		Assert.assertTrue(eventEntity.getFromUser().equals("60"));
		Assert.assertTrue(eventEntity.getToUser().equals("50"));
	}

	@Test
	public void parseStatusUpdate() {
		String message = "634|S|32";
		EventEntity eventEntity = actionParser.parse(message);
		Assert.assertTrue(eventEntity.getEventType() == EventType.STATUS_UPDATE);
		Assert.assertTrue(eventEntity.getMessage().equals(message + "\n"));
		Assert.assertTrue(eventEntity.getFromUser().equals("32"));
	}

	@Test
	public void parseLongInput() {
		String message = "634|S|32|32|32|32";
		Assert.assertNull(actionParser.parse(message));
	}

	@Test
	public void parseEmptyMessage() {
		String message = "";
		Assert.assertNull(actionParser.parse(message));
	}

	@Test
	public void parseIllegalAction() {
		String message = "634|K|32";
		Assert.assertNull(actionParser.parse(message));
	}


}