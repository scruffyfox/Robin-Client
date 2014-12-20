package in.controller.adapter;

import android.content.Context;
import android.widget.AdapterView.OnItemLongClickListener;

import in.controller.adapter.base.RobinAdapter;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.model.Post;
import in.view.delegate.PostCollapsedDelegate;
import in.view.delegate.PostDelegate;
import in.view.delegate.PostMentionDelegate;

public class PostAdapter extends RobinAdapter<Post> implements OnItemLongClickListener
{
	private static final int TYPE_POST = 0;
	private static final int TYPE_MENTION = 1;
	private static final int TYPE_COLLAPSED = 2;

	private String userId = "-1";

	public PostAdapter(Context context)
	{
		this(context, UserManager.getInstance().getUser().getId());
	}

	/**
	 * @param context
	 * @param userId The user ID for the current adapter set. Defaults to the current logged in
	 *               user.
	 */
	public PostAdapter(Context context, String userId)
	{
		super(context);

		this.userId = userId;
		getItemTypes().put(TYPE_POST, new PostDelegate(this));
		getItemTypes().put(TYPE_MENTION, new PostMentionDelegate(this));
		getItemTypes().put(TYPE_COLLAPSED, new PostCollapsedDelegate(this));
	}

	@Override public int getItemViewType(int position)
	{
		Post item = getItem(position);

		if (SettingsManager.getInstance().getCollapsedThreadIds().contains(item.getThreadId()))
		{
			return TYPE_COLLAPSED;
		}
		else if (item.isMention(userId))
		{
			return TYPE_MENTION;
		}

		return TYPE_POST;
	}
}
