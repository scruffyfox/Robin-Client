package in.rob.client.dialog;

import in.lib.Constants;
import in.model.PrivateMessage;
import in.rob.client.R;
import android.os.Bundle;

/**
 * Reply message dialog used for replying to a message
 *
 * Possible extra arguments:
 * <ul>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_REPLY_TO}</b>: The {@link PrivateMessage} object to reply to</li>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_REPLY_TO_EXTRA}</b>: A string resource of extra @usernames to include</li>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_TEXT}</b>: A string resource to include at the end of the post</li>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_MESSAGE_ID}</b>: A string resource of the Post ID to reply to, if you supply this, you <b>must</b> supply <b>{@linkplain Constants Constants.EXTRA_REPLY_TO_EXTRA}</b> with the user's mention name
 * </ul>
 */
public class ReplyMessageDialog extends NewMessageDialog
{
	@Override public void retrieveArguments(Bundle instances)
	{
		super.retrieveArguments(instances);

		tempTitle = getString(R.string.reply_message);

		if (instances != null)
		{
			String postText = "";

			if (instances.containsKey(Constants.EXTRA_TEXT))
			{
				postText += (instances.getString(Constants.EXTRA_TEXT));
			}

			if (instances.containsKey(Constants.EXTRA_REPLY_TO))
			{
				PrivateMessage replyMessage = (PrivateMessage)instances.getParcelable(Constants.EXTRA_REPLY_TO);

				if (instances.containsKey(Constants.EXTRA_REPLY_TO_EXTRA))
				{
					postText += (instances.getString(Constants.EXTRA_REPLY_TO_EXTRA));
				}
				else
				{
					postText += (getString(R.string.at) + replyMessage.getPoster().getMentionName() + " ");
				}

				getCurrentPost().setPostText(postText);
				getCurrentPost().setReplyId(replyMessage.getId());
				tempTitle = String.format(getString(R.string.reply_to), replyMessage.getPoster().getMentionName());
			}
			else if (instances.containsKey(Constants.EXTRA_MESSAGE_ID))
			{
				if (instances.containsKey(Constants.EXTRA_REPLY_TO_EXTRA))
				{
					postText = "@" + (instances.getString(Constants.EXTRA_REPLY_TO_EXTRA));
				}
				else
				{
					finish();
				}

				getCurrentPost().setPostText(postText + " ");
				getCurrentPost().setReplyId(instances.getString(Constants.EXTRA_MESSAGE_ID));
				tempTitle = String.format(getString(R.string.reply_to), instances.getString(Constants.EXTRA_REPLY_TO_EXTRA));
			}
		}

		if (instances.containsKey(Constants.EXTRA_TITLE))
		{
			tempTitle = instances.getString(Constants.EXTRA_TITLE);
		}
	}
}