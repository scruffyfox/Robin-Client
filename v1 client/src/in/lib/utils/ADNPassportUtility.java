package in.lib.utils;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

public class ADNPassportUtility
{
	public static final String APP_PACKAGE = "net.app.passport";
	private static final String AUTHORIZE_ACTION = "net.app.adnpassport.authorize";
	private static final String FIND_FRIENDS_ACTION = "net.app.adnpassport.findfriends";
	private static final String RECOMMENDED_USERS_ACTION = "net.app.adnpassport.recommendedusers";
	private static final String INVITE_FRIENDS_ACTION = "net.app.adnpassport.invite";

	/**
	 * Check to see if App.net Passport is available for third party application
	 * authorization.
	 *
	 * @param context
	 *            Context
	 * @return true if Passport is available for authorization, false otherwise.
	 */
	public static boolean isPassportAuthorizationAvailable(Context context)
	{
		return isActionAvailable(context, new Intent(AUTHORIZE_ACTION));
	}

	/**
	 * Check to see if App.net Passport's Find Friends Activity can be launched.
	 *
	 * @param context
	 *            Context
	 * @return true if the Find Friends Activity can be launched, false
	 *         otherwise.
	 */
	public static boolean isFindFriendsAvailable(Context context)
	{
		return isActionAvailable(context, new Intent(FIND_FRIENDS_ACTION));
	}

	/**
	 * Check to see if App.net Passport's Recommended Users Activity can be
	 * launched.
	 *
	 * @param context
	 *            Context
	 * @return true if the Recommended Users Activity can be launched, false
	 *         otherwise.
	 */
	public static boolean isRecommendedUsersAvailable(Context context)
	{
		return isActionAvailable(context, new Intent(RECOMMENDED_USERS_ACTION));
	}

	/**
	 * Check to see if App.net Passport's Invite Friends Activity can be
	 * launched.
	 *
	 * @param context
	 *            Context
	 * @return true if the Invite Friends Activity can be launched, false
	 *         otherwise.
	 */
	public static boolean isInviteFriendsAvailable(Context context)
	{
		return isActionAvailable(context, new Intent(INVITE_FRIENDS_ACTION));
	}

	/**
	 * Launch App.net Passport's Find Friends Activity
	 *
	 * @param context
	 *            Context
	 * @param clientId
	 *            Your client ID.
	 * @return false if The Find Friends Activity is not available, true
	 *         otherwise.
	 */
	public static boolean launchFindFriends(Context context, String clientId)
	{
		return launchPassportAction(context, clientId, FIND_FRIENDS_ACTION);
	}

	/**
	 * Launch App.net Passport's Recommended Users Activity
	 *
	 * @param context
	 *            Context
	 * @param clientId
	 *            Your client ID.
	 * @return false if The Recommended Users Activity is not available, true
	 *         otherwise.
	 */
	public static boolean launchRecommendedUsers(Context context, String clientId)
	{
		return launchPassportAction(context, clientId, RECOMMENDED_USERS_ACTION);
	}

	/**
	 * Launch App.net Passport's Invite Friends Activity
	 *
	 * @param context
	 *            Context
	 * @param clientId
	 *            Your client ID.
	 * @return false if The Invite Friends Activity is not available, true
	 *         otherwise.
	 */
	public static boolean launchInviteFriends(Context context, String clientId)
	{
		return launchPassportAction(context, clientId, INVITE_FRIENDS_ACTION);
	}

	/**
	 * Launch Google Play to install App.net Passport. You should use
	 * {@link net.app.adnlogin.ADNPassportUtility#isPassportAuthorizationAvailable(android.content.Context)}
	 * to check if Passport is installed before calling this method. By
	 * registering a BroadcastReceiver for action
	 * {@link android.content.Intent#ACTION_PACKAGE_ADDED}, you can find out
	 * when App.net Passport has successfully been installed and perform follow
	 * up tasks, e.g. launch the authorization flow.
	 *
	 * @param context
	 *            Context
	 */
	public static void launchPassportInstallation(Context context)
	{
		Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("market://details?id=%s", APP_PACKAGE)));
		marketIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		context.startActivity(marketIntent);
	}

	/**
	 * Get an Intent to be used with App.net Passport's third party app
	 * authorization flow. From your Activity, pass this Intent to
	 * startActivityForResult. In the onActivityResult method, a resultCode of 1
	 * indicates authorization success. The data Intent will contain three
	 * String extras: accessToken, userId, and username.
	 *
	 * @param clientId
	 *            Your client ID.
	 * @param scope
	 *            The requested scopes (Comma separated, e.g.
	 *            "basic,write_post,files,stream")
	 * @return an Intent to be used for App.net Passport's third party app
	 *         authorization flow.
	 */
	public static Intent getAuthorizationIntent(String clientId, String scope)
	{
		Intent i = new Intent(AUTHORIZE_ACTION);
		i.putExtra("clientId", clientId);
		i.putExtra("scope", scope);
		return i;
	}

	private static boolean isActionAvailable(Context context, Intent intent)
	{
		PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
		return activities.size() > 0;
	}

	private static boolean launchPassportAction(Context context, String clientId, String intentAction)
	{
		Intent i = new Intent(intentAction);
		if (isActionAvailable(context, i))
		{
			i.putExtra("clientId", clientId);
			context.startActivity(i);
			return true;
		}

		return false;
	}
}