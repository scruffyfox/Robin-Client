package in.lib.handler;

import in.lib.event.UnFollowUserEvent;
import in.lib.handler.base.UserResponseHandler;
import in.lib.helper.BusHelper;

public class UserFollowResponseHandler extends UserResponseHandler
{
	private boolean failed = true;

	public UserFollowResponseHandler()
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
			BusHelper.getInstance().post(new UnFollowUserEvent(getUser()));
		}
	}
}