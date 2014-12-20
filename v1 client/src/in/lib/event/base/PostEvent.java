package in.lib.event.base;

import in.model.Post;
import lombok.Getter;

public class PostEvent
{
	@Getter private Post post;

	public PostEvent(Post post)
	{
		this.post = post;
	}
}