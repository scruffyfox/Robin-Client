package in.rob.client.dialog;

import android.os.Bundle;
import android.widget.EditText;

import in.lib.Constants;
import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.view.AvatarImageView;
import in.model.DraftPost;
import in.model.Post;
import in.rob.client.R;

@Injectable
public class QuotePostDialog extends NewPostDialog
{
	@InjectView private AvatarImageView avatar;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Views.inject(this);
	}

	@Override public int getContentView()
	{
		return R.layout.post_quote_dialog;
	}

	@Override public void initialiseDialog()
	{
		super.initialiseDialog();

		((EditText)getPostInput()).setSelection(0);
	}

	@Override public void retrieveArguments(Bundle instances)
	{
		super.retrieveArguments(instances);

		String tempTitle = "Quote Post";

		if (instances != null)
		{
			if (instances.containsKey(Constants.EXTRA_POST))
			{
				Post post = (Post)instances.getParcelable(Constants.EXTRA_POST);
				tempTitle = "Quote @" + post.getPoster().getUsername();

				((DraftPost)getDraft()).setPostText(" RP @" + post.getPoster().getUsername() + " " + post.getPostText().getText());
				((DraftPost)getDraft()).setReplyId(post.getId());
				avatar.setUser(post.getPoster());
			}
		}

		if (instances.containsKey(Constants.EXTRA_TITLE))
		{
			tempTitle = instances.getString(Constants.EXTRA_TITLE);
		}

		setTitle(tempTitle);
	}
}
