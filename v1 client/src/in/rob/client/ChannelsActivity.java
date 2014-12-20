package in.rob.client;

import in.lib.Constants;
import in.lib.adapter.AccountAdapter;
import in.lib.adapter.AutoCompleteAdapter;
import in.lib.adapter.CreateChannelAdapter;
import in.lib.adapter.PhonePageAdapter;
import in.lib.handler.base.UserStreamResponseHandler;
import in.lib.loader.base.Loader;
import in.lib.manager.APIManager;
import in.lib.manager.CacheManager;
import in.lib.manager.UserManager;
import in.lib.thread.FragmentRunnable;
import in.lib.view.UserSuggestView;
import in.model.SimpleUser;
import in.model.Stream;
import in.model.User;
import in.model.base.NetObject;
import in.rob.client.base.RobinSlidingActivity;
import in.rob.client.dialog.NewChannelDialog;
import in.rob.client.dialog.base.DialogBuilder;
import in.rob.client.page.ChannelsPage;
import in.rob.client.page.UserFriendsPage;
import in.rob.client.page.base.StreamFragment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

public class ChannelsActivity extends RobinSlidingActivity
{
	private CreateChannelAdapter channelAdapter;

	@Override public void setup(boolean isPhone)
	{
		LinkedHashMap<Class, Bundle> pages = new LinkedHashMap<Class, Bundle>();
		Bundle extras = new Bundle();

		Bundle bundle1 = new Bundle(extras);
		bundle1.putString(Constants.EXTRA_TITLE, getString(R.string.channels));
		pages.put(ChannelsPage.class, bundle1);

		PhonePageAdapter adapter = new PhonePageAdapter(this, getSupportFragmentManager(), getViewPager(), pages, getSupportActionBar().getCustomView());
		setAdapter(adapter);
		getViewPager().setAdapter(adapter);
		getAdapter().setIndicatorVisible(false);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.channel, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item == null) return false;

		if (item.getItemId() == R.id.menu_new_channel)
		{
			ArrayList<SimpleUser> list = new ArrayList<SimpleUser>();
			list.add(SimpleUser.parseFromUser(UserManager.getUser()));
			channelAdapter = new CreateChannelAdapter(getContext(), R.layout.user_channel_dialog_list_item, list);
			newChannel();
			return true;
		}

		if (item.getItemId() == android.R.id.home)
		{
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void lockOrientation()
	{
		switch (getResources().getConfiguration().orientation)
		{
			case Configuration.ORIENTATION_PORTRAIT:
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO)
				{
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				}
				else
				{
					int rotation = getWindowManager().getDefaultDisplay().getRotation();
					if (rotation == android.view.Surface.ROTATION_90 || rotation == android.view.Surface.ROTATION_180)
					{
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
					}
					else
					{
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
					}
				}
				break;

			case Configuration.ORIENTATION_LANDSCAPE:
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.FROYO)
				{
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				}
				else
				{
					int rotation = getWindowManager().getDefaultDisplay().getRotation();
					if (rotation == android.view.Surface.ROTATION_0 || rotation == android.view.Surface.ROTATION_90)
					{
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
					}
					else
					{
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
					}
				}
				break;
		}
	}

	public void newChannel()
	{
		lockOrientation();
		DialogBuilder.create(getContext())
			.setTitle(R.string.add_users)
			.setCancelable(false)
			.setAdapter(channelAdapter, null)
			.setNegativeButton(R.string.cancel, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				}
			})
			.setNeutralButton(R.string.add_user, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					addUser();
				}
			})
			.setPositiveButton(R.string.next, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					ArrayList<SimpleUser> list = new ArrayList<SimpleUser>();
					int count = channelAdapter.getCount();

					for (int index = 0; index < count; index++)
					{
						list.add((SimpleUser)channelAdapter.getItem(index));
					}

					if (count < 1 || !list.contains(UserManager.getUser()))
					{
						list.add(SimpleUser.parseFromUser(UserManager.getUser()));
					}

					Intent channelMessage = new Intent(getContext(), NewChannelDialog.class);
					channelMessage.putExtra(Constants.EXTRA_USER_LIST, list);
					startActivity(channelMessage);
				}
			})
		.show();
	}

	private AutoCompleteAdapter adapter;
	public void addUser()
	{
		View v = getLayoutInflater().inflate(R.layout.search_user, null, false);
		final UserSuggestView suggest = (UserSuggestView)v.findViewById(R.id.search_user);
		List<NetObject> l = new ArrayList<NetObject>();

		adapter = new AutoCompleteAdapter(getContext(), l);
		suggest.setAdapter(adapter);
		suggest.setThreshold(2);
		suggest.setDropDownBackgroundResource(R.drawable.profile_avatar_fade);
		new CacheLoader().execute();

		Dialog dialog = DialogBuilder.create(getContext())
			.setTitle(R.string.search_users)
			.setView(v)
			.setOnCancelListener(new OnCancelListener()
			{
				@Override public void onCancel(DialogInterface dialog)
				{
					newChannel();
				}
			})
			.setNegativeButton(R.string.close, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					newChannel();
				}
			})
			.setPositiveButton(R.string.add_user, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					InputMethodManager m = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
					m.hideSoftInputFromWindow(suggest.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

					if (suggest.getSelectedUser() == null)
					{
						final ProgressDialog loader = new ProgressDialog(getContext());
						loader.setMessage(getString(R.string.searching_for_user));
						loader.show();

						UserStreamResponseHandler response = new UserStreamResponseHandler(getContext(), false)
						{
							@Override public void onCallback()
							{
								List<SimpleUser> users = CacheManager.getInstance().readFileAsObject(Constants.CACHE_USERNAMES, new ArrayList<SimpleUser>());
								List<String> usersStr = CacheManager.getInstance().readFileAsObject(Constants.CACHE_USERNAMES_STR, new ArrayList<String>());

								for (NetObject o : getObjects())
								{
									User u = (User)o;
									if (!usersStr.contains(u.getId()))
									{
										users.add(SimpleUser.parseFromUser(u));
										usersStr.add(u.getId());
										u.save();
									}
								}

								CacheManager.getInstance().writeFile(Constants.CACHE_USERNAMES, users);
								CacheManager.getInstance().writeFile(Constants.CACHE_USERNAMES_STR, usersStr);

								runOnUiThread(responseRunner);
							}

							private FragmentRunnable<StreamFragment> responseRunner = new FragmentRunnable<StreamFragment>()
							{
								@Override public void run()
								{
									if (getObjects().size() > 0)
									{
										if (getObjects().size() > 1)
										{
											final AccountAdapter userAdapter = new AccountAdapter(getContext(), R.layout.account_list_item, getObjects());
											DialogBuilder.create(getContext())
												.setTitle(R.string.select_user)
												.setOnCancelListener(new OnCancelListener()
												{
													@Override public void onCancel(DialogInterface dialog)
													{
														addUser();
													}
												})
												.setAdapter(userAdapter, new OnClickListener()
												{
													@Override public void onClick(DialogInterface dialog, int which)
													{
														channelAdapter.appendItem(userAdapter.getItem(which));
														newChannel();
														dialog.dismiss();
													}
												})
											.show();
										}
										else
										{
											if (channelAdapter.getItemById(getObjects().get(0).getId()) == null)
											{
												channelAdapter.appendItem(getObjects().get(0));
											}

											newChannel();
										}
									}
									else
									{
										DialogBuilder.create(getContext())
											.setTitle(R.string.error)
											.setMessage(R.string.unfound_user)
											.setNegativeButton(R.string.close, new OnClickListener()
											{
												@Override public void onClick(DialogInterface dialog, int which)
												{
													dialog.dismiss();
													addUser();
												}
											})
										.show();
									}

									loader.dismiss();
									//super.run();
								}
							};
						};

						APIManager.getInstance().searchUsers(suggest.getText().toString(), "", response);
					}
					else
					{
						if (channelAdapter.getItemById(suggest.getSelectedUser().getId()) == null)
						{
							channelAdapter.appendItem(suggest.getSelectedUser());
						}

						newChannel();
					}
				}
			})
		.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}

	class CacheLoader extends Loader<List<NetObject>>
	{
		public CacheLoader()
		{
			super("");
		}

		@Override public List<NetObject> doInBackground()
		{
			List<SimpleUser> users = CacheManager.getInstance().readFileAsObject(Constants.CACHE_USERNAMES, new ArrayList<SimpleUser>());
			List<NetObject> tags = new ArrayList<NetObject>();

			if (users.size() < 1)
			{
				// load default followers/following list and add them to the autocomplete
				Stream following = CacheManager.getInstance().readFileAsObject(String.format(Constants.CACHE_USER_LIST_NAME, UserFriendsPage.Mode.FOLLOWING.getModeText(), UserManager.getUserId()), Stream.class);
				Stream followers = CacheManager.getInstance().readFileAsObject(String.format(Constants.CACHE_USER_LIST_NAME, UserFriendsPage.Mode.FOLLOWERS.getModeText(), UserManager.getUserId()), Stream.class);

				if (following != null)
				{
					for (NetObject object : following.getObjects())
					{
						users.add(SimpleUser.parseFromUser((User)object));
					}
				}

				if (followers != null)
				{
					for (NetObject object : followers.getObjects())
					{
						users.add(SimpleUser.parseFromUser((User)object));
					}
				}

				CacheManager.getInstance().asyncWriteFile(Constants.CACHE_USERNAMES, users);
			}

			tags.addAll(users);
			return tags;
		}

		@Override public void onPostExecute(List<NetObject> tags)
		{
			adapter.setItems(tags);
			adapter.notifyDataSetChanged();
		}
	}
}