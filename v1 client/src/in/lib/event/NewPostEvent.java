package in.lib.event;

import in.lib.event.base.PostEvent;
import in.model.Post;

public class NewPostEvent extends PostEvent
{
	public NewPostEvent(Post post)
	{
		super(post);
	}
}