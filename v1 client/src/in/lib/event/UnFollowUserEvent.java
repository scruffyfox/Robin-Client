package in.lib.event;

import in.lib.event.base.UserEvent;
import in.model.User;

public class UnFollowUserEvent extends UserEvent
{
	public UnFollowUserEvent(User user)
	{
		super(user);
	}
}