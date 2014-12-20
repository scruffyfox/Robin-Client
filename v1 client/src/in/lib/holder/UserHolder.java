package in.lib.holder;

import in.lib.adapter.UserAdapter;
import in.lib.annotation.InjectView;
import in.lib.holder.base.ViewHolder;
import in.lib.manager.SettingsManager;
import in.lib.utils.Views;
import in.lib.view.AvatarView;
import in.model.User;
import in.rob.client.MainApplication;
import in.rob.client.R;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class UserHolder implements ViewHolder
{
	@InjectView(R.id.title) public TextView title;
	@InjectView(R.id.text) public TextView text;
	@InjectView(R.id.follow_button) public Button followButton;
	@InjectView(R.id.mute_button) public Button muteButton;
	@InjectView(R.id.avatar) public AvatarView avatar;

	public UserHolder(View currentView)
	{
		Views.inject(this, currentView);
	}

	@Override public void onViewDestroyed(View v)
	{
		if (SettingsManager.isListAnimationEnabled())
		{
			v.clearAnimation();
		}

		avatar.setImageBitmap(null);
		ImageLoader.getInstance().cancelDisplayTask(avatar);
	}

	public void populate(User user, UserAdapter adapter)
	{
		// set values
		title.setText(user.getUserName());
		text.setText("@" + user.getMentionName());
		followButton.setText(user.isYou() ? R.string.edit_profile : (user.getYouFollow() ? R.string.unfollow : R.string.follow));
		followButton.setBackgroundResource(user.getYouFollow() ? adapter.getButtonRes()[1] : adapter.getButtonRes()[0]);
		muteButton.setText(user.isMuted() ? R.string.unmute : R.string.mute);
		muteButton.setBackgroundResource(user.isMuted() ? adapter.getButtonRes()[1] : adapter.getButtonRes()[0]);

		if (SettingsManager.getShowAvatars())
		{
			avatar.setVisibility(View.VISIBLE);
			avatar.setContentDescription(user.getMentionName());
			avatar.setImageBitmap(null);
			ImageLoader.getInstance().cancelDisplayTask(avatar);

			if (user.isAvatarDefault())
			{
				avatar.setImageResource(R.drawable.default_avatar);
			}
			else
			{
				ImageLoader.getInstance().displayImage(user.getAvatarUrl() + "?avatar=1&id=" + user.getId(), avatar, MainApplication.getAvatarImageOptions());
			}
		}
		else
		{
			avatar.setVisibility(View.GONE);
			avatar.setImageBitmap(null);
		}

		if (SettingsManager.isCustomFontsEnabled())
		{
			title.setTypeface(Typeface.defaultFromStyle(0));
			text.setTypeface(Typeface.defaultFromStyle(0));
		}
	}
}