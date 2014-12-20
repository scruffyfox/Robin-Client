package in.lib.handler;

import in.lib.event.FollowUserEvent;
import in.lib.handler.base.UserResponseHandler;
import in.lib.helper.BusHelper;

public class UserUnFollowResponseHandler extends UserResponseHandler
{
	private boolean failed = true;

	public UserUnFollowResponseHandler()
	{
		super(null);
	}

	@Override public void onCallback()
	{
		failed = false;
	}

	@Override public void onFinish(boolean failed)
	{
		if (!this.failed)
		{
			BusHelper.getInstance().post(new FollowUserEvent(getUser()));
		}
	}
}