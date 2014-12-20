package in.lib.event;

import in.model.User;
import lombok.Getter;

public class ProfileUpdatedEvent
{
	@Getter private User user;

	public ProfileUpdatedEvent(User user)
	{
		this.user = user;
	}
}