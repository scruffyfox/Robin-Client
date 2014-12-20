 package in.lib.handler.streams;

import in.lib.Debug;
import in.lib.adapter.base.MessageAdapter;
import in.lib.exception.ExceptionHandler;
import in.lib.handler.base.MessageStreamResponseHandler;
import in.lib.thread.StreamFragmentRunnable;
import in.model.base.Message;
import in.rob.client.R;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

public class MessagesResponseHandler extends MessageStreamResponseHandler
{
	public MessagesResponseHandler(Context c, boolean append)
	{
		super(c, append);
		setFailMessage(c.getString(R.string.messages_stream_fail));
	}

	@Override public void onCallback()
	{
		if (getFragment() != null)
		{
			getFragment().runOnUiThread(responseRunner);
		}

		if (getFragment() != null)
		{
			getFragment().extractUsersAndTags(getObjects());
		}
	}

	@Override public void onFinish(boolean failed)
	{
		if (failed)
		{
			if (getConnectionInfo().responseCode == 403)
			{
				Toast.makeText(getContext(), R.string.unauthorized, Toast.LENGTH_LONG).show();
				getFragment().getActivity().finish();
			}
			else
			{
				Toast.makeText(getContext(), R.string.stream_fail, Toast.LENGTH_LONG).show();
			}

			if (getFragment() != null)
			{
				getFragment().setLoading(false);
				getFragment().getRefreshHelper().finish();
			}
		}
	}

	private StreamFragmentRunnable responseRunner = new StreamFragmentRunnable()
	{
		@Override public void run()
		{
			if (getFragment() != null)
			{
				try
				{
					getFragment().getRefreshHelper().finish();
					getFragment().getHeadedListView().setBlockLayoutChildren(true);

					if (!TextUtils.isEmpty(getLastId()))
					{
						getFragment().getAdapter().setLastId(getLastId());
					}

					if (getAppend())
					{
						getFragment().getAdapter().addItems(getObjects());
					}
					else
					{
						int[] pos = getFragment().getLastViewPosition(getObjects());
						getFragment().getAdapter().setFirstId(getFirstId());
						getFragment().getAdapter().setItems(getObjects());
						getFragment().registerPositionReset(pos[0], pos[1]);

						if (((MessageAdapter)getFragment().getAdapter()).getCenter() == null && getObjects().size() > 0)
						{
							((MessageAdapter)getFragment().getAdapter()).setCenter((Message)getObjects().get(0));
						}
					}

					getFragment().getAdapter().setHasMore(getHasMore());
					getFragment().getAdapter().setStreamMarker(getMarker());
					setFinishedLoading(getAppend());
				}
				catch (Exception e)
				{
					Debug.out(e);
					ExceptionHandler.sendException(e);
				}
			}

			super.run();
		}
	};
}