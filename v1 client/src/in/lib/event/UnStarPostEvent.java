package in.lib.event;

import in.lib.event.base.PostEvent;
import in.model.Post;

public class UnStarPostEvent extends PostEvent
{
	public UnStarPostEvent(Post post)
	{
		super(post);
	}
}