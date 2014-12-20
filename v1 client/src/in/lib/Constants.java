package in.lib;

import in.rob.client.R;
import lombok.Getter;

/**
 * Constants static holders to use throughout the app
 */
public class Constants
{
	/**************************************************
	 *
	 *  RESPONSE HANDLER KEYS
	 *
	 **************************************************/
	public static final String RESPONSE_TIMELINE = "timeline_%s";
	public static final String RESPONSE_TIMELINE_MISSING_POSTS = "timeline_missing_%s";
	public static final String RESPONSE_GLOBAL_MISSING_POSTS = "global_missing";
	public static final String RESPONSE_GLOBAL = "global";
	public static final String RESPONSE_MENTIONS = "mentions_%s";
	public static final String RESPONSE_PROFILE_POSTS = "profile_posts_%s";
	public static final String RESPONSE_PROFILE_USER = "profile_user_%s";
	public static final String RESPONSE_STARRED = "starred_%s";
	public static final String RESPONSE_TAG_SEARCH = "tag_search_%s";
	public static final String RESPONSE_THREAD = "thread_%s";

	public static final String RESPONSE_MUTED = "muted";
	public static final String RESPONSE_REPOSTED = "reposted_%s";
	public static final String RESPONSE_FRIENDS = "user_following_followers_%s";
	public static final String RESPONSE_USER_SEARCH = "user_search_%s";

	public static final String RESPONSE_CHANNELS = "channels_%s";
	public static final String RESPONSE_MESSAGES = "messages_%s";

	/**************************************************
	 *
	 *  MISC API
	 *
	 **************************************************/
	public static final String API_BETA_URL = "";
	public static final String API_BETA_CHECK = "";

	public static final String API_NOTIFICATION_URL = "";
	public static final String API_NOTIFICATION_VERSION = "";
	public static final String API_NOTIFICATION_USERS = "users/";
	public static final String API_NOTIFICATION_DEVICES = "devices/";

	/**************************************************
	 *
	 *	ACTIONS
	 *
	 **************************************************/
	public static final String ACTION_NEW_POST = "in.rob.posting.new";
	public static final String ACTION_DELETE_POST = "in.rob.posting.delete";
	public static final String ACTION_REPOST = "in.rob.posting.repost";
	public static final String ACTION_STAR_POST = "in.rob.posting.star";
	public static final String ACTION_NEW_MESSAGE = "in.rob.posting.new_message";
	public static final String ACTION_DELETE_MESSAGE = "in.rob.posting.delete_message";
	public static final String ACTION_NEW_CHANNEL = "in.rob.posting.new_channel";

	public static final String ACTION_INTENT_NEW_POST = "in.rob.client.widget.intent.NEW_POST";
	public static final String ACTION_INTENT_OPEN_APP = "in.rob.client.widget.intent.OPEN_APP";
	public static final String ACTION_INTENT_THREAD = "in.rob.client.widget.intent.THREAD";
	public static final String ACTION_INTENT_REFRESH = "in.rob.client.widget.intent.REFRESH";
	public static final String ACTION_INTENT_RELOAD = "in.rob.client.widget.intent.RELOAD";

	/**************************************************
	 *
	 *	EXTRAS
	 *
	 **************************************************/
	public static final String EXTRA_DELETE = "delete";
	public static final String EXTRA_FORCE_REFRESH = "force_refresh";
	public static final String EXTRA_MENTION_NAME = "name";
	public static final String EXTRA_MUTED_LIST = "muted";
	public static final String EXTRA_POST = "post";
	public static final String EXTRA_POST_DRAFT = "cached_post";
	public static final String EXTRA_POST_ID = "post_id";
	public static final String EXTRA_ADAPTER_LIST = "adapter_list";
	public static final String EXTRA_RELATED_POST = "related_post";
	public static final String EXTRA_REPOST = "repost";
	public static final String EXTRA_REPLY_TO = "reply_to";
	public static final String EXTRA_REPLY_TO_EXTRA = "other_replies";
	public static final String EXTRA_RESEND = "resend";
	public static final String EXTRA_START_PAGE = "start_tab";
	public static final String EXTRA_START_TAB = "start";
	public static final String EXTRA_TAG_NAME = "tag_name";
	public static final String EXTRA_TEXT = "text";
	public static final String EXTRA_IMAGE = "image";
	public static final String EXTRA_USER = "user";
	public static final String EXTRA_USER_ID = "user_id";
	public static final String EXTRA_USER_LIST = "user_list";
	public static final String EXTRA_USER_NAME = "user_name";
	public static final String EXTRA_USER_COVER_URL = "user_cover_url";
	public static final String EXTRA_REFRESH_LIST = "refresh_list";
	public static final String EXTRA_REFRESH_ANIMATIONS = "refresh_animations";
	public static final String EXTRA_REFRESH_MUTED = "refresh_muted";
	public static final String EXTRA_REFRESH_TIMELINE = "refresh_timeline";
	public static final String EXTRA_REFRESH_ALL_DATA = "refresh_all_data";
	public static final String EXTRA_REFRESH_INLINE = "refresh_inline";
	public static final String EXTRA_REFRESH_FONTS = "refresh_fonts";
	public static final String EXTRA_REFRESH_NAMES = "refresh_names";
	public static final String EXTRA_REFRESH_GLOBAL = "refresh_global";
	public static final String EXTRA_REFRESH_COVER = "refresh_cover";
	public static final String EXTRA_NEW_USER = "new_user";
	public static final String EXTRA_SELECT_USER = "select_user";
	public static final String EXTRA_REFRESH_TIMES = "refresh_times";
	public static final String EXTRA_OPEN_THREAD = "thread_details";
	public static final String EXTRA_OPEN_EDIT_PROFILE = "open_profile";
	public static final String EXTRA_SHOWING_PROGRESS = "showing_progress";
	public static final String EXTRA_CENTER_POST = "center_post";
	public static final String EXTRA_NAME_DIALOG = "showing_dialog";
	public static final String EXTRA_NEW_POST_DRAFT = "new_post_draft";
	public static final String EXTRA_NEW_POST_SKIP_IMAGE = "skip_image";
	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_CENTER_POST_ID = "center_post_id";
	public static final String EXTRA_PAGE_NUMBER = "current_page";
	public static final String EXTRA_LAST_ID = "last_id";
	public static final String EXTRA_IS_LOADED = "loaded";
	public static final String EXTRA_REFRESH_TIMELINE_COVER = "timeline_cover_refresh";
	public static final String EXTRA_CHANNEL_ID = "channel_id";
	public static final String EXTRA_MESSAGE = "message";
	public static final String EXTRA_MESSAGE_ID = "message_id";
	public static final String EXTRA_CHANNEL = "channel";
	public static final String EXTRA_CHANNEL_NAME = "channel_name";
	public static final String EXTRA_CLEAR_DASH = "clear_dash";
	public static final String EXTRA_CENTER_MESSAGE = "center_message";
	public static final String EXTRA_PREVIEW_URL = "preview_url";
	public static final String EXTRA_WEB_URL = "web_url";
	public static final String EXTRA_IS_PUBLIC = "public_room";
	public static final String EXTRA_IMAGE_POSITION = "image_pos";
	public static final String EXTRA_NOTIFICATION_ID = "notification_id";
	public static final String EXTRA_MODE = "mode";

	/**************************************************
	 *
	 *	ACTIVITY REQUEST
	 *
	 **************************************************/
	public static final int REQUEST_DELETE_POST = 0x501;
	public static final int REQUEST_NEW_POST = 0x500;
	public static final int REQUEST_RESULT = 0x502;
	public static final int REQUEST_REPLY_POST = 0x504;
	public static final int REQUEST_SETTINGS = 0x503;
	public static final int REQUEST_PROFILE = 0x505;
	public static final int REQUEST_HASHTAG_SEARCH = 0x506;
	public static final int REQUEST_HASHTAG = 0x507;
	public static final int REQUEST_CAMERA = 0x508;
	public static final int REQUEST_GALLERY = 0x509;
	public static final int REQUEST_RINGTONE = 0x50A;
	public static final int REQUEST_CAMERA_AVATAR = 0x50B;
	public static final int REQUEST_GALLERY_AVATAR = 0x50C;
	public static final int REQUEST_CAMERA_COVER = 0x50D;
	public static final int REQUEST_GALLERY_COVER = 0x50E;
	public static final int REQUEST_IMAGE_CROP = 0x50F;
	public static final int REQUEST_COVER_CROP = 0x510;
	public static final int REQUEST_CODE_AUTHORIZE = 0x511;

	/**************************************************
	 *
	 *	ACTIVITY RESULTS
	 *
	 **************************************************/
	public static final int RESULT_REFRESH = 0x214;

	/**************************************************
	 *
	 *	CACHE NAMES
	 *
	 **************************************************/
	public static final String CACHE_GLOBAL_LIST_NAME = "global";
	public static final String CACHE_MENTION_LIST_NAME = "mention_%s";
	public static final String CACHE_MUTED_LIST_NAME = "muted_%s";
	public static final String CACHE_USER_TIMELINE_LIST_NAME = "user_timeline_%s";
	public static final String CACHE_STARRED_LIST_NAME = "starred_%s";
	public static final String CACHE_TIMELINE_LIST_NAME = "timeline_%s";
	public static final String CACHE_USER_LIST_NAME = "user_%s_%s";
	public static final String CACHE_LINKED_ACCOUNTS = "linked_accounts";
	public static final String CACHE_USER = "user_%s";
	public static final String CACHE_CURRENT_LOCATION = "current_location";
	public static final String CACHE_NAME_HISTORY = "name_history";
	public static final String CACHE_USERNAMES = "suggest_users_list";
	public static final String CACHE_HASHTAGS = "suggest_tags";
	public static final String CACHE_USERNAMES_STR = "suggest_users_str";
	public static final String CACHE_HASHTAGS_STR = "suggest_tags_str";
	public static final String CACHE_MESSAGE_LIST_NAME = "messages_%s";
	public static final String CACHE_CHANNELS_LIST_NAME = "channels_%s";
	public static final String CACHE_AUTH = "auth";
	public static final String CACHE_DRAFT_PREFIX = "draft";
	public static final String CACHE_DRAFT_POST = CACHE_DRAFT_PREFIX + "_%s_%s";

	/**
	 * Stream list enum to use when selecting a stream to display in
	 * a widget or pinned fragment.
	 */
	public static enum StreamList
	{
		TIMELINE(Constants.CACHE_TIMELINE_LIST_NAME, R.string.timeline),
		MENTIONS(Constants.CACHE_MENTION_LIST_NAME, R.string.mentions),
		GLOBAL(Constants.CACHE_GLOBAL_LIST_NAME, R.string.global);
		//USER_FOLLOWERS,
		//USER_FOLLOWING,
		//THREAD,
		//CHANNEL,
		//USER_TIMELINE(Constants.CACHE_USER_TIMELINE_LIST_NAME, -1);

		private String fileName;
		@Getter private int labelRes;

		private StreamList(String cacheFile, int labelRes)
		{
			this.fileName = cacheFile;
			this.labelRes = labelRes;
		}

		@Override public String toString()
		{
			return this.fileName;
		}

		public static StreamList getStreamFromString(String name)
		{
			StreamList[] values = values();
			for (StreamList value : values)
			{
				if (value.toString().equals(name))
				{
					return value;
				}
			}

			return null;
		}
	}

	/**************************************************
	 *
	 *	PREFERENCE KEYS
	 *
	 **************************************************/
	public static final String PREFS_HAS_BETA = "has_beta";
	public static final String PREFS_SETTINGS_KEY = "general_settings";
	public static final String PREFS_ANIMATIONS = "animations";
	public static final String PREFS_DIRECTED_POSTS_VISIBLE = "@_directed_posts_visible";
	public static final String PREFS_DIRECTED_MENTIONS_VISIBLE = "@_directed_mentions_visible";
	public static final String PREFS_INLINE_IMAGES = "inline_images";
	public static final String PREFS_NOTIFICATIONS = "notification";
	public static final String PREFS_NOTIFICATIONS_SOUND = "notification_sound";
	public static final String PREFS_NOTIFICATIONS_FOLLOWING = "notification_following";
	public static final String PREFS_FONT_SIZE = "font_sizes";
	public static final String PREFS_PAGE_SIZE = "page_size";
	public static final String PREFS_LONG_DATE = "show_long_date";
	public static final String PREFS_SELECTED_USER = "selected_user";
	public static final String PREFS_NAME_DISPLAY = "username_display";
	public static final String PREFS_AVATARS_ENABLED = "avatars_enabled";
	public static final String PREFS_RECENT_SEARCH = "recent_search";
	public static final String PREFS_NOTIFICATION_ID = "notification_id";
	public static final String PREFS_NOTIFICATION_COUNT = "notification_count";
	public static final String PREFS_NOTIFICATION_PREVIEW_LINES = "notification_preview_lines";
	public static final String PREFS_CACHE_TIMEOUT = "cache_timeout";
	public static final String PREFS_GLOBAL_ENABLED = "global_enabled";
	public static final String PREFS_USING_UNIFIED = "using_unified";
	public static final String PREFS_QUIET_MODE_ENABLED = "quiet_mode_enabled";
	public static final String PREFS_QUIET_MODE_FROM = "quiet_mode_from";
	public static final String PREFS_QUIET_MODE_TO = "quiet_mode_to";
	public static final String PREFS_ANALYTICS_ENABLED = "analytics_enabled";
	public static final String PREFS_CRASH_REPORTING_ENABLED = "crash_reporting_enabled";
	public static final String PREFS_TIMELINE_COVER = "timeline_cover";
	public static final String PREFS_NOTIFICATION_TONE = "notification_tone";
	public static final String PREFS_IMAGE_PROVIDER = "image_provider";
	public static final String PREFS_TIMELINE_BREAK = "timeline_break";
	public static final String PREFS_DASH_USER_ID = "dash_user_id";
	public static final String PREFS_SHAKE_REFRESH_ENABLED = "shake_refresh_enabled";
	public static final String PREFS_QUICK_POST_ENABLED = "quick_post_enabled";
	public static final String PREFS_NOTIFICATION_LED_ENABLED = "notification_led_enabled";
	public static final String PREFS_NOTIFICATION_VIBRATE = "notification_vibrate";
	public static final String PREFS_THEME = "theme";
	public static final String PREFS_STREAM_MARKERS_ENABLED = "stream_markers_enabled";
	public static final String PREFS_STREAM_MARKERS = "stream_markers";
	public static final String PREFS_REQUEST_TIMEOUT = "request_timeout";
	public static final String PREFS_KEYWORD_SEARCH_ENABLED = "keyword_search_enabled";
	public static final String PREFS_INVERT_POST_CLICK_ENABLED = "invert_post_enabled";
	public static final String PREFS_LIGHTBOX_ENABLED = "lightbox_enabled";
	public static final String PREFS_IMAGE_VIEWER_ENABLED = "image_viewer_enabled";
	public static final String PREFS_NOTIFICATIONS_OPTIONS = "notification_options";
	public static final String PREFS_SWARM_PROTECTION = "swarm_protection";
	public static final String PREFS_CUSTOM_FONTS = "custom_fonts_enabled";
	public static final String PREFS_SCROLL_WIDGET_USER_ID = "scroll_widget_user_id";
	public static final String PREFS_INLINE_IMAGE_WIFI = "inline_image_wifi";
	public static final String PREFS_CACHE_SIZE = "cache_size";
	public static final String PREFS_IMAGE_CACHE_SIZE = "image_cache_size";
	public static final String PREFS_LOCALE = "app_locale";
	public static final String PREFS_DEFAULT_LOCALE = "default_app_locale";
	public static final String PREFS_POST_EMPHASIS = "post_emphasis";
	public static final String PREFS_WEB_READABILITY = "web_readability";
	public static final String PREFS_SINGLE_CLICK_LINKS = "single_click_link";

	public static final String PREFS_SAVED_TAGS = "saved_hash";
	public static final String PREFS_MUTED_TAGS = "muted_hash";
	public static final String PREFS_MUTED_THREADS = "muted_threads";

	public static final String PREFS_POST = "post_prefs";
	public static final String PREFS_POST_IMAGE_KEY = "post_image_uri";
	public static final String PREFS_POST_USE_LOCATION = "use_location";
	public static final String PREFS_KEY_BLACK_LISTED = "black_listed";
	public static final String PREFS_CD_KEY = "cd_key";

	public static final String PREFS_MESSAGE_LENGTH = "pm_length";
	public static final String PREFS_POST_LENGTH = "post_length";
	public static final String PREFS_BIO_LENGTH = "bio_length";
	public static final String PREFS_POST_ID_LENGTH = "post_id_length";
	public static final String PREFS_MESSAGE_ID_LENGTH = "message_id_length";

	public static final String PREFS_USER_SETTINGS = "user_settings_prefs";
	public static final String PREFS_USER_SETTINGS_IMAGE_KEY = "settings_image_uri";

	public static final String PREFS_TIMELINE_TOP_POSITION = "timeline_top_position_%s";
	public static final String PREFS_TIMELINE_TOP_POSITION_Y = "timeline_top_position_y_%s";

	public static final String PREFS_GLOBAL_TOP_POSITION = "global_top_position_%s";
	public static final String PREFS_GLOBAL_TOP_POSITION_Y = "global_top_position_y_%s";

	/**************************************************
	 *
	 *	INSTANCE KEYS
	 *
	 **************************************************/
	public static final String INSTANCE_POSTS = "posts";
	public static final String INSTANCE_USERS = "users";

	/**************************************************
	 *
	 *	NOTIFICATION IDS
	 *
	 **************************************************/
	public static int SEND_NOTIFICATION_ID = 0x453;
	public static int FAILED_NOTIFICATION_ID = 0x454;

	/**************************************************
	 *
	 *	MENU IDS
	 *
	 **************************************************/
	public static final int MENU_MENTION_ID = 0x30;
	public static final int MENU_MORE_ID = 0x50;
	public static final int MENU_MUTE_ID = 0x30;
	public static final int MENU_NEW_POST_ID = 0x20;
	public static final int MENU_SAVE_ID = 0x10;
	public static final int MENU_UNMUTE_ID = 0x31;
	public static final int MENU_UNSAVE_ID = 0x11;

	/**************************************************
	 *
	 *	BIT CONSTANTS
	 *
	 **************************************************/
	public static final int BIT_ANIMATION_LIST = 0x0000001;
	public static final int BIT_ANIMATION_INLINE_IMAGE = 0x0000002;
	public static final int BIT_ANIMATION_COVER_IMAGE = 0x0000004;
	public static final int BIT_ANIMATION_PAGINATION = 0x0000008;

	public static final int BIT_NOTIFICATION_MENTION = 0x0000001;
	public static final int BIT_NOTIFICATION_MESSAGE = 0x0000002;
	public static final int BIT_NOTIFICATION_PATTER_PM = 0x0000004;
	public static final int BIT_NOTIFICATION_FOLLOW = 0x0000008;
	public static final int BIT_NOTIFICATION_REPOST = 0x0000010;
	public static final int BIT_NOTIFICATION_STAR = 0x0000020;

	public static final int BIT_EMPHASIS_ITALIC = 0x0000001;
	public static final int BIT_EMPHASIS_BOLD = 0x0000002;
	public static final int BIT_EMPHASIS_UNDERLINE = 0x0000004;

	public static final int BIT_LINK_HASHTAG = 0x0000001;
	public static final int BIT_LINK_MENTION = 0x0000002;
	public static final int BIT_LINK_URL = 0x0000004;

	public static final int BIT_STREAM_MARKER_ENABLED = 0x0000001;
	public static final int BIT_STREAM_MARKER_PAST = 0x0000002;
}