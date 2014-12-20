package in.rob.client.page;

import in.lib.Constants;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.handler.streams.UserFriendsResponseHandler;
import in.lib.helper.ResponseHelper;
import in.lib.helper.ThemeHelper;
import in.lib.manager.APIManager;
import in.lib.utils.Views;
import in.model.User;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.page.base.UserStreamFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * TODO: Add cache loading in loadUserList
 */
public class UserFriendsPage extends UserStreamFragment implements OnClickListener
{
	public static enum Mode
	{
		FOLLOWING("following"),
		FOLLOWERS("followers");

		private final String mModeText;
		private Mode(String modeText)
		{
			this.mModeText = modeText;
		}

		public String getModeText()
		{
			return mModeText;
		}
	}

	private Mode mMode = Mode.FOLLOWERS;
	private User mUser;

	// views
	@OnClick @InjectView(R.id.followers_button) public View mFollowersBtn;
	@OnClick @InjectView(R.id.following_button) public View mFollowingBtn;
	@OnClick @InjectView(R.id.mode_switch) public ImageView mModeSwitch;
	@InjectView(R.id.followers_text) public TextView mFollowersTextTv;
	@InjectView(R.id.following_text) public TextView mFollowingTextTv;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.split_timeline_view, container, false);
		Views.inject(this, v);
		return v;
	}

	@Override public void retrieveArguments(Bundle arguments)
	{
		super.retrieveArguments(arguments);

		if (arguments != null)
		{
			if (arguments.containsKey(Constants.EXTRA_USER))
			{
				mUser = (User)arguments.getParcelable(Constants.EXTRA_USER);
			}
			else if (arguments.containsKey(Constants.EXTRA_USER_ID))
			{
				mUser = User.loadUser(arguments.getString(Constants.EXTRA_USER_ID));

				if (mUser == null)
				{
					mUser = new User();
					mUser.setId(getArguments().getString(Constants.EXTRA_USER_ID));
				}
			}
		}
	}

	@Override public void fetchStream(String lastId, boolean append)
	{
		UserFriendsResponseHandler response = new UserFriendsResponseHandler(getApplicationContext(), append);
		ResponseHelper.getInstance().addResponse(getResponseKeys()[0], response, this);

		if (mMode == Mode.FOLLOWERS)
		{
			response.setFailMessage(getString(R.string.followers_stream_fail));
			APIManager.getInstance().getUserFollowers(mUser.getId(), lastId, response);
		}
		else
		{
			response.setFailMessage(getString(R.string.followings_stream_fail));
			APIManager.getInstance().getUserFollowing(mUser.getId(), lastId, response);
		}
	}

	@Override public void removeLoadMoreView()
	{
		getLoadMoreView().setVisibility(View.GONE);
	}

	@Override public void onClick(View v)
	{
		if (v.getId() == R.id.mode_switch)
		{
			if (mMode == Mode.FOLLOWERS)
			{
				v = mFollowingBtn;
			}
			else
			{
				v = mFollowersBtn;
			}
		}

		int selectedColor = ThemeHelper.getColorResource(getContext(), R.attr.rbn_post_link_color);
		int deselectedColor = ThemeHelper.getColorResource(getContext(), R.attr.rbn_sub_title);

		if (v.getId() == R.id.followers_button && mMode != Mode.FOLLOWERS)
		{
			if ((getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE &&
				(getContext().getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE
			)
			{
				((ProfileActivity)getActivity()).setTitle2(getString(R.string.followers));
			}
			else
			{
				getActivity().setTitle(getString(R.string.followers));
			}

			mMode = Mode.FOLLOWERS;
			mFollowersTextTv.setTextColor(selectedColor);
			mFollowingTextTv.setTextColor(deselectedColor);
			mModeSwitch.setImageResource(R.drawable.switch_left);
		}
		else if (v.getId() == R.id.following_button && mMode != Mode.FOLLOWING)
		{
			if ((getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE &&
				(getContext().getResources().getConfiguration().orientation & Configuration.ORIENTATION_LANDSCAPE) == Configuration.ORIENTATION_LANDSCAPE
			)
			{
				((ProfileActivity)getActivity()).setTitle2(getString(R.string.following));
			}
			else
			{
				getActivity().setTitle(getString(R.string.following));
			}

			mMode = Mode.FOLLOWING;

			mFollowingTextTv.setTextColor(selectedColor);
			mFollowersTextTv.setTextColor(deselectedColor);
			mModeSwitch.setImageResource(R.drawable.switch_right);
		}

		showProgressLoader();
		getLoadMoreView().setVisibility(View.VISIBLE);
		getAdapter().clear();
		getAdapter().notifyDataSetChanged();
		initData();
	}

	@Override public String getCacheFileName()
	{
		return String.format(Constants.CACHE_USER_LIST_NAME, mMode.getModeText(), mUser.getId());
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{String.format(Constants.RESPONSE_FRIENDS, mUser.getId())};
	}
}