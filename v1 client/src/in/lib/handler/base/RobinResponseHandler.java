package in.lib.handler.base;

import in.lib.manager.UserManager;
import in.rob.client.AuthenticateActivity;

import java.lang.ref.WeakReference;

import lombok.Getter;
import lombok.Setter;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

public abstract class RobinResponseHandler<T extends Fragment> extends ResponseHandler
{
	protected WeakReference<T> fragment;
	@Getter private Context context;
	@Getter @Setter private String responseKey = "";

	public RobinResponseHandler(Context c)
	{
		this.context = c;
	}

	public T getFragment()
	{
		if (fragment == null)
		{
			return null;
		}

		return fragment.get();
	}

	public void attach(T f)
	{
		this.fragment = new WeakReference<T>(f);
	}

	/**
	 * This should only be called when the response handler is finished
	 */
	public void detach()
	{
		this.context = null;
		this.fragment = null;
	}

	@Override public void onFinish()
	{
		super.onFinish();

		if (getConnectionInfo().responseCode == 401 && context != null && !TextUtils.isEmpty(UserManager.getAccessToken()))
		{
			Intent auth = new Intent(context, AuthenticateActivity.class);
			UserManager.logout(context);
			auth.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			auth.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			auth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(auth);
			return;
		}
	}
}