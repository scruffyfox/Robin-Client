package in.rob.client.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import in.controller.handler.AuthenticationHandler;
import in.controller.handler.UserResponseHandler;
import in.controller.handler.base.ResponseListener;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.manager.ResponseManager;
import in.lib.manager.UserManager;
import in.lib.utils.ADNPassportUtility;
import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.utils.Views.OnClick;
import in.model.User;
import in.rob.client.MainActivity;
import in.rob.client.R;
import in.rob.client.fragment.base.BaseFragment;

@Injectable
public class AuthenticationFragment extends BaseFragment implements ResponseListener<User>
{
	@InjectView private EditText username;
	@InjectView private EditText password;

	private ProgressDialog dialog;

	private final BroadcastReceiver installReceiver = new BroadcastReceiver()
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			String dataString = intent.getDataString();
			if (Intent.ACTION_PACKAGE_ADDED.equals(action) && dataString.equals(String.format("package:%s", ADNPassportUtility.APP_PACKAGE)))
			{
				onAdnLoginClick(null);
			}
		}
	};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.authentication_layout, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onAttach(Activity activity)
	{
		super.onAttach(activity);

		dialog = new ProgressDialog(getContext());
		dialog.setMessage(getString(R.string.logging_in));
		dialog.setCanceledOnTouchOutside(false);

		if (ResponseManager.getInstance().getResponse(getResponseKeys()[0]) != null)
		{
			dialog.show();
		}
	}

	@Override public void onDestroy()
	{
		super.onDestroy();

		try
		{
			getActivity().unregisterReceiver(installReceiver);
		}
		catch (Exception e){}
	}

	@Override public void onDetach()
	{
		super.onDetach();

		if (dialog != null)
		{
			dialog.dismiss();
			dialog = null;
		}
	}

	@OnClick public void onAdnLoginClick(View view)
	{
		if (ADNPassportUtility.isPassportAuthorizationAvailable(getContext()))
		{
			Intent authorizationIntent = ADNPassportUtility.getAuthorizationIntent(Constants.CLIENT_TOKEN, Constants.API_SCOPES_CSV);
			getActivity().startActivityForResult(authorizationIntent, Constants.REQUEST_CODE_AUTHORIZE);
		}
		else
		{
			ADNPassportUtility.launchPassportInstallation(getContext());
			IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
			filter.addDataScheme("package");
			getActivity().registerReceiver(installReceiver, filter);
		}
	}

	@OnClick public void onLoginClick(View view)
	{
		String username = this.username.getText().toString();
		String password = this.password.getText().toString();

		AuthenticationHandler response = new AuthenticationHandler();
		ResponseManager.getInstance().addResponse(getResponseKeys()[0], response, this);
		APIManager.getInstance().authenticate(username, password, response);

		dialog.show();
	}

	@Override public void handleResponse(User user)
	{
		if (dialog != null)
		{
			dialog.dismiss();
			dialog = null;
		}

		if (user != null)
		{
			if (!TextUtils.isEmpty(UserManager.getInstance().getAccessToken()))
			{
				UserManager.getInstance().setUser(user, UserManager.getInstance().getAccessToken());
			}
			else if (!TextUtils.isEmpty(user.getToken()))
			{
				UserManager.getInstance().setUser(user, user.getToken());
			}

			user.save();

			if (getArguments() == null || (getArguments() != null && !getArguments().containsKey(Constants.EXTRA_FINISH)))
			{
				Intent main = new Intent(getActivity(), MainActivity.class);
				startActivity(main);
			}

			getActivity().finish();
		}
	}

	public void handleFailure(String slug, String errorText)
	{
		if (dialog != null)
		{
			dialog.dismiss();
		}

		if (slug.equals("invalid_grant"))
		{
			Toast.makeText(getContext(), errorText, Toast.LENGTH_SHORT).show();
		}
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Constants.REQUEST_CODE_AUTHORIZE)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				String username = data.getStringExtra("username");
				String accessToken = data.getStringExtra("accessToken");
				String userId = data.getStringExtra("userId");

				UserManager.getInstance().setAccessToken(accessToken);

				UserResponseHandler response = new UserResponseHandler();
				ResponseManager.getInstance().addResponse(getResponseKeys()[0], response, this);
				APIManager.getInstance().getUser(userId, accessToken, response);

				dialog.show();
			}
		}
	}

	@Override public String[] getResponseKeys()
	{
		return new String[]{"auth"};
	}
}
