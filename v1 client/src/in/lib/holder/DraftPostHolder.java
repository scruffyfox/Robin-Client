package in.lib.holder;

import in.lib.adapter.DraftAdapter;
import in.lib.annotation.InjectView;
import in.lib.holder.base.ViewHolder;
import in.lib.manager.SettingsManager;
import in.lib.utils.Views;
import in.lib.view.AvatarView;
import in.lib.view.LinkifiedTextView;
import in.model.DraftPost;
import in.model.User;
import in.obj.entity.Entity.Type;
import in.obj.entity.LinkEntity;
import in.rob.client.MainApplication;
import in.rob.client.R;

import java.util.Date;

import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * View holder for list item which holds references
 * to the views so its like uber quick
 * @author Robin
 */
public class DraftPostHolder implements ViewHolder
{
	@InjectView(R.id.title) public TextView title;
	@InjectView(R.id.sub_title) public TextView subTitle;
	@InjectView(R.id.time) public TextView time;
	@InjectView(R.id.text) public LinkifiedTextView text;
	@InjectView(R.id.avatar) public AvatarView avatar;
	@InjectView(R.id.options_container) public View optionsContainer;
	@InjectView(R.id.media_image) public ImageView media;
	@InjectView(R.id.send) public ImageView sendButton;
	@InjectView(R.id.edit) public ImageView editButton;
	@InjectView(R.id.duplicate) public ImageView duplicateButton;
	@InjectView(R.id.delete) public ImageView deleteButton;

	public DraftPostHolder(View convertView)
	{
		Views.inject(this, convertView);
	}

	@Override public void onViewDestroyed(View v)
	{
		avatar.setImageBitmap(null);
		ImageLoader.getInstance().cancelDisplayTask(avatar);

		media.setImageBitmap(null);
		ImageLoader.getInstance().cancelDisplayTask(media);
	}

	/**
	 * Method to use when populating a view's members with the
	 * data from post.
	 * @param holder The holder containing the views
	 * @param post The post object
	 */
	public void populate(DraftPost post, DraftAdapter adapter)
	{
		((View)media.getParent()).setVisibility(View.GONE);

		User poster = User.loadUser(post.getSelectedAccountId());
		String postText = post.getPostText();

		if (post.getEntities().get(Type.LINK) != null && postText.length() > 0)
		{
			int count = post.getEntities().get(Type.LINK).size();
			for (int index = count - 1; index > -1; index--)
			{
				LinkEntity entity = (LinkEntity)post.getEntities().get(Type.LINK).get(index);

				String start = postText.substring(0, entity.getPos());
				String linkText = postText.substring(entity.getPos(), entity.getPos() + entity.getLen());
				String end = "";

				if (entity.getPos() + entity.getLen() < postText.length())
				{
					end = postText.substring(entity.getPos() + entity.getLen(), postText.length());
				}

				postText = start + "<md href=\"" + entity.getUrl() + "\" data-anchor=\"" + linkText + "\">" + linkText + "</md>" + end;
			}
		}

		title.setText(poster.getFormattedMentionName()[0]);
		subTitle.setText(poster.getFormattedMentionName()[1]);
		text.setText(postText);
		text.setLinkMovementMethod(null);
		time.setText(SettingsManager.getDateFormat().format(new Date(post.getDate())));

		if (SettingsManager.isCustomFontsEnabled())
		{
			title.setTypeface(Typeface.defaultFromStyle(0));
			subTitle.setTypeface(Typeface.defaultFromStyle(0));
			text.setTypeface(Typeface.defaultFromStyle(0));
		}

		if (SettingsManager.getShowAvatars())
		{
			avatar.setVisibility(View.VISIBLE);
			avatar.setContentDescription(poster.getFormattedMentionName()[0]);
			avatar.setImageBitmap(null);
			ImageLoader.getInstance().cancelDisplayTask(avatar);

			if (poster.isAvatarDefault())
			{
				avatar.setImageResource(R.drawable.default_avatar);
			}
			else
			{
				ImageLoader.getInstance().displayImage(poster.getAvatarUrl() + "?avatar=1&id=" + poster.getId(), avatar, MainApplication.getAvatarImageOptions());
			}
		}
		else
		{
			avatar.setContentDescription("");
			avatar.setVisibility(View.GONE);
		}

		boolean block = SettingsManager.isInlineImagesEnabled();
		block = block && (!SettingsManager.isInlineImageWifiEnabled() || (SettingsManager.isInlineImageWifiEnabled() && MainApplication.isOnWifi()));
		block = block && !TextUtils.isEmpty(post.getImagePath());

		if (block)
		{
			String imageToLoad = post.getImagePath();
			ImageLoader.getInstance().displayImage(imageToLoad, media, MainApplication.getMediaImageOptions());
			((View)media.getParent()).setVisibility(View.VISIBLE);
		}
	}
}