package in.lib.utils;

import in.rob.client.R;
import android.content.Context;

public class DateUtils
{
	private final String now, sec, min, hour, day, week, month, year;

	public DateUtils(Context context)
	{
		now = context.getString(R.string.now);
		sec = context.getString(R.string.abbr_seconds);
		min = context.getString(R.string.abbr_minutes);
		hour = context.getString(R.string.abbr_hours);
		day = context.getString(R.string.abbr_days);
		week = context.getString(R.string.abbr_weeks);
		month = context.getString(R.string.abbr_months);
		year = context.getString(R.string.abbr_years);
	}

	/**
	 * Converts a timestamp to how long ago syntax
	 *
	 * @param time The time in milliseconds
	 * @return The formatted time
	 */
	public String timeAgo(long time)
	{
		Unit[] units = new Unit[]
		{
			new Unit(sec, 60, 1),
			new Unit(min, 3600, 60),
			new Unit(hour, 86400, 3600),
			new Unit(day, 604800, 86400),
			new Unit(week, 2629743, 604800),
			new Unit(month, 31556926, 2629743),
			new Unit(year, 0, 31556926)
		};

		long currentTime = System.currentTimeMillis();
		int difference = (int)((currentTime - time) / 1000);

		if (difference < 5)
		{
			return now;
		}

		int i = 0;
		Unit unit = null;
		while ((unit = units[i++]) != null)
		{
			if (difference < unit.limit || unit.limit == 0)
			{
				int newDiff = (int)Math.floor(difference / unit.inSeconds);
				return newDiff + "" + unit.name;
			}
		}

		return "";
	}

	static class Unit
	{
		public String name;
		public int limit;
		public int inSeconds;

		public Unit(String name, int limit, int inSeconds)
		{
			this.name = name;
			this.limit = limit;
			this.inSeconds = inSeconds;
		}
	}
}
