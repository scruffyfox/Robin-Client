package in.rob.client;

import android.app.Application;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import in.lib.manager.CacheManager;
import in.lib.manager.MigrationManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;

public class MainApplication extends Application
{
	@Override public void onCreate()
	{
		super.onCreate();

		CacheManager.setCachePath(getFilesDir().getAbsolutePath());
		MigrationManager.getInstance().migrate(this);
		SettingsManager.getInstance().initialise(this);
		UserManager.getInstance().initialise(this);
		initImageLoader();

		SettingsManager.getInstance().toggleQuickPost();
	}

	public void initImageLoader()
	{
		ImageLoader coverImageLoader = ImageLoader.getInstance();
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(getApplicationContext());
		builder.threadPoolSize(1);
		builder.discCacheFileNameGenerator(new FileNameGenerator()
		{
			@Override public String generate(String imageUri)
			{
				Uri uri = Uri.parse(imageUri);

				if (!TextUtils.isEmpty(uri.getQueryParameter("avatar")))
				{
					return "avatar_" + uri.getQueryParameter("id") + "_" + String.valueOf(imageUri.hashCode());
				}

				return String.valueOf(imageUri.hashCode());
			}
		});
		builder.denyCacheImageMultipleSizesInMemory();
		builder.tasksProcessingOrder(QueueProcessingType.LIFO);
		builder.discCacheSize(1024 * 1024 * 30);
		builder.memoryCacheExtraOptions(300, 300);

		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			builder.taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			builder.taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR);
		}

		ImageLoaderConfiguration config = builder.build();
		coverImageLoader.init(config);
	}

	@Override public void onLowMemory()
	{
		ImageLoader.getInstance().clearMemoryCache();
		super.onLowMemory();
	}
}
