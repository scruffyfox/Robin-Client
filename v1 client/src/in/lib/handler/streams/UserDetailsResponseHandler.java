package in.lib.handler.streams;

import in.lib.Debug;
import in.lib.adapter.ProfilePostAdapter;
import in.lib.handler.base.UserResponseHandler;
import in.lib.manager.UserManager;
import in.lib.thread.FragmentRunnable;
import in.rob.client.R;
import in.rob.client.page.ProfilePage;
import android.content.Context;
import android.widget.Toast;

public class UserDetailsResponseHandler extends UserResponseHandler<ProfilePage>
{
	public UserDetailsResponseHandler(Context c)
	{
		super(c);
		setFailMessage(c.getString(R.string.user_fail));
	}

	@Override public void onCallback()
	{
		if (getFragment() != null)
		{
			getFragment().runOnUiThread(responseRunner);
		}

		if (getFragment() != null)
		{
			getFragment().extractUser();
		}
	}

	private FragmentRunnable<ProfilePage> responseRunner = new FragmentRunnable<ProfilePage>()
	{
		@Override public void run()
		{
			if (getFragment() != null)
			{
				try
				{
					if (getUser() == null)
					{
						getFragment().getActivity().finish();
						Toast.makeText(getContext(), R.string.user_fail, Toast.LENGTH_LONG).show();
						return;
					}

					if (getFragment().getMFollowBtn() != null)
					{
						if (getFragment().getMFollowBtn().getTag(R.id.TAG_IS_LOADING) != null && (Boolean)getFragment().getMFollowBtn().getTag(R.id.TAG_IS_LOADING))
						{
							getUser().setYouFollow(getFragment().getUser().getYouFollow());
						}
					}

					getFragment().setUser(getUser());
					getFragment().getUser().save();

					if (getUser().isYou())
					{
						UserManager.setUser(getUser(), getContext());
					}

					((ProfilePostAdapter)getFragment().getAdapter()).setUserId(getUser().getId());
					getFragment().loadUserDetails();
				}
				catch (Exception e)
				{
					Debug.out(e);
				}
			}

			super.run();
		}
	};
}