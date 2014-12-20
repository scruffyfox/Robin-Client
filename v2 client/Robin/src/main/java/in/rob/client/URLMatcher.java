package in.rob.client;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import in.lib.Constants;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.BitUtils;
import in.rob.client.dialog.NewPostDialog;

public class URLMatcher extends Activity
{
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (!UserManager.getInstance().isLoggedIn())
		{
			Intent auth = new Intent(this, AuthenticationActivity.class);
			auth.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			auth.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			auth.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			auth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(auth);
			finish();
			return;
		}

		setVisible(false);
		Uri uri = getIntent().getData();
		if (uri == null || uri.getHost() == null)
		{
			finish();
			return;
		}

		if (uri.getHost().equalsIgnoreCase("alpha.app.net") && (uri.getPathSegments() != null && uri.getPathSegments().size() > 0))
		{
			// intent
			if (uri.getPathSegments().get(0).equalsIgnoreCase("intent"))
			{
				if (uri.getPathSegments().get(1).equalsIgnoreCase("post"))
				{
					Intent postIntent = new Intent(this, NewPostDialog.class);
					postIntent.putExtra(Intent.EXTRA_TEXT, uri.getQueryParameter("text"));
					startActivity(postIntent);
				}
			}
			// user
			else if (uri.getPathSegments().size() == 1)
			{
//				Intent profileIntent = new Intent(this, ProfileActivity.class);
//				profileIntent.putExtra(Constants.EXTRA_USER_ID, "%40" + uri.getPathSegments().get(0));
//				startActivity(profileIntent);
			}
			// post
			else if (uri.getPathSegments().size() > 1)
			{
				if (uri.getPathSegments().get(1).equals("post"))
				{
//					Intent postIntent = new Intent(this, ThreadActivity.class);
//					postIntent.putExtra(Constants.EXTRA_POST_ID, uri.getPathSegments().get(2));
//					startActivity(postIntent);
				}
			}
		}
		else if (uri.getHost().equalsIgnoreCase("posts.app.net") && (uri.getPathSegments() != null && uri.getPathSegments().size() > 0))
		{
//			Intent postIntent = new Intent(this, ThreadActivity.class);
//			postIntent.putExtra(Constants.EXTRA_POST_ID, uri.getPathSegments().get(0));
//			startActivity(postIntent);
		}
		else if (uri.getHost().equalsIgnoreCase("patter-app.net"))
		{
			if (uri.getQueryParameter("channel") != null)
			{
//				Intent channelIntent = new Intent(this, MessagesActivity.class);
//				channelIntent.putExtra(Constants.EXTRA_CHANNEL_ID, uri.getQueryParameter("channel"));
//				startActivity(channelIntent);
			}
		}
		else if (uri.getHost().equalsIgnoreCase("omega.app.net") && (uri.getPathSegments() != null && uri.getPathSegments().size() > 1))
		{
			if (uri.getPathSegments().get(0).equalsIgnoreCase("channel"))
			{
//				Intent channelIntent = new Intent(this, MessagesActivity.class);
//				channelIntent.putExtra(Constants.EXTRA_CHANNEL_ID, uri.getPathSegments().get(1));
//				startActivity(channelIntent);
			}
		}
		else
		{
			if (BitUtils.contains(SettingsManager.getInstance().getInAppViewerBit(), Constants.BIT_IN_APP_VIEWER_BROWSER))
			{
				Intent intent = new Intent(this, WebBrowserDialog.class);
				intent.putExtra(Constants.EXTRA_PREVIEW_URL, uri.toString());
				startActivity(intent);
			}
			else
			{
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addCategory("android.intent.category.BROWSABLE");
				intent.setData(uri);
				startActivity(intent);
			}
		}

		finish();
	}
}