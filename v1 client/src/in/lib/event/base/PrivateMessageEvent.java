package in.lib.event.base;

import in.model.PrivateMessage;
import lombok.Getter;

public class PrivateMessageEvent
{
	@Getter private PrivateMessage message;

	public PrivateMessageEvent(PrivateMessage message)
	{
		this.message = message;
	}
}