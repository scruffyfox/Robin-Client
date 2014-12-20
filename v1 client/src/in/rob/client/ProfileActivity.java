package in.rob.client;

import in.lib.Constants;
import in.lib.adapter.PhonePageAdapter;
import in.lib.helper.ThemeHelper;
import in.lib.manager.APIManager;
import in.lib.manager.CacheManager;
import in.lib.utils.Dimension;
import in.model.User;
import in.rob.client.base.RobinSlidingActivity;
import in.rob.client.dialog.NewPostDialog;
import in.rob.client.navigation.ProfileNavigationFragment;
import in.rob.client.page.MentionsPage;
import in.rob.client.page.ProfilePage;
import in.rob.client.page.UserFriendsPage;

import java.util.LinkedHashMap;

import lombok.Getter;
import lombok.Setter;
import android.content.Intent;
import android.content.res.Configuration;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnOpenListener;

public class ProfileActivity extends RobinSlidingActivity implements OnOpenListener
{
	@Getter @Setter private User user;
	private String userId;
	private String userName;

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
						NdefMessage msg;

						if (user != null)
						{
							msg = new NdefMessage(new NdefRecord[]
							{
								NdefRecord.createMime("application/vnd.in.rob.client.profileactivity", CacheManager.Serializer.serializeObject(user)),
								NdefRecord.createApplicationRecord(getPackageName())
							});
						}
						else if (!TextUtils.isEmpty(userId))
						{
							msg = new NdefMessage(new NdefRecord[]
							{
								NdefRecord.createMime("application/vnd.in.rob.client.profileactivity", CacheManager.Serializer.serializeObject(userId)),
								NdefRecord.createApplicationRecord(getPackageName())
							});
						}
						else
						{
							return null;
						}

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

	public void handleintent(Intent i)
	{
		Parcelable[] data = i.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

		if (data != null)
		{
			NdefMessage msg = (NdefMessage)data[0];
			Object object = CacheManager.Serializer.desterializeObject(msg.getRecords()[0].getPayload());

			if (object instanceof User)
			{
				user = (User)object;
				if (user == null)
				{
					Toast.makeText(getContext(), R.string.could_not_read_user, Toast.LENGTH_LONG).show();
					finish();
					return;
				}

				getIntent().putExtra(Constants.EXTRA_USER, user);
			}
			else
			{
				userId = object.toString();
				getIntent().putExtra(Constants.EXTRA_USER_ID, userId);
			}
		}
	}

	@Override public void retrieveArguments(Bundle instances)
	{
		if (instances != null)
		{
			if (instances.containsKey(Constants.EXTRA_USER_NAME))
			{
				userName = getIntent().getExtras().getString(Constants.EXTRA_USER_NAME);
				userName = userName.replace("%40", "");
				userName = userName.replace("@", "");
				userName = "@" + userName;
			}

			if (instances.containsKey(Constants.EXTRA_USER_ID))
			{
				userId = getIntent().getExtras().getString(Constants.EXTRA_USER_ID);
			}

			if (instances.containsKey(Constants.EXTRA_USER))
			{
				this.user = (User)getIntent().getExtras().getParcelable(Constants.EXTRA_USER);

				if (user != null)
				{
					this.userId = this.user.getId();
				}
			}
		}
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		if (this.user != null)
		{
			outState.putParcelable(Constants.EXTRA_USER, this.user);
		}

		super.onSaveInstanceState(outState);
	}

	@Override public void onOpen()
	{
		prepareRightSlidingMenu();
	}

	@Override public void setupForTablet()
	{
		// sliding menus
		setBehindRightContentView(R.layout.profile_navigation_fragment);
		getSlidingMenu().setOnOpenListener(this);

		//	Calculate the size for the sliding menu
		Dimension dimension = new Dimension(this);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		getSlidingMenu().setBehindWidth(dimension.densityPixel(100), SlidingMenu.RIGHT);

		setup(false);

		getAdapter().setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override public void onPageSelected(int index)
			{
				if (index == 2)
				{
					getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN, SlidingMenu.RIGHT);
				}
				else
				{
					getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
				}
			}

			@Override public void onPageScrolled(int arg0, float arg1, int arg2){}
			@Override public void onPageScrollStateChanged(int arg0){}
		});
	}

	/**
	 * Set up the activity for phone devices
	 */
	@Override public void setupForPhone()
	{
		// sliding menus
		setBehindRightContentView(R.layout.profile_navigation_fragment);

		getSlidingMenu().setOnOpenListener(this);

		//	Calculate the size for the sliding menu
		Dimension dimension = new Dimension(this);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			getSlidingMenu().setBehindWidth((int)dimension.getWidthFromRatio(100), SlidingMenu.LEFT | SlidingMenu.RIGHT);
		}
		else
		{
			getSlidingMenu().setBehindWidth((int)dimension.getWidthFromRatio(70), SlidingMenu.LEFT | SlidingMenu.RIGHT);
		}

		setup(true);

		getSlidingMenu().setBehindScrollScale(0.2f, SlidingMenu.BOTH);
		getAdapter().setOnPageChangeListener(new OnPageChangeListener()
		{
			@Override public void onPageSelected(int index)
			{
				if (index == 0 || index == 2)
				{
					getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN, index == 0 ? SlidingMenu.LEFT : SlidingMenu.RIGHT);
				}
				else
				{
					getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
				}
			}

			@Override public void onPageScrolled(int arg0, float arg1, int arg2){}
			@Override public void onPageScrollStateChanged(int arg0){}
		});
	}

	@Override public void setup(boolean isPhone)
	{
		String userTitle = "user";
		if (this.user != null && !TextUtils.isEmpty(this.user.getMentionName()) && !this.user.getMentionName().equals("null"))
		{
			userTitle = this.user.getMentionName();
		}
		else if (this.userName != null)
		{
			userTitle = userName;
		}

		userTitle = userTitle.replace("%40", "");
		userTitle = userTitle.replace("@", "");

		LinkedHashMap<Class, Bundle> pages = new LinkedHashMap<Class, Bundle>(3);
		Bundle extras = new Bundle();

		if (this.user != null)
		{
			extras.putParcelable(Constants.EXTRA_USER, this.user);
		}
		else if (this.userId != null)
		{
			extras.putString(Constants.EXTRA_USER_ID, this.userId);
		}
		else if (this.userName != null)
		{
			extras.putString(Constants.EXTRA_USER_NAME, this.userName);
		}

		Bundle bundle1 = new Bundle(extras);
		bundle1.putString(Constants.EXTRA_TITLE, getString(R.string.at) + userTitle);
		pages.put(ProfilePage.class, bundle1);

		Bundle bundle2 = new Bundle(extras);
		bundle2.putString(Constants.EXTRA_TITLE, getString(R.string.mentions));
		pages.put(MentionsPage.class, bundle2);

		Bundle bundle3 = new Bundle(extras);
		bundle3.putString(Constants.EXTRA_TITLE, getString(R.string.followers));
		pages.put(UserFriendsPage.class, bundle3);

		PhonePageAdapter adapter = new PhonePageAdapter(this, getSupportFragmentManager(), getViewPager(), pages, getSupportActionBar().getCustomView());
		getViewPager().setAdapter(adapter);
		setAdapter(adapter);
	}

	/**
	 * Set up the right menu when it gets opened
	 */
	private void prepareRightSlidingMenu()
	{
		View rightMenu = getSlidingMenu();
		ProfileNavigationFragment nav = (ProfileNavigationFragment)getSupportFragmentManager().findFragmentById(R.id.profile_frame);

		if (this.user == null)
		{
			((SlidingMenu)rightMenu).showAbove();
			return;
		}

		if (!this.user.isYou())
		{
			nav.mFollow.setImageResource(this.user.getYouFollow() ? R.drawable.nav_icon_unfollow : R.drawable.nav_icon_follow);
			nav.mFollow.setContentDescription(this.user.getYouFollow() ? getString(R.string.unfollow) : getString(R.string.follow));
			nav.mMute.setImageResource(this.user.isMuted() ? R.drawable.nav_icon_unmute : R.drawable.nav_icon_mute);
			nav.mMute.setContentDescription(this.user.isMuted() ? getString(R.string.unmute) : getString(R.string.mute));
			nav.mBlock.setImageResource(this.user.isBlocked() ? R.drawable.nav_icon_unblock : R.drawable.nav_icon_block);
			nav.mBlock.setContentDescription(this.user.isBlocked() ? getString(R.string.unblock) : getString(R.string.block));
			nav.mMuted.setVisibility(View.GONE);
		}
		else
		{
			nav.mFollow.setVisibility(View.GONE);
			nav.mMessage.setVisibility(View.GONE);
			nav.mMute.setVisibility(View.GONE);
			nav.mBlock.setVisibility(View.GONE);
			nav.mMuted.setVisibility(View.VISIBLE);
		}
	}

	@Override public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		// menu button
		if (keyCode == KeyEvent.KEYCODE_MENU && !handledLongPress && this.user != null)
		{
			toggle(SlidingMenu.RIGHT);
			return true;
		}

		handledLongPress = false;
		return super.onKeyUp(keyCode, event);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		int replyicon = ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_icon_reply);
		int menuicon = ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_icon_menu);
		menu.clear();

		if (user != null)
		{
			MenuItem mention = menu.add(0, Constants.MENU_MENTION_ID, 0, getString(R.string.mention)).setIcon(replyicon);
			MenuItemCompat.setShowAsAction(mention, MenuItem.SHOW_AS_ACTION_ALWAYS);

			MenuItem more = menu.add(0, Constants.MENU_MORE_ID, 0, getString(R.string.options)).setIcon(menuicon);
			MenuItemCompat.setShowAsAction(more, MenuItem.SHOW_AS_ACTION_ALWAYS);
			prepareRightSlidingMenu();
		}
		else
		{
			getMenuInflater().inflate(R.menu.empty, menu);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item == null) return false;

		if (item.getItemId() == android.R.id.home)
		{
			finish();
			return true;
		}

		if (item.getItemId() == Constants.MENU_MENTION_ID)
		{
			Intent mentionIntent = new Intent(getContext(), NewPostDialog.class);
			mentionIntent.putExtra(Constants.EXTRA_MENTION_NAME, this.user.getMentionName());
			startActivity(mentionIntent);
		}
		else if (item.getItemId() == Constants.MENU_MORE_ID)
		{
			if (this.user != null)
			{
				prepareRightSlidingMenu();
				toggle(SlidingMenu.RIGHT);
			}
		}

		return super.onOptionsItemSelected(item);
	}

	public void followUnfollow()
	{
		SlidingMenu rightMenu = getSlidingMenu();
		boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		final int redButton = ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_red_button);
		final int greyButton = ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_grey_button);

		if (user.getYouFollow())
		{
			user.setYouFollow(false);
			user.setFollowersCount(user.getFollowersCount() - 1);
			((ImageView)rightMenu.findViewById(R.id.menu_follow_button)).setContentDescription(getString(R.string.follow));
			((ImageView)rightMenu.findViewById(R.id.menu_follow_button)).setImageResource(R.drawable.nav_icon_follow);
			((Button)findViewById(R.id.follow_button)).setText(getString(R.string.follow));
			((Button)findViewById(R.id.follow_button)).setBackgroundResource(redButton);

			APIManager.getInstance().unfollowUser(user.getId(), null);
		}
		else
		{
			user.setYouFollow(true);
			user.setFollowersCount(user.getFollowersCount() + 1);
			((ImageView)rightMenu.findViewById(R.id.menu_follow_button)).setContentDescription(getString(R.string.unfollow));
			((ImageView)rightMenu.findViewById(R.id.menu_follow_button)).setImageResource(R.drawable.nav_icon_unfollow);
			((Button)findViewById(R.id.follow_button)).setText(getString(R.string.unfollow));
			((Button)findViewById(R.id.follow_button)).setBackgroundResource(greyButton);

			APIManager.getInstance().followUser(user.getId(), null);
		}

		((TextView)findViewById(R.id.follow_count)).setText
		(
			Html.fromHtml(String.format(getString(R.string.profile_stats),
					"<b>" + user.getFollowingCount() + "</b>",
					(landscape ? " - " : "<br />") + "<b>" + user.getFollowersCount() + "</b>",
					"<br /><b>" + user.getStarredCount() + "</b>",
					"<br /><b>" + user.getPostCount() + "</b>"))
		);
	}

	public void muteUnmute()
	{
		SlidingMenu rightMenu = getSlidingMenu();

		if (user.isMuted())
		{
			user.setMuted(false);
			((ImageView)rightMenu.findViewById(R.id.mute_button)).setContentDescription(getString(R.string.mute));
			((ImageView)rightMenu.findViewById(R.id.mute_button)).setImageResource(R.drawable.nav_icon_mute);

			APIManager.getInstance().unMuteUser(user.getId(), null);
		}
		else
		{
			user.setMuted(true);
			((ImageView)rightMenu.findViewById(R.id.mute_button)).setContentDescription(getString(R.string.unmute));
			((ImageView)rightMenu.findViewById(R.id.mute_button)).setImageResource(R.drawable.nav_icon_unmute);

			APIManager.getInstance().muteUser(user.getId(), null);
		}
	}

	public void blockUnblock()
	{
		SlidingMenu rightMenu = getSlidingMenu();

		if (user.isBlocked())
		{
			user.setBlocked(false);
			((ImageView)rightMenu.findViewById(R.id.block_button)).setContentDescription(getString(R.string.block));
			((ImageView)rightMenu.findViewById(R.id.block_button)).setImageResource(R.drawable.nav_icon_block);

			APIManager.getInstance().unblockUser(user.getId(), null);
		}
		else
		{
			user.setBlocked(true);
			((ImageView)rightMenu.findViewById(R.id.block_button)).setContentDescription(getString(R.string.unblock));
			((ImageView)rightMenu.findViewById(R.id.block_button)).setImageResource(R.drawable.nav_icon_unblock);

			APIManager.getInstance().blockUser(user.getId(), null);
		}
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

	@Override public void setLeftNavigationContentForTablet(){}
}