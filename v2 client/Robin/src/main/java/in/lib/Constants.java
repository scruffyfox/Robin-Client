package in.lib;

public class Constants
{
	// API Constants //
	public static final String API_URL = "https://alpha-api.app.net/";
	public static final String API_STREAM = "stream/";
	public static final String API_VERSION = "0/";
	public static final String API_AUTH = "https://alpha.app.net/oauth/access_token";

	public static final String API_POSTS = "posts";
	public static final String API_TIMELINE_STREAM = API_POSTS + "/stream/";
	public static final String API_UNIFIED_TIMELINE_STREAM = API_TIMELINE_STREAM + "/unified";
	public static final String API_POST_THREAD = API_POSTS + "/%s/replies";
	public static final String API_POST_REPOST = API_POSTS + "/%s/repost";
	public static final String API_SEARCH_POST = API_POSTS + "/search";

	public static final String API_USERS = "users";
	public static final String API_USER_DETAILS = API_USERS + "/%s/";
	public static final String API_USER_MENTIONS = API_USER_DETAILS + "mentions";
	public static final String API_USER_POSTS = API_USER_DETAILS + "posts";
	public static final String API_USER_FOLLOWERS = API_USER_DETAILS + "followers";
	public static final String API_USER_FOLLOWING = API_USER_DETAILS + "following";
	public static final String API_USER_FILES = API_USER_DETAILS + "files";
	public static final String API_USER_STARRED = API_USER_DETAILS + "stars";
	public static final String API_USER_INTERACTIONS = API_USERS + "/me/interactions";
	public static final String API_SEARCH_USER = API_USERS + "/search";

	public static final String API_CHANNELS = "channels";
	public static final String API_CHANNEL_MESSAGES = API_CHANNELS + "/%s/messages";
	public static final String API_CHANNEL_SUBSCRIBE = API_CHANNELS + "/%s/subscribe";
	public static final String API_CHANNEL_MESSAGE_DETAILS = API_CHANNEL_MESSAGES + "/%s";

	public static final String API_SCOPES = "stream email write_post follow messages update_profile files";
	public static final String API_SCOPES_CSV = "stream,email,write_post,follow messages,update_profile,files";

	public static final String API_COUNT = "count";
	public static final String API_BEFORE_ID = "before_id";
	public static final String API_SINCE_ID = "since_id";
	public static final String API_ACCESS_TOKEN = "access_token";
	public static final String API_INCLUDE_DELETED = "include_deleted";
	public static final String API_INCLUDE_ANNOTATIONS = "include_annotations";
	public static final String API_INCLUDE_DIRECTED_POSTS = "include_directed_posts";
	public static final String API_INCLUDE_STARRED = "include_starred_by";
	public static final String API_INCLUDE_REPOSTERS = "include_reposters";
	public static final String API_REPLY_TO = "reply_to";
	public static final String API_REPOST = "repost_of";
	public static final String API_TEXT = "text";
	public static final String API_CHANNEL_ID = "channel_id";
	public static final String API_NAME = "name";
	public static final String API_DESC = "description";
	public static final String API_LOCALE = "locale";
	public static final String API_TIMEZONE = "timezone";
	public static final String API_CHANNEL_TYPES = "channel_types";
	public static final String API_ALLOW_RECENT_MESSAGE = "include_recent_message";
	public static final String API_INTERACTIONS = "interaction_actions";

	public static final String CLIENT_TOKEN = "";
	public static final String PASSWORD_GRANT = "";

	public static final String CHANNEL_TYPES = "net.app.core.pm,net.patter-app.room";

	// Preference constants //
	public static final String PREFS_AUTH = "in.rob.client.auth";
	public static final String PREFS_VERSION = "version";
	public static final String PREFS_AUTH_USER_ACCESS_TOKEN = PREFS_AUTH + "user_%s_access_token";
	public static final String PREFS_AUTH_SELECTED_USER = PREFS_AUTH + "selected_user";
	public static final String PREFS_USERNAME_TITLE = "user_title";
	public static final String PREFS_COLLAPSED_THREADS = "collapsed_threads";
	public static final String PREFS_SHAKE_REFRESH_ENABLED = "shake_refresh_enabled";
	public static final String PREFS_QUICK_POST_ENABLED = "quick_post_enabled";
	public static final String PREFS_INLINE_WIFI_ENABLED = "inline_image_wifi_enabled";
	public static final String PREFS_STREAM_MARKERS = "stream_markers";
	public static final String PREFS_SINGLE_CLICK_OPTIONS = "single_click_options";
	public static final String PREFS_SHOWHIDE_OPTIONS = "showhide_options";
	public static final String PREFS_IN_APP_VIEWER_OPTIONS = "in_app_viewer_options";
	public static final String PREFS_WEB_READABILITY_MODE_ENABLED = "web_readbility_mode_enabled";
	public static final String PREFS_NON_FOLLOWING_MENTIONS_ENABLED = "non_following_mentions_enabled";
	public static final String PREFS_EMPHASIS_OPTIONS = "emphasis_options";

	// Response constants //
	public static final String RESPONSE_TIMELINE = "timeline_%s";
	public static final String RESPONSE_MENTIONS = "mentions_%s";
	public static final String RESPONSE_FOLLOWERS = "followers_%s";
	public static final String RESPONSE_FOLLOWINGS = "followings_%s";
	public static final String RESPONSE_USER_STREAM = "user_%s_stream";
	public static final String RESPONSE_USER = "user_%s";
	public static final String RESPONSE_THREAD = "thread_%s";
	public static final String RESPONSE_CHANNELS = "channels_%s";
	public static final String RESPONSE_CHANNEL_MESSAGES = "channels_messages_%s";
	public static final String RESPONSE_INTERACTIONS = "interactions";
	public static final String RESPONSE_STARRED = "user_%s_starred";
	public static final String RESPONSE_SEARCH_POST = "search_%s";
	public static final String RESPONSE_SEARCH_USER = "search_user_%s";

	// Request contstants //
	public static final int REQUEST_CODE_AUTHORIZE = 0x01;

	// Cache constants //
	public static final String CACHE_TIMELINE = "timeline_%s";
	public static final String CACHE_MENTIONS = "mentions_%s";
	public static final String CACHE_FOLLOWERS = "followers_%s";
	public static final String CACHE_FOLLOWINGS = "followings_%s";
	public static final String CACHE_USER_STREAM = "user_%s_stream";
	public static final String CACHE_USER = "user_%s";
	public static final String CACHE_THREAD = "thread_%s";
	public static final String CACHE_CHANNELS = "channels_%s";
	public static final String CACHE_CHANNEL_MESSAGES = "channel_messages_%s";
	public static final String CACHE_INTERACTIONS = "interactions";
	public static final String CACHE_STARRED = "user_%s_starred";
	public static final String CACHE_AUTOCOMPLETE_USERNAMES = "autocomplete_usernames";
	public static final String CACHE_AUTOCOMPLETE_HASHTAGS = "autocomplete_hashtags";

	// Extra constants //
	public static final String EXTRA_ADAPTER_LIST = "adapter_list";
	public static final String EXTRA_USER = "user";
	public static final String EXTRA_POST = "post";
	public static final String EXTRA_TITLE = "title";
	public static final String EXTRA_REPLY_ALL = "reply_all?";
	public static final String EXTRA_REPLY_EXTRA = "reply_extra";
	public static final String EXTRA_FILE = "file";
	public static final String EXTRA_CHANNEL = "channel";
	public static final String EXTRA_CHANNEL_ID = "channel_id";
	public static final String EXTRA_START_PAGE = "start_page";
	public static final String EXTRA_SEARCH_TERM = "search_term";
	public static final String EXTRA_DRAFT_POST = "draft_post";
	public static final String EXTRA_SEND = "force_send";
	public static final String EXTRA_NEW_USER = "new_user";
	public static final String EXTRA_FINISH = "finish";
	public static final String EXTRA_PREVIEW_URL = "preview_url";

	public static final int QUICK_POST_ID = 0x83198712;

	// Request constants //
	public static final int REQUEST_STORAGE = 0x001;
	public static final int REQUEST_GALLERY = 0x002;
	public static final int REQUEST_CAMERA = 0x003;
	public static final int REQUEST_ADD_ACCOUNT = 0x004;

	// Bit constants //
	public static final int BIT_STREAM_MARKER_ENABLED = 0x0000001;
	public static final int BIT_STREAM_MARKER_PAST = 0x0000002;

	public static final int BIT_SINGLE_CLICK_HASHTAG = 0x0000001;
	public static final int BIT_SINGLE_CLICK_MENTIONS = 0x0000002;
	public static final int BIT_SINGLE_CLICK_URLS = 0x0000004;

	public static final int BIT_SHOWHIDE_AVATARS = 0x0000001;
	public static final int BIT_SHOWHIDE_INLINE_IMAGES = 0x0000002;
	public static final int BIT_SHOWHIDE_TIMELINE_COVER = 0x0000004;

	public static final int BIT_IN_APP_VIEWER_BROWSER = 0x0000001;
	public static final int BIT_IN_APP_VIEWER_IMAGE = 0x0000002;
	public static final int BIT_IN_APP_VIEWER_YOUTUBE = 0x0000004;

	public static final int BIT_EMPHASIS_ITALIC = 0x0000001;
	public static final int BIT_EMPHASIS_BOLD = 0x0000002;
	public static final int BIT_EMPHASIS_UNDERLINE = 0x0000004;

	// Actions //
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
}
