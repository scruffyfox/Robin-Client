package in.lib.manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.Set;

import in.lib.Constants;
import in.lib.utils.Debug;
import in.model.User;
import in.rob.client.R;
import in.rob.client.dialog.NewPostDialog;
import lombok.Data;

@Data
public class SettingsManager
{
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD})
	public @interface Key
	{
		String value();
	}

	private Context context;
	private SharedPreferences prefs;
	private static SettingsManager instance;

	private int avatarSize = 100;

	@Key(Constants.PREFS_USERNAME_TITLE) private String userTitle = "@#{username}|#{fullname}";
	@Key(Constants.PREFS_COLLAPSED_THREADS) private Set<String> collapsedThreadIds = new LinkedHashSet<String>();
	//private ImageProvider imageProvider;
	@Key(Constants.PREFS_SHAKE_REFRESH_ENABLED) private boolean shakeToRefreshEnabled;
	@Key(Constants.PREFS_QUICK_POST_ENABLED) private boolean quickPostEnabled;
	@Key(Constants.PREFS_INLINE_WIFI_ENABLED) private boolean inlineWifiEnabled;
	@Key(Constants.PREFS_WEB_READABILITY_MODE_ENABLED) private boolean webReadabilityModeEnabled;
	@Key(Constants.PREFS_NON_FOLLOWING_MENTIONS_ENABLED) private boolean nonFollowingMentionEnabled;
	@Key(Constants.PREFS_STREAM_MARKERS) private int streamMarkerBit = Constants.BIT_STREAM_MARKER_ENABLED | Constants.BIT_STREAM_MARKER_PAST;
	@Key(Constants.PREFS_SINGLE_CLICK_OPTIONS) private int singleClickBit = 0;
	@Key(Constants.PREFS_SHOWHIDE_OPTIONS) private int showHideBit = Constants.BIT_SHOWHIDE_AVATARS | Constants.BIT_SHOWHIDE_INLINE_IMAGES | Constants.BIT_SHOWHIDE_TIMELINE_COVER;
	@Key(Constants.PREFS_IN_APP_VIEWER_OPTIONS) private int inAppViewerBit = Constants.BIT_IN_APP_VIEWER_BROWSER | Constants.BIT_IN_APP_VIEWER_IMAGE | Constants.BIT_IN_APP_VIEWER_YOUTUBE;
	@Key(Constants.PREFS_EMPHASIS_OPTIONS) private int emphasisBit = 0;

	public static SettingsManager getInstance()
	{
		if (instance == null)
		{
			synchronized (SettingsManager.class)
			{
				if (instance == null)
				{
					instance = new SettingsManager();
				}
			}
		}

		return instance;
	}

	public void initialise(Context context)
	{
		this.context = context.getApplicationContext();
		this.prefs = context.getSharedPreferences(context.getPackageName() + ".settings", Context.MODE_PRIVATE);
		this.avatarSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_width);
		load();
	}

	public void save()
	{
		SharedPreferences.Editor editor = prefs.edit();
		for (Field field : getClass().getDeclaredFields())
		{
			if (field.isAnnotationPresent(Key.class))
			{
				try
				{
					Key key = (Key)field.getAnnotation(Key.class);
					field.setAccessible(true);

					if (field.getType().equals(boolean.class))
					{
						editor.putBoolean(key.value(), field.getBoolean(this));
					}
					else if (field.getType().equals(int.class))
					{
						editor.putInt(key.value(), field.getInt(this));
					}
					else if (field.getType().equals(Set.class))
					{
						editor.putStringSet(key.value(), (Set<String>)field.get(this));
					}
				}
				catch (Exception e)
				{
					Debug.out(e);
				}
			}
		}

		editor.apply();
	}

	public void load()
	{
		for (Field field : getClass().getDeclaredFields())
		{
			if (field.isAnnotationPresent(Key.class))
			{
				try
				{
					Key key = (Key)field.getAnnotation(Key.class);
					field.setAccessible(true);

					if (field.getType().equals(boolean.class))
					{
						field.set(this, prefs.getBoolean(key.value(), field.getBoolean(this)));
					}
					else if (field.getType().equals(int.class))
					{
						field.set(this, prefs.getInt(key.value(), field.getInt(this)));
					}
					else if (field.getType().equals(Set.class))
					{
						field.set(this, prefs.getStringSet(key.value(), (Set<String>)field.get(this)));
					}
				}
				catch (Exception e)
				{
					Debug.out(e);
				}
			}
		}
	}

	public void collapseThread(String threadId)
	{
		collapsedThreadIds.add(threadId);
		save();
	}

	public void expandThread(String threadId)
	{
		collapsedThreadIds.remove(threadId);
		save();
	}

	public void toggleQuickPost()
	{
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(Constants.QUICK_POST_ID);

		if (isQuickPostEnabled())
		{
			Notification.Builder notificationBuilder = new Notification.Builder(context);

			notificationBuilder.setAutoCancel(false);
			notificationBuilder.setSmallIcon(R.drawable.quickpost_icon);
			notificationBuilder.setContentText(context.getString(R.string.tap_to_compose));
			notificationBuilder.setContentTitle(context.getString(R.string.notification_new_post));

			Intent newPostIntent = new Intent(context, NewPostDialog.class);
			newPostIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, newPostIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			notificationBuilder.setContentIntent(contentIntent);
			notificationBuilder.setOngoing(true);

			int width = context.getResources().getDimensionPixelSize(R.dimen.notification_large_icon_width);
			int height = context.getResources().getDimensionPixelSize(R.dimen.notification_large_icon_height);
			Bitmap b = User.loadAvatar(context, UserManager.getInstance().getUser().getId(), width, height);

			if (b != null)
			{
				notificationBuilder.setLargeIcon(b);
			}

			Notification notification = notificationBuilder.getNotification();
			notificationManager.notify(Constants.QUICK_POST_ID, notification);
		}
	}
}
