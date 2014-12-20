package in.rob.client;

import in.lib.Constants;
import in.lib.adapter.PhonePageAdapter;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.model.Post;
import in.rob.client.base.RobinSlidingActivity;
import in.rob.client.page.ThreadPage;

import java.util.LinkedHashMap;

import lombok.Setter;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Toast;

/**
 * TODO: Add support to remember position
 * TODO: Figgure out pagination for pages < current
 * TODO: Add menu items + setup broadcast reciever
 */
public class ThreadActivity extends RobinSlidingActivity
{
	private MenuItem mUnsubscribeMenu;
	@Setter private Post post;

	@Override public void onCreate(Bundle arg0)
	{
		super.onCreate(arg0);

		// if the intent extras are null but the saved instances isnt
		// reset the intent extras to the saved instances
		if (getIntent().getExtras() == null && arg0 != null)
		{
			getIntent().putExtras(arg0);
		}

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD_MR1)
		{
			NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
			if (adapter != null && adapter.isEnabled())
			{
				adapter.setNdefPushMessageCallback(new CreateNdefMessageCallback()
				{
					@Override public NdefMessage createNdefMessage(NfcEvent event)
					{
						NdefMessage msg = new NdefMessage
						(
							new NdefRecord[]
							{
								NdefRecord.createMime("application/vnd.in.rob.client.threadactivity", CacheManager.Serializer.serializeObject(getIntent().getExtras().get(Constants.EXTRA_POST))),
								NdefRecord.createApplicationRecord(getPackageName())
							}
						);
						return msg;
					}
				}, this);

				if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()))
				{
					handleintent(getIntent());
				}
			}
		}
	}

	@Override public void retrieveArguments(Bundle instances)
	{
		if (instances.containsKey(Constants.EXTRA_POST))
		{
			post = (Post)instances.getParcelable(Constants.EXTRA_POST);
		}

		super.retrieveArguments(instances);
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		if (post != null)
		{
			outState.putParcelable(Constants.EXTRA_POST, post);
		}

		super.onSaveInstanceState(outState);
	}

	public void handleintent(Intent i)
	{
		Parcelable[] data = i.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

		if (data != null)
		{
			NdefMessage msg = (NdefMessage)data[0];
			post = (Post)CacheManager.Serializer.desterializeObject(msg.getRecords()[0].getPayload());
			if (post == null)
			{
				Toast.makeText(getContext(), R.string.could_not_read_post, Toast.LENGTH_LONG).show();
				finish();
				return;
			}

			getIntent().putExtra(Constants.EXTRA_POST, post);
		}
	}

	@Override public void setup(boolean isPhone)
	{
		LinkedHashMap<Class, Bundle> pages = new LinkedHashMap<Class, Bundle>(1);

		Bundle bundle1 = new Bundle();
		bundle1.putString(Constants.EXTRA_TITLE, getString(R.string.conversation));
		bundle1.putAll(getIntent().getExtras());
		pages.put(ThreadPage.class, bundle1);

		PhonePageAdapter adapter = new PhonePageAdapter(getContext(), getSupportFragmentManager(), getViewPager(), pages, getSupportActionBar().getCustomView());
		getViewPager().setAdapter(adapter);
		setAdapter(adapter);
		getAdapter().setIndicatorVisible(false);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.thread, menu);

		if (Build.VERSION.SDK_INT >= 11 && ViewConfiguration.get(this).hasPermanentMenuKey())
		{
			menu.getItem(0).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.getItem(0).setVisible(true);
		}

		mUnsubscribeMenu = menu.findItem(R.id.menu_mute);

		if (post != null && SettingsManager.isThreadMuted(post.getThreadId()))
		{
			mUnsubscribeMenu.setTitle(R.string.unmute_thread);
		}

		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_mute)
		{
			SettingsManager settings = SettingsManager.getInstance(getContext());

			if (SettingsManager.isThreadMuted(post.getThreadId()))
			{
				settings.unmuteThread(post.getThreadId());
				mUnsubscribeMenu.setTitle(R.string.mute_thread);
			}
			else
			{
				settings.muteThread(post.getThreadId());
				mUnsubscribeMenu.setTitle(R.string.unmute_thread);
			}
		}
		else if (item.getItemId() == R.id.menu_new_post)
		{
			getCurrentFragment().onOptionsItemSelected(item);
		}
		else if (item.getItemId() == R.id.menu_refresh)
		{
			((ThreadPage)getAdapter().getCurrentFragment()).beginLoadFromApi();
		}

		return super.onOptionsItemSelected(item);
	}

	@Override public boolean checkMenuKey(int keyCode)
	{
		return false;
	}

	@Override public void onClick(View v)
	{
		if (v.getId() == R.id.up_button)
		{
			Intent homeIntent = new Intent(getContext(), MainActivity.class);
			homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(homeIntent);
			return;
		}

		super.onClick(v);
	}
}