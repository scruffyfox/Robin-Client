package in.lib.event;

import in.lib.event.base.PrivateMessageEvent;
import in.model.PrivateMessage;

public class NewPrivateMessageEvent extends PrivateMessageEvent
{
	public NewPrivateMessageEvent(PrivateMessage message)
	{
		super(message);
	}
}