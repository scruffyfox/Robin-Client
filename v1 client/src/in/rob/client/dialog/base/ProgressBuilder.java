package in.rob.client.dialog.base;

import in.lib.manager.SettingsManager;
import in.rob.client.R;
import android.app.ProgressDialog;
import android.content.Context;

public class ProgressBuilder
{
	public static ProgressDialog create(Context c)
	{
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			int theme = ProgressDialog.THEME_HOLO_LIGHT;

			try
			{
				String styleName = SettingsManager.getThemeName();
				int styleRes = c.getResources().getIdentifier(styleName, "style", c.getPackageName());
				if (styleRes == R.style.DefaultDark)
				{
					theme = ProgressDialog.THEME_HOLO_DARK;
				}
			}
			catch (Exception e){}

			ProgressDialog progress = new ProgressDialog(c, theme);
			return progress;
		}
		else
		{
			ProgressDialog progress = new ProgressDialog(c);
			return progress;
		}
	}
}