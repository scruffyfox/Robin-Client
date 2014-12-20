package in.lib.event;

import in.lib.event.base.PostDraftEvent;
import in.model.DraftPost;

public class UpdatedPostDraftEvent extends PostDraftEvent
{
	public UpdatedPostDraftEvent(DraftPost post)
	{
		super(post);
	}
}