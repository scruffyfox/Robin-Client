package in.rob.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import in.lib.manager.UserManager;

public class SplashActivity extends Activity
{
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (UserManager.getInstance().isLoggedIn())
		{
			Intent main = new Intent(this, MainActivity.class);
			startActivity(main);
		}
		else
		{
			Intent auth = new Intent(this, MainChoiceActivity.class);
			startActivity(auth);
		}
	}
}
