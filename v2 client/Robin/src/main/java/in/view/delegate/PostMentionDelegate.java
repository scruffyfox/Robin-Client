package in.view.delegate;

import in.controller.adapter.base.RobinAdapter;
import in.model.Post;
import in.rob.client.R;

public class PostMentionDelegate extends PostDelegate
{
	public PostMentionDelegate(RobinAdapter<Post> adapter)
	{
		super(adapter);
	}

	@Override public int getLayout()
	{
		return R.layout.post_mention_view;
	}
}
