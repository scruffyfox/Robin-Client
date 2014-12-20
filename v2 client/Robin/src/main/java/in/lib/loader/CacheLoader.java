package in.lib.loader;

import in.data.TSerializable;
import in.lib.loader.Loader.OnFileLoadedListener;

public class CacheLoader<T extends TSerializable>
{
	private Loader<T> cacheLoaderTask;
	private String fileName = "";

	public CacheLoader(String fileName)
	{
		this.cacheLoaderTask = new Loader<T>();
		this.fileName = fileName;
	}

	public void setOnFileLoadedListener(OnFileLoadedListener<T> l)
	{
		this.cacheLoaderTask.setOnFileLoadedListener(l);
	}

	public void load(Class<T> instance)
	{
		cacheLoaderTask.execute(fileName, instance);
	}
}