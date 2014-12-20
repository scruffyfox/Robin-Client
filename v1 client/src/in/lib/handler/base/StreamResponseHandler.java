package in.lib.handler.base;

import in.lib.Debug;
import in.lib.exception.ExceptionHandler;
import in.lib.thread.FragmentRunnable;
import in.lib.thread.StreamFragmentRunnable;
import in.model.base.NetObject;
import in.obj.StreamMarker;
import in.rob.client.R;
import in.rob.client.page.base.StreamFragment;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

public abstract class StreamResponseHandler<T extends StreamFragment> extends RobinResponseHandler<T>
{
	@Getter @Setter private List<NetObject> objects;
	@Getter @Setter private StreamMarker marker;
	@Getter @Setter private Boolean append = false;
	@Getter @Setter private Boolean hasMore = false;
	@Getter @Setter private Boolean didFail = false;
	@Getter @Setter private String lastId = "";
	@Getter @Setter private String firstId = "";
	@Getter @Setter private String failMessage = "";

	public StreamResponseHandler(Context c, boolean append)
	{
		super(c);
		this.append = append;
		setFailMessage(c.getString(R.string.stream_fail));
	}

	@Override public void onSend()
	{
		super.onSend();

		if (getFragment() != null)
		{
			getFragment().setLoading(true);
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
	 * Implement this callback and use {@link #getChannels()} to get the returned List of posts
	 * For fragments, use onCallback to execute {@link RobinFragment.runWhenReady()} and pass <b>this</b>
	 * Then override {@link #run()} to finish the adapter stuff.
	 *
	 * See also: {@link #getHasMore()}, {@link #getLastId()}
	 */
	public abstract void onCallback();

	protected StreamFragmentRunnable responseRunner = new StreamFragmentRunnable()
	{
		@Override public void run()
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
					getFragment().getAdapter().setFirstId(getFirstId());
					getFragment().getAdapter().setItems(getObjects());
				}

				getFragment().getAdapter().setHasMore(getHasMore());
				getFragment().getAdapter().setStreamMarker(getMarker());
				setFinishedLoading(getAppend());
			}
			catch (Exception e)
			{
				ExceptionHandler.sendException(e);
			}

			super.run();
		}
	};
}