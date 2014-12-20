package in.rob.client;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import in.lib.manager.UserManager;
import in.lib.utils.Views;
import in.lib.utils.Views.Injectable;
import in.lib.utils.Views.OnClick;

@Injectable
public class MainChoiceActivity extends Activity
{
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (UserManager.getInstance().isLoggedIn())
		{
			startMainActivity();
			return;
		}

		showSplash();
	}

	@OnClick public void onLoginButtonClick(View v)
	{
		startActivity(new Intent(this, AuthenticationActivity.class));
		finish();
	}

	@OnClick public void onSignupButtonClick(View v)
	{
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://join.app.net/"));
		startActivity(browserIntent);
	}

	public void showSplash()
	{
		setContentView(R.layout.main_choice_view);
		Views.inject(this);
	}

	public void startMainActivity()
	{
		Intent main = new Intent(this, MainActivity.class);
		main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		main.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		main.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		startActivity(main);
		finish();
	}
}