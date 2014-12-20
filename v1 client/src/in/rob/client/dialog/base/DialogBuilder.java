package in.rob.client.dialog.base;

import in.lib.manager.SettingsManager;
import in.rob.client.R;
import android.app.AlertDialog;
import android.content.Context;

public class DialogBuilder
{
	public static AlertDialog.Builder create(Context c)
	{
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			int theme = AlertDialog.THEME_HOLO_LIGHT;

			try
			{
				String styleName = SettingsManager.getThemeName();
				int styleRes = c.getResources().getIdentifier(styleName, "style", c.getPackageName());
				if (styleRes == R.style.DefaultDark)
				{
					theme = AlertDialog.THEME_HOLO_DARK;
				}
			}
			catch (Exception e){}

			return new AlertDialog.Builder(c, theme);
		}
		else
		{
			return new AlertDialog.Builder(c);
		}
	}
}