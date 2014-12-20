package in.lib.utils;

import in.lib.manager.CacheManager;
import in.rob.client.MainApplication;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;

public class CacheUtils
{
	/**
	 * Max size in bytes
	 */
	private static final int MAX_SIZE = 20 * 1024 * 1024;
	private final ArrayList<String> mIgnore;

	public CacheUtils(Context context)
	{
		MainApplication application = (MainApplication)context.getApplicationContext();
		//mCacheManager = new CacheManager(context, context.getPackageName(), true);

		mIgnore = new ArrayList<String>();

//		for (String user : application.getAddedUsers())
//		{
//			mIgnore.add(CacheManager.getPrefix() + String.format(Constants.CACHE_USER, user));
//			mIgnore.add(CacheManager.getPrefix() + String.format(Constants.CACHE_TIMELINE_LIST_NAME, user));
//			mIgnore.add(CacheManager.getPrefix() + String.format(Constants.CACHE_MENTION_LIST_NAME, user));
//		}

		//mIgnore.add(CacheManager.getPrefix() + Constants.CACHE_CURRENT_LOCATION);
		//mIgnore.add(CacheManager.getPrefix() + Constants.CACHE_LINKED_ACCOUNTS);
	}

	/**
	 * Clears the cache up to the {@link #MAX_SIZE}
	 */
	public void clearCache()
	{
		File files = new File(CacheManager.getInstance().getCachePath());
		FileFilter filter = new FileFilter()
		{
			@Override public boolean accept(File arg0)
			{
				if (arg0.isDirectory())
				{
					return false;
				}

				return !mIgnore.contains(arg0.getName());
			};
		};

		File[] fileList = files.listFiles(filter);
		Arrays.sort(fileList, new Comparator<File>()
		{
			@Override public int compare(File object1, File object2)
			{
				if (object1.lastModified() > object2.lastModified())
				{
					return -1;
				}
				else if (object1.lastModified() < object2.lastModified())
				{
					return +1;
				}
				else
				{
					return 0;
				}
			}
		});

		long cacheSize = 0L;//mCacheManager.getCacheSize();

		int index = 0;
		while (cacheSize > MAX_SIZE)
		{
			cacheSize -= fileList[index].length();
			fileList[index++].delete();
		}
	}
}
