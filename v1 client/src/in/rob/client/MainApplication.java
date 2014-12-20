package in.rob.client;

import in.lib.Debug;
import in.lib.exception.ExceptionHandler;
import in.lib.manager.APIManager;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.BitmapUtils;
import in.model.User;
import in.rob.client.dialog.NewPostDialog;
import in.rob.client.page.GeneralSettingsPage;
import lombok.Getter;
import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings.Secure;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.ImageView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class MainApplication extends Application
{
	public static enum ApplicationType
	{
		DEBUG,
		BETA,
		PLAY_STORE,
		CD_KEY;
	}

	@Getter private static boolean onWifi = false;
	@Getter private static boolean onMobile = false;
	@Getter private static boolean hasConnection = false;

	@Getter private ApplicationType applicationType = ApplicationType.DEBUG;

	@Getter private ImageFader avatarFader;
	@Getter private static DisplayImageOptions avatarImageOptions, mediaImageOptions, inlineMediaImageOptions, centerPostMediaOptions, threadAvatarImageOptions;

	@Override public void onCreate()
	{
		super.onCreate();

		ExceptionHandler.getInstance().register(this, getString(R.string.report_url));
		CacheManager.getInstance().setCachePath(getFilesDir().getAbsolutePath());

		int allocated = (int)(Runtime.getRuntime().maxMemory() / 4);
		int allocatedMB = (int)(allocated / 1024.0 / 1024.0);

		SettingsManager.setAllocatedMemory(allocatedMB);
		Debug.out("Setting max memory: " + allocatedMB + " out of " + (Runtime.getRuntime().maxMemory() / 1024 / 1024));

		try
		{
			applicationType = ((getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE) != 0) ? ApplicationType.DEBUG : applicationType;
			Signature[] sigs = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES).signatures;

			for (Signature s : sigs)
			{
				if (("" + s.hashCode()).equals(getString(R.string.play_cert)))
				{
					applicationType = ApplicationType.PLAY_STORE;
				}

				if (("" + s.hashCode()).equals(getString(R.string.debug_cert)) || ("" + s.hashCode()).equals(getString(R.string.beta_cert)))
				{
					applicationType = ApplicationType.BETA;
				}

				if (("" + s.hashCode()).equals(getString(R.string.cd_cert)))
				{
					applicationType = ApplicationType.CD_KEY;
				}
			}
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		Debug.setDebugMode(applicationType == ApplicationType.DEBUG || applicationType == ApplicationType.BETA);

		if (applicationType == applicationType.BETA || applicationType == ApplicationType.DEBUG)
		{
			ExceptionHandler.getInstance().register(this, getString(R.string.beta_report_url));
		}

		ConnectivityManager connManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		onWifi = wifi != null && wifi.isConnected();
		onMobile = mobile != null && mobile.isConnected();
		hasConnection = onWifi || onMobile;

		SettingsManager.getInstance(this);
		initApplication();
	}

	public boolean isConnected()
	{
		ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting())
		{
			return true;
		}

		return false;
	}

	public void initApplication()
	{
		UserManager.loadUser(getApplicationContext());
		initConfiguration();
		initImageLoader();
		initQuickPost();
	}

	public void initConfiguration()
	{
		AsyncHttpClient client = new AsyncHttpClient(APIManager.API_URL + APIManager.API_STREAM + APIManager.API_VERSION);
		client.get(APIManager.API_CONFIGURATION, new JsonResponseHandler()
		{
			@Override public void onSuccess()
			{
				JsonElement resp = getContent();

				if (resp != null)
				{
					resp = resp.getAsJsonObject().get("data");
					JsonObject text = resp.getAsJsonObject().get("text").getAsJsonObject().get("uri_template_length").getAsJsonObject();
					JsonObject user = resp.getAsJsonObject().get("user").getAsJsonObject();
					JsonObject post = resp.getAsJsonObject().get("post").getAsJsonObject();
					JsonObject message = resp.getAsJsonObject().get("message").getAsJsonObject();

					SettingsManager m = SettingsManager.getInstance();
					m.setPostIdLength(text.get("post_id").getAsInt() + 3);
					m.setMessageIdLength(text.get("message_id").getAsInt() + 3);
					m.setPostLength(post.get("text_max_length").getAsInt());
					m.setMessageLength(message.get("text_max_length").getAsInt());
					m.setBioLength(user.get("text_max_length").getAsInt());
				}
			}
		});
	}

	public void initQuickPost()
	{
		if (SettingsManager.isQuickPostEnabled() && UserManager.isLoggedIn())
		{
			NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			NotificationCompat.Builder notification = new NotificationCompat.Builder(this);

			notification.setAutoCancel(false);
			notification.setSmallIcon(R.drawable.quickpost_icon);
			notification.setContentText(getString(R.string.tap_to_compose));
			notification.setContentTitle(getString(R.string.notification_new_post));

			Intent newPostIntent = new Intent(this, NewPostDialog.class);
			newPostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			PendingIntent contentIntent = PendingIntent.getActivity(this, (int)(System.currentTimeMillis() / 1000), newPostIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.setContentIntent(contentIntent);
			notification.setOngoing(true);

			Bitmap b = User.loadAvatar(this, UserManager.getUserId());
			if (b != null)
			{
				int width = getResources().getDimensionPixelSize(R.dimen.notification_large_icon_width);
				int height = getResources().getDimensionPixelSize(R.dimen.notification_large_icon_height);

				b = BitmapUtils.resize(b, width, height);
				notification.setLargeIcon(b);
			}

			Notification not = notification.build();
			notificationManager.notify(GeneralSettingsPage.QUICK_POST_ID, not);
		}
	}

	public String getDeviceId()
	{
		// Create re-hashable password for the device
		String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		deviceId = deviceId == null ? "NOID" + System.currentTimeMillis() : deviceId;

		// Now we hash it and b64 it
		String deviceHash = CacheManager.getHash(deviceId);
		return deviceHash.trim();
	}

	public void initImageLoader()
	{
		avatarFader = new ImageFader(400);

		ImageLoader coverImageLoader = ImageLoader.getInstance();
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(getApplicationContext())
			.threadPoolSize(1)
			.discCacheFileNameGenerator(new FileNameGenerator()
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
			})
			.denyCacheImageMultipleSizesInMemory()
			.tasksProcessingOrder(QueueProcessingType.LIFO)
			.memoryCache(new LRULimitedMemoryCache(Math.min(Math.max(SettingsManager.getAllocatedMemory() / 2, 4), 16) * 1024 * 1024))
			.discCacheSize(1024 * 1024 * SettingsManager.getMaxImageCacheSize());

		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
//			builder.taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
//				.taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR);
		}

		ImageLoaderConfiguration config = builder.build();
		coverImageLoader.init(config);

		Builder avatarImageOptionsBuilder = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.displayer(avatarFader)
			.bitmapConfig(Config.RGB_565)
			.showImageOnLoading(R.drawable.default_avatar)
			.showImageForEmptyUri(R.drawable.default_avatar)
			.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
			.resetViewBeforeLoading(true);

		Builder threadAvatarImageOptionsBuilder = new DisplayImageOptions.Builder()
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.bitmapConfig(Config.RGB_565)
			.showImageOnLoading(R.drawable.default_avatar)
			.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
			.resetViewBeforeLoading(true)
			.showImageForEmptyUri(R.drawable.default_avatar);

		Builder mediaImageOptionsBuilder = new DisplayImageOptions.Builder()
			.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
			.bitmapConfig(Config.RGB_565)
			.resetViewBeforeLoading(true);

		Builder inlineMediaImageOptionsBuilder = new DisplayImageOptions.Builder()
			.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
			.bitmapConfig(Config.RGB_565)
			.resetViewBeforeLoading(true)
			.cacheInMemory(true);

		Builder centerPostMediaOptionsBuilder = new DisplayImageOptions.Builder()
			.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
			.bitmapConfig(Config.RGB_565)
			.cacheInMemory(true);

		if (SettingsManager.getAllocatedMemory() <= 8)
		{
			inlineMediaImageOptionsBuilder.cacheInMemory(false);
			centerPostMediaOptionsBuilder.cacheInMemory(false);
		}

		avatarImageOptions = avatarImageOptionsBuilder.build();
		threadAvatarImageOptions = threadAvatarImageOptionsBuilder.build();
		mediaImageOptions = mediaImageOptionsBuilder.build();
		inlineMediaImageOptions = inlineMediaImageOptionsBuilder.build();
		centerPostMediaOptions = centerPostMediaOptionsBuilder.build();
	}

	@Override public void onLowMemory()
	{
		ImageLoader.getInstance().clearMemoryCache();
		super.onLowMemory();
	}

	public static class ImageFader extends FadeInBitmapDisplayer
	{
		public ImageFader(int delay)
		{
			super(delay);
		}

		@Override public Bitmap display(Bitmap bitmap, ImageView imageView, LoadedFrom loadedFrom)
		{
			if (loadedFrom != LoadedFrom.MEMORY_CACHE)
			{
				return super.display(bitmap, imageView, loadedFrom);
			}
			else
			{
				imageView.setImageBitmap(bitmap);
				return bitmap;
			}
		}
	}
}