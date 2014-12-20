package in.rob.client.page;

import in.lib.Constants;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.manager.ImageAPIManager;
import in.lib.manager.ImageAPIManager.Provider;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.StringUtils;
import in.lib.utils.Views;
import in.rob.client.MainApplication;
import in.rob.client.R;
import in.rob.client.SplashActivity;
import in.rob.client.base.RobinFragment;
import in.rob.client.dialog.base.DialogBuilder;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;

public class GeneralSettingsPage extends RobinFragment implements OnCheckedChangeListener, OnSeekBarChangeListener, OnClickListener
{
	@InjectView(R.id.inline_wifi) public CheckBox mInlineWifi;
	@InjectView(R.id.shake_refresh) public CheckBox mShakeRefresh;
	@InjectView(R.id.quick_post_notification) public CheckBox mQuickPost;
	@InjectView(R.id.refresh_timeout) public SeekBar mRefreshTimeout;
	@InjectView(R.id.timeout_text) public TextView mRefreshTv;
	@InjectView(R.id.image_provider) public TextView mImageProvider;
	@InjectView(R.id.notification_quiet_mode) public CheckBox mQuietMode;
	@InjectView(R.id.quite_hours_summary) public TextView mQuietSummary;
	@InjectView(R.id.quiet_mode_summary) public TextView mQuietModeSummary;
	@InjectView(R.id.request_timeout) public SeekBar mRequestTimeout;
	@InjectView(R.id.request_text) public TextView mRequestTv;
	@OnClick @InjectView(R.id.quiet_hours) public View mQuietHours;
	@OnClick @InjectView(R.id.image_provider_container) public View mImageProvView;
	@OnClick @InjectView(R.id.logout_button) public View mLogout;

	private SettingsManager mSettingsManager;

	private Intent mIntent;
	private NotificationManager notificationManager;
	public static final int QUICK_POST_ID = 0x5135;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.general_settings_view, null);
		Views.inject(this, view);
		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		notificationManager = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
		mSettingsManager = SettingsManager.getInstance();
		mIntent = new Intent();

		// init inputs
		mRefreshTimeout.setOnSeekBarChangeListener(this);
		mRefreshTimeout.setProgress(SettingsManager.getCacheTimeout() < 0 ? 0 : (int)(SettingsManager.getCacheTimeout() / 1000 / 60));
		mRefreshTv.setText(SettingsManager.getCacheTimeout() < 0 ? getString(R.string.never) : (int)(SettingsManager.getCacheTimeout() / 1000 / 60) + " mins");
		mQuickPost.setChecked(SettingsManager.isQuickPostEnabled());
		mQuickPost.setOnCheckedChangeListener(this);
		mShakeRefresh.setChecked(SettingsManager.isShakeRefreshEnabled());
		mShakeRefresh.setOnCheckedChangeListener(this);
		mImageProvider.setText(SettingsManager.getImageProvider().getName());
		mQuietMode.setOnCheckedChangeListener(this);
		mQuietMode.setChecked(SettingsManager.isQuietModeEnabled());
		mInlineWifi.setOnCheckedChangeListener(this);
		mInlineWifi.setChecked(SettingsManager.isInlineImageWifiEnabled());

		mRequestTimeout.setOnSeekBarChangeListener(this);
		mRequestTimeout.setProgress(SettingsManager.getRequestTimeout() <= 0 ? 0 : (int)(SettingsManager.getRequestTimeout() / 1000));
		mRequestTv.setText(SettingsManager.getRequestTimeout() <= 0 ? getString(R.string.never) : (int)(SettingsManager.getRequestTimeout() / 1000) + " seconds");

		setQuietStrings();
	}

	public void setQuietStrings()
	{
		GregorianCalendar dateAfter = new GregorianCalendar();
		dateAfter.setTimeInMillis(SettingsManager.getQuietModeFrom());
		GregorianCalendar dateBefore = new GregorianCalendar();
		dateBefore.setTimeInMillis(SettingsManager.getQuietModeTo());

		String fromHour = "" + dateAfter.get(Calendar.HOUR_OF_DAY) + ":";
		String fromMinute = StringUtils.padTo("" + (dateAfter.get(Calendar.MINUTE)), 2, "0", true);
		String fromAmPm = "";//dateAfter.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm";
		String toHour = "" + dateBefore.get(Calendar.HOUR_OF_DAY) + ":";
		String toMinute = StringUtils.padTo("" + (dateBefore.get(Calendar.MINUTE)), 2, "0", true);
		String toAmPm = "";//dateBefore.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm";
		mQuietSummary.setText(getString(R.string.quiet_summary) + " " + fromHour + fromMinute + fromAmPm + "-" + toHour + toMinute + toAmPm);
		mQuietModeSummary.setText(getString(R.string.quiet_mode_summary) + " " + fromHour + fromMinute + fromAmPm + "-" + toHour + toMinute + toAmPm);
	}

	@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (buttonView == mQuietMode)
		{
			mSettingsManager.setQuietModeEnabled(isChecked);
		}
		else if (buttonView == mInlineWifi)
		{
			mSettingsManager.setInlineImageWifiOnly(isChecked);
		}
		else if (buttonView == mQuickPost)
		{
			mSettingsManager.setQuickPostEnabled(isChecked);

			if (isChecked)
			{
				((MainApplication)getActivity().getApplication()).initQuickPost();
			}
			else
			{
				notificationManager.cancel(QUICK_POST_ID);
			}
		}
		else if (buttonView == mShakeRefresh)
		{
			mSettingsManager.setShakeRefreshEnabled(isChecked);
		}

		if (mIntent.getExtras() != null)
		{
			getActivity().setResult(Constants.RESULT_REFRESH, mIntent);
		}
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
		if (v == mQuietHours)
		{
			final GregorianCalendar dateAfter = new GregorianCalendar();
			dateAfter.setTimeInMillis(SettingsManager.getQuietModeFrom());
			final GregorianCalendar dateBefore = new GregorianCalendar();
			dateBefore.setTimeInMillis(SettingsManager.getQuietModeTo());

			final TimePickerDialog fromTime = new TimePickerDialog(getContext(), new OnTimeSetListener()
			{
				@Override public void onTimeSet(TimePicker view, int hourOfDay, int minute)
				{
					dateAfter.set(Calendar.DAY_OF_MONTH, 1);
					dateAfter.set(Calendar.HOUR_OF_DAY, hourOfDay);
					dateAfter.set(Calendar.MINUTE, minute);
				}
			}, dateAfter.get(Calendar.HOUR_OF_DAY), dateAfter.get(Calendar.MINUTE), true);

			final TimePickerDialog toTime = new TimePickerDialog(getContext(), new OnTimeSetListener()
			{
				@Override public void onTimeSet(TimePicker view, int hourOfDay, int minute)
				{
					dateBefore.set(Calendar.DAY_OF_MONTH, 1);

					if (hourOfDay < dateAfter.get(Calendar.HOUR_OF_DAY))
					{
						dateBefore.add(Calendar.DAY_OF_MONTH, 1);
					}

					dateBefore.set(Calendar.HOUR_OF_DAY, hourOfDay);
					dateBefore.set(Calendar.MINUTE, minute);

					mSettingsManager.setQuietHours(dateAfter.getTimeInMillis(), dateBefore.getTimeInMillis());
					setQuietStrings();
				}
			}, dateBefore.get(Calendar.HOUR_OF_DAY), dateBefore.get(Calendar.MINUTE), true);

			fromTime.setOnDismissListener(new OnDismissListener()
			{
				@Override public void onDismiss(DialogInterface dialog)
				{
					toTime.show();
				}
			});

			toTime.setTitle(R.string.set_to_time);
			fromTime.setTitle(R.string.set_from_time);
			fromTime.show();
		}
		else if (v == mImageProvView)
		{
			final HashMap<String, ImageAPIManager.Provider> providers = new HashMap<String, ImageAPIManager.Provider>();
			for (Provider p : ImageAPIManager.Provider.values())
			{
				providers.put(p.getName(), p);
			}

			DialogBuilder.create(getContext())
				.setTitle(R.string.pick_option)
				.setItems(providers.keySet().toArray(new String[providers.size()]), new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						String[] prov = providers.keySet().toArray(new String[providers.size()]);
						mSettingsManager.setImageProvider(providers.get(prov[which]));
						ImageAPIManager.getInstance().registerForToken(getContext(), UserManager.getUser());
						mImageProvider.setText(prov[which]);
					}
				})
				.show();
		}
		else if (v == mLogout)
		{
			DialogBuilder.create(getContext())
				.setTitle(R.string.confirm)
				.setMessage(R.string.logout_description)
				.setPositiveButton(R.string.logout, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						// clear user
						UserManager.logout(getContext());

						// restart app
						Intent auth = new Intent(getContext(), SplashActivity.class);
						auth.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						auth.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
						auth.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
						auth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(auth);
					}
				})
				.setNegativeButton(R.string.cancel, null)
			.show();
		}
	}

	/**
	 * Popup for animation settings
	 */
	@OnClick(R.id.stream_markers) public void showStreamMarkerOptions()
	{
		final boolean[] options = new boolean[getResources().getStringArray(R.array.stream_marker_options).length];
		final int[] ints = getResources().getIntArray(R.array.stream_marker_choice_mask);

		for (int index = 0; index < ints.length; index++)
		{
			options[index] = (SettingsManager.getStreamMarker() & ints[index]) == ints[index];
		}

		DialogBuilder.create(getContext())
			.setTitle(R.string.pick_option)
			.setMultiChoiceItems(R.array.stream_marker_options, options, new DialogInterface.OnMultiChoiceClickListener()
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

					mSettingsManager.setStreamMarkerOptions(finalInt);
				}
			})
			.setNegativeButton(R.string.cancel, null)
		.show();
	}

	@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		if (seekBar == mRefreshTimeout)
		{
			if (progress == 0)
			{
				mRefreshTv.setText(R.string.never);
				mSettingsManager.setCacheTimeout(-1);
			}
			else
			{
				mRefreshTv.setText(progress + " mins");
				mSettingsManager.setCacheTimeout(progress * 60 * 1000);
			}
		}
		else if (seekBar == mRequestTimeout)
		{
			if (progress == 0)
			{
				mRequestTv.setText(R.string.never);
				mSettingsManager.setRequestTimeout(-1);
			}
			else
			{
				mRequestTv.setText(progress + " seconds");
				mSettingsManager.setRequestTimeout(progress * 1000);
			}
		}
	}

	@Override public void onStartTrackingTouch(SeekBar seekBar){}
	@Override public void onStopTrackingTouch(SeekBar seekBar){}
}