package in.lib.handler;

import in.lib.event.UnStarPostEvent;
import in.lib.handler.base.PostResponseHandler;
import in.lib.helper.BusHelper;

public class PostUnStarResponseHandler extends PostResponseHandler
{
	private boolean failed = true;

	public PostUnStarResponseHandler()
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
			BusHelper.getInstance().post(new UnStarPostEvent(getPost()));
		}
	}
}