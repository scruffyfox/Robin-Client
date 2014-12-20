package in.lib.holder.base;
import in.lib.adapter.base.RobinAdapter;
import in.lib.annotation.InjectView;
import in.lib.manager.SettingsManager;
import in.lib.utils.Views;
import in.lib.view.AvatarView;
import in.lib.view.LinkTouchMovementMethod;
import in.lib.view.LinkifiedTextView;
import in.lib.view.spannable.HashtagClickableSpan;
import in.lib.view.spannable.MentionClickableSpan;
import in.lib.view.spannable.UrlClickableSpan;
import in.model.base.Message;
import in.rob.client.MainApplication;
import in.rob.client.R;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;


/**
 * View holder for list item which holds references
 * to the views so its like uber quick
 */
public class MessageHolder implements ViewHolder
{
	@InjectView(R.id.title) public TextView title;
	@InjectView(R.id.sub_title) public TextView subTitle;
	@InjectView(R.id.time) public TextView time;
	@InjectView(R.id.text) public LinkifiedTextView text;
	@InjectView(R.id.avatar) public AvatarView avatar;
	@InjectView(R.id.video_play_button) public ImageView videoMediaButton;
	@InjectView(R.id.reply) public ImageView replyButton;
	@InjectView(R.id.reply_all) public ImageView replyAllButton;
	@InjectView(R.id.share) public ImageView shareButton;
	@InjectView(R.id.options_container) public View optionsContainer;
	@InjectView(R.id.location_container) public View locationContainer;
	@InjectView(R.id.media_container) public View mediaContainer;
	@InjectView(R.id.progress) public ProgressBar mediaProgress;

	public MessageHolder(View convertView)
	{
		Views.inject(this, convertView);
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

	/**
	 * Method to use when populating a view's members with the
	 * data from post.
	 * @param holder The holder containing the views
	 * @param post The post object
	 */
	public void populate(Message post, RobinAdapter adapter)
	{
		title.setText(post.getPoster().getFormattedMentionName()[0]);
		subTitle.setText(post.getPoster().getFormattedMentionName()[1]);
		text.setText(post.getFormattedText());

		if (SettingsManager.getSingleClickLinks() > 0)
		{
			String s = Integer.toBinaryString(SettingsManager.getSingleClickLinks());
			Class[] c = new Class[s.length()];

			int index = 0;
			if (!SettingsManager.isSingleClickHashtagEnabled())
			{
				c[index++] = HashtagClickableSpan.class;
			}

			if (!SettingsManager.isSingleClickMentionEnabled())
			{
				c[index++] = MentionClickableSpan.class;
			}

			if (!SettingsManager.isSingleClickUrlEnabled())
			{
				c[index++] = UrlClickableSpan.class;
			}

			text.setLinkMovementMethod(new LinkTouchMovementMethod(c));
		}
		else
		{
			text.setLinkMovementMethod(null);
		}

		time.setText(post.getDateStr());

		if (SettingsManager.isCustomFontsEnabled())
		{
			title.setTypeface(Typeface.defaultFromStyle(0));
			subTitle.setTypeface(Typeface.defaultFromStyle(0));
			text.setTypeface(Typeface.defaultFromStyle(0));
		}

		if (SettingsManager.getShowAvatars())
		{
			ImageLoader.getInstance().cancelDisplayTask(avatar);

			avatar.setVisibility(View.VISIBLE);
			avatar.setContentDescription(post.getPoster().getFormattedMentionName()[0]);

			if (post.getPoster().isAvatarDefault())
			{
				avatar.setImageResource(R.drawable.default_avatar);
			}
			else
			{
				ImageLoader.getInstance().displayImage(post.getPoster().getAvatarUrl() + "?avatar=1&id=" + post.getPoster().getId(), avatar, MainApplication.getAvatarImageOptions());
			}
		}
		else
		{
			avatar.setContentDescription("");
			avatar.setVisibility(View.GONE);
		}
	}
}