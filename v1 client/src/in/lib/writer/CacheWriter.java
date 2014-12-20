package in.lib.writer;

import in.lib.manager.CacheManager;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import android.os.AsyncTask;
import android.os.Build;

public class CacheWriter extends AsyncTask<Object, Void, Void>
{
	@Getter private final String[] filenames;
	@Setter private WriterListener writerListener;

	public interface WriterListener
	{
		public void onFinishedWriting();
	}

	public CacheWriter(String... filename)
	{
		this.filenames = filename;
	}

	public CacheWriter(Set<String> filenames)
	{
		this.filenames = filenames.toArray(new String[filenames.size()]);
	}

	/**
	 * Use this over {@link execute(T... params)} to fix issue with
	 * threadding post HC.
	 * @param params
	 */
	public void write(Object... params)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
		}
		else
		{
			execute(params);
		}
	}

	@Override protected Void doInBackground(Object... params)
	{
		for (int index = 0; index < filenames.length; index++)
		{
			CacheManager.getInstance().writeFile(filenames[index], params[index]);
		}

		return null;
	}

	@Override protected void onPostExecute(Void result)
	{
		if (writerListener != null)
		{
			writerListener.onFinishedWriting();
		}
	}
}