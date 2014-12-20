package in.view.holder;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.view.AvatarImageView;
import in.model.SimpleUser;
import in.rob.client.R;
import in.view.holder.base.Holder;
import lombok.Getter;

@Injectable
public class AccountHolder extends Holder<SimpleUser>
{
	@Getter @InjectView(R.id.username_title) protected TextView usernameTitle;
	@Getter @InjectView(R.id.username_subtitle) protected TextView usernameSubtitle;
	@Getter @InjectView(R.id.avatar) protected AvatarImageView avatar;
	@Getter @InjectView(R.id.user_action) protected Button userAction;

	public AccountHolder(View view)
	{
		super(view);
	}

	@Override public void populate(SimpleUser model)
	{
		usernameTitle.setText(model.getFormattedMentionNameTitle());
		usernameSubtitle.setText(model.getFormattedMentionNameSubTitle());
		avatar.setUser(model);
	}
}