package in.lib.manager;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.utils.StringUtils;
import in.model.DraftPost;
import in.model.SimpleUser;
import in.obj.annotation.Annotation;
import in.obj.entity.Entity;
import in.obj.entity.LinkEntity;
import in.rob.client.R;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.obj.entity.JsonEntity;
import net.callumtaylor.asynchttp.obj.entity.MultiPartEntity;
import net.callumtaylor.asynchttp.obj.entity.RequestEntity;
import net.callumtaylor.asynchttp.response.AsyncHttpResponseHandler;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Handles all api calls
 * @author CallumTaylor
 */
public class APIManager
{
	/* URL definitions */
	public static final String API_URL = "https://alpha-api.app.net/";
	public static final String API_STREAM = "stream";
	public static final String API_AUTH = "https://alpha.app.net/oauth/access_token";
	public static final String API_VERSION = "/0/";
	public static final String API_TRENDING_URL = "https://api.nanek.net/trends";
	public static final String API_SEARCH_URL = "https://api.nanek.net/search";
	public static final String API_TRENDING_KEY = "";

	public static final String API_ROBIN_URL = "";
	public static final String API_LOG = "";
	public static final String API_UPDATES = "";

	public static final String API_CONFIGURATION = "config";

	/* Post calls */
	public static final String API_POST_CREATE = "posts";
	public static final String API_POST_DETAILS = API_POST_CREATE + "/%s";
	public static final String API_POST_SEARCH = API_POST_CREATE + "/search";
	public static final String API_POST_THREAD = API_POST_CREATE + "/%s/replies";
	public static final String API_POST_REPOST = API_POST_CREATE + "/%s/repost";
	public static final String API_POST_STAR = API_POST_CREATE + "/%s/star";
	public static final String API_POST_REPOSTS = API_POST_CREATE + "/%s/reposters";
	public static final String API_POST_STARRED = API_POST_CREATE + "/%s/stars";
	public static final String API_POST_REPORT = API_POST_CREATE + "/%s/report";

	/* Timeline calls */
	public static final String API_TIMELINE_STREAM = "posts/stream";
	public static final String API_GLOBAL_TIMELINE_STREAM = API_TIMELINE_STREAM + "/global";
	public static final String API_UNIFIED_TIMELINE_STREAM = API_TIMELINE_STREAM + "/unified";
	public static final String API_TAGGED_TIMELINE_STREAM = "posts/tag/%s";

	/* Channel calls */
	public static final String API_CHANNEL_STREAM = "channels";
	public static final String API_CHANNEL_MESSAGES = "channels/%s/messages";
	public static final String API_CHANNEL_SUBSCRIBE = "channels/%s/subscribe";
	public static final String API_CHANNEL_MESSAGE_DETAILS = API_CHANNEL_MESSAGES + "/%s";

	/* User api calls */
	public static final String API_USERS = "users";
	public static final String API_USER_DETAILS = "users/%s";
	public static final String API_USER_MENTIONS = API_USER_DETAILS + "/mentions";
	public static final String API_USER_POSTS = API_USER_DETAILS + "/posts";
	public static final String API_USER_FOLLOW = API_USER_DETAILS + "/follow";
	public static final String API_USER_FOLLOWERS = API_USER_DETAILS + "/followers";
	public static final String API_USER_FOLLOWING = API_USER_DETAILS + "/following";
	public static final String API_USER_MUTE = API_USER_DETAILS + "/mute";
	public static final String API_USER_MUTED = "users/me/muted";
	public static final String API_USER_SEARCH = "users/search";
	public static final String API_USER_STARRED = API_USER_DETAILS + "/stars";
	public static final String API_USER_DETAILS_AVATAR = API_USER_DETAILS + "/avatar";
	public static final String API_USER_DETAILS_COVER = API_USER_DETAILS + "/cover";
	public static final String API_USER_BLOCK = API_USER_DETAILS + "/block";
	public static final String API_FULL_USER_AVATAR = API_URL + API_STREAM + API_VERSION + API_USER_DETAILS_AVATAR + "?avatar=1&id=%0$s";

	/* Query stubs */
	private static final String API_COUNT = "count";
	private static final String API_LAST_ID = "before_id";
	private static final String API_SINCE_ID = "since_id";
	private static final String API_ACCESS_TOKEN = "access_token";
	private static final String API_INCLUDE_DELETED = "include_deleted";
	private static final String API_INCLUDE_ANNOTATIONS = "include_annotations";
	private static final String API_INCLUDE_DIRECTED_POSTS = "include_directed_posts";
	private static final String API_INCLUDE_STARRED = "include_starred_by";
	private static final String API_INCLUDE_REPOSTERS = "include_reposters";
	private static final String API_REPLY_TO = "reply_to";
	private static final String API_REPOST = "repost_of";
	private static final String API_TEXT = "text";
	private static final String API_NAME = "name";
	private static final String API_DESC = "description";
	private static final String API_LOCALE = "locale";
	private static final String API_TIMEZONE = "timezone";

	/* Header K/V */
	private static final String API_CHANNEL_TYPES = "channel_types";
	private static final String API_ALLOW_RECENT_MESSAGE = "include_recent_message";

	/* API Authentication constants */
	public static final String CLIENT_TOKEN = "";
	public static final String PASSWORD_GRANT = "";

	/* Authentication */
	public static final String API_SCOPES = "stream email write_post follow messages update_profile files";
	public static final String API_SCOPES_CSV = "stream,email,write_post,follow messages,update_profile,files";
	private static final String CHANNEL_TYPES = "net.app.core.pm,net.patter-app.room";

	private static APIManager mAPIManager;

	/**
	 * Get the instance of APIManager or create it if it's null
	 * @return The APIManager instance
	 */
	public static APIManager getInstance()
	{
		if (mAPIManager == null)
		{
			mAPIManager = new APIManager();
		}

		return mAPIManager;
	}

	/**
	 * Logs in the user
	 * @param username The user's username or email
	 * @param password The password
	 * @param c The <b>activity</b> context of the request
	 * @param response The response from the api call
	 * @return
	 */
	public AsyncHttpClient login(String username, String password, Context c, AsyncHttpResponseHandler response)
	{
		RequestEntity params = new RequestEntity();
		params.add("client_id", CLIENT_TOKEN);
		params.add("password_grant_secret", PASSWORD_GRANT);
		params.add("grant_type", "password");
		params.add("scope", API_SCOPES);
		params.add("username", username);
		params.add("password", password);

		AsyncHttpClient mClient = new AsyncHttpClient(API_AUTH, SettingsManager.getRequestTimeout());
		mClient.post(params, response);
		return mClient;
	}

	/**
	 * Gets the global timeline
	 * @param response The response from the api call
	 */
	public void getGlobalTimeLine(String lastId, AsyncHttpResponseHandler response)
	{
		int maxCount = SettingsManager.getPageSize();
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(API_GLOBAL_TIMELINE_STREAM, params, response);
	}

	/**
	 * Gets timeline stream
	 * @param response The response from the api call
	 */
	public void getTimeLine(String lastId, AsyncHttpResponseHandler response)
	{
		int maxCount = SettingsManager.getPageSize();
		getTimeLine(lastId, maxCount, response);
	}

	/**
	 * Gets timeline stream
	 * @param response The response from the api call
	 */
	public void getTimeLine(String lastId, int maxCount, AsyncHttpResponseHandler response)
	{
		boolean includeDirectedPosts = SettingsManager.getShowDirectedPosts();
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));

		if (lastId.equals("last_read"))
		{
			params.add(new BasicNameValuePair(API_SINCE_ID, "" + lastId));
		}
		else
		{
			params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		}

		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_DIRECTED_POSTS, includeDirectedPosts ? "1" : "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(API_TIMELINE_STREAM, params, response);
	}

	/**
	 * Gets users unified timeline stream
	 * @param response The response from the api call
	 */
	public void getUnifiedTimeLine(String lastId, AsyncHttpResponseHandler response)
	{
		int maxCount = SettingsManager.getPageSize();
		getUnifiedTimeLine(lastId, maxCount, response);
	}

	/**
	 * Gets users unified timeline stream
	 * @param response The response from the api call
	 */
	public void getUnifiedTimeLine(String lastId, int maxCount, AsyncHttpResponseHandler response)
	{
		boolean includeDirectedPosts = SettingsManager.getShowDirectedPosts();
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));

		if (lastId.equals("last_read"))
		{
			params.add(new BasicNameValuePair(API_SINCE_ID, "" + lastId));
		}
		else
		{
			params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		}

		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_DIRECTED_POSTS, includeDirectedPosts ? "1" : "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(API_UNIFIED_TIMELINE_STREAM, params, response);
	}

	/**
	 * Gets missing posts from the last id in global stream
	 * @param response The response from the api call
	 */
	public void getMissingGlobal(String lastId, int maxCount, AsyncHttpResponseHandler response)
	{
		boolean includeDirectedPosts = SettingsManager.getShowDirectedPosts();
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_SINCE_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_DIRECTED_POSTS, includeDirectedPosts ? "1" : "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(API_GLOBAL_TIMELINE_STREAM, params, response);
	}

	/**
	 * Gets missing posts from unified timeline stream
	 * @param response The response from the api call
	 */
	public void getMissingUnifiedTimeLine(String lastId, int maxCount, AsyncHttpResponseHandler response)
	{
		boolean includeDirectedPosts = SettingsManager.getShowDirectedPosts();
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_SINCE_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_DIRECTED_POSTS, includeDirectedPosts ? "1" : "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(API_UNIFIED_TIMELINE_STREAM, params, response);
	}

	/**
	 * Gets missing posts from timeline stream
	 * @param response The response from the api call
	 */
	public void getMissingTimeLine(String lastId, int maxCount, AsyncHttpResponseHandler response)
	{
		boolean includeDirectedPosts = SettingsManager.getShowDirectedPosts();
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_SINCE_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_DIRECTED_POSTS, includeDirectedPosts ? "1" : "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(API_TIMELINE_STREAM, params, response);
	}

	/**
	 * Gets users mention stream
	 * @param response The response from the api call
	 */
	public void getMentions(String userId, String lastId, AsyncHttpResponseHandler response)
	{
		boolean includeDirectedMentions = SettingsManager.getShowDirectedMentions();
		int maxCount = SettingsManager.getPageSize();
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_DIRECTED_POSTS, includeDirectedMentions ? "1" : "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(String.format(API_USER_MENTIONS, userId), params, response);
	}

	/**
	 * Gets the thread of the selected post
	 * @param postId the post details to get
	 * @param response The response from the api call
	 */
	public void getPostThread(String postId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));
		params.add(new BasicNameValuePair(API_COUNT, "" + 200));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(String.format(API_POST_THREAD, postId), params, response);
	}

	/**
	 * Gets a user's starred posts
	 * @param userId The user id to search against
	 * @param lastId The last id in the list
	 * @param response The response handler
	 */
	public void getStarredPosts(String userId, String lastId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		int maxCount = SettingsManager.getPageSize();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(String.format(API_USER_STARRED, userId), params, response);
	}

	/**
	 * Gets a list of users who have reposted a post
	 * @param postId
	 * @param lastId
	 * @param response
	 */
	public void getPostReposts(String postId, String lastId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		int maxCount = SettingsManager.getPageSize();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(String.format(API_POST_REPOSTS, postId), params, response);
	}

	/**
	 * Gets the list of users who have starred a post
	 * @param postId
	 * @param lastId
	 * @param response
	 */
	public void getPostStars(String postId, String lastId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		int maxCount = SettingsManager.getPageSize();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(String.format(API_POST_STARRED, postId), params, response);
	}

	/**
	 * Gets the details of a user
	 * @param userId the user to get
	 * @param response The response from the api call
	 */
	public void getUserDetails(String userId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(String.format(API_USER_DETAILS, userId), params, response);
	}

	/**
	 * Gets the details of multiple users
	 * @param userIds the user to get
	 * @param response The response from the api call
	 */
	public void getUsers(Context c, Collection<String> userIds, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("ids", StringUtils.join(userIds, ",")));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(API_USERS, params, response);
	}

	/**
	 * Gets a user's posts
	 * @param userId The user ud
	 * @param lastId The last post
	 * @param response The api response callback
	 */
	public void getUserPosts(Context c, String userId, String lastId, AsyncHttpResponseHandler response)
	{
		int maxCount = SettingsManager.getPageSize();
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(String.format(API_USER_POSTS, userId), params, response);
	}

	/**
	 * Gets a user's muted list
	 * @param userId The user id (not yet used)
	 * @param lastId The last post
	 * @param response The api response callback
	 */
	public void getUserMuted(String userId, String lastId, AsyncHttpResponseHandler response)
	{
		int maxCount = SettingsManager.getPageSize();
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(API_USER_MUTED, params, response);
	}

	/**
	 * Gets a user's followers
	 * @param userId The user ud
	 * @param lastId The last post
	 * @param response The api response callback
	 */
	public void getUserFollowers(String userId, String lastId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		int maxCount = SettingsManager.getPageSize();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(String.format(API_USER_FOLLOWERS, userId), params, response);
	}

	/**
	 * Gets a user's followers
	 * @param userId The user ud
	 * @param lastId The last post
	 * @param response The api response callback
	 */
	public void getUserFollowing(String userId, String lastId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		int maxCount = SettingsManager.getPageSize();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(String.format(API_USER_FOLLOWING, userId), params, response);
	}

	/**
	 * Delete status
	 * @param postId The id of the post to delete
	 * @param response The response from the api call
	 */
	public void deletePost(String accessToken, String postId, AsyncHttpResponseHandler response)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.delete(String.format(API_POST_DETAILS, postId), params, response);
	}

	/**
	 * Repost a post
	 * @param message The message to repost
	 * @param response The response from the api call
	 */
	public void repost(String accessToken, String postId, AsyncHttpResponseHandler response)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		JsonObject object = new JsonObject();
		object.addProperty(API_REPOST, postId);

		try
		{
			JsonEntity postData = new JsonEntity(object);

			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.post
			(
				String.format(API_POST_REPOST, postId),
				params,
				postData,
				response
			);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	/**
	 * Reports a post
	 * @param postId The post ID to report
	 * @param response
	 */
	public void report(String postId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		try
		{
			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.post
			(
				String.format(API_POST_REPORT, postId),
				params,
				response
			);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	/**
	 * Stars a post: Note: A repost cannot be starred. Please star the parent Post.
	 * @param postId The post id to star
	 * @param response The repsonse from the api call
	 */
	public void starPost(String postId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));

		try
		{
			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.post
			(
				String.format(API_POST_STAR, postId),
				params,
				response
			);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	/**
	 * Unstars a post
	 * @param postId The post id to unstar
	 * @param response The response from the api call
	 */
	public void unstarPost(String postId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));

		try
		{
			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.delete
			(
				String.format(API_POST_STAR, postId),
				params,
				response
			);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	/**
	 * Reply to Post
	 * @param message The message to post
	 * @param postId The post ID to reply to
	 * @param entities The embeddable entity list to include
	 * @param response The response from the api call
	 */
	public void replyPost(String accessToken, DraftPost post, AsyncHttpResponseHandler response)
	{
		//String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));

		JsonObject object = new JsonObject();
		object.addProperty(API_TEXT, post.getPostText());
		object.addProperty(API_REPLY_TO, post.getReplyId());

		JsonArray annotations = new JsonArray();
		if (post.getAnnotations() != null)
		{
			for (Annotation e : post.getAnnotations())
			{
				/*
				 * if e.type == image -> upload image -> shorten link -> add annotation to jsonarray
				 */
				JsonObject annotation = e.toAnnotation().getAsJsonObject();
				if (annotation != null)
				{
					annotations.add(annotation);
				}
			}

			if (annotations.size() > 0)
			{
				object.add("annotations", annotations);
			}
		}

		if (post.getEntities() != null)
		{
			if (post.getEntities().containsKey(Entity.Type.LINK))
			{
				JsonArray linksArr = new JsonArray();

				for (Entity entity : post.getEntities().get(Entity.Type.LINK))
				{
					LinkEntity linkEntity = (LinkEntity)entity;
					JsonObject link = new JsonObject();
					link.addProperty("pos", linkEntity.getPos());
					link.addProperty("len", linkEntity.getLen());
					link.addProperty("url", linkEntity.getUrl());

					linksArr.add(link);
				}

				JsonObject links = new JsonObject();
				links.add("links", linksArr);
				links.addProperty("parse_links", true);
				object.add("entities", links);
			}
		}

		try
		{
			JsonEntity postData = new JsonEntity(object);

			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.post
			(
				String.format(API_POST_CREATE),
				params,
				postData,
				response
			);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	/**
	 * Mutes a user
	 * @param userId The user id to mute
	 * @param response The response from the api call
	 */
	public void muteUser(String userId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.post(String.format(API_USER_MUTE, userId), params, response);
	}

	/**
	 * Unmutes a user
	 * @param userId The user id to unmute
	 * @param response The response from the api call
	 */
	public void unMuteUser(String userId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.delete(String.format(API_USER_MUTE, userId), params, response);
	}

	/**
	 * Post status
	 * @param message The message to post
	 * @param response The response from the api call
	 */
	public void postStatus(String accessToken, DraftPost post, AsyncHttpResponseHandler response)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));

		JsonObject object = new JsonObject();
		object.addProperty(API_TEXT, post.getPostText());

		JsonArray annotations = new JsonArray();
		if (post.getAnnotations() != null)
		{
			for (Annotation e : post.getAnnotations())
			{
				/*
				 * if e.type == image -> upload image -> shorten link -> add annotation to jsonarray
				 */
				JsonObject annotation = e.toAnnotation().getAsJsonObject();
				if (annotation != null)
				{
					annotations.add(annotation);
				}
			}

			if (annotations.size() > 0)
			{
				object.add("annotations", annotations);
			}
		}

		if (post.getEntities() != null)
		{
			if (post.getEntities().containsKey(Entity.Type.LINK))
			{
				JsonArray linksArr = new JsonArray();

				for (Entity entity : post.getEntities().get(Entity.Type.LINK))
				{
					LinkEntity linkEntity = (LinkEntity)entity;
					JsonObject link = new JsonObject();
					link.addProperty("pos", linkEntity.getPos());
					link.addProperty("len", linkEntity.getLen());
					link.addProperty("url", linkEntity.getUrl());

					linksArr.add(link);
				}

				JsonObject links = new JsonObject();
				links.add("links", linksArr);
				links.addProperty("parse_links", true);
				object.add("entities", links);
			}
		}

		try
		{
			JsonEntity postData = new JsonEntity(object);

			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.post
			(
				String.format(API_POST_CREATE),
				params,
				postData,
				response
			);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	/**
	 * Follows a user
	 * @param userId The user to follow
	 * @param response The response from the api call
	 */
	public AsyncHttpClient followUser(String userId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.post(String.format(API_USER_FOLLOW, userId), params, response);
		return mClient;
	}

	/**
	 * Unfollows a user
	 * @param userId The user to unfollow
	 * @param response The response from the api call
	 */
	public AsyncHttpClient unfollowUser(String userId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.delete(String.format(API_USER_FOLLOW, userId), params, response);
		return mClient;
	}

	/**
	 * Blocks a user
	 * @param userId The user to block
	 * @param response The response from the api call
	 */
	public void blockUser(String userId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.post(String.format(API_USER_BLOCK, userId), params, response);
	}

	/**
	 * Unblocks a user
	 * @param userId The user to unblock
	 * @param response The response from the api call
	 */
	public void unblockUser(String userId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.delete(String.format(API_USER_BLOCK, userId), params, response);
	}

	/**
	 * Updates the current user's data
	 * @param name The users full name
	 * @param descriptionText The user's bio description
	 * @param locale The user's locale
	 * @param timezone The user's timezone
	 * @param response
	 */
	public void updateUser(String name, String descriptionText, String locale, String timezone, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();

		JsonObject object = new JsonObject();
		object.addProperty(API_NAME, name);
		object.addProperty(API_LOCALE, locale);
		object.addProperty(API_TIMEZONE, timezone);

		JsonObject desc = new JsonObject();
		desc.addProperty("text", descriptionText);
		object.add(API_DESC, desc);

		try
		{
			JsonEntity postData = new JsonEntity(object);

			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.put
			(
				String.format(API_USER_DETAILS, "me") + "?access_token=" + accessToken,
				postData,
				response
			);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	/**
	 * Gets the list of channels the user is subscribed to
	 * @param lastId The last id for pagination
	 * @param response The response handler
	 */
	public void getMessageChannels(String lastId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		int maxCount = SettingsManager.getPageSize();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_CHANNEL_TYPES, CHANNEL_TYPES));
		params.add(new BasicNameValuePair(API_ALLOW_RECENT_MESSAGE, "1"));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout() * 2);
		mClient.get(API_CHANNEL_STREAM, params, response);
	}

	/**
	 * Gets the messages in a channel
	 * @param channelId The channel Id
	 * @param lastId The id of the last post
	 * @param response The response from the api call
	 */
	public void getMessages(String channelId, String lastId, AsyncHttpResponseHandler response)
	{
		int maxCount = SettingsManager.getPageSize();
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_DIRECTED_POSTS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(String.format(API_CHANNEL_MESSAGES, channelId), params, response);
	}

	/**
	 * Post message
	 * @param accessToken
	 * @param channelId The id of the channel to post do
	 * @param message The message to post
	 * @param response The response from the api call
	 */
	public void postMessage(String accessToken, DraftPost post, AsyncHttpResponseHandler response)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));

		JsonObject object = new JsonObject();
		object.addProperty(API_TEXT, post.getPostText());
		object.addProperty("channel_id", post.getChannelId());

		JsonArray annotationsArr = new JsonArray();
		if (post.getAnnotations() != null)
		{
			for (Annotation e : post.getAnnotations())
			{
				/*
				 * if e.type == image -> upload image -> shorten link -> add annotation to jsonarray
				 */
				JsonObject annotation = e.toAnnotation().getAsJsonObject();
				if (annotation != null)
				{
					annotationsArr.add(annotation);
				}
			}

			if (annotationsArr.size() > 0)
			{
				object.add("annotations", annotationsArr);
			}
		}

		if (post.getEntities() != null)
		{
			if (post.getEntities().containsKey(Entity.Type.LINK))
			{
				JsonArray linksArr = new JsonArray();

				for (Entity entity : post.getEntities().get(Entity.Type.LINK))
				{
					LinkEntity linkEntity = (LinkEntity)entity;
					JsonObject link = new JsonObject();
					link.addProperty("pos", linkEntity.getPos());
					link.addProperty("len", linkEntity.getLen());
					link.addProperty("url", linkEntity.getUrl());

					linksArr.add(link);
				}

				JsonObject links = new JsonObject();
				links.add("links", linksArr);
				links.addProperty("parse_links", true);
				object.add("entities", links);
			}
		}

		try
		{
			JsonEntity postData = new JsonEntity(object);

			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.post
			(
				String.format(API_CHANNEL_MESSAGES, post.getChannelId()),
				params,
				postData,
				response
			);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	/**
	 * Reply to Post
	 * @param message The message to post
	 * @param postId The post ID to reply to
	 * @param entities The embeddable entity list to include
	 * @param response The response from the api call
	 */
	public void replyMessage(String accessToken, DraftPost post, AsyncHttpResponseHandler response)
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));

		JsonObject object = new JsonObject();
		object.addProperty(API_TEXT, post.getPostText());
		object.addProperty("channel_id", post.getChannelId());

		JsonArray annotationsArr = new JsonArray();
		if (post.getAnnotations() != null)
		{
			for (Annotation e : post.getAnnotations())
			{
				/*
				 * if e.type == image -> upload image -> shorten link -> add annotation to jsonarray
				 */
				JsonObject annotation = e.toAnnotation().getAsJsonObject();
				if (annotation != null)
				{
					annotationsArr.add(annotation);
				}
			}

			if (annotationsArr.size() > 0)
			{
				object.add("annotations", annotationsArr);
			}
		}

		if (post.getEntities() != null)
		{
			if (post.getEntities().containsKey(Entity.Type.LINK))
			{
				JsonArray linksArr = new JsonArray();

				for (Entity entity : post.getEntities().get(Entity.Type.LINK))
				{
					LinkEntity linkEntity = (LinkEntity)entity;
					JsonObject link = new JsonObject();
					link.addProperty("pos", linkEntity.getPos());
					link.addProperty("len", linkEntity.getLen());
					link.addProperty("url", linkEntity.getUrl());

					linksArr.add(link);
				}

				JsonObject links = new JsonObject();
				links.add("links", linksArr);
				links.addProperty("parse_links", true);
				object.add("entities", links);
			}
		}

		try
		{
			JsonEntity postData = new JsonEntity(object);

			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.post
			(
				String.format(API_CHANNEL_MESSAGES, post.getChannelId()),
				params,
				postData,
				response
			);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	/**
	 * Delete message
	 * @param channelId The id of the channel
	 * @param messageId The id of the message to delete
	 * @param response The response from the api call
	 */
	public void deleteMessage(String channelId, String messageId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.delete(String.format(API_CHANNEL_MESSAGE_DETAILS, channelId, messageId), params, response);
	}

	/**
	 * Creates a channel and posts a PM Message
	 * @param mUsers The list of users to include
	 * @param post The post object for the first message
	 * @param response The response from the api call
	 */
	public void createChannelMessage(List<SimpleUser> mUsers, DraftPost post, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));

		JsonObject object = new JsonObject();

		JsonArray userList = new JsonArray();
		for (SimpleUser u : mUsers)
		{
			userList.add(new JsonPrimitive(u.getId()));
		}

		object.add("destinations", userList);
		object.addProperty("text", post.getPostText());

		try
		{
			JsonEntity postData = new JsonEntity(object);

			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.post
			(
				String.format(API_CHANNEL_MESSAGES, "pm"),
				params,
				postData,
				response
			);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	/**
	 * Subscribes to a channel
	 * @param channelId The channel id to subscribe to
	 * @param response The response from the api call
	 */
	public void subscribeChannel(String channelId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.post
		(
			String.format(API_CHANNEL_SUBSCRIBE, channelId),
			params,
			response
		);
	}

	/**
	 * Subscribes to a channel
	 * @param channelId The channel id to subscribe to
	 * @param response The response from the api call
	 */
	public void unsubscribeChannel(String channelId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.delete(String.format(API_CHANNEL_SUBSCRIBE, channelId), params, response);
	}

	/**
	 * Gets a list of trending hashtags
	 * @param response
	 */
	public void getTrending(AsyncHttpResponseHandler response)
	{
		List<Header> header = new ArrayList<Header>();
		header.add(new BasicHeader("Authorization", "Basic cm9iaW46RzV4Q3JBU3pFYkF4"));

		AsyncHttpClient mClient = new AsyncHttpClient(API_TRENDING_URL, SettingsManager.getRequestTimeout());
		mClient.setAllowAllSsl(true);
		mClient.get(header, response);
	}

	/**
	 * Searches app.net search using a keyword
	 * @param input
	 * @param lastId
	 * @param response
	 */
	public void keywordSearch(String input, String lastId, AsyncHttpResponseHandler response)
	{
		int maxCount = SettingsManager.getPageSize();
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("text", input));
		params.add(new BasicNameValuePair(API_COUNT, String.valueOf(maxCount)));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_STARRED, "1"));
		params.add(new BasicNameValuePair(API_INCLUDE_REPOSTERS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(API_POST_SEARCH, params, response);
	}

	/**
	 * Searches for users based on the input
	 * @param response The response from the api call
	 */
	public void searchUsers(String tag, String lastId, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("q", tag));
		params.add(new BasicNameValuePair(API_COUNT, "200"));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		try
		{
			tag = URLEncoder.encode(tag, "UTF-8");
		}
		catch (Exception e) {}

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(API_USER_SEARCH, params, response);
	}

	/**
	 * Gets the global timeline filtered by hashtag
	 * @param response The response from the api call
	 */
	public void searchPosts(String tag, String lastId, final AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		int maxCount = SettingsManager.getPageSize();

		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_COUNT, "" + maxCount));
		params.add(new BasicNameValuePair(API_LAST_ID, "" + lastId));
		params.add(new BasicNameValuePair(API_INCLUDE_DELETED, "0"));
		params.add(new BasicNameValuePair(API_INCLUDE_ANNOTATIONS, "1"));
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		try
		{
			tag = URLEncoder.encode(tag, "UTF-8");
		}
		catch (Exception e) {}

		AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
		mClient.get(String.format(API_TAGGED_TIMELINE_STREAM, tag), params, response);
	}

	/**
	 * Updates the user's avatar
	 * @param newAvatar
	 * @param response
	 */
	public void updateAvatar(File newAvatar, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		try
		{
			MultiPartEntity data = new MultiPartEntity();
			data.addPart("avatar", new FileBody(newAvatar, "image/jpeg"));

			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.post(String.format(API_USER_DETAILS_AVATAR, "me"), params, data, response);
		}
		catch (Exception e) {}
	}

	/**
	 * Updates the user's cover
	 * @param newCover
	 * @param response
	 */
	public void updateCover(File newCover, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		try
		{
			MultiPartEntity data = new MultiPartEntity();
			data.addPart("cover", new FileBody(newCover, "image/jpeg"));

			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.post(String.format(API_USER_DETAILS_COVER, "me"), params, data, response);
		}
		catch (Exception e) {}
	}

	/**
	 * Updates a stream marker
	 * @param id The id of the last read post
	 * @param markerName The name of the marker
	 * @param response
	 */
	public void updateMarker(String id, String markerName, AsyncHttpResponseHandler response)
	{
		String accessToken = UserManager.getAccessToken();
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(API_ACCESS_TOKEN, accessToken));

		try
		{
			JsonObject json = new JsonObject();
			json.addProperty("name", markerName);
			json.addProperty("id", id);

			JsonEntity data = new JsonEntity(json);

			AsyncHttpClient mClient = new AsyncHttpClient(API_URL + API_STREAM + API_VERSION, SettingsManager.getRequestTimeout());
			mClient.post("posts/marker", params, data, response);
		}
		catch (Exception e) {}
	}

	/**
	 * Logs access of a user using the CD_KEY app
	 * @param context
	 * @param userId
	 * @param cdKey
	 * @param deviceId
	 */
	public void logAccess(final Context context, String userId, String cdKey, String deviceId)
	{
		try
		{
			JsonObject json = new JsonObject();
			json.addProperty("user_id", userId);
			json.addProperty("app_key", cdKey);
			json.addProperty("device_id", deviceId);

			JsonEntity data = new JsonEntity(json);

			AsyncHttpClient mClient = new AsyncHttpClient(API_ROBIN_URL);
			mClient.post(API_LOG, data, new JsonResponseHandler()
			{
				@Override public void onSuccess(){}
				@Override public void onFailure()
				{
					JsonElement e = getContent();
					if (e != null && getConnectionInfo().responseCode == 401)
					{
						if (e.getAsJsonObject().get("error").getAsInt() == 1)
						{
							context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
							.edit()
								.putBoolean(Constants.PREFS_KEY_BLACK_LISTED, true)
							.apply();
						}
					}
				}
			});
		}
		catch (Exception e) {}
	}

	/**
	 * Checks for updates using a standard response
	 * @param context
	 */
	public void checkUpdates(final Context context)
	{
		JsonResponseHandler handler = new JsonResponseHandler()
		{
			@Override public void onSuccess()
			{
				JsonElement response = getContent();

				if (response != null)
				{
					try
					{
						String url = response.getAsJsonObject().get("update_url").getAsString();
						int build = response.getAsJsonObject().get("build").getAsInt();
						int currentBuild = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;

						if (build > currentBuild)
						{
							NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
							NotificationCompat.Builder notifBuilder = new Builder(context);
							notifBuilder.setTicker(context.getString(R.string.new_update));
							notifBuilder.setContentTitle(context.getString(R.string.new_update_title));
							notifBuilder.setContentText(context.getString(R.string.new_update));
							notifBuilder.setAutoCancel(true);
							notifBuilder.setSmallIcon(R.drawable.notif);

							Intent updateIntent = new Intent(context, UpdateManager.class);
							updateIntent.putExtra(Constants.EXTRA_WEB_URL, url);
							notifBuilder.setContentIntent(PendingIntent.getActivity(context, 1, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT));

							notificationManager.notify(65834, notifBuilder.build());
						}
					}
					catch (Exception e){}
				}
			}
		};

		checkUpdates(context, handler);
	}

	/**
	 * Checks for updates for users using the CD_KEY app
	 * @param context
	 * @param handler
	 */
	public void checkUpdates(final Context context, AsyncHttpResponseHandler handler)
	{
		AsyncHttpClient mClient = new AsyncHttpClient(API_ROBIN_URL);
		mClient.get(API_UPDATES, handler);
	}
}