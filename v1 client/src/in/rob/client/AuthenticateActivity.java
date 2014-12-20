package in.rob.client;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.annotation.InjectView;
import in.lib.handler.base.LoginResponseHandler;
import in.lib.handler.base.UserResponseHandler;
import in.lib.handler.base.UserStreamResponseHandler;
import in.lib.manager.APIManager;
import in.lib.manager.CacheManager;
import in.lib.manager.ImageAPIManager;
import in.lib.manager.UserManager;
import in.lib.receiver.NotificationReceiver;
import in.lib.utils.ADNPassportUtility;
import in.lib.utils.Views;
import in.model.SimpleUser;
import in.model.User;
import in.model.base.NetObject;
import in.obj.Auth;
import in.rob.client.MainApplication.ApplicationType;
import in.rob.client.base.RobinActivity;
import in.rob.client.dialog.base.DialogBuilder;
import in.rob.client.dialog.base.ProgressBuilder;
import in.rob.client.page.UserFriendsPage;

import java.util.ArrayList;
import java.util.List;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.obj.entity.JsonEntity;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

public class AuthenticateActivity extends RobinActivity implements OnEditorActionListener
{
	@InjectView(R.id.username) public EditText mUsername;
	@InjectView(R.id.password) public EditText mPassword;
	@InjectView(R.id.beta_build) public TextView mBetaBuild;
	@InjectView(R.id.login_button) public Button mLoginButton;
	@InjectView(R.id.cd_key) public EditText mCdKey;
	@InjectView(R.id.cd_key_container) public View mCdKeyContainer;

	private ProgressDialog progress;

	private final BroadcastReceiver installReceiver = new BroadcastReceiver()
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			String dataString = intent.getDataString();
			if (Intent.ACTION_PACKAGE_ADDED.equals(action) && dataString.equals(String.format("package:%s", ADNPassportUtility.APP_PACKAGE)))
			{
				adnButtonClick(null);
			}
		}
	};

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_view);
		Views.inject(this);

		if (getIntent() != null && ((MainApplication)getApplication()).getApplicationType() == ApplicationType.CD_KEY)
		{
			if (getIntent().getData() != null)
			{
				Uri data = getIntent().getData();
				mCdKey.setText(data.getQueryParameter("key"));
			}
		}

		if (getIntent().getExtras() == null || !getIntent().getExtras().getBoolean(Constants.EXTRA_NEW_USER, false))
		{
			if (!TextUtils.isEmpty(UserManager.getAccessToken()))
			{
				startMainActivity();
				finish();
				return;
			}
		}

		setTitle(R.string.login_title);
		if (progress == null)
		{
			progress = ProgressBuilder.create(getContext());
			progress.setMessage(getString(R.string.logging_in));
			progress.setCanceledOnTouchOutside(false);

			if (savedInstanceState != null && savedInstanceState.containsKey(Constants.EXTRA_SHOWING_PROGRESS))
			{
				if (savedInstanceState.getBoolean(Constants.EXTRA_SHOWING_PROGRESS))
				{
					progress.show();
				}
			}
		}

		if (((MainApplication)getApplication()).getApplicationType() == ApplicationType.CD_KEY)
		{
			mCdKeyContainer.setVisibility(View.VISIBLE);
			mPassword.setImeOptions(EditorInfo.IME_ACTION_NEXT);

			try
			{
				String key = CacheManager.getInstance().readFileAsObject(Environment.getExternalStorageDirectory().getAbsolutePath() + "/robin.cdkey", String.class);

				if (key != null)
				{
					mCdKey.setText(key);
				}
				else
				{
					SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
					mCdKey.setText(prefs.getString(Constants.PREFS_CD_KEY, ""));
				}
			}
			catch (Exception e){}
		}

		if (((MainApplication)getApplication()).getApplicationType() == ApplicationType.BETA)
		{
			try
			{
				PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
				findViewById(R.id.beta_container).setVisibility(View.VISIBLE);
				mBetaBuild.setText("Beta build number: " + info.versionCode);
			}
			catch (Exception e) {}
		}

		mPassword.setOnEditorActionListener(this);
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putBoolean(Constants.EXTRA_SHOWING_PROGRESS, progress.isShowing());
		progress.dismiss();
		super.onSaveInstanceState(outState);
	}

	public void adnButtonClick(View v)
	{
		if (ADNPassportUtility.isPassportAuthorizationAvailable(this))
		{
			Intent i = ADNPassportUtility.getAuthorizationIntent(APIManager.CLIENT_TOKEN, APIManager.API_SCOPES_CSV);
			startActivityForResult(i, Constants.REQUEST_CODE_AUTHORIZE);
		}
		else
		{
			ADNPassportUtility.launchPassportInstallation(this);
			IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
			filter.addDataScheme("package");
			registerReceiver(installReceiver, filter);
		}
	}

	@Override protected void onDestroy()
	{
		super.onDestroy();

		try
		{
			unregisterReceiver(installReceiver);
		}
		catch (Exception e){}
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Constants.REQUEST_CODE_AUTHORIZE)
		{
			if (resultCode == 1)
			{
				String username = data.getStringExtra("username");
				String accessToken = data.getStringExtra("accessToken");
				String userId = data.getStringExtra("userId");

				progress.show();

				if (((MainApplication)getApplication()).getApplicationType() == ApplicationType.BETA)
				{
					checkBetaKeyLogin(accessToken, userId, username);
				}
				else
				{
					downloadUser(accessToken, userId);
				}
			}
		}
	}

	/**
	 * Called when the login button is hit
	 * @param v The login button (dont use this as it can be called from keyboard IME)
	 */
	public void loginButtonClick(View v)
	{
		String usernameStr = mUsername.getText().toString();
		String passwordStr = mPassword.getText().toString();

		mUsername.setHintTextColor(getResources().getColor(R.color.light_login_hint));
		mPassword.setHintTextColor(getResources().getColor(R.color.light_login_hint));

		if (TextUtils.isEmpty(usernameStr))
		{
			mUsername.setHintTextColor(getResources().getColor(R.color.light_login_hint_error));
			return;
		}
		else
		{
			mUsername.setHintTextColor(getResources().getColor(R.color.light_login_hint));
		}

		if (TextUtils.isEmpty(passwordStr))
		{
			mPassword.setHintTextColor(getResources().getColor(R.color.light_login_hint_error));
			return;
		}
		else
		{
			mPassword.setHintTextColor(getResources().getColor(R.color.light_login_hint));
		}

		if (((MainApplication)getApplication()).getApplicationType() == ApplicationType.BETA)
		{
			progress.show();
			checkBetaKeyLogin(usernameStr, passwordStr);
			return;
		}
		else if (((MainApplication)getApplication()).getApplicationType() == ApplicationType.CD_KEY)
		{
			mCdKey.setError(null);
			String key = mCdKey.getText().toString();

			// perform some initial checks
			boolean verified = false;
			boolean okToCheck = true;
			okToCheck &= key.length() == 23;
			okToCheck &= key.replaceAll("[0-9a-zA-Z]", "").length() == 3;
			okToCheck &= key.replaceAll("[0-9A-Za-z\\-]", "").length() == 0;

			if (okToCheck)
			{
				verified = in.rob.keygen.Main.v(key);
			}

			if (!okToCheck || !verified)
			{
				mCdKey.setError("Invalid key");
				return;
			}
		}

		progress.show();
		loginUser(usernameStr, passwordStr);
	}

	public void checkBetaKeyLogin(final String username, final String password)
	{
		try
		{
			final AsyncHttpClient checker = new AsyncHttpClient(Constants.API_BETA_URL);
			JsonObject data = new JsonObject();
			data.addProperty("token", "");
			data.addProperty("username", username);
			data.addProperty("device_id", getDeviceId());
			data.addProperty("version", getString(R.string.app_version));

			progress.setOnCancelListener(new OnCancelListener()
			{
				@Override public void onCancel(DialogInterface dialog)
				{
					checker.cancel();
				}
			});

			JsonEntity postData = new JsonEntity(data);
			checker.post(Constants.API_BETA_CHECK, postData, new JsonResponseHandler()
			{
				@Override public void onSuccess(){}

				@Override public void onFinish(boolean failed)
				{
					if (failed)
					{
						Debug.out(getConnectionInfo());

						progress.dismiss();

						if (getConnectionInfo().responseCode == 401)
						{
							DialogBuilder.create(getContext())
								.setTitle(R.string.error)
								.setMessage(R.string.beta_error)
								.setPositiveButton(R.string.close, null)
							.show();
						}
						else
						{
							DialogBuilder.create(getContext())
								.setTitle(R.string.error)
								.setMessage(R.string.login_error)
								.setPositiveButton(R.string.close, null)
							.show();
						}
					}
					else
					{
						SharedPreferences.Editor editor = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit();
						editor.putBoolean(Constants.PREFS_HAS_BETA, true).apply();

						loginUser(username, password);
					}
				}
			});
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public void checkBetaKeyLogin(final String accessToken, final String userId, String username)
	{
		try
		{
			final AsyncHttpClient checker = new AsyncHttpClient(Constants.API_BETA_URL);
			JsonObject data = new JsonObject();
			data.addProperty("token", "");
			data.addProperty("username", username);
			data.addProperty("device_id", getDeviceId());
			data.addProperty("version", getString(R.string.app_version));

			progress.setOnCancelListener(new OnCancelListener()
			{
				@Override public void onCancel(DialogInterface dialog)
				{
					checker.cancel();
				}
			});

			JsonEntity postData = new JsonEntity(data);
			checker.post(Constants.API_BETA_CHECK, postData, new JsonResponseHandler()
			{
				@Override public void onSuccess(){}

				@Override public void onFinish(boolean failed)
				{
					if (failed)
					{
						Debug.out(getConnectionInfo());

						progress.dismiss();

						if (getConnectionInfo().responseCode == 401)
						{
							DialogBuilder.create(getContext())
								.setTitle(R.string.error)
								.setMessage(R.string.beta_error)
								.setPositiveButton(R.string.close, null)
							.show();
						}
						else
						{
							DialogBuilder.create(getContext())
								.setTitle(R.string.error)
								.setMessage(R.string.login_error)
								.setPositiveButton(R.string.close, null)
							.show();
						}
					}
					else
					{
						SharedPreferences.Editor editor = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit();
						editor.putBoolean(Constants.PREFS_HAS_BETA, true).apply();
						downloadUser(accessToken, userId);
					}
				}
			});
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	public void infoButtonClick(View v)
	{
		String information = getString(R.string.login_info_description_1) +
				"<br /><br />" +
				getString(R.string.login_info_description_2) +
				"<br /><br />" +
				"&#149; <b>" + getString(R.string.stream) + "</b><br />" + getString(R.string.stream_desc) + "<br /><br />" +
				"&#149; <b>" + getString(R.string.email) + "</b><br />" + getString(R.string.email_desc) + "<br /><br />" +
				"&#149; <b>" + getString(R.string.write_post) + "</b><br />" + getString(R.string.write_post_desc) + "<br /><br />" +
				"&#149; <b>" + getString(R.string.follow) + "</b><br />" + getString(R.string.follow_desc) + "<br /><br />" +
				"&#149; <b>" + getString(R.string.messages) + "</b><br />" + getString(R.string.messages_desc) + "<br /><br />" +
				"&#149; <b>" + getString(R.string.update_profile) + "</b><br />" + getString(R.string.update_profile_desc) + "<br /><br />" +
				"&#149; <b>" + getString(R.string.files) + "</b><br />" + getString(R.string.files_desc) + "<br /><br />" +
				getString(R.string.login_info_description_3);

		DialogBuilder.create(getContext())
			.setTitle(R.string.information)
			.setMessage(Html.fromHtml(information))
			.setPositiveButton(R.string.close, null)
		.show();
	}

	/**
	 * Initiates the login call and downloads the user's profile data
	 * @param username The username of the user logging in
	 * @param password The password of the user logging in
	 */
	public void loginUser(String username, String password)
	{
		final AsyncHttpClient request = APIManager.getInstance().login(username, password, getContext(), new LoginResponseHandler()
		{
			@Override public void onCallback()
			{
				if (((MainApplication)getApplication()).getApplicationType() == ApplicationType.CD_KEY)
				{
					try
					{
						CacheManager.getInstance().writeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/robin.cdkey", mCdKey.getText().toString());
					}
					catch (Exception e){}

					SharedPreferences prefs = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
					prefs.edit().putString(Constants.PREFS_CD_KEY, mCdKey.getText().toString()).apply();
					APIManager.getInstance().logAccess(getApplicationContext(), "" + getUserId(), mCdKey.getText().toString(), getDeviceId());
				}

				downloadUser(getAccessToken(), "" + getUserId());
			}

			@Override public void onFinish(boolean failed)
			{
				if (failed)
				{
					progress.dismiss();
					String errorMessage = getString(R.string.vague_error);
					try
					{
						JsonElement data = getContent();
						JsonObject returnedData = data.getAsJsonObject();
						errorMessage = returnedData.get("error").getAsString();
					}
					catch (Exception e){}

					mUsername.setTextColor(getResources().getColor(R.color.light_login_text_error));
					mPassword.setTextColor(getResources().getColor(R.color.light_login_text_error));

					DialogBuilder.create(getContext())
						.setTitle(getString(R.string.error))
						.setMessage(errorMessage)
						.setPositiveButton(getString(R.string.close), null)
					.show();
				}
			}
		});

		progress.setOnCancelListener(new OnCancelListener()
		{
			@Override public void onCancel(DialogInterface dialog)
			{
				request.cancel();
			}
		});
	}

	/**
	 * Downloads the user profile and stores it in a cahced file
	 * @param accessToken The access token of the user
	 * @param userId The user id of the user
	 */
	public void downloadUser(final String accessToken, final String userId)
	{
		Auth a = new Auth();
		a.setAccessToken(accessToken);
		UserManager.setAuth(a);
		UserManager.setAccessToken(accessToken);
		UserManager.setUserId(userId);

		APIManager.getInstance().getUserDetails(userId, new UserResponseHandler(getApplicationContext())
		{
			@Override public void onSuccess()
			{
				super.onSuccess();

				if (getFilesDir() != null)
				{
					CacheManager.getInstance().writeFile(getFilesDir().getAbsolutePath() + "user_" + getUser().getId(), getUser());
				}

				getUser().save();
				UserManager.addUser(getUser(), UserManager.getAuth(), getApplicationContext());
				UserManager.setUser(getUser(), getApplicationContext());

				new NotificationReceiver().registerUserForPush(getApplicationContext());
			}

			@Override public void onFinish()
			{
				Debug.out(getConnectionInfo());

				if (getUser() != null)
				{
					// Download the user's cover image
					ImageLoader coverImageLoader = ImageLoader.getInstance();
					coverImageLoader.loadImage(getUser().getCoverUrl(), MainApplication.getMediaImageOptions(), new ImageLoadingListener()
					{
						@Override public void onLoadingStarted(String arg0, View arg1){}

						@Override public void onLoadingCancelled(String arg0, View arg1)
						{
							progress.dismiss();
							startMainActivity();
						}

						@Override public void onLoadingComplete(String arg0, View arg1, Bitmap arg2)
						{
							progress.dismiss();
							startMainActivity();
						}

						@Override public void onLoadingFailed(String arg0, View arg1, FailReason arg2)
						{
							progress.dismiss();
							startMainActivity();
						}
					});

					getImageDelegateToken();
					getFollowingList();
				}
				else
				{
					progress.dismiss();
					String errorMessage = getString(R.string.vague_error);
					DialogBuilder.create(getContext())
						.setTitle(getString(R.string.error))
						.setMessage(errorMessage)
						.setPositiveButton(getString(R.string.close), null)
					.show();
				}
			}

			@Override public void onCallback(){}
		});
	}

	/**
	 * Gets the image delegation token for the initial image service
	 */
	public void getImageDelegateToken()
	{
		ImageAPIManager.getInstance().registerForToken(getContext(), UserManager.getUser());
	}

	/**
	 * Used to build an initial list of users for the auto complete
	 */
	public void getFollowingList()
	{
		APIManager.getInstance().getUserFollowing(UserManager.getUserId(), "", new UserStreamResponseHandler(getApplicationContext(), false)
		{
			@Override public void onCallback()
			{
				List<SimpleUser> users = CacheManager.getInstance().readFileAsObject(Constants.CACHE_USERNAMES, new ArrayList<SimpleUser>());

				for (NetObject u : getObjects())
				{
					SimpleUser user = SimpleUser.parseFromUser((User)u);
					if (!SimpleUser.containsUser(users, user))
					{
						users.add(user);
					}
				}

				CacheManager.getInstance().asyncWriteFile(String.format(Constants.CACHE_USER_LIST_NAME, UserFriendsPage.Mode.FOLLOWING.getModeText(), UserManager.getUserId()), getObjects());
				CacheManager.getInstance().asyncWriteFile(Constants.CACHE_USERNAMES, users);
			}
		});
	}

	public void startMainActivity()
	{
		Intent main = new Intent(this, MainActivity.class);
		main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		main.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(main);
		finish();
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.empty, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
	{
		if (actionId == EditorInfo.IME_ACTION_GO)
		{
			mLoginButton.performClick();
			return true;
		}

		return false;
	}
}