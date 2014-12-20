package in.lib.event;

import in.lib.event.base.PostDraftEvent;
import in.model.DraftPost;

public class NewPostDraftEvent extends PostDraftEvent
{
	public NewPostDraftEvent(DraftPost post)
	{
		super(post);
	}
}