package in.lib.handler.base;

import in.lib.Debug;
import in.lib.thread.FragmentRunnable;
import in.model.User;
import in.rob.client.page.base.StreamFragment;
import lombok.Getter;
import lombok.Setter;
import android.content.Context;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Standard response handler for individual user
 *
 * Use {@link #getUser()} in {@link #onCallback()} to get the returned user
 */
public abstract class UserResponseHandler<T extends StreamFragment> extends RobinResponseHandler<T>
{
	@Getter @Setter private User user;
	@Getter @Setter private String failMessage = "";
	@Getter @Setter private Boolean didFail = false;

	public UserResponseHandler(Context c)
	{
		super(c);
	}

	@Override public void onSend()
	{
		super.onSend();

		if (getFragment() != null)
		{
			getFragment().setLoading(true);
		}
	}

	@Override public void onSuccess()
	{
		JsonElement elements = getContent();

		if (elements != null)
		{
			try
			{
				JsonObject jUser = elements.getAsJsonObject().get("data").getAsJsonObject();
				user = new User().createFrom(jUser);

				if (user == null) return;
				onCallback();
			}
			catch (Exception e)
			{
				Debug.out(e);
				setDidFail(true);
			}
		}
		else
		{
			setDidFail(true);
		}
	}

	@Override public void onFinish(boolean failed)
	{
		if (failed || didFail)
		{
			Debug.out(getConnectionInfo());

			if (getFragment() != null)
			{
				getFragment().runWhenReady(new FragmentRunnable<StreamFragment>()
				{
					@Override public void run()
					{
						getFragment().getRefreshHelper().finish();
						getFragment().setLoading(false);
						Toast.makeText(getFragment().getContext(), getFailMessage(), Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
	}

	/**
	 * Implement this callback and use {@link #getUser()} to get the returned List of posts
	 * For fragments, use onCallback to execute {@link RobinFragment.runWhenReady()} and pass <b>this</b>
	 * Then override {@link #run()} to finish the adapter stuff.
	 */
	public abstract void onCallback();
}