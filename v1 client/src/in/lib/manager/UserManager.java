package in.lib.manager;

import in.lib.Constants;
import in.lib.receiver.NotificationReceiver;
import in.model.User;
import in.obj.Auth;
import in.rob.client.MainActivity;
import in.rob.client.MainApplication;
import in.rob.client.MainApplication.ApplicationType;
import in.rob.client.ProfileActivity;
import in.rob.client.page.GeneralSettingsPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import net.callumtaylor.asynchttp.AsyncHttpClient;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * User manager class used to hold static fields for current logged in user details such as
 * 	- Access token
 * 	- User id
 */
public class UserManager
{
	@Getter @Setter private static String accessToken = "";
	@Getter @Setter private static String userId = "";
	@Getter private static User user;
	@Getter @Setter private static Auth auth = new Auth();

	/**
	 * @return True if a user has been logged in or false if not
	 */
	public static boolean isLoggedIn()
	{
		return user != null && !TextUtils.isEmpty(accessToken);
	}

	/**
	 * Logs out all accounts and removes the linked accounts list
	 * @param c The context to use when setting new cache
	 */
	public static void logout(Context c)
	{
		user = null;
		accessToken = "";
		userId = "";
		auth = null;

		List<String> users = getLinkedUserIds(c);
		for (String user : users)
		{
			AsyncHttpClient registerPush = new AsyncHttpClient(Constants.API_NOTIFICATION_URL + Constants.API_NOTIFICATION_VERSION + Constants.API_NOTIFICATION_USERS);
			registerPush.delete(user, null);
		}

		// load in the logged in user's details if they exist
		SharedPreferences preferences = c.getSharedPreferences(c.getPackageName(), Context.MODE_PRIVATE);
		preferences.edit().clear().commit();

		String cachePath = c.getCacheDir().getAbsolutePath() + "cache_";
		CacheManager.getInstance().writeFile(cachePath + Constants.CACHE_LINKED_ACCOUNTS, new ArrayList<String>());
		CacheManager.getInstance().writeFile(cachePath + Constants.CACHE_AUTH, new HashMap<String, Auth>());

		NotificationManager notificationManager = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(GeneralSettingsPage.QUICK_POST_ID);
	}

	/**
	 * Sets the current logged in user
	 * @param user The user to set
	 */
	public static void setUser(User user, Context c)
	{
		String cachePath = c.getCacheDir().getAbsolutePath() + "cache_";
		SharedPreferences prefs = c.getSharedPreferences(c.getPackageName(), Context.MODE_PRIVATE);

		ArrayList<String> addedUsers = CacheManager.getInstance().readFileAsObject(cachePath + Constants.CACHE_LINKED_ACCOUNTS, new ArrayList<String>());

		HashMap<String, Auth> auths = CacheManager.getInstance().readFileAsObject(cachePath + Constants.CACHE_AUTH, new HashMap<String, Auth>());
		auth = auths.get(user.getId());

		UserManager.user = user;
		UserManager.accessToken = auth.getAccessToken();
		UserManager.userId = user.getId();

		if (addedUsers != null)
		{
			int userIndex = addedUsers.indexOf(user.getId());
			if (userIndex >= 0)
			{
				prefs.edit().putInt(Constants.PREFS_SELECTED_USER, userIndex).apply();
			}
		}
		else
		{
			addUser(user, null, c);
			prefs.edit().putInt(Constants.PREFS_SELECTED_USER, 0).apply();
		}
	}

	/**
	 * Adds the user to the list of logged in accounts
	 * @param user The user to set
	 */
	public static void addUser(User user, Auth a, Context c)
	{
		String cachePath = c.getCacheDir().getAbsolutePath() + "cache_";
		ArrayList<String> addedUsers = CacheManager.getInstance().readFileAsObject(cachePath + Constants.CACHE_LINKED_ACCOUNTS, new ArrayList<String>());

		if (!addedUsers.contains(user.getId()))
		{
			addedUsers.add(user.getId());
		}

		if (a != null)
		{
			HashMap<String, Auth> userAuth = CacheManager.getInstance().readFileAsObject(cachePath + Constants.CACHE_AUTH, new HashMap<String, Auth>());
			userAuth.put(user.getId(), a);
			auth = a;
			CacheManager.getInstance().writeFile(cachePath + Constants.CACHE_AUTH, userAuth);
		}

		CacheManager.getInstance().writeFile(cachePath + Constants.CACHE_LINKED_ACCOUNTS, addedUsers);
	}

	public static void save(Context c)
	{
		String cachePath = c.getCacheDir().getAbsolutePath() + "cache_";
		HashMap<String, Auth> userAuth = CacheManager.getInstance().readFileAsObject(cachePath + Constants.CACHE_AUTH, new HashMap<String, Auth>());
		userAuth.put(user.getId(), auth);
		CacheManager.getInstance().writeFile(cachePath + Constants.CACHE_AUTH, userAuth);
	}

	/**
	 * Selects an account from index stored in cache
	 * @param c The context to use when reading the cache
	 * @param index The index of the user stored in cache
	 */
	public static void selectUser(Context c, int index)
	{
		selectUser(c, index, true);
	}

	/**
	 * Selects an account from index stored in cache
	 * @param c The context to use when reading the cache
	 * @param index The index of hte user stored in cahce
	 * @param restartActivity Weather to restart the app or not
	 */
	public static void selectUser(final Context c, int index, final boolean restartActivity)
	{
		SharedPreferences prefs = c.getSharedPreferences(c.getPackageName(), Context.MODE_PRIVATE);

		if (prefs.getInt(Constants.PREFS_SELECTED_USER, 0) == index)
		{
			if (restartActivity)
			{
				Intent main = new Intent(c, ProfileActivity.class);
				main.putExtra(Constants.EXTRA_USER, user);
				c.startActivity(main);
			}

			return;
		}

		prefs.edit().putInt(Constants.PREFS_SELECTED_USER, index).apply();
		if (loadUser(c) && restartActivity)
		{
			Intent main = new Intent(c, MainActivity.class);
			main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			main.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			c.startActivity(main);
		}
	}

	/**
	 * Gets a list of users logged into the current device
	 * @param c The context to use when reading the cache
	 * @return The list of user IDs
	 */
	public static List<String> getLinkedUserIds(Context c)
	{
		String cachePath = c.getCacheDir().getAbsolutePath() + "cache_";
		List<String> accs = CacheManager.getInstance().readFileAsObject(cachePath + Constants.CACHE_LINKED_ACCOUNTS, new ArrayList<String>());

		if (accs.size() < 1 && user != null)
		{
			accs.add(user.getId());
		}

		return accs;
	}

	public static HashMap<String, Auth> getAuths(Context c)
	{
		String cachePath = c.getCacheDir().getAbsolutePath() + "cache_";
		return CacheManager.getInstance().readFileAsObject(cachePath + Constants.CACHE_AUTH, new HashMap<String, Auth>());
	}

	public static void setAuths(Context c, HashMap<String, Auth> auths)
	{
		String cachePath = c.getCacheDir().getAbsolutePath() + "cache_";
		CacheManager.getInstance().writeFile(cachePath + Constants.CACHE_AUTH, auths);
	}

	/**
	 * Loads a user from cache based on the selected user in prefs (default: 0)
	 * @param c The context to use
	 * @return True if the user was loaded, false if not
	 */
	public static boolean loadUser(Context c)
	{
		String cachePath = c.getCacheDir().getAbsolutePath() + "cache_";
		SharedPreferences prefs = c.getSharedPreferences(c.getPackageName(), Context.MODE_PRIVATE);
		int selectedUser = prefs.getInt(Constants.PREFS_SELECTED_USER, 0);
		ArrayList<String> addedUsers = CacheManager.getInstance().readFileAsObject(cachePath + Constants.CACHE_LINKED_ACCOUNTS, new ArrayList<String>());
		HashMap<String, Auth> userAuth = CacheManager.getInstance().readFileAsObject(cachePath + Constants.CACHE_AUTH, new HashMap<String, Auth>());

		if (addedUsers == null || addedUsers.size() < 1 || userAuth.size() < 1)
		{
			return false;
		}
		else
		{
			userId = addedUsers.get(selectedUser);
			user = User.loadUser(userId);
			auth = userAuth.get(userId);

			if (user == null || auth == null)
			{
				return false;
			}

			accessToken = auth.getAccessToken();

			// register the user to notification api
			new NotificationReceiver().registerUserForPush(c);

			if (((MainApplication)c.getApplicationContext()).getApplicationType() == ApplicationType.CD_KEY)
			{
				APIManager.getInstance().checkUpdates(c);
				APIManager.getInstance().logAccess(c, getUserId(), prefs.getString(Constants.PREFS_CD_KEY, ""), ((MainApplication)c.getApplicationContext()).getDeviceId());
			}

			return true;
		}
	}
}