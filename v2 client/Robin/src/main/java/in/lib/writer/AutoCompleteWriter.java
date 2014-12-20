package in.lib.writer;

import java.util.ArrayList;
import java.util.List;

import in.data.entity.HashEntity;
import in.lib.Constants;
import in.lib.manager.CacheManager;
import in.lib.type.TListWrapper;
import in.model.AdnModel;
import in.model.Message;
import in.model.SimpleUser;
import in.model.User;

public class AutoCompleteWriter
{
	public void writeUsernames(List<? extends AdnModel> users)
	{
		int size = users.size();
		TListWrapper listWrapper = CacheManager.getInstance().readFile(Constants.CACHE_AUTOCOMPLETE_USERNAMES, TListWrapper.class);

		if (listWrapper == null || (listWrapper != null && listWrapper.getList() == null))
		{
			listWrapper = new TListWrapper(new ArrayList<SimpleUser>(size), SimpleUser.class);
		}

		for (int index = 0; index < size; index++)
		{
			SimpleUser user;

			if (users.get(index) instanceof Message)
			{
				user = new SimpleUser().createFrom(((Message)users.get(index)).getPoster());
			}
			else if (users.get(index) instanceof User)
			{
				user = new SimpleUser().createFrom((User)users.get(index));
			}
			else if (users.get(index) instanceof SimpleUser)
			{
				user = (SimpleUser)users.get(index);
			}
			else
			{
				continue;
			}

			if (user != null && !listWrapper.getList().contains(user))
			{
				listWrapper.getList().add(user);
			}
		}

		CacheWriter writer = new CacheWriter(Constants.CACHE_AUTOCOMPLETE_USERNAMES);
		writer.write(listWrapper);
	}

	public void writeHashtags(List<? extends AdnModel> items)
	{
		int size = items.size();
		TListWrapper listWrapper = CacheManager.getInstance().readFile(Constants.CACHE_AUTOCOMPLETE_HASHTAGS, TListWrapper.class);

		if (listWrapper == null || (listWrapper != null && listWrapper.getList() == null))
		{
			listWrapper = new TListWrapper(new ArrayList<HashEntity>(size), HashEntity.class);
		}

		for (int index = 0; index < size; index++)
		{
			List<HashEntity> hashes;

			if (items.get(index) instanceof Message && ((Message)items.get(index)).getPostText() != null)
			{
				hashes = ((Message)items.get(index)).getPostText().getHashTags();
			}
			else if (items.get(index) instanceof User && ((User)items.get(index)).getDescription() != null)
			{
				hashes = ((User)items.get(index)).getDescription().getHashTags();
			}
			else
			{
				continue;
			}

			for (HashEntity tag : hashes)
			{
				if (tag != null && !listWrapper.getList().contains(tag))
				{
					listWrapper.getList().add(tag);
				}
			}
		}

		CacheWriter writer = new CacheWriter(Constants.CACHE_AUTOCOMPLETE_HASHTAGS);
		writer.write(listWrapper);
	}
}