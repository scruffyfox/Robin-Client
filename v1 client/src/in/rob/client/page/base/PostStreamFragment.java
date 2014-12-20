package in.rob.client.page.base;

import in.lib.Constants;
import in.lib.adapter.PostAdapter;
import in.lib.event.DeletePostEvent;
import in.lib.event.NewPostEvent;
import in.lib.event.StarPostEvent;
import in.lib.event.UnStarPostEvent;
import in.lib.manager.SettingsManager;
import in.model.Post;
import in.model.SimpleUser;
import in.model.base.NetObject;
import in.rob.client.R;
import in.rob.client.ThreadActivity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.squareup.otto.Subscribe;

public abstract class PostStreamFragment extends StreamFragment
{
	@Override public void onDataReady()
	{

	}

	@Override public void retrieveArguments(Bundle arguments)
	{

	}

	@Override public void setupAdapters()
	{
		if (getAdapter() == null)
		{
			setAdapter(new PostAdapter(getContext(), new ArrayList<Post>()));
		}
		else
		{
			setAdapter(getAdapter());
		}
	}

	@Override public void onRefresh()
	{
		((PostAdapter)getAdapter()).setLastPositionAnimated(5);
		super.onRefresh();
	}

	/**
	 * Loops through posts and builds a list of users and tags to use in auto suggest
	 */
	public void extractUsersAndTags(List<NetObject> items)
	{
		List<SimpleUser> users = getCacheManager().readFileAsObject(Constants.CACHE_USERNAMES, new ArrayList<SimpleUser>());
		List<NetObject> tags = getCacheManager().readFileAsObject(Constants.CACHE_HASHTAGS, new ArrayList<NetObject>());
		List<String> tagsStr = getCacheManager().readFileAsObject(Constants.CACHE_HASHTAGS_STR, new ArrayList<String>());

		if (items != null)
		{
			for (NetObject o : items)
			{
				Post p = (Post)o;

				SimpleUser user = SimpleUser.parseFromUser(p.getPoster());
				if (!SimpleUser.containsUser(users, user))
				{
					users.add(user);
				}

				if (p.getHashTags() != null)
				{
					for (String s : p.getHashTags())
					{
						if (!tagsStr.contains("#" + s))
						{
							NetObject tag = new NetObject();
							tag.setFilterTag("#" + s);
							tags.add(tag);
							tagsStr.add("#" + s);
						}
					}
				}
			}
		}

		getCacheManager().asyncWriteFile(Constants.CACHE_USERNAMES, users);
		getCacheManager().asyncWriteFile(Constants.CACHE_HASHTAGS, tags);
		getCacheManager().asyncWriteFile(Constants.CACHE_HASHTAGS_STR, tagsStr);
	}

	@Override public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3)
	{
		if (!SettingsManager.isInvertPostClick() || (SettingsManager.isInvertPostClick() && v.getTag(R.id.TAG_STOP_STACK_OVERFLOW) != null))
		{
			v.setTag(R.id.TAG_STOP_STACK_OVERFLOW, null);
			Post post = (Post)getAdapter().getItem(position - getHeadedListView().getHeaderViewsCount());
			Intent postDetails = new Intent(getContext(), ThreadActivity.class);

			postDetails.putExtra(Constants.EXTRA_POST, post);
			startActivity(postDetails);
		}
		else
		{
			v.setTag(R.id.TAG_STOP_STACK_OVERFLOW, true);
			getAdapter().onItemLongClick(arg0, v, position, arg3);
		}
	}

	/**************************************************
	 *
	 *  OTTO EVENTS
	 *
	 **************************************************/

	/**
	 * Called when a post is recieved from a broadcast.
	 *
	 * Override this to change the default behavour.
	 * Default: prepend adapter with post
	 *
	 * @param p The post recieved
	 */
	@Subscribe public void onPostRecieved(NewPostEvent event)
	{
		final Post p = event.getPost();

		if (p != null && getAdapter() != null)
		{
			if (isReady())
			{
				getHeadedListView().setBlockLayoutChildren(true);
				Post replyee = (Post)getAdapter().getItemById(p.getReplyTo());
				if (replyee != null)
				{
					replyee.setHasReplies(true);
					replyee.setReplyCount(replyee.getReplyCount() + 1);
				}

				int[] pos = getLastViewPosition(null);
				prependItem(p);
				registerPositionReset(pos[0] + 1, pos[1]);
				refreshAdapter();
			}
			else
			{
				prependItem(p);
			}
		}
	}

	/**
	 * Called when a post is deleted from a broadcast.
	 *
	 * Override this to change the default behavour.
	 * Default: delete post from adapter
	 *
	 * @param p The post deleted
	 */
	@Subscribe public void onPostDeleted(DeletePostEvent event)
	{
		Post p = event.getPost();

		if (p != null && getAdapter() != null)
		{
			deleteItem(p);
		}
	}

	/**
	 * Called when a post is starred
	 *
	 * Override this to change the default behavour.
	 *
	 * @param p The post starred
	 */
	@Subscribe public void onPostStarred(StarPostEvent event){}

	/**
	 * Called when a post is unstarred
	 *
	 * Override this to change the default behavour.
	 *
	 * @param p The post unstarred
	 */
	@Subscribe public void onPostUnStarred(UnStarPostEvent event){}
}