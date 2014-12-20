package in.rob.client.dialog;

import in.lib.Constants;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.handler.UserFollowResponseHandler;
import in.lib.handler.UserUnFollowResponseHandler;
import in.lib.helper.ThemeHelper;
import in.lib.manager.APIManager;
import in.lib.manager.UserManager;
import in.lib.utils.Dimension;
import in.lib.utils.Views;
import in.lib.view.LinkTouchMovementMethod;
import in.lib.view.LinkifiedTextView;
import in.model.SimpleUser;
import in.model.User;
import in.rob.client.MainApplication;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.base.RobinDialogActivity;
import in.rob.client.dialog.base.DialogBuilder;

import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class PopupProfileDialog extends RobinDialogActivity implements OnClickListener
{
	private User user;
	@InjectView(R.id.avatar_image) public ImageView avatar;
	@InjectView(R.id.cover_image) public ImageView cover;
	@InjectView(R.id.bio_text) public LinkifiedTextView bio;
	@InjectView(R.id.followers) public TextView followers;
	@InjectView(R.id.mention_name) public TextView mentionName;
	@InjectView(R.id.username) public TextView userName;
	@OnClick @InjectView(R.id.icon_follow) public ImageButton follow;
	@OnClick @InjectView(R.id.icon_mention) public ImageButton mention;
	@OnClick @InjectView(R.id.icon_message) public ImageButton message;
	@OnClick @InjectView(R.id.icon_more) public ImageButton more;
	@OnClick @InjectView(R.id.icon_profile) public ImageButton profile;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.popup_user_dialog);
		Views.inject(this);

		Dimension d = new Dimension(this);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
		else
		{
			getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		}

		getWindow().setGravity(Gravity.CENTER);
		getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_bg));

		user = (User)getIntent().getExtras().getParcelable(Constants.EXTRA_USER);

		String bioText = user.getFormattedDescription();
		bio.setLinkMovementMethod(new LinkTouchMovementMethod());
		bio.setText(bioText);

		if (TextUtils.isEmpty(bioText))
		{
			((View)bio.getParent()).setVisibility(View.GONE);
		}

		userName.setText(user.getUserName());
		mentionName.setText("@" + user.getMentionName());

		if (!user.isAvatarDefault())
		{
			ImageLoader.getInstance().displayImage(user.getAvatarUrl() + "?avatar=1&id=" + user.getId(), avatar, MainApplication.getAvatarImageOptions());
		}

		if (!user.isCoverDefault())
		{
			DisplayImageOptions opts = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.showStubImage(R.drawable.default_cover)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.resetViewBeforeLoading(true)
				.showImageForEmptyUri(R.drawable.default_cover)
			.build();

			ImageLoader.getInstance().displayImage(user.getCoverUrl(), cover, opts, new ImageLoadingListener()
			{
				@Override public void onLoadingStarted(String imageUri, View view){}
				@Override public void onLoadingFailed(String imageUri, View view, FailReason failReason){}
				@Override public void onLoadingCancelled(String imageUri, View view){}

				@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
				{
					((ImageView)view).setScaleType(ScaleType.CENTER_CROP);
				}
			});
		}

		followers.setText(getString(R.string.follows_following, user.getFollowingCount(), user.getFollowersCount()));

		if (user.isYou())
		{
			follow.setVisibility(View.INVISIBLE);
			mention.setVisibility(View.INVISIBLE);
			message.setVisibility(View.INVISIBLE);
			more.setVisibility(View.INVISIBLE);
		}
		else
		{
			if (user.getYouFollow())
			{
				follow.setContentDescription(getString(R.string.unfollow));
				follow.setImageResource(ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_popup_profile_icon_unfollow));
			}
		}
	}

	@Override public void onClick(View v)
	{
		if (v == message)
		{
			ArrayList<SimpleUser> recipients = new ArrayList<SimpleUser>();
			recipients.add(SimpleUser.parseFromUser(UserManager.getUser()));
			recipients.add(SimpleUser.parseFromUser(user));
			Intent messageActivity = new Intent(getContext(), NewChannelDialog.class);
			messageActivity.putExtra(Constants.EXTRA_USER_LIST, recipients);
			getContext().startActivity(messageActivity);
		}
		else if (v == mention)
		{
			Intent inReply = new Intent(getContext(), NewPostDialog.class);
			inReply.putExtra(Constants.EXTRA_MENTION_NAME, user.getMentionName());
			getContext().startActivity(inReply);
		}
		else if (v == follow)
		{
			if (user.getYouFollow())
			{
				user.setYouFollow(false);
				follow.setContentDescription(getString(R.string.follow));
				follow.setImageResource(ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_popup_profile_icon_follow));

				APIManager.getInstance().unfollowUser(user.getId(), new UserUnFollowResponseHandler()
				{
					@Override public void onFinish(boolean failed)
					{
						super.onFinish(failed);

						if (failed)
						{
							Context c = PopupProfileDialog.this.getContext();
							user.setYouFollow(true);
							follow.setContentDescription(getString(R.string.unfollow));
							follow.setImageResource(ThemeHelper.getDrawableResource(c, R.attr.rbn_popup_profile_icon_unfollow));

							if (c != null)
							{
								String message = c.getString(R.string.unfollow_failed) + " @" + user.getMentionName();
								Toast.makeText(c, message, Toast.LENGTH_LONG).show();
							}
						}
					}
				});
			}
			else
			{
				user.setYouFollow(true);
				follow.setContentDescription(getString(R.string.unfollow));
				follow.setImageResource(ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_popup_profile_icon_unfollow));

				APIManager.getInstance().followUser(user.getId(), new UserFollowResponseHandler()
				{
					@Override public void onFinish(boolean failed)
					{
						super.onFinish(failed);

						if (failed)
						{
							Context c = PopupProfileDialog.this.getContext();
							user.setYouFollow(false);
							follow.setContentDescription(getString(R.string.follow));
							follow.setImageResource(ThemeHelper.getDrawableResource(c, R.attr.rbn_popup_profile_icon_follow));

							if (c != null)
							{
								String message = c.getString(R.string.follow_failed) + " @" + user.getMentionName();
								if (getConnectionInfo().responseCode == 507)
								{
									message = c.getString(R.string.too_many_follow);
								}

								Toast.makeText(c, message, Toast.LENGTH_LONG).show();
							}
						}
					}
				});
			}

			user.save();
		}
		else if (v == more)
		{
			ArrayList<String> options = new ArrayList<String>();
			options.add(user.isMuted() ? getString(R.string.unmute) : getString(R.string.mute));
			options.add(user.isBlocked() ? getString(R.string.unblock) : getString(R.string.block));
			//options.add(getString(R.string.report));

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				final PopupMenu moreoptions;
				moreoptions = new PopupMenu(getContext(), more);

				for (int index = 0; index < options.size(); index++)
				{
					moreoptions.getMenu().add(0, index, 0, options.get(index));
				}

				moreoptions.setOnMenuItemClickListener(new OnMenuItemClickListener()
				{
					@Override public boolean onMenuItemClick(MenuItem item)
					{
						int index = item.getItemId();
						handleUserOption(index);
						moreoptions.dismiss();
						return true;
					}
				});
				moreoptions.show();
			}
			else
			{
				DialogBuilder.create(getContext())
					.setTitle(R.string.pick_option)
					.setItems(options.toArray(new String[options.size()]), new DialogInterface.OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							handleUserOption(which);
							dialog.dismiss();
						}
					})
				.show();
			}

			return;
		}
		else if (v == profile)
		{
			Intent intent = new Intent(getContext(), ProfileActivity.class);
			intent.putExtra(Constants.EXTRA_USER, user);
			getContext().startActivity(intent);
		}

		//finish();
	}

	public void handleUserOption(int index)
	{
		if (index == 0)
		{
			if (user.isMuted())
			{
				unmute();
			}
			else
			{
				mute();
			}
		}
		else if (index == 1)
		{
			if (user.isBlocked())
			{
				unblock();
			}
			else
			{
				block();
			}
		}
		else if (index == 2)
		{
			report();
		}

		user.save();
	}

	public void unmute()
	{
		Toast.makeText(getContext(), R.string.user_unmuted, Toast.LENGTH_SHORT).show();

		user.setMuted(false);
		APIManager.getInstance().unMuteUser(user.getId(), null);
	}

	public void mute()
	{
		Toast.makeText(getContext(), R.string.user_muted, Toast.LENGTH_SHORT).show();

		user.setMuted(true);
		APIManager.getInstance().muteUser(user.getId(), null);
	}

	public void unblock()
	{
		Toast.makeText(getContext(), R.string.user_unblocked, Toast.LENGTH_SHORT).show();

		user.setBlocked(false);
		APIManager.getInstance().unblockUser(user.getId(), null);
	}

	public void block()
	{
		Toast.makeText(getContext(), R.string.user_blocked, Toast.LENGTH_SHORT).show();

		user.setBlocked(true);
		APIManager.getInstance().blockUser(user.getId(), null);
	}

	public void report()
	{

	}
}