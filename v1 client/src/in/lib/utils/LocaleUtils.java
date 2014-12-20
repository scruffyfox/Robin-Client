package in.lib.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class LocaleUtils
{
	public static List<Locale> getSortedAvailableLocales()
	{
		List<Locale> result = Arrays.asList(Locale.getAvailableLocales());
		Collections.sort(result, new Comparator<Locale>()
		{
			@Override public int compare(Locale lhs, Locale rhs)
			{
				return lhs.getDisplayName().compareTo(rhs.getDisplayName());
			}
		});

		return result;
	}
}
