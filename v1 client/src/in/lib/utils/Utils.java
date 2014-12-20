package in.lib.utils;

import android.annotation.SuppressLint;
import android.widget.ListView;

public class Utils
{
	@SuppressLint("NewApi")
	public static void scrollTo(ListView list, int position)
	{
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			list.smoothScrollToPositionFromTop(position, 0);
		}
		else if (android.os.Build.VERSION.SDK_INT >= 8)
		{
			int firstVisible = list.getFirstVisiblePosition();
			int lastVisible = list.getLastVisiblePosition();
			if (position < firstVisible)
			{
				list.smoothScrollToPosition(position);
			}
			else
			{
				list.smoothScrollToPosition(position + lastVisible - firstVisible - 2);
			}
		}
	}
}
