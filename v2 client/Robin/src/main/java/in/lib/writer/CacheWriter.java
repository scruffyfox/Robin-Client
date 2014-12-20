package in.lib.writer;

import android.os.AsyncTask;
import android.os.Build;

import in.data.TSerializable;
import in.lib.manager.CacheManager;

public class CacheWriter
{
	private class Writer extends AsyncTask<TSerializable, Void, Void>
	{
		private String fileName;

		public Writer(String fileName)
		{
			this.fileName = fileName;
		}

		@Override protected Void doInBackground(TSerializable... params)
		{
			CacheManager.getInstance().writeFile(fileName, params[0]);
			return null;
		}
	}

	private Writer cacheWriterTask;

	public CacheWriter(String fileName)
	{
		this.cacheWriterTask = new Writer(fileName);
	}

	public void write(TSerializable data)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			cacheWriterTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data);
		}
		else
		{
			cacheWriterTask.execute(data);
		}
	}
}