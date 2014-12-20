package in.lib.event.base;

import in.model.DraftPost;
import lombok.Getter;

public class PostDraftEvent
{
	@Getter private DraftPost post;

	public PostDraftEvent(DraftPost post)
	{
		this.post = post;
	}
}