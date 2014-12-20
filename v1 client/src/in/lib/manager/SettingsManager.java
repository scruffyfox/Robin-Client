package in.lib.manager;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.manager.ImageAPIManager.Provider;
import in.lib.type.FIFOArrayList;
import in.lib.utils.DateUtils;
import in.lib.utils.StringUtils;
import in.model.User;
import in.rob.client.MainApplication;
import in.rob.client.R;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.obj.entity.JsonEntity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.JsonObject;

public class SettingsManager
{
	@Getter private static boolean timelineBreakEnabled = true;
	@Getter private static boolean notificationLedEnabled = true;
	@Getter private static boolean quickPostEnabled = false;
	@Getter private static boolean shakeRefreshEnabled = false;
	@Getter private static boolean notificationsEnabled = true;
	@Getter private static boolean notificationsSoundEnabled = true;
	@Getter private static boolean notificationsVibrateEnabled = false;
	@Getter private static boolean notificationsOnlyFollowing = false;
	@Getter private static boolean inlineImagesEnabled = true;
	@Getter private static boolean globalEnabled = false;
	@Getter private static boolean usingUnified = true;
	@Getter private static boolean quietModeEnabled = false;
	@Getter private static boolean analyticsEnabled = true;
	@Getter private static boolean crashReportEnabled = true;
	@Getter private static boolean keywordSearchEnabled = true;
	@Getter private static boolean invertPostClick = false;
	@Getter private static boolean lightboxEnabled = true;
	@Getter private static boolean imageViewerEnabled = true;
	@Getter private static boolean customFontsEnabled = false;
	@Getter private static boolean inlineImageWifiEnabled = false;
	@Getter private static Boolean showDirectedPosts = true;
	@Getter private static Boolean showDirectedMentions = true;
	@Getter private static Boolean showLongDates = false;
	@Getter private static Boolean showAvatars = true;
	@Getter private static Boolean showTimelineCover = true;
	@Getter private static int fontSizeIndex = 1;
	@Getter private static long quietModeFrom = 82800000;
	@Getter private static long quietModeTo = 28800000;
	@Getter private static String notificationTone = null;
	@Getter private static ImageAPIManager.Provider imageProvider = Provider.APPNET;
	@Getter private static String nameDisplayOrder = "@{#username}|{#fullname}";
	@Getter private static DateFormat dateFormat;
	@Getter private static DateFormat timeFormat;
	@Getter private static DateUtils dateUtils;
	@Getter private static FIFOArrayList<String> recentSearches = new FIFOArrayList<String>(5);
	@Getter private static long cacheTimeout = 60 * 15 * 1000;
	@Getter private static long requestTimeout = 10 * 1000;
	@Getter private static int maxCacheSize = 30;
	@Getter private static int maxImageCacheSize = 30;
	@Getter private static int pageSize = 30;
	@Getter private static int swarmProtectionIndex = 0;
	@Getter private static String themeName = "DefaultLight";
	@Getter private static String locale = "";
	@Getter private static boolean webReadabilityEnabled = false;
	@Getter @Setter private static int allocatedMemory = 0;
	private static String savedTags = "";
	private static String mutedTags = "";
	private static String mutedThreads = "";

	@Getter private static int postIdLength = 12;
	@Getter private static int messageIdLength = 15;
	@Getter private static int postLength = 256;
	@Getter private static int messageLength = 256;
	@Getter private static int bioLength = 256;

	/**
	 * BIT_NOTIFICATION_MENTION = 0x0000001;
	 * BIT_NOTIFICATION_MESSAGE = 0x0000002;
	 * BIT_NOTIFICATION_PATTER_PM = 0x0000004;
	 * BIT_NOTIFICATION_FOLLOW = 0x0000008;
	 * BIT_NOTIFICATION_REPOST = 0x0000010;
	 * BIT_NOTIFICATION_STAR = 0x0000020;
	 *
	 * Default all: 63
	 */
	@Getter private static int notifications = 63;

	/**
	 * BIT_ANIMATION_LIST = 0x0000001;
	 * BIT_ANIMATION_INLINE_IMAGE = 0x0000002;
	 * BIT_ANIMATION_COVER_IMAGE = 0x0000004;
	 * BIT_ANIMATION_PAGINATION = 0x0000008;
	 *
	 * Default all: 15
	 */
	@Getter private static int animations = 15;

	/**
	 * BIT_EMPHASIS_ITALIC = 0x0000001;
	 * BIT_EMPHASIS_BOLD = 0x0000002;
	 * BIT_EMPHASIS_UNDERLINE = 0x0000004;
	 *
	 * Default all: 0
	 */
	@Getter private static int postEmphasis = 0;

	/**
	 * BIT_LINK_HASHTAG = 0x0000001;
	 * BIT_LINK_MENTION = 0x0000002;
	 * BIT_LINK_URL = 0x0000004;
	 *
	 * Default all: 0
	 */
	@Getter private static int singleClickLinks = 0;

	/**
	 * BIT_STREAM_MARKER_ENABLED = 0x0000001;
	 * BIT_STREAM_MARKER_PAST = 0x0000002;
	 *
	 * Default all: 0
	 */
	@Getter private static int streamMarker = 0;

	/**
	 * Font sizes in %. Values: {@value}
	 */
	private static float[] mFontSizes = new float[]{0.8f, 1.0f, 1.3f, 1.5f, 1.7f, 1.9f, 2.1f};

	private SharedPreferences mPrefs;
	private static SettingsManager mInstance;

	/**
	 * Get the instance of SettingsManager or create it if it's null.
	 *
	 * You should only call this ONCE in your application singleton
	 * @param c The <b>APPLICATION</b> context
	 * @return The SettingsManager instance
	 */
	public static SettingsManager getInstance(Context c)
	{
		if (mInstance == null)
		{
			synchronized (SettingsManager.class)
			{
				if (mInstance == null)
				{
					mInstance = new SettingsManager(c.getApplicationContext());
				}
			}
		}

		return mInstance;
	}

	/**
	 * Get the instance of SettingsManager or create it if it's null
	 * @return The SettingsManager instance
	 */
	public static SettingsManager getInstance()
	{
		return mInstance;
	}

	public SettingsManager(Context context)
	{
		inlineImagesEnabled = allocatedMemory > 8;
		animations = allocatedMemory > 8 ? animations : 0;

		mPrefs = context.getSharedPreferences(Constants.PREFS_SETTINGS_KEY, Context.MODE_PRIVATE);
		pageSize = mPrefs.getInt(Constants.PREFS_PAGE_SIZE, 60);
		showLongDates = mPrefs.getBoolean(Constants.PREFS_LONG_DATE, showLongDates);
		showAvatars = mPrefs.getBoolean(Constants.PREFS_AVATARS_ENABLED, showAvatars);
		notificationsEnabled = mPrefs.getBoolean(Constants.PREFS_NOTIFICATIONS, notificationsEnabled);
		notificationsSoundEnabled = mPrefs.getBoolean(Constants.PREFS_NOTIFICATIONS_SOUND, notificationsSoundEnabled);
		notificationsOnlyFollowing = mPrefs.getBoolean(Constants.PREFS_NOTIFICATIONS_FOLLOWING, notificationsOnlyFollowing);
		inlineImagesEnabled = mPrefs.getBoolean(Constants.PREFS_INLINE_IMAGES, inlineImagesEnabled);
		showDirectedPosts = mPrefs.getBoolean(Constants.PREFS_DIRECTED_POSTS_VISIBLE, showDirectedPosts);
		showDirectedMentions = mPrefs.getBoolean(Constants.PREFS_DIRECTED_MENTIONS_VISIBLE, showDirectedMentions);
		globalEnabled = mPrefs.getBoolean(Constants.PREFS_GLOBAL_ENABLED, globalEnabled);
		usingUnified = mPrefs.getBoolean(Constants.PREFS_USING_UNIFIED, usingUnified);
		quietModeEnabled = mPrefs.getBoolean(Constants.PREFS_QUIET_MODE_ENABLED, quietModeEnabled);
		analyticsEnabled = mPrefs.getBoolean(Constants.PREFS_ANALYTICS_ENABLED, analyticsEnabled);
		crashReportEnabled = mPrefs.getBoolean(Constants.PREFS_CRASH_REPORTING_ENABLED, crashReportEnabled);
		showTimelineCover = mPrefs.getBoolean(Constants.PREFS_TIMELINE_COVER, showTimelineCover);
		fontSizeIndex = mPrefs.getInt(Constants.PREFS_FONT_SIZE, fontSizeIndex);
		savedTags = mPrefs.getString(Constants.PREFS_SAVED_TAGS, "robin,#robintips,");
		mutedTags = mPrefs.getString(Constants.PREFS_MUTED_TAGS, "test,");
		mutedThreads = mPrefs.getString(Constants.PREFS_MUTED_THREADS, "");
		notificationTone = mPrefs.getString(Constants.PREFS_NOTIFICATION_TONE, null);
		nameDisplayOrder = mPrefs.getString(Constants.PREFS_NAME_DISPLAY, nameDisplayOrder);
		cacheTimeout = mPrefs.getLong(Constants.PREFS_CACHE_TIMEOUT, cacheTimeout);
		quietModeFrom = mPrefs.getLong(Constants.PREFS_QUIET_MODE_FROM, quietModeFrom);
		quietModeTo = mPrefs.getLong(Constants.PREFS_QUIET_MODE_TO, quietModeTo);
		imageProvider = Provider.getProviderById(mPrefs.getInt(Constants.PREFS_IMAGE_PROVIDER, 4));
		timelineBreakEnabled = mPrefs.getBoolean(Constants.PREFS_TIMELINE_BREAK, timelineBreakEnabled);
		shakeRefreshEnabled = mPrefs.getBoolean(Constants.PREFS_SHAKE_REFRESH_ENABLED, shakeRefreshEnabled);
		quickPostEnabled = mPrefs.getBoolean(Constants.PREFS_QUICK_POST_ENABLED, quickPostEnabled);
		notificationLedEnabled = mPrefs.getBoolean(Constants.PREFS_NOTIFICATION_LED_ENABLED, notificationLedEnabled);
		notificationsVibrateEnabled = mPrefs.getBoolean(Constants.PREFS_NOTIFICATION_VIBRATE, notificationsVibrateEnabled);
		themeName = mPrefs.getString(Constants.PREFS_THEME, themeName);
		requestTimeout = mPrefs.getLong(Constants.PREFS_REQUEST_TIMEOUT, requestTimeout);
		keywordSearchEnabled = mPrefs.getBoolean(Constants.PREFS_KEYWORD_SEARCH_ENABLED, keywordSearchEnabled);
		invertPostClick = mPrefs.getBoolean(Constants.PREFS_INVERT_POST_CLICK_ENABLED, invertPostClick);
		lightboxEnabled = mPrefs.getBoolean(Constants.PREFS_LIGHTBOX_ENABLED, lightboxEnabled);
		imageViewerEnabled = mPrefs.getBoolean(Constants.PREFS_IMAGE_VIEWER_ENABLED, imageViewerEnabled);
		swarmProtectionIndex = mPrefs.getInt(Constants.PREFS_SWARM_PROTECTION, swarmProtectionIndex);
		notifications = mPrefs.getInt(Constants.PREFS_NOTIFICATIONS_OPTIONS, notificationsEnabled ? notifications : 0);
		customFontsEnabled = mPrefs.getBoolean(Constants.PREFS_CUSTOM_FONTS, customFontsEnabled);
		animations = mPrefs.getInt(Constants.PREFS_ANIMATIONS, context.getResources().getInteger(R.integer.animations));
		postEmphasis = mPrefs.getInt(Constants.PREFS_POST_EMPHASIS, postEmphasis);
		inlineImageWifiEnabled = mPrefs.getBoolean(Constants.PREFS_INLINE_IMAGE_WIFI, inlineImageWifiEnabled);
		maxCacheSize = mPrefs.getInt(Constants.PREFS_CACHE_SIZE, maxCacheSize);
		maxImageCacheSize = mPrefs.getInt(Constants.PREFS_IMAGE_CACHE_SIZE, maxImageCacheSize);
		locale = mPrefs.getString(Constants.PREFS_LOCALE, locale);
		webReadabilityEnabled = mPrefs.getBoolean(Constants.PREFS_WEB_READABILITY, webReadabilityEnabled);
		singleClickLinks = mPrefs.getInt(Constants.PREFS_SINGLE_CLICK_LINKS, singleClickLinks);

		postIdLength = mPrefs.getInt(Constants.PREFS_POST_ID_LENGTH, postIdLength);
		messageIdLength = mPrefs.getInt(Constants.PREFS_MESSAGE_ID_LENGTH, messageIdLength);
		postLength = mPrefs.getInt(Constants.PREFS_POST_LENGTH, postLength);
		messageLength = mPrefs.getInt(Constants.PREFS_MESSAGE_LENGTH, messageLength);
		bioLength = mPrefs.getInt(Constants.PREFS_BIO_LENGTH, bioLength);

		boolean markerEnabled = mPrefs.getBoolean(Constants.PREFS_STREAM_MARKERS_ENABLED, false);
		streamMarker = mPrefs.getInt(Constants.PREFS_STREAM_MARKERS, streamMarker);

		if (markerEnabled)
		{
			setStreamMarkerOptions(3);
			mPrefs.edit().remove(Constants.PREFS_STREAM_MARKERS_ENABLED).apply();
		}

		String searches = mPrefs.getString(Constants.PREFS_RECENT_SEARCH, "");
		recentSearches.addAll(Arrays.asList(searches.split("[,]")));

		dateFormat = android.text.format.DateFormat.getDateFormat(context);
		timeFormat = android.text.format.DateFormat.getTimeFormat(context);
		dateUtils = new DateUtils(context);
	}

	/**
	 * Updates the notification server with the selected settings
	 */
	public void saveSettings(Context mContext)
	{
		MainApplication app = (MainApplication)mContext.getApplicationContext();
		String deviceHash = app.getDeviceId();

		// update all user's registered with this device
		List<String> users = UserManager.getLinkedUserIds(mContext);
		for (String user : users)
		{
			AsyncHttpClient settingsUpdater = new AsyncHttpClient(Constants.API_NOTIFICATION_URL + Constants.API_NOTIFICATION_VERSION);
			User u = User.loadUser(user);
			JsonObject obj = new JsonObject();
			obj.addProperty("id", deviceHash);
			obj.addProperty("enabled", notifications);
			obj.addProperty("follow_enabled", notificationsOnlyFollowing);

			try
			{
				settingsUpdater.put
				(
					Constants.API_NOTIFICATION_USERS + user + "/" + Constants.API_NOTIFICATION_DEVICES + deviceHash,
					new JsonEntity(obj),
					null
				);
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}
	}

	/**************************************************
	 *
	 *	STREAM MARKER SETTINGS
	 *
	 **************************************************/
	public void setStreamMarkerOptions(int options)
	{
		streamMarker = options;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_STREAM_MARKERS, options);
		editor.apply();
	}

	public static boolean isStreamMarkerEnabled()
	{
		return ((streamMarker & Constants.BIT_STREAM_MARKER_ENABLED) == Constants.BIT_STREAM_MARKER_ENABLED);
	}

	public static boolean isStreamMarkerPastEnabled()
	{
		return ((streamMarker & Constants.BIT_STREAM_MARKER_PAST) == Constants.BIT_STREAM_MARKER_PAST);
	}

	/**************************************************
	 *
	 *	ANIMATION SETTINGS
	 *
	 **************************************************/
	public void setAnimationOptions(int options)
	{
		animations = options;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_ANIMATIONS, options);
		editor.apply();
	}

	public static boolean isListAnimationEnabled()
	{
		return ((animations & Constants.BIT_ANIMATION_LIST) == Constants.BIT_ANIMATION_LIST);
	}

	public static boolean isInlineAnimationEnabled()
	{
		return ((animations & Constants.BIT_ANIMATION_INLINE_IMAGE) == Constants.BIT_ANIMATION_INLINE_IMAGE);
	}

	public static boolean isCoverImageAnimationEnabled()
	{
		return ((animations & Constants.BIT_ANIMATION_COVER_IMAGE) == Constants.BIT_ANIMATION_COVER_IMAGE);
	}

	public static boolean isPaginationAnimationEnabled()
	{
		return ((animations & Constants.BIT_ANIMATION_PAGINATION) == Constants.BIT_ANIMATION_PAGINATION);
	}

	/**************************************************
	 *
	 *	POST EMPHASIS SETTINGS
	 *
	 **************************************************/
	public void setPostEmphasisOptions(int options)
	{
		postEmphasis = options;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_POST_EMPHASIS, postEmphasis);
		editor.apply();
	}

	public static boolean isItalicEnabled()
	{
		return ((postEmphasis & Constants.BIT_EMPHASIS_ITALIC) == Constants.BIT_EMPHASIS_ITALIC);
	}

	public static boolean isBoldEnabled()
	{
		return ((postEmphasis & Constants.BIT_EMPHASIS_BOLD) == Constants.BIT_EMPHASIS_BOLD);
	}

	public static boolean isUnderlineEnabled()
	{
		return ((postEmphasis & Constants.BIT_EMPHASIS_UNDERLINE) == Constants.BIT_EMPHASIS_UNDERLINE);
	}

	/**************************************************
	 *
	 *	SINGLE CLICK SETTINGS
	 *
	 **************************************************/
	public void setSingleClickOptions(int options)
	{
		singleClickLinks = options;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_SINGLE_CLICK_LINKS, singleClickLinks);
		editor.apply();
	}

	public static boolean isSingleClickHashtagEnabled()
	{
		return ((singleClickLinks & Constants.BIT_LINK_HASHTAG) == Constants.BIT_LINK_HASHTAG);
	}

	public static boolean isSingleClickMentionEnabled()
	{
		return ((singleClickLinks & Constants.BIT_LINK_MENTION) == Constants.BIT_LINK_MENTION);
	}

	public static boolean isSingleClickUrlEnabled()
	{
		return ((singleClickLinks & Constants.BIT_LINK_URL) == Constants.BIT_LINK_URL);
	}

	/**************************************************
	 *
	 *	OTHER SETTINGS
	 *
	 **************************************************/
	public void setPostIdLength(int length)
	{
		postIdLength = length;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_POST_ID_LENGTH, length);
		editor.apply();
	}

	public void setMessageIdLength(int length)
	{
		messageIdLength = length;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_MESSAGE_ID_LENGTH, length);
		editor.apply();
	}

	public void setPostLength(int length)
	{
		postLength = length;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_POST_LENGTH, length);
		editor.apply();
	}

	public void setMessageLength(int length)
	{
		messageLength = length;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_MESSAGE_LENGTH, length);
		editor.apply();
	}

	public void setBioLength(int length)
	{
		bioLength = length;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_BIO_LENGTH, length);
		editor.apply();
	}

	public void setWebReadabilityEnabled(boolean enabled)
	{
		webReadabilityEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_WEB_READABILITY, enabled);
		editor.apply();
	}

	public void setMaxCacheSize(int size)
	{
		maxCacheSize = size;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_CACHE_SIZE, size);
		editor.apply();
	}

	public void setMaxImageCacheSize(int size)
	{
		maxImageCacheSize = size;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_IMAGE_CACHE_SIZE, size);
		editor.apply();
	}

	public void setInlineImageWifiOnly(boolean enabled)
	{
		inlineImageWifiEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_INLINE_IMAGE_WIFI, enabled);
		editor.apply();
	}

	public void setNotificationOptions(int options)
	{
		notifications = options;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_NOTIFICATIONS_OPTIONS, options);
		editor.apply();
	}

	public void addSearchHistory(String tag)
	{
		if (recentSearches.contains(tag))
		{
			recentSearches.remove(tag);
		}

		recentSearches.add(tag);
		Editor editor = mPrefs.edit();
		editor.putString(Constants.PREFS_RECENT_SEARCH, StringUtils.join(recentSearches, ","));
		editor.apply();
	}

	public void removeSearchHistory(String tag)
	{
		if (recentSearches.contains(tag))
		{
			recentSearches.remove(tag);
		}

		Editor editor = mPrefs.edit();
		editor.putString(Constants.PREFS_RECENT_SEARCH, StringUtils.join(recentSearches, ","));
		editor.apply();
	}

	public void setLocale(String locale)
	{
		SettingsManager.locale = locale;
		Editor editor = mPrefs.edit();
		editor.putString(Constants.PREFS_LOCALE, locale);
		editor.apply();
	}

	public void setSwarmProtectionIndex(int index)
	{
		swarmProtectionIndex = index;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_SWARM_PROTECTION, index);
		editor.apply();
	}

	public void setCustomFontsEnabled(boolean enabled)
	{
		customFontsEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_CUSTOM_FONTS, enabled);
		editor.apply();
	}

	public void setAppTheme(String theme)
	{
		themeName = theme;
		Editor editor = mPrefs.edit();
		editor.putString(Constants.PREFS_THEME, theme);
		editor.apply();
	}

	public void setLightboxEnabled(boolean enabled)
	{
		lightboxEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_LIGHTBOX_ENABLED, enabled);
		editor.apply();
	}

	public void setImageViewerEnabled(boolean enabled)
	{
		imageViewerEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_IMAGE_VIEWER_ENABLED, enabled);
		editor.apply();
	}

	public void setNotificationVibrateEnabled(boolean enabled)
	{
		notificationsVibrateEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_NOTIFICATION_VIBRATE, enabled);
		editor.apply();
	}

	public void setRequestTimeout(long timeout)
	{
		requestTimeout = timeout;
		Editor editor = mPrefs.edit();
		editor.putLong(Constants.PREFS_REQUEST_TIMEOUT, timeout);
		editor.apply();
	}

	public void setKeywordSearchEnabled(boolean enabled)
	{
		keywordSearchEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_KEYWORD_SEARCH_ENABLED, enabled);
		editor.apply();
	}

	public void setInvertPostEnabled(boolean enabled)
	{
		invertPostClick = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_INVERT_POST_CLICK_ENABLED, enabled);
		editor.apply();
	}

	/**
	 * Sets if quick post is enabled or not
	 */
	public void setQuickPostEnabled(boolean enabled)
	{
		quickPostEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_QUICK_POST_ENABLED, enabled);
		editor.apply();
	}

	/**
	 * Sets if the notification led colour is enabled or not
	 */
	public void setNotificationLedEnabled(boolean enabled)
	{
		notificationLedEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_NOTIFICATION_LED_ENABLED, enabled);
		editor.apply();
	}

	/**
	 * Sets if shake to refresh is enabled or not
	 */
	public void setShakeRefreshEnabled(boolean enabled)
	{
		shakeRefreshEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_SHAKE_REFRESH_ENABLED, enabled);
		editor.apply();
	}

	/**
	 * Sets the image upload provider
	 * @param p The new provider
	 */
	public void setImageProvider(Provider p)
	{
		imageProvider = p;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_IMAGE_PROVIDER, p.getId());
		editor.apply();
	}

	/**
	 * Sets the custom quiet hours for notifications
	 * @param from The time in milliseconds (time only) from 1970
	 * @param to The time in milliseconds (time only) from 1970
	 */
	public void setQuietHours(long from, long to)
	{
		quietModeFrom = from;
		quietModeTo = to;
		Editor editor = mPrefs.edit();
		editor.putLong(Constants.PREFS_QUIET_MODE_FROM, from);
		editor.putLong(Constants.PREFS_QUIET_MODE_TO, to);
		editor.apply();
	}

	/**
	 * Sets if the timeline cover is enabled or not
	 * @param enabled
	 */
	public void setTimelineCoverEnabled(boolean enabled)
	{
		showTimelineCover = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_TIMELINE_COVER, enabled);
		editor.apply();
	}

	/**
	 * Sets if the timeline will break for new posts
	 * @param enabled
	 */
	public void setTimelineBreakEnabled(boolean enabled)
	{
		timelineBreakEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_TIMELINE_BREAK, enabled);
		editor.apply();
	}

	/**
	 * Sets the notification tone
	 * @param url the path to the tone
	 */
	public void setNotificationTone(String url)
	{
		notificationTone = url;
		Editor editor = mPrefs.edit();
		editor.putString(Constants.PREFS_NOTIFICATION_TONE, notificationTone);
		editor.apply();
	}

	/**
	 * Sets if the crash reporting is enabled or not
	 * @param eneabled True/false
	 */
	public void setCrashReportingEnabled(boolean enabled)
	{
		crashReportEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_CRASH_REPORTING_ENABLED, crashReportEnabled);
		editor.apply();
	}

	/**
	 * Sets if the global stream is enabled or not
	 * @param eneabled True/false
	 */
	public void setAnalyticsEnabled(boolean enabled)
	{
		analyticsEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_ANALYTICS_ENABLED, analyticsEnabled);
		editor.apply();
	}

	/**
	 * Sets if the global stream is enabled or not
	 * @param eneabled True/false
	 */
	public void setGlobalEnabled(boolean enabled)
	{
		globalEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_GLOBAL_ENABLED, globalEnabled);
		editor.apply();
	}

	/**
	 * Sets if the quiet mode is enabled or not
	 * @param eneabled True/false
	 */
	public void setQuietModeEnabled(boolean enabled)
	{
		quietModeEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_QUIET_MODE_ENABLED, quietModeEnabled);
		editor.apply();
	}

	/**
	 * Sets if to use a unified stream
	 * @param eneabled True/false
	 */
	public void setUsingUnified(boolean enabled)
	{
		usingUnified = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_USING_UNIFIED, usingUnified);
		editor.apply();
	}

	/**
	 * Sets the cache timeout time in MS
	 * @param timeout the max timeout in MS
	 */
	public void setCacheTimeout(long timeout)
	{
		cacheTimeout = timeout;
		Editor editor = mPrefs.edit();
		editor.putLong(Constants.PREFS_CACHE_TIMEOUT, cacheTimeout);
		editor.apply();
	}

	/**
	 * Set the name display order
	 * @param order The order script
	 */
	public void setNameDisplayOrder(String order)
	{
		nameDisplayOrder = order;
		Editor editor = mPrefs.edit();
		editor.putString(Constants.PREFS_NAME_DISPLAY, nameDisplayOrder);
		editor.apply();
	}

	/**
	 * Sets weather to show long dates or not
	 * @param showLong True if so, false if not
	 */
	public void setShowLongDates(boolean showLong)
	{
		showLongDates = showLong;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_LONG_DATE, showLong);
		editor.apply();
	}

	/**
	 * Sets weather to show avatars
	 * @param showAvatars True if so, false if not
	 */
	public void setShowAvatars(boolean showAvatars)
	{
		this.showAvatars = showAvatars;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_AVATARS_ENABLED, showAvatars);
		editor.apply();
	}

	/**
	 * Sets the font size to the index of {@link #mFontSizes}
	 * @param index The index of the selected font from {@link #mFontSizes}
	 */
	public void setFontSize(int index)
	{
		fontSizeIndex = index;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_FONT_SIZE, index);
		editor.apply();
	}

	/**
	 * Sets the page size
	 * @param pageSize The signed value for page size (0-Integer.MAX)
	 */
	public void setPageSize(int pageSize)
	{
		pageSize = pageSize;
		Editor editor = mPrefs.edit();
		editor.putInt(Constants.PREFS_PAGE_SIZE, pageSize);
		editor.apply();
	}

	/**
	 * @return Gets the set font size increase in %
	 */
	public static float getFontSize()
	{
		return mFontSizes[fontSizeIndex];
	}

	/**
	 * @return The font size index from {@link #mFontSizes}
	 */
	public static int getFontSizeIndex()
	{
		return fontSizeIndex;
	}

	/**
	 * @param enabled True to enable notifcations, false to disable
	 */
	public void setNotificationFollowingEnabled(boolean enabled)
	{
		notificationsOnlyFollowing = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_NOTIFICATIONS_FOLLOWING, enabled);
		editor.apply();
	}

	/**
	 * @param enabled True to enable notifcations, false to disable
	 */
	public void setNotificationsEnabled(boolean enabled)
	{
		notificationsEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_NOTIFICATIONS, enabled);
		editor.apply();
	}

	/**
	 * @param enabled  True to enable notifcation sound, false to disable
	 */
	public void setNotificationsSoundEnabled(boolean enabled)
	{
		notificationsSoundEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_NOTIFICATIONS_SOUND, enabled);
		editor.apply();
	}

	/**
	 * @param enabled True to enable inline images, false to disable
	 */
	public void setInlineImagesEnabled(boolean enabled)
	{
		inlineImagesEnabled = enabled;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_INLINE_IMAGES, enabled);
		editor.apply();
	}

	/**
	 * @param visible True to enable directed posts, false to disable
	 */
	public void setShowDirectedPosts(boolean visible)
	{
		showDirectedPosts = visible;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_DIRECTED_POSTS_VISIBLE, visible);
		editor.apply();
	}

	/**
	 * @param visible True to enable directed posts, false to disable
	 */
	public void setShowDirectedMentions(boolean visible)
	{
		showDirectedMentions = visible;
		Editor editor = mPrefs.edit();
		editor.putBoolean(Constants.PREFS_DIRECTED_MENTIONS_VISIBLE, visible);
		editor.apply();
	}

	/**
	 * @return String array of saved tags
	 */
	public static String[] getSavedTags()
	{
		return savedTags.split(",");
	}

	/**
	 * @param tag The tag to check if is saved
	 * @return True if saved, false if not
	 */
	public static boolean isTagSaved(String tag)
	{
		return savedTags.contains("," + tag + ",") || savedTags.startsWith(tag + ",");
	}

	/**
	 * Saves a tag to shared preferences
	 * @param tag The tag to save
	 */
	public void saveTag(String tag)
	{
		if (!isTagSaved(tag))
		{
			savedTags = savedTags += tag + ",";
			mPrefs.edit().putString(Constants.PREFS_SAVED_TAGS, savedTags).commit();
		}
	}

	/**
	 * Unsaves a tag from preferences
	 * @param tag The tag to unsave
	 */
	public void unsaveTag(String tag)
	{
		if (isTagSaved(tag))
		{
			savedTags = savedTags.replace(tag + ",", "");
			mPrefs.edit().putString(Constants.PREFS_SAVED_TAGS, savedTags).commit();
		}
	}

	/**
	 * @return A string array of muted tags
	 */
	public static String[] getMutedTags()
	{
		return mutedTags.split("[,]");
	}

	/**
	 * Checks if a tag is muted
	 * @param tag The tag to check
	 * @return True if muted, false if not
	 */
	public static boolean isTagMuted(String tag)
	{
		return mutedTags.contains("," + tag + ",") || mutedTags.startsWith(tag + ",");
	}

	/**
	 * Mutes a tag
	 * @param tag The tag to mute
	 */
	public void muteTag(String tag)
	{
		tag = tag.replace("#", "");
		if (!isTagMuted(tag))
		{
			mutedTags = mutedTags += tag + ",";
			mPrefs.edit().putString(Constants.PREFS_MUTED_TAGS, mutedTags).commit();
		}
	}

	/**
	 * Unmutes a tag
	 * @param tag The tag to unmute
	 */
	public void unmuteTag(String tag)
	{
		tag = tag.replace("#", "");
		if (isTagMuted(tag))
		{
			mutedTags = mutedTags.replace(tag + ",", "");
			mPrefs.edit().putString(Constants.PREFS_MUTED_TAGS, mutedTags).commit();
		}
	}

	/**
	 * @return Gets a string array of muted thread IDs
	 */
	public static String[] getMutedThreads()
	{
		return mutedThreads.split(",");
	}

	/**
	 * Checks if a thread is muted
	 * @param id The id of the thread
	 * @return True if the thread is muted, false if not
	 */
	public static boolean isThreadMuted(String id)
	{
		return mutedThreads.contains("," + id + ",") || mutedThreads.startsWith(id + ",");
	}

	/**
	 * Mutes a thread
	 * @param id The ID of the thread (Note: not the post id, the "thread_id")
	 */
	public void muteThread(String id)
	{
		if (!isThreadMuted(id))
		{
			mutedThreads = mutedThreads += id + ",";
			mPrefs.edit().putString(Constants.PREFS_MUTED_THREADS, mutedThreads).commit();
		}
	}

	/**
	 * Unmutes a thread
	 * @param id The ID of the thread (Note: not the post id, the "thread_id")
	 */
	public void unmuteThread(String id)
	{
		if (isThreadMuted(id))
		{
			mutedThreads = mutedThreads.replace(id + ",", "");
			mPrefs.edit().putString(Constants.PREFS_MUTED_THREADS, mutedThreads).commit();
		}
	}

	/**
	 * @return The current settings in a string format
	 */
	public static String dump()
	{
		try
		{
			String dump = "SettingsManager:\r\n[";
			Field[] fields = SettingsManager.class.getDeclaredFields();
			List<Field> staticFields = new ArrayList<Field>();
			for (Field field : fields)
			{
				if (java.lang.reflect.Modifier.isStatic(field.getModifiers()))
				{
					dump += "\r\n\t" + field.getName() + " = " + field.get(null);
				}
			}

			dump += "\r\n]";
			return dump;
		}
		catch (Exception e)
		{
			Debug.out(e);
			return "";
		}
	}
}