package in.rob.client.page;

import in.lib.Constants;
import in.lib.URLMatcher;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.manager.APIManager;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UpdateManager;
import in.lib.manager.UserManager;
import in.lib.utils.Views;
import in.rob.client.Licences;
import in.rob.client.MainApplication;
import in.rob.client.MainApplication.ApplicationType;
import in.rob.client.R;
import in.rob.client.base.RobinFragment;
import in.rob.client.dialog.base.DialogBuilder;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
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
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.nostra13.universalimageloader.core.ImageLoader;

public class AdditionalSettingsPage extends RobinFragment implements OnClickListener, OnCheckedChangeListener, OnSeekBarChangeListener
{
	@OnClick @InjectView(R.id.change_log) public View mChangeLog;
	@OnClick @InjectView(R.id.developer_info) public View mDeveloperInfo;
	@OnClick @InjectView(R.id.image_cache) public View mImageCache;
	@OnClick @InjectView(R.id.cache) public View mCache;
	@OnClick @InjectView(R.id.licenses) public View mLicences;
	@OnClick @InjectView(R.id.check_updates) public View mCheckUpdates;
	@InjectView(R.id.image_cache_size) public TextView mImageCacheSize;
	@InjectView(R.id.cache_size) public TextView mCacheSize;
	@InjectView(R.id.version_name) public TextView mVersionName;
	@InjectView(R.id.device_id) public TextView mDeviceId;
	@InjectView(R.id.build_number) public TextView mBuildNumber;
	@InjectView(R.id.analytics) public CheckBox mAnalytics;
	@InjectView(R.id.crash_reports) public CheckBox mCrashReporting;
	@InjectView(R.id.max_cache) public SeekBar mMaxCache;
	@InjectView(R.id.max_image_cache) public SeekBar mMaxImageCache;
	@InjectView(R.id.max_cache_size) public TextView mMaxCacheSize;
	@InjectView(R.id.max_image_size) public TextView mMaxImageCacheSize;

	private SettingsManager mSettings;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.additional_settings_view, null);
		Views.inject(this, view);
		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		mSettings = SettingsManager.getInstance();

		try
		{
			mBuildNumber.setText("" + getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionCode);
		}
		catch (Exception e) {}

		mDeviceId.setText(getDeviceId());
		mVersionName.setText(getString(R.string.version) + " " + getString(R.string.app_version));
		mImageCacheSize.setText(getString(R.string.currently) + ": " + calculateImageCacheSize());
		mCacheSize.setText(getString(R.string.currently) + ": " + calculateCacheSize());
		mAnalytics.setOnCheckedChangeListener(this);
		mAnalytics.setChecked(SettingsManager.isAnalyticsEnabled());
		mCrashReporting.setOnCheckedChangeListener(this);
		mCrashReporting.setChecked(SettingsManager.isCrashReportEnabled());

		mMaxCacheSize.setText(SettingsManager.getMaxCacheSize() + "mb");
		mMaxImageCacheSize.setText(SettingsManager.getMaxImageCacheSize() + "mb");
		mMaxCache.setProgress(SettingsManager.getMaxCacheSize() - 10);
		mMaxImageCache.setProgress(SettingsManager.getMaxImageCacheSize() - 10);
		mMaxCache.setOnSeekBarChangeListener(this);
		mMaxImageCache.setOnSeekBarChangeListener(this);

		if (((MainApplication)getActivity().getApplication()).getApplicationType() == ApplicationType.CD_KEY)
		{
			((View)mCheckUpdates.getParent()).setVisibility(View.VISIBLE);
		}
	}

	public String calculateImageCacheSize()
	{
		try
		{
			if (getContext().getExternalCacheDir() != null)
			{
				File f = new File(getContext().getExternalCacheDir().getAbsolutePath() + "/uil-images/");
				File[] files = f.listFiles();
				long size = 0;

				for (File file : files)
				{
					size += file.length();
				}

				return Math.floor((size / 1024.0d / 1024.0d) * 100.0d) / 100.0d + "MB";
			}
			else
			{
				return "unavailable";
			}
		}
		catch (Exception e)
		{
			return "unavailable";
		}
	}

	public String calculateCacheSize()
	{
		try
		{
			if (getContext().getFilesDir() != null)
			{
				File f = new File(getContext().getFilesDir().getAbsolutePath());
				File[] files = f.listFiles();
				long size = 0;

				for (File file : files)
				{
					size += file.length();
				}

				return Math.floor((size / 1024.0d / 1024.0d) * 100.0d) / 100.0d + "MB";
			}
			else
			{
				return "unavailable";
			}
		}
		catch (Exception e)
		{
			return "unavailable";
		}
	}

	@Override public void onDestroy()
	{
		((MainApplication)getApplicationContext()).initImageLoader();
		CacheManager.getInstance().checkLimit();

		super.onDestroy();
	}

	@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		progress += 10;

		if (seekBar == mMaxCache)
		{
			mMaxCacheSize.setText(progress + "mb");
			mSettings.setMaxCacheSize(progress);
		}
		else if (seekBar == mMaxImageCache)
		{
			mMaxImageCacheSize.setText(progress + "mb");
			mSettings.setMaxImageCacheSize(progress);
		}
	}

	@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (buttonView == mAnalytics)
		{
			mSettings.setAnalyticsEnabled(isChecked);
		}
		else if (buttonView == mCrashReporting)
		{
			mSettings.setCrashReportingEnabled(isChecked);
		}
	}

	@Override public void onClick(View v)
	{
		if (v == mCache)
		{
			DialogBuilder.create(getContext())
				.setTitle(R.string.confirm)
				.setMessage(R.string.clear_cache)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						File f = new File(getContext().getFilesDir().getAbsolutePath());
						File[] files = f.listFiles();

						List<String> userIds = UserManager.getLinkedUserIds(getContext());
						for (int index = 0; index < userIds.size(); index++)
						{
							userIds.set(index, "cache_" + String.format(Constants.CACHE_USER, userIds.get(index)));
						}

						for (File file : files)
						{
							if (!file.getName().equals("cache_" + Constants.CACHE_AUTH) &&
								!file.getName().equals("cache_" + Constants.CACHE_LINKED_ACCOUNTS) &&
								!userIds.contains(file.getName())
							)
							{
								file.delete();
							}
						}

						mCacheSize.setText("Currently: 0.0MB");
						Toast.makeText(getContext(), R.string.cache_cleared, Toast.LENGTH_LONG).show();
					}
				})
				.setNegativeButton(R.string.cancel, null)
			.show();
		}
		else if (v == mCheckUpdates)
		{
			Toast.makeText(getContext(), R.string.checking_for_updates, Toast.LENGTH_LONG).show();
			APIManager.getInstance().checkUpdates(getContext(), new JsonResponseHandler()
			{
				@Override public void onSuccess(){}
				@Override public void onFinish(boolean failed)
				{
					if (!failed)
					{
						JsonElement response = getContent();

						if (response != null)
						{
							try
							{
								final String url = response.getAsJsonObject().get("update_url").getAsString();
								String version = response.getAsJsonObject().get("version").getAsString();
								int build = response.getAsJsonObject().get("build").getAsInt();
								int currentBuild = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionCode;

								if (build > currentBuild)
								{
									DialogBuilder.create(getContext())
										.setTitle(R.string.new_update_title)
										.setMessage(getString(R.string.update_message_long, version))
										.setPositiveButton(R.string.update, new DialogInterface.OnClickListener()
										{
											@Override public void onClick(DialogInterface dialog, int which)
											{
												Intent updateIntent = new Intent(getContext(), UpdateManager.class);
												updateIntent.putExtra(Constants.EXTRA_WEB_URL, url);
												startActivity(updateIntent);
											}
										})
										.setNegativeButton(R.string.cancel, null)
									.show();
								}
								else
								{
									Toast.makeText(getContext(), R.string.no_updates, Toast.LENGTH_LONG).show();
								}
							}
							catch (Exception e){}
						}
					}
				}
			});
		}
		else if (v == mLicences)
		{
			Intent licences = new Intent(getActivity(), Licences.class);
			startActivity(licences);
		}
		else if (v == mImageCache)
		{
			DialogBuilder.create(getContext())
				.setTitle(R.string.confirm)
				.setMessage(R.string.clear_images)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						ImageLoader.getInstance().clearDiscCache();
						ImageLoader.getInstance().clearMemoryCache();
						mImageCacheSize.setText("Currently: 0.0MB");
						Toast.makeText(getContext(), R.string.image_cache_cleared, Toast.LENGTH_LONG).show();
					}
				})
				.setNegativeButton(R.string.cancel, null)
			.show();
		}
		else if (v == mChangeLog)
		{
			Intent changeLog = new Intent(getContext(), URLMatcher.class);
			changeLog.setData(Uri.parse("http://blog.robinapp.net/version010breleasenotes"));
			startActivity(changeLog);
		}
		else if (v == mDeveloperInfo)
		{
			String email = "";

			email += "Device id: " + getDeviceId();
			email += "<br />";
			email += "App version: " + getString(R.string.app_version);
			try
			{
				email += "<br />";
				email += "Build number: " + getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionCode;
			}
			catch (Exception e) {}
			email += "<br /><br />";
			email += "Name: " + UserManager.getUser().getUserName();
			email += "<br /><br />";
			email += "Username: " + UserManager.getUser().getMentionName();
			email += "<br />";
			email += "User id: " + UserManager.getUser().getId();
			email += "<br /><br />";
			email += "Device: " + android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL;
			email += "<br />";
			email += "OS Version: " + android.os.Build.VERSION.RELEASE;

			Double allocated = new Double(android.os.Debug.getNativeHeapAllocatedSize()) / new Double((1048576));
			Double available = new Double(android.os.Debug.getNativeHeapSize() / 1048576.0);
			Double free = new Double(android.os.Debug.getNativeHeapFreeSize() / 1048576.0);
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			df.setMinimumFractionDigits(2);

			email += "<br />";
			email += "Memory Heap Native: Allocated " + df.format(allocated) + "MB of " + df.format(available) + "MB (" + df.format(free) + "MB free) in [" + getClass().getName() + "]";
			email += "<br />";
			email += "Memory Heap App: Allocated: " + df.format(new Double(Runtime.getRuntime().totalMemory() / 1048576)) + "MB of " + df.format(new Double(Runtime.getRuntime().maxMemory() / 1048576)) + "MB (" + df.format(new Double(Runtime.getRuntime().freeMemory() / 1048576)) + "MB free)";

			email += "<br /><br />";
			email += SettingsManager.dump().replace("\r\n", "<br />").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");

			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/html");
			intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(email));
			intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"help@robinapp.net"});
			intent.putExtra(Intent.EXTRA_SUBJECT, "Robin: Developer information");
			startActivity(Intent.createChooser(intent, "Send via"));
		}
	}

	@Override public void onStartTrackingTouch(SeekBar seekBar){}
	@Override public void onStopTrackingTouch(SeekBar seekBar){}
}