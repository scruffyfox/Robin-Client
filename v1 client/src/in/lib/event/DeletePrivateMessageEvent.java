package in.lib.event;

import in.lib.event.base.PrivateMessageEvent;
import in.model.PrivateMessage;

public class DeletePrivateMessageEvent extends PrivateMessageEvent
{
	public DeletePrivateMessageEvent(PrivateMessage message)
	{
		super(message);
	}
}