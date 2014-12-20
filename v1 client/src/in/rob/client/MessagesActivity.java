package in.rob.client;

import in.lib.Constants;
import in.lib.adapter.PhonePageAdapter;
import in.lib.manager.APIManager;
import in.model.Channel;
import in.model.Channel.Type;
import in.rob.client.base.RobinSlidingActivity;
import in.rob.client.dialog.NewMessageDialog;
import in.rob.client.page.MessagesPage;

import java.util.LinkedHashMap;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

public class MessagesActivity extends RobinSlidingActivity
{
	private String channelId = "";
	private Channel channel;
	private MenuItem mUnsubscribeMenu;

	@Override public void retrieveArguments(Bundle instances)
	{
		if (instances.containsKey(Constants.EXTRA_CHANNEL))
		{
			channel = (Channel)instances.getParcelable(Constants.EXTRA_CHANNEL);
			channelId = channel.getId();
		}
		else if (instances.containsKey(Constants.EXTRA_CHANNEL_ID))
		{
			channelId = instances.getString(Constants.EXTRA_CHANNEL_ID);
		}

		super.retrieveArguments(instances);
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putString(Constants.EXTRA_CHANNEL_ID, channelId);

		if (channel != null)
		{
			outState.putParcelable(Constants.EXTRA_CHANNEL, channel);
		}

		super.onSaveInstanceState(outState);
	}

	@Override public void setup(boolean isPhone)
	{
		LinkedHashMap<Class, Bundle> pages = new LinkedHashMap<Class, Bundle>(1);

		Bundle bundle1 = new Bundle();
		String title = getString(R.string.messages);

		if (channel != null)
		{
			bundle1.putParcelable(Constants.EXTRA_CHANNEL, channel);

			if (!TextUtils.isEmpty(channel.getTitle()))
			{
				title = channel.getTitle();
			}
			else if (channel.getType() == Type.PRIVATE_MESSAGE)
			{
				title = getString(R.string.private_messaging);
			}
		}
		else
		{
			bundle1.putString(Constants.EXTRA_CHANNEL_ID, channelId);
		}

		bundle1.putString(Constants.EXTRA_TITLE, title);
		pages.put(MessagesPage.class, bundle1);

		PhonePageAdapter adapter = new PhonePageAdapter(this, getSupportFragmentManager(), getViewPager(), pages, getSupportActionBar().getCustomView());
		getViewPager().setAdapter(adapter);
		setAdapter(adapter);
		getAdapter().setIndicatorVisible(false);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.message, menu);

		mUnsubscribeMenu = menu.findItem(R.id.menu_unsubscribe);

		if (channel != null && channel.isSubscribed())
		{
			mUnsubscribeMenu.setTitle(R.string.unsubscribe);
		}

		return true;
	}

	@Override public boolean onOptionsItemSelected(android.view.MenuItem item)
	{
		if (item == null) return false;

		if (item.getItemId() == R.id.menu_new_message)
		{
			Intent messageDialog = new Intent(getContext(), NewMessageDialog.class);
			messageDialog.putExtra(Constants.EXTRA_CHANNEL_ID, channelId);

			if (channel != null)
			{
				messageDialog.putExtra(Constants.EXTRA_IS_PUBLIC, channel.isPublic());
				messageDialog.putExtra(Constants.EXTRA_CHANNEL_NAME, channel.getTitle());
			}

			startActivity(messageDialog);
		}
		else if (item.getItemId() == R.id.menu_unsubscribe)
		{
			if (channel != null)
			{
				if (channel.isSubscribed())
				{
					channel.setSubscribed(false);
					APIManager.getInstance().unsubscribeChannel(channel.getId(), null);
					mUnsubscribeMenu.setTitle(R.string.subscribe);
				}
				else
				{
					channel.setSubscribed(true);
					APIManager.getInstance().subscribeChannel(channel.getId(), null);
					mUnsubscribeMenu.setTitle(R.string.unsubscribe);
				}
			}
			else
			{
				APIManager.getInstance().subscribeChannel(channelId, null);
			}
		}
		else if (item.getItemId() == android.R.id.home)
		{
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override public boolean checkMenuKey(int keyCode)
	{
		return false;
	}
}