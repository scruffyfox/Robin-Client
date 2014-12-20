package in.lib.helper;

import android.content.Context;
import android.util.TypedValue;

public class ThemeHelper
{
	public static int getDrawableResource(Context c, int attribute)
	{
		TypedValue typedValue = new TypedValue();
		c.getTheme().resolveAttribute(attribute, typedValue, true);
		return typedValue.resourceId;
	}

	public static int getColorResource(Context c, int attribute)
	{
		TypedValue a = new TypedValue();
		c.getTheme().resolveAttribute(attribute, a, true);
		if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT)
		{
			return a.data;
		}

		return -1;
	}
}