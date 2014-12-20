package in.lib.handler;

import in.lib.event.StarPostEvent;
import in.lib.handler.base.PostResponseHandler;
import in.lib.helper.BusHelper;

public class PostStarResponseHandler extends PostResponseHandler
{
	private boolean failed = true;

	public PostStarResponseHandler()
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
			BusHelper.getInstance().post(new StarPostEvent(getPost()));
		}
	}
}