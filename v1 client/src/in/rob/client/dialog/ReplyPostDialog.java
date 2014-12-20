package in.rob.client.dialog;

import in.lib.Constants;
import in.model.Post;
import in.rob.client.R;
import android.os.Bundle;

/**
 * Reply post dialog used for replying to a post in timeline or thread
 *
 * Possible extra arguments:
 * <ul>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_REPLY_TO}</b>: The {@link Post} object to reply to</li>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_REPLY_TO_EXTRA}</b>: A string resource of extra @usernames to include</li>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_TEXT}</b>: A string resource to include at the end of the post</li>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_POST_ID}</b>: A string resource of the Post ID to reply to, if you supply this, you <b>must</b> supply <b>{@linkplain Constants Constants.EXTRA_REPLY_TO_EXTRA}</b> with the user's mention name
 * </ul>
 */
public class ReplyPostDialog extends NewPostDialog
{
	@Override public void retrieveArguments(Bundle instances)
	{
		super.retrieveArguments(instances);

		tempTitle = getString(R.string.reply_post);

		if (instances != null)
		{
			String postText = "";

			if (instances.containsKey(Constants.EXTRA_TEXT))
			{
				postText += (instances.getString(Constants.EXTRA_TEXT));
			}

			if (instances.containsKey(Constants.EXTRA_POST_ID))
			{
				if (instances.containsKey(Constants.EXTRA_REPLY_TO_EXTRA))
				{
					postText = "@" + (instances.getString(Constants.EXTRA_REPLY_TO_EXTRA));
					tempTitle = String.format(getString(R.string.reply_to), instances.getString(Constants.EXTRA_REPLY_TO_EXTRA));
				}
				else
				{
					tempTitle = getString(R.string.reply_thread);
				}

				if (postText.length() > 0)
				{
					postText += " ";
				}

				getCurrentPost().setPostText(postText);
				getCurrentPost().setReplyId(instances.getString(Constants.EXTRA_POST_ID));
			}
		}

		if (instances.containsKey(Constants.EXTRA_TITLE))
		{
			tempTitle = instances.getString(Constants.EXTRA_TITLE);
		}
	}
}