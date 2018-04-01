package soundcloud.parser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import soundcloud.entity.Action;
import soundcloud.entity.ActionType;

/**
 * @author akt.
 */
public class ActionParserImplTest {

	private ActionParserImpl actionParser;

	@Before
	public void setUp() throws Exception {
		this.actionParser = new ActionParserImpl();

	}

	@Test
	public void parseBroadcast() {
		String message = "542532\\|B";
		Action action = actionParser.parse(message);
		Assert.assertTrue(action.getActionType() == ActionType.BROADCAST);
		Assert.assertTrue(action.getMessage().equals(message));
	}

	@Test
	public void parseFollow() {
		String message = "666\\|F\\|60\\|50";
		Action action = actionParser.parse(message);
		Assert.assertTrue(action.getActionType() == ActionType.FOLLOW);
		Assert.assertTrue(action.getMessage().equals(message));
		Assert.assertTrue(action.getFromUser().equals("60"));
		Assert.assertTrue(action.getToUser().equals("50"));
	}

	@Test
	public void parseStatusUpdate() {
		String message = "634\\|S\\|32";
		Action action = actionParser.parse(message);
		Assert.assertTrue(action.getActionType() == ActionType.STATUS_UPDATE);
		Assert.assertTrue(action.getMessage().equals(message));
		Assert.assertTrue(action.getFromUser().equals("32"));
	}

	@Test
	public void parseLongInput() {
		String message = "634\\|S\\|32\\|32\\|32\\|32";
		Assert.assertNull(actionParser.parse(message));
	}

	@Test
	public void parseEmptyMessage() {
		String message = "";
		Assert.assertNull(actionParser.parse(message));
	}

	@Test
	public void parseIllegalAction() {
		String message = "634\\|K\\|32";
		Assert.assertNull(actionParser.parse(message));
	}


}