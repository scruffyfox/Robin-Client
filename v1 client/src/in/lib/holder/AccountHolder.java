package in.lib.holder;

import in.model.SimpleUser;
import in.model.base.NetObject;
import in.rob.client.MainApplication;
import in.rob.client.R;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class AccountHolder
{
	public ImageView avatar;
	public TextView username, mentionName;
	public Button actionButton;

	public static void populate(AccountHolder holder, View convertView, NetObject item)
	{
		if (item instanceof SimpleUser)
		{
			holder.username.setText(((SimpleUser)item).getUserName());

			if (!TextUtils.isEmpty(((SimpleUser)item).getMentionName()))
			{
				holder.mentionName.setText("@" + ((SimpleUser)item).getMentionName());
			}

			if (holder.actionButton != null)
			{
				if (holder.actionButton.getId() == R.id.follow_button)
				{
					holder.actionButton.setText(((SimpleUser)item).isYou() ? R.string.edit_profile : (((SimpleUser)item).getYouFollow() ? R.string.unfollow : R.string.follow));
					holder.actionButton.setBackgroundResource(((SimpleUser)item).getYouFollow() ? R.drawable.grey_button : R.drawable.red_button);
				}
			}

			ImageLoader.getInstance().cancelDisplayTask(holder.avatar);
			ImageLoader.getInstance().displayImage(((SimpleUser)item).getAvatarUrl() + "?avatar=1&id=" + item.getId(), holder.avatar, MainApplication.getAvatarImageOptions());
		}
	}
}