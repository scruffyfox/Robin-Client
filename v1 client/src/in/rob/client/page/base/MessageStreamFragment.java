package in.rob.client.page.base;

import in.lib.Constants;
import in.lib.adapter.PrivateMessageAdapter;
import in.lib.event.DeletePrivateMessageEvent;
import in.lib.event.NewPrivateMessageEvent;
import in.model.PrivateMessage;
import in.model.SimpleUser;
import in.model.base.NetObject;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

import com.squareup.otto.Subscribe;

public abstract class MessageStreamFragment extends StreamFragment
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
			setAdapter(new PrivateMessageAdapter(getContext(), new ArrayList<PrivateMessage>()));
		}
		else
		{
			setAdapter(getAdapter());
		}
	}

	/**
	 * Loops through posts and builds a list of users and tags to use in auto suggest
	 */
	public synchronized void extractUsersAndTags(List<NetObject> items)
	{
		List<SimpleUser> users = getCacheManager().readFileAsObject(Constants.CACHE_USERNAMES, new ArrayList<SimpleUser>());
		List<String> usersStr = getCacheManager().readFileAsObject(Constants.CACHE_USERNAMES_STR, new ArrayList<String>());
		List<NetObject> tags = getCacheManager().readFileAsObject(Constants.CACHE_HASHTAGS, new ArrayList<NetObject>());
		List<String> tagsStr = getCacheManager().readFileAsObject(Constants.CACHE_HASHTAGS_STR, new ArrayList<String>());

		if (items != null)
		{
			for (NetObject o : items)
			{
				PrivateMessage p = (PrivateMessage)o;
				if (!usersStr.contains(p.getPoster().getId()))
				{
					users.add(SimpleUser.parseFromUser(p.getPoster()));
					usersStr.add(p.getPoster().getId());
				}

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

		getCacheManager().asyncWriteFile(Constants.CACHE_USERNAMES, users);
		getCacheManager().asyncWriteFile(Constants.CACHE_HASHTAGS, tags);
		getCacheManager().asyncWriteFile(Constants.CACHE_USERNAMES_STR, usersStr);
		getCacheManager().asyncWriteFile(Constants.CACHE_HASHTAGS_STR, tagsStr);

		users.clear();
		tags.clear();
		tagsStr.clear();
		usersStr.clear();
	}

	/**************************************************
	 *
	 *  OTTO EVENTS
	 *
	 **************************************************/

	/**
	 * Called when a private messsage is recieved from a broadcast.
	 *
	 * Override this to change the default behavour.
	 * Default: prepend adapter with message
	 *
	 * <b>You must override this method and call super() in order for
	 * these events to be registered in your class</b>
	 *
	 * @param p The message recieved
	 */
	@Subscribe public void onMessageRecieved(NewPrivateMessageEvent p)
	{
		if (p.getMessage() != null)
		{
			prependItem(p.getMessage());
		}
	}

	/**
	 * Called when a private message is deleted from a broadcast.
	 *
	 * Override this to change the default behavour.
	 * Default: delete message from adapter
	 *
	 * <b>You must override this method and call super() in order for
	 * these events to be registered in your class</b>
	 *
	 * @param p The message recieved
	 */
	@Subscribe public void onMessageDeleted(DeletePrivateMessageEvent p)
	{
		if (p.getMessage() != null)
		{
			deleteItem(p.getMessage());
		}
	}
}