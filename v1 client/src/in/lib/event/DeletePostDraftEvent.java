package in.lib.event;

import in.lib.event.base.PostDraftEvent;
import in.model.DraftPost;

public class DeletePostDraftEvent extends PostDraftEvent
{
	public DeletePostDraftEvent(DraftPost post)
	{
		super(post);
	}
}