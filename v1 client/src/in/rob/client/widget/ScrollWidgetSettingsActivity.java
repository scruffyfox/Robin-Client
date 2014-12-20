package in.rob.client.widget;

import in.lib.Constants;
import in.lib.Constants.StreamList;
import in.lib.manager.UserManager;
import in.model.User;
import in.rob.client.R;

import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

public class ScrollWidgetSettingsActivity extends PreferenceActivity
{
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	private CharSequence[] usernames;
	private List<String> ids;
	private SharedPreferences realPrefs;

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getActionBar().setIcon(R.drawable.ic_launcher);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		realPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null)
		{
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		// If they gave us an intent without the widget id, just bail.
		if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID)
		{
			finish();
		}
	}

	@Override protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
	}

	@Override public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			finish();
			return true;
		}
		else if (item.getItemId() == R.id.menu_ok)
		{
			AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
			ScrollWidgetProvider.updateAppWidget(this, appWidgetManager, mAppWidgetId);

			Intent resultValue = new Intent();
			resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
			setResult(RESULT_OK, resultValue);
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.widget, menu);
		return super.onCreateOptionsMenu(menu);
	}

	private void setupSimplePreferencesScreen()
	{
		addPreferencesFromResource(R.xml.scroller_prefs);

		ListPreference p = (ListPreference)findPreference("scroller_user_id");
		p.setKey(p.getKey() + "_" + mAppWidgetId);
		ListPreference stream = (ListPreference)findPreference("scroller_stream_id");
		stream.setKey(stream.getKey() + "_" + mAppWidgetId);
		ListPreference theme = (ListPreference)findPreference("scroller_theme");
		theme.setKey(theme.getKey() + "_" + mAppWidgetId);

		setUserList(p);
		setStreamList(stream);
		setThemeList(theme);
	}

	public void setStreamList(ListPreference stream)
	{
		StreamList[] list = StreamList.values();
		CharSequence[] labels = new CharSequence[list.length];
		CharSequence[] ids = new CharSequence[list.length];
		String set = realPrefs.getString(stream.getKey(), list[0].toString());
		CharSequence selected = getString(list[0].getLabelRes());

		for (int index = 0; index < list.length; index++)
		{
			labels[index] = getString(list[index].getLabelRes());
			ids[index] = list[index].toString();

			if (ids[index].equals(set))
			{
				selected = labels[index];
			}
		}

		stream.setSummary(selected);
		stream.setValue(set);
		stream.setEntries(labels);
		stream.setEntryValues(ids);
		stream.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			@Override public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				ListPreference listPreference = (ListPreference)preference;
				int index = listPreference.findIndexOfValue(newValue.toString());

				preference.setSummary(((ListPreference)preference).getEntries()[index]);
				return true;
			}
		});
	}

	public void setThemeList(ListPreference stream)
	{
		CharSequence[] labels = {"Light", "Dark"};
		String set = realPrefs.getString(stream.getKey(), labels[0].toString());
		CharSequence selected = set;

		stream.setSummary(selected);
		stream.setValue(set);
		stream.setEntries(labels);
		stream.setEntryValues(labels);
		stream.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			@Override public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				ListPreference listPreference = (ListPreference)preference;
				int index = listPreference.findIndexOfValue(newValue.toString());

				preference.setSummary(((ListPreference)preference).getEntries()[index]);
				return true;
			}
		});
	}

	public void setUserList(ListPreference p)
	{
		ids = UserManager.getLinkedUserIds(this);
		usernames = new CharSequence[ids.size()];
		String selectedUser = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(Constants.PREFS_SCROLL_WIDGET_USER_ID + mAppWidgetId, UserManager.getUserId());
		String selected = "";

		int index = 0;
		for (String id : ids)
		{
			User u = User.loadUser(id);
			usernames[index++] = "@" + u.getMentionName();

			if (u.getId().equals(selectedUser))
			{
				selected = u.getMentionName();
			}
		}

		p.setSummary("@" + selected);
		p.setEntries(usernames);
		p.setEntryValues(ids.toArray(new String[usernames.length]));
		p.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			@Override public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference)preference;
				int index = listPreference.findIndexOfValue(newValue.toString());

				// Set the summary to reflect the new value.
				preference.setSummary(index >= 0 ? usernames[index] : null);
				getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().putString(Constants.PREFS_SCROLL_WIDGET_USER_ID + mAppWidgetId, ids.get(index)).apply();

				return true;
			}
		});
	}
}