package in.lib.event;

import in.lib.event.base.PostEvent;
import in.model.Post;

public class DeletePostEvent extends PostEvent
{
	public DeletePostEvent(Post post)
	{
		super(post);
	}
}