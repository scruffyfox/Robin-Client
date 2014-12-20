package in.lib.event;

import in.lib.event.base.UserEvent;
import in.model.User;

public class FollowUserEvent extends UserEvent
{
	public FollowUserEvent(User user)
	{
		super(user);
	}
}