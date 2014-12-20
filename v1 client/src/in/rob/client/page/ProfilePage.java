package in.rob.client.page;

import in.lib.Constants;
import in.lib.URLMatcher;
import in.lib.adapter.ProfilePostAdapter;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.event.DeletePostEvent;
import in.lib.event.NewPostEvent;
import in.lib.event.ProfileUpdatedEvent;
import in.lib.handler.UserFollowResponseHandler;
import in.lib.handler.UserUnFollowResponseHandler;
import in.lib.handler.streams.ProfilePostsResponseHandler;
import in.lib.handler.streams.UserDetailsResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.helper.ThemeHelper;
import in.lib.manager.APIManager;
import in.lib.manager.SettingsManager;
import in.lib.utils.CodeUtils;
import in.lib.utils.Views;
import in.lib.view.AvatarView;
import in.lib.view.LinkTouchMovementMethod;
import in.lib.view.LinkifiedTextView;
import in.model.Post;
import in.model.SimpleUser;
import in.model.User;
import in.rob.client.MainApplication;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.SettingsActivity;
import in.rob.client.dialog.AvatarViewDialog;
import in.rob.client.page.base.PostStreamFragment;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

/**
 * Page used to display a user profile page Extends PostStreamFragment for basic
 * stuff like onSaveInstanceState but implements its own initData(Bundle arguments)
 */
public class ProfilePage extends PostStreamFragment implements OnClickListener, OnLongClickListener
{
	// arguments
	@Getter private User user;
	private boolean userLoaded = false;

	// views
	@OnClick @Getter @InjectView(R.id.follow_button) public Button mFollowBtn;
	@OnClick @InjectView(R.id.edit_button) public Button mEditBtn;
	@OnClick @InjectView(R.id.avatar) public AvatarView mAvatar;
	@InjectView(R.id.verification) public TextView mVerification;
	@InjectView(R.id.follow_count) public TextView mFollowCountTv;
	@InjectView(R.id.follows_you) public TextView mFollowsYouTv;
	@InjectView(R.id.username) public TextView mUsernameTv;
	@InjectView(R.id.bio_text) public LinkifiedTextView mBioTextTv;
	@InjectView(R.id.details_container) public View mDetailsContainer;

	@Override public void retrieveArguments(Bundle arguments)
	{
		// get arguments
		if (arguments != null && getUser() == null)
		{
			if (arguments.containsKey(Constants.EXTRA_USER))
			{
				setUser((User)arguments.getParcelable(Constants.EXTRA_USER));
				userLoaded = true;
			}
			else if (arguments.containsKey(Constants.EXTRA_USER_ID))
			{
				setUser(new User());
				getUser().setId(arguments.getString(Constants.EXTRA_USER_ID));
			}
			else if (arguments.containsKey(Constants.EXTRA_USER_NAME))
			{
				setUser(new User());
				getUser().setId(arguments.getString(Constants.EXTRA_USER_NAME));
			}
			else
			{
				getActivity().finish();
			}
		}
	}

	public void setUser(User user)
	{
		this.user = user;

		if (!user.getId().equals("-1"))
		{
			userLoaded = true;
		}
	}

	@Override public void setupAdapters()
	{
		if (getAdapter() == null)
		{
			setAdapter(new ProfilePostAdapter(getContext(), R.layout.post_list_item, new ArrayList<Post>(), getUser().getId()));
		}
		else
		{
			setAdapter(getAdapter());
		}
	}

	@Override public void onViewCreated(View view, Bundle arguments)
	{
		super.onViewCreated(view, arguments);

		getHeadedListView().setHeaderView(R.layout.profile_header_stub);

		Views.inject(this, getHeadedListView());

		mBioTextTv.setLinkMovementMethod(new LinkTouchMovementMethod());
		mAvatar.setOnLongClickListener(this);

		if (!userLoaded)
		{
			refreshUserDetails();
		}
		else
		{
			loadUserDetails();
		}
	}

	@Override public void onResume()
	{
		super.onResume();

		if (userLoaded)
		{
			if (SettingsManager.getCacheTimeout() > 0 && getCacheManager().fileOlderThan(String.format(Constants.CACHE_USER, getUser().getId()), System.currentTimeMillis() - SettingsManager.getCacheTimeout()))
			{
				refreshUserDetails();
			}
		}
	}

	public void extractUser()
	{
		List<SimpleUser> users = getCacheManager().readFileAsObject(Constants.CACHE_USERNAMES, new ArrayList<SimpleUser>());
		List<String> usersStr = getCacheManager().readFileAsObject(Constants.CACHE_USERNAMES_STR, new ArrayList<String>());

		if (getUser() != null && !usersStr.contains(getUser().getId()))
		{
			users.add(SimpleUser.parseFromUser(getUser()));
			usersStr.add(getUser().getId());
		}

		getCacheManager().asyncWriteFile(Constants.CACHE_USERNAMES, users);
		getCacheManager().asyncWriteFile(Constants.CACHE_USERNAMES_STR, usersStr);

		users.clear();
		usersStr.clear();
	}

	/**
	 * Loads the users details from the id. If it exists in cache, we'll use
	 * that if not, we'll use getUser() (if its not null)
	 */
	public void getUserDetails()
	{
		User user = User.loadUser(getUser().getId());

		if (user != null)
		{
			setUser(user);
			extractUser();
			userLoaded = true;
			loadUserDetails();
		}
		else
		{
			refreshUserDetails();
		}
	}

	@Override public void onDestroy()
	{
		super.onDestroy();
		getUser().save();
	}

	@Override public void onRefresh()
	{
		super.onRefresh();

		if (((MainApplication)getApplicationContext()).isConnected())
		{
			refreshUserDetails();
		}
	}

	/**
	 * Fetch user details from an API call
	 * @param userId
	 */
	public void refreshUserDetails()
	{
		UserDetailsResponseHandler handler = new UserDetailsResponseHandler(getApplicationContext());
		ResponseHelper.getInstance().addResponse(String.format(Constants.RESPONSE_PROFILE_USER, user.getId()), handler, this);
		APIManager.getInstance().getUserDetails(getUser().getId(), handler);
	}

	/**
	 * Loads user related UI. Should only be called when getUser() != null
	 */
	public void loadUserDetails()
	{
		boolean landscape = false;

		try
		{
			landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		}
		catch (Exception e) {}

		if (!getUser().isYou())
		{
			mEditBtn.setVisibility(View.GONE);
			mFollowBtn.setVisibility(View.VISIBLE);

			if (mFollowBtn.getTag(R.id.TAG_IS_LOADING) == null || !(Boolean)mFollowBtn.getTag(R.id.TAG_IS_LOADING))
			{
				if (getUser().getYouFollow())
				{
					TypedValue typedValue = new TypedValue();
					getActivity().getTheme().resolveAttribute(R.attr.rbn_grey_button, typedValue, true);
					mFollowBtn.setBackgroundResource(typedValue.resourceId);
				}

				mFollowBtn.setText(getUser().getYouFollow() ? getString(R.string.unfollow) : getString(R.string.follow));
				mFollowsYouTv.setText(getUser().isFollowingYou() ? getString(R.string.follows_you) : getString(R.string.doesnt_follow_you));
			}
		}
		else
		{
			mFollowBtn.setVisibility(View.GONE);
			mEditBtn.setVisibility(View.VISIBLE);
		}

		if (mAvatar.getTag(R.id.TAG_IMAGE_URL) == null || !((String)mAvatar.getTag(R.id.TAG_IMAGE_URL)).equals(getUser().getAvatarUrl()))
		{
			if (getUser().isAvatarDefault())
			{
				mAvatar.setImageResource(R.drawable.default_avatar);
			}
			else
			{
				ImageLoader.getInstance().displayImage(getUser().getAvatarUrl() + "?avatar=1&id=" + getUser().getId(), mAvatar, MainApplication.getAvatarImageOptions());
			}

			mAvatar.setTag(R.id.TAG_IMAGE_URL, getUser().getAvatarUrl());
		}

		String desc = getUser().getFormattedDescription();
		mBioTextTv.setText(desc);

		String[] name = CodeUtils.nameOrderParse(SettingsManager.getNameDisplayOrder(), getUser());
		mUsernameTv.setText(name[0] + (TextUtils.isEmpty(name[1]) ? "" : "\n" + name[1]));
		mFollowCountTv.setText(Html.fromHtml(String.format(getString(R.string.profile_stats), "<b>" + getUser().getFollowingCount() + "</b>", (landscape ? " - " : "<br />") + "<b>" + getUser().getFollowersCount() + "</b>", "<br /><b>" + getUser().getStarredCount() + "</b>", "<br /><b>" + getUser().getPostCount() + "</b>")));

		if (!getUser().isCoverDefault())
		{
			setHeaderUrl(getUser().getCoverUrl());
		}

		if (!TextUtils.isEmpty(getUser().getVerifiedDomain()))
		{
			mVerification.setVisibility(View.VISIBLE);
			mVerification.setText(getString(R.string.verified, getUser().getVerifiedDomain()));
		}

		if (getActivity() instanceof ProfileActivity)
		{
			if (((ProfileActivity)getActivity()).getCurrentFragment() == this)
			{
				((ProfileActivity)getActivity()).setTitle("@" + getUser().getMentionName());
			}

			((ProfileActivity)getActivity()).setUser(getUser());
			getArguments().putString(Constants.EXTRA_TITLE, getString(R.string.at) + getUser().getMentionName());
			getActivity().supportInvalidateOptionsMenu();
		}
	}

	@Override public void fetchStream(String lastId, final boolean append)
	{
		ProfilePostsResponseHandler handler = new ProfilePostsResponseHandler(getApplicationContext(), append);
		handler.setFailMessage(getString(R.string.user_stream_fail, "@" + user.getMentionName()));
		ResponseHelper.getInstance().addResponse(String.format(Constants.RESPONSE_PROFILE_POSTS, user.getId()), handler, this);
		APIManager.getInstance().getUserPosts(getContext(), getUser().getId(), lastId, handler);
	}

	@Override public void onClick(View v)
	{
		if (v == mFollowBtn)
		{
			if (v.getTag(R.id.TAG_IS_LOADING) != null && (Boolean)v.getTag(R.id.TAG_IS_LOADING)) return;
			boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

			final int redButton = ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_red_button);
			final int greyButton = ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_grey_button);

			if (getUser().getYouFollow())
			{
				getUser().setYouFollow(false);
				getUser().setFollowersCount(getUser().getFollowersCount() - 1);
				mFollowBtn.setEnabled(false);
				mFollowBtn.setTag(R.id.TAG_IS_LOADING, true);
				mFollowBtn.setBackgroundResource(redButton);
				mFollowBtn.setText(getString(R.string.follow));
				mFollowCountTv.setText(Html.fromHtml(String.format(getString(R.string.profile_stats), "<b>" + getUser().getFollowingCount() + "</b>", (landscape ? " - " : "<br />") + "<b>" + getUser().getFollowersCount() + "</b>", "<br /><b>" + getUser().getStarredCount() + "</b>", "<br /><b>" + getUser().getPostCount() + "</b>")));

				APIManager.getInstance().unfollowUser(getUser().getId(), new UserUnFollowResponseHandler()
				{
					@Override public void onFinish(boolean failed)
					{
						super.onFinish(failed);

						mFollowBtn.setTag(R.id.TAG_IS_LOADING, false);
						mFollowBtn.setEnabled(true);

						if (ProfilePage.this.getUser() != null)
						{
							if (failed)
							{
								ProfilePage.this.getUser().setFollowersCount(ProfilePage.this.getUser().getFollowersCount() + 1);
								ProfilePage.this.getUser().setYouFollow(true);
								mFollowBtn.setText(R.string.unfollow);
								mFollowBtn.setBackgroundResource(greyButton);
								Toast.makeText(getContext(), getString(R.string.unfollow_failed_formatted, ProfilePage.this.getUser().getMentionName()), Toast.LENGTH_SHORT).show();
							}
							else
							{
								ProfilePage.this.getUser().setYouFollow(false);
							}
						}
					}
				});
			}
			else
			{
				getUser().setYouFollow(true);
				getUser().setFollowersCount(getUser().getFollowersCount() + 1);
				mFollowBtn.setEnabled(false);
				mFollowBtn.setTag(R.id.TAG_IS_LOADING, true);
				mFollowBtn.setBackgroundResource(greyButton);
				mFollowBtn.setText(getString(R.string.unfollow));
				mFollowCountTv.setText(Html.fromHtml(String.format(getString(R.string.profile_stats), "<b>" + getUser().getFollowingCount() + "</b>", (landscape ? " - " : "<br />") + "<b>" + getUser().getFollowersCount() + "</b>", "<br /><b>" + getUser().getStarredCount() + "</b>", "<br /><b>" + getUser().getPostCount() + "</b>")));

				APIManager.getInstance().followUser(getUser().getId(), new UserFollowResponseHandler()
				{
					@Override public void onFinish(boolean failed)
					{
						super.onFinish(failed);

						mFollowBtn.setTag(R.id.TAG_IS_LOADING, false);
						mFollowBtn.setEnabled(true);

						if (ProfilePage.this.getUser() != null)
						{
							if (failed)
							{
								Context c = ProfilePage.this.getContext();
								ProfilePage.this.getUser().setFollowersCount(ProfilePage.this.getUser().getFollowersCount() - 1);
								ProfilePage.this.getUser().setYouFollow(false);
								mFollowBtn.setText(R.string.follow);
								mFollowBtn.setBackgroundResource(redButton);

								if (c != null)
								{
									String message = c.getString(R.string.follow_failed) + " @" + ProfilePage.this.getUser().getMentionName();
									if (getConnectionInfo().responseCode == 507)
									{
										message = c.getString(R.string.too_many_follow);
									}

									Toast.makeText(c, message, Toast.LENGTH_LONG).show();
								}
							}
							else
							{
								ProfilePage.this.getUser().setYouFollow(true);
							}
						}
					}
				});
			}
		}
		else if (v == mAvatar)
		{
			Bundle args = new Bundle();
			args.putString(Constants.EXTRA_IMAGE, getUser().getAvatarUrl());

			FragmentManager fragmentManager = getFragmentManager();
			AvatarViewDialog lightboxFragment = new AvatarViewDialog();
			lightboxFragment.setArguments(args);
			lightboxFragment.show(fragmentManager, "dialog");
		}
		else if (v == mEditBtn)
		{
			Intent settings = new Intent(getContext(), SettingsActivity.class);
			settings.putExtra(Constants.EXTRA_START_PAGE, 0);
			startActivity(settings);
		}
		else if (v == mVerification)
		{
			String url = getUser().getVerifiedDomain();
			if (!getUser().getVerifiedDomain().startsWith("http"))
			{
				url = "http://" + url;
			}

			Intent view = new Intent(getContext(), URLMatcher.class);
			view.setData(Uri.parse(url));
			startActivity(view);
		}
	}


	@Subscribe public void onProfileUpdated(ProfileUpdatedEvent event)
	{
		if (event.getUser().getId().equals(user.getId()))
		{
			setHeaderUrl(event.getUser().getCoverUrl());
			ImageLoader.getInstance().displayImage(event.getUser().getAvatarUrl() + "?avatar=1&id=" + event.getUser().getId(), mAvatar, MainApplication.getAvatarImageOptions());
			mAvatar.setTag(R.id.TAG_IMAGE_URL, getUser().getAvatarUrl());
		}
	}

	@Subscribe @Override public void onPostRecieved(NewPostEvent event)
	{
		Post p = event.getPost();

		if (p != null)
		{
			if (p.getPoster().getId() == getUser().getId())
			{
				super.onPostRecieved(event);
			}
		}
	}

	@Subscribe @Override public void onPostDeleted(DeletePostEvent event)
	{
		super.onPostDeleted(event);
	}

	@Override public boolean onLongClick(View v)
	{
		if (v.getId() == R.id.avatar)
		{
			mAvatar.triggerLongPress(getUser());
			return true;
		}

		return false;
	}

	@Override public String getCacheFileName()
	{
		return String.format(Constants.CACHE_USER_TIMELINE_LIST_NAME, getUser().getId());
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]
		{
			String.format(Constants.RESPONSE_PROFILE_POSTS, user.getId()),
			String.format(Constants.RESPONSE_PROFILE_USER, this.user.getId())
		};
	}
}