package in.view.holder;

import android.view.View;

import in.lib.utils.Views.Injectable;
import in.model.Post;

@Injectable
public class ThreadPostHolder extends PostHolder
{
	public ThreadPostHolder(View view)
	{
		super(view);
	}

	@Override public void populate(Post model)
	{
		optionsContainer.setVisibility(View.GONE);

		date.setTime(model.getDate());

		usernameTitle.setText(model.getPoster().getFormattedMentionNameTitle());
		usernameSubtitle.setText(model.getPoster().getFormattedMentionNameSubTitle());
		postText.setLinkMovementMethod();
		postText.setText(model.getPostText());
		avatar.setUser(model.getPoster());
	}
}
