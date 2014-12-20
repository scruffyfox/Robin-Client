package in.lib.manager;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

import in.lib.Constants;

public class MigrationManager
{
	private static final int VERSION = 1;
	private static MigrationManager instance;

	public static MigrationManager getInstance()
	{
		if (instance == null)
		{
			synchronized (MigrationManager.class)
			{
				if (instance == null)
				{
					instance = new MigrationManager();
				}
			}
		}

		return instance;
	}

	private MigrationManager()
	{

	}

	public void migrate(Context context)
	{
		context = context.getApplicationContext();
		SharedPreferences prefs = context.getSharedPreferences(context.getPackageName() + ".migration", Context.MODE_PRIVATE);

		int version = prefs.getInt(Constants.PREFS_VERSION, 0);

		if (version != VERSION)
		{
			clearCache();
			prefs.edit().putInt(Constants.PREFS_VERSION, VERSION).apply();
		}
	}

	private void clearCache()
	{
		File f = new File(CacheManager.getCachePath());
		File[] files = f.listFiles();

		if (files != null)
		{
			for (File file : files)
			{
				file.delete();
			}
		}
	}
}
