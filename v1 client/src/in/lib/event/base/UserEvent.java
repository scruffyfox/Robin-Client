package in.lib.event.base;

import in.model.User;
import lombok.Getter;

public class UserEvent
{
	@Getter private User user;

	public UserEvent(User user)
	{
		this.user = user;
	}
}