package in.rob.client;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import in.lib.Constants;
import in.lib.adapter.ViewPageAdapter;
import in.lib.manager.UserManager;
import in.lib.utils.Views.Injectable;
import in.rob.client.base.BaseActivity;
import in.rob.client.fragment.AuthenticationFragment;

@Injectable
public class AuthenticationActivity extends BaseActivity
{
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getIntent().getExtras() == null || !getIntent().getExtras().getBoolean(Constants.EXTRA_NEW_USER, false))
		{
			if (!TextUtils.isEmpty(UserManager.getInstance().getAccessToken()))
			{
				Intent main = new Intent(this, MainActivity.class);
				main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				main.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(main);
				finish();
				return;
			}
		}

		setPageAdapter(new ViewPageAdapter(this, getFragmentManager(), getViewPager()));
		getPageAdapter().addPage(AuthenticationFragment.class, getIntent().getExtras());
		getViewPager().setAdapter(getPageAdapter());

//		getActionBar().getCustomView().findViewById(R.id.up_button).setVisibility(View.GONE);
		((TextView)getActionBar().getCustomView().findViewById(R.id.title)).setText(R.string.login_title);

		getSlidingMenu().setBehindWidth(0);
		getSlidingMenu().setSlidingEnabled(false);
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (getPageAdapter().getCurrentFragment() != null)
		{
			getPageAdapter().getCurrentFragment().onActivityResult(requestCode, resultCode, data);
		}
	}
}
