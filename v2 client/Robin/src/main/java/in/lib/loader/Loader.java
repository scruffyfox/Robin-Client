package in.lib.loader;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import in.data.TSerializable;
import in.lib.manager.CacheManager;
import lombok.Setter;

@SuppressLint("NewApi")
@SuppressWarnings("unchecked")
public class Loader<T extends TSerializable> extends AsyncTask<String, Void, T>
{
	@Setter private OnFileLoadedListener<T> onFileLoadedListener;
	private long fileAge = 0L;
	private Class<T> instance;

	public interface OnFileLoadedListener<T>
	{
		public void onFileLoaded(T data, long age);
	}

	public void execute(String filename, Class<T> instance)
	{
		this.instance = instance;
		executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filename);
	}

	@Override public T doInBackground(String... params)
	{
		try
		{
			fileAge = CacheManager.getInstance().getFileAge(params[0]);
			T stream = CacheManager.getInstance().readFile(params[0], instance);
			return stream;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@Override protected void onPostExecute(T result)
	{
		super.onPostExecute(result);

		if (onFileLoadedListener != null)
		{
			onFileLoadedListener.onFileLoaded(result, fileAge);
		}
	}
}
