package in.view.holder;

import android.view.View;
import android.widget.TextView;

import in.lib.manager.UserManager;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.view.AvatarImageView;
import in.lib.view.LinkedTextView;
import in.lib.view.TextChronometer;
import in.model.DraftPost;
import in.model.User;
import in.view.holder.base.Holder;
import lombok.Getter;

@Injectable
public class DraftPostHolder extends Holder<DraftPost>
{
	@Getter @InjectView private AvatarImageView avatar;
	@Getter @InjectView private TextChronometer date;
	@Getter @InjectView private TextView usernameTitle;
	@Getter @InjectView private TextView usernameSubtitle;
	@Getter @InjectView private LinkedTextView postText;

	@Getter @InjectView private View send;
	@Getter @InjectView private View edit;
	@Getter @InjectView private View duplicate;
	@Getter @InjectView private View delete;

	public DraftPostHolder(View view)
	{
		super(view);
	}

	@Override public void populate(DraftPost model)
	{
		User user = UserManager.getInstance().getUser();

		date.setTime(model.getDate());

		usernameTitle.setText(user.getFormattedMentionNameTitle());
		usernameSubtitle.setText(user.getFormattedMentionNameSubTitle());
		postText.setText(model.getPostText());
		avatar.setUser(user);
	}
}
