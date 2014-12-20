package in.lib.event;

import in.lib.event.base.PostEvent;
import in.model.Post;

public class NewRepostEvent extends PostEvent
{
	public NewRepostEvent(Post post)
	{
		super(post);
	}
}