package in.lib.loader.base;

import lombok.Getter;
import android.os.AsyncTask;
import android.os.Build;

public abstract class Loader<Param> extends AsyncTask<Boolean, Void, Param>
{
	@Getter private String filename;

	public Loader(String filename)
	{
		this.filename = filename;
	}

	public void execute()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);
		}
		else
		{
			execute(false);
		}
	}

	@Override protected final Param doInBackground(Boolean... params)
	{
		return doInBackground();
	}

	public abstract Param doInBackground();
}