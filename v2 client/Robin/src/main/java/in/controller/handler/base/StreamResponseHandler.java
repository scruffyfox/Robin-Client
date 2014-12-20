package in.controller.handler.base;

import android.app.Fragment;

import in.data.stream.base.Stream;
import in.lib.utils.Debug;
import in.rob.client.fragment.base.StreamFragment;
import lombok.Getter;
import lombok.Setter;

public abstract class StreamResponseHandler<T extends Stream> extends ResponseHandler
{
	@Getter private boolean append;
	@Getter @Setter private T stream;

	public StreamResponseHandler(boolean append)
	{
		this.append = append;
	}

	@Override public void onSend()
	{
		super.onSend();
		getFragment().setLoading(true);
	}

	@Override public StreamFragment getFragment()
	{
		return ((StreamFragment)super.getFragment());
	}

	@Override public void onFinish(boolean failed)
	{
		if (getFragment() instanceof StreamResponseListener)
		{
			if (failed)
			{
				Debug.out("Response failed");
				Debug.out(getConnectionInfo());
				Debug.out(getContent());

				stream = null;
			}

			if (getFragment() != null)
			{
				getFragment().setLoading(false);
				((StreamResponseListener)getFragment()).handleResponse(stream, append);
				detachResponse();
			}
			else
			{
				Debug.out("Waiting for fragment to reattach");
				setOnFragmentAttachedListener(new OnFragmentAttachedListener()
				{
					@Override public void onFragmentAttached(Fragment fragment)
					{
						getFragment().setLoading(false);
						((StreamResponseListener)getFragment()).handleResponse(stream, append);
						setOnFragmentAttachedListener(null);
						detachResponse();
					}
				});
			}
		}
	}
}
