package in.lib.event;

import in.lib.event.base.PostEvent;
import in.model.Post;

public class StarPostEvent extends PostEvent
{
	public StarPostEvent(Post post)
	{
		super(post);
	}
}