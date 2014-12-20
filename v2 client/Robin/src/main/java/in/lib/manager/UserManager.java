package in.lib.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import in.lib.Constants;
import in.model.User;
import lombok.Getter;
import lombok.Setter;

public class UserManager
{
	private static UserManager instance;

	public static UserManager getInstance()
	{
		if (instance == null)
		{
			synchronized (UserManager.class)
			{
				if (instance == null)
				{
					instance = new UserManager();
				}
			}
		}

		return instance;
	}

	// TODO: Add user variables
	@Getter private Context context;
	@Getter private User user;
	@Getter @Setter private String accessToken = "";

	private UserManager()
	{

	}

	public void initialise(Context context)
	{
		this.context = context.getApplicationContext();
		loginUser();
	}

	public void loginUser()
	{
		SharedPreferences authPrefs = context.getSharedPreferences(Constants.PREFS_AUTH, Context.MODE_PRIVATE);

		String selectedUser = authPrefs.getString(Constants.PREFS_AUTH_SELECTED_USER, "-1");
		String accessToken = authPrefs.getString(String.format(Constants.PREFS_AUTH_USER_ACCESS_TOKEN, selectedUser), "");

		if (!TextUtils.isEmpty(accessToken))
		{
			this.accessToken = accessToken;
			this.user = User.load(selectedUser);
		}
	}

	public List<String> getLinkedUserIds()
	{
		SharedPreferences authPrefs = context.getSharedPreferences(Constants.PREFS_AUTH, Context.MODE_PRIVATE);
		Map<String, ?> prefMap = authPrefs.getAll();
		Iterator<String> keys = prefMap.keySet ().iterator();

		List<String> users = new ArrayList<String>(prefMap.size());

		String[] parts = String.format(Constants.PREFS_AUTH_USER_ACCESS_TOKEN, ":").split(":");
		while (keys.hasNext())
		{
			users.add(keys.next().replace(parts[0], "").replace(parts[1], ""));
		}

		return users;
	}

	public void addUser(User user, String accessToken)
	{
		Editor authPrefs = context.getSharedPreferences(Constants.PREFS_AUTH, Context.MODE_PRIVATE).edit();
		authPrefs.putString(String.format(Constants.PREFS_AUTH_USER_ACCESS_TOKEN, user.getId()), accessToken);
		authPrefs.apply();
	}

	public void setUser(User user, String accessToken)
	{
		addUser(user, accessToken);
		selectUser(user);

		this.user = user;
		this.accessToken = accessToken;
	}

	public String getToken(String userId)
	{
		SharedPreferences authPrefs = context.getSharedPreferences(Constants.PREFS_AUTH, Context.MODE_PRIVATE);
		return authPrefs.getString(String.format(Constants.PREFS_AUTH_USER_ACCESS_TOKEN, userId), "");
	}

	public boolean isLoggedIn()
	{
		return !TextUtils.isEmpty(accessToken) && user != null;
	}

	public void selectUser(User user)
	{
		SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_AUTH, Context.MODE_PRIVATE);
		Editor authPrefs = prefs.edit();
		authPrefs.putString(Constants.PREFS_AUTH_SELECTED_USER, user.getId());
		authPrefs.apply();

		this.user = user;
		this.accessToken = prefs.getString(String.format(Constants.PREFS_AUTH_USER_ACCESS_TOKEN, user.getId()), "");
	}
}
