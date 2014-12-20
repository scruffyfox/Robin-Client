package in.rob.client.page;

import in.lib.Constants;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.manager.SettingsManager;
import in.lib.utils.Views;
import in.rob.client.R;
import in.rob.client.base.RobinFragment;
import in.rob.client.dialog.base.DialogBuilder;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class NotificationSettingsPage extends RobinFragment implements OnCheckedChangeListener, OnClickListener
{
	@InjectView(R.id.notification_sound) public CheckBox mToggleNotificationSounds;
	@InjectView(R.id.notification_vibrate) public CheckBox mToggleNotificationVibrate;
	@InjectView(R.id.notifications_following) public CheckBox mToggleNotificationFollowing;
	@InjectView(R.id.notifications_led) public CheckBox mNotificationsLed;
	@OnClick @InjectView(R.id.notification_sound_tone_container) public View mNotificationTone;
	@OnClick @InjectView(R.id.notifications) public View mNotifications;
	@OnClick @InjectView(R.id.swarm_protection) public View mSwarmProtection;

	private SettingsManager mSettingsManager;
	private boolean saveNotification = false;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.notification_settings_view, null);
		Views.inject(this, view);
		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		mSettingsManager = SettingsManager.getInstance();
		mToggleNotificationSounds.setChecked(SettingsManager.isNotificationsSoundEnabled());
		mToggleNotificationSounds.setOnCheckedChangeListener(this);
		mToggleNotificationVibrate.setChecked(SettingsManager.isNotificationsVibrateEnabled());
		mToggleNotificationVibrate.setOnCheckedChangeListener(this);
		mToggleNotificationFollowing.setChecked(SettingsManager.isNotificationsOnlyFollowing());
		mToggleNotificationFollowing.setOnCheckedChangeListener(this);
		mNotificationsLed.setChecked(SettingsManager.isNotificationLedEnabled());
		mNotificationsLed.setOnCheckedChangeListener(this);
	}

	@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (buttonView == mNotificationsLed)
		{
			mSettingsManager.setNotificationLedEnabled(isChecked);
		}
		else if (buttonView == mToggleNotificationVibrate)
		{
			mSettingsManager.setNotificationVibrateEnabled(isChecked);
		}
		else if (buttonView == mToggleNotificationSounds)
		{
			mSettingsManager.setNotificationsSoundEnabled(isChecked);
		}
		else if (buttonView == mToggleNotificationFollowing)
		{
			saveNotification = true;
			mSettingsManager.setNotificationFollowingEnabled(isChecked);
		}
	}

	public void showNotificationOptions()
	{
		final boolean[] options = new boolean[getResources().getStringArray(R.array.notification_setting_choice).length];
		final int[] ints = getResources().getIntArray(R.array.notification_setting_choice_mask);

		for (int index = 0; index < ints.length; index++)
		{
			options[index] = (SettingsManager.getNotifications() & ints[index]) == ints[index];
		}

		DialogBuilder.create(getContext())
			.setTitle(R.string.select_notification)
			.setMultiChoiceItems(R.array.notification_setting_choice, options, new DialogInterface.OnMultiChoiceClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which, boolean isChecked)
				{
					options[which] = isChecked;
				}
			})
			.setPositiveButton(R.string.done, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					int finalInt = 0;
					for (int index = 0; index < options.length; index++)
					{
						if (options[index])
						{
							finalInt |= ints[index];
						}
					}

					mSettingsManager.setNotificationOptions(finalInt);
				}
			})
			.setNegativeButton(R.string.cancel, null)
		.show();
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Constants.REQUEST_RINGTONE)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				if (uri != null)
				{
					mSettingsManager.setNotificationTone(uri.toString());
				}
			}
		}
	}

	@Override public void onClick(View v)
	{
		if (v == mNotificationTone)
		{
			Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.select_tone));
			intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, TextUtils.isEmpty(SettingsManager.getNotificationTone()) ? null : Uri.parse(SettingsManager.getNotificationTone()));
			startActivityForResult(intent, Constants.REQUEST_RINGTONE);
		}
		else if (v == mNotifications)
		{
			saveNotification = true;
			showNotificationOptions();
		}
		else if (v == mSwarmProtection)
		{
			DialogBuilder.create(getContext())
				.setTitle(R.string.pick_option)
				.setSingleChoiceItems(R.array.swarm_options, SettingsManager.getSwarmProtectionIndex(), new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						mSettingsManager.setSwarmProtectionIndex(which);
						dialog.dismiss();
					}
				})
			.show();
		}
	}

	@Override public void onDestroy()
	{
		if (saveNotification)
		{
			mSettingsManager.saveSettings(getContext());
		}

		super.onDestroy();
	}
}