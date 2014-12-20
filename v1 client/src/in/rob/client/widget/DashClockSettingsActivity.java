/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.rob.client.widget;

import in.lib.Constants;
import in.lib.manager.UserManager;
import in.model.User;
import in.rob.client.R;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class DashClockSettingsActivity extends PreferenceActivity
{
	private CharSequence[] usernames;
	private List<String> ids;

	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getActionBar().setIcon(R.drawable.ic_launcher);
		getActionBar().setDisplayHomeAsUpEnabled(true);
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

		return super.onOptionsItemSelected(item);
	}

	private void setupSimplePreferencesScreen()
	{
		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.dash_prefs);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		ListPreference p = (ListPreference)findPreference("dash_user_id");

		ids = UserManager.getLinkedUserIds(this);
		usernames = new CharSequence[ids.size()];
		String selectedUser = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(Constants.PREFS_DASH_USER_ID, UserManager.getUserId());
		String selected = "";

		int index = 0;
		for (String id : ids)
		{
			User u = User.loadUser(id);
			if (u != null)
			{
				usernames[index++] = "@" + u.getMentionName();

				if (u.getId().equals(selectedUser))
				{
					selected = "@" + u.getMentionName();
				}
			}
		}

		p.setSummary(selected);
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
				getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().putString(Constants.PREFS_DASH_USER_ID, ids.get(index)).apply();

				return true;
			}
		});
	}
}