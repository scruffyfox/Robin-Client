package in.rob.client.dialog.base;

import in.lib.manager.SettingsManager;
import in.rob.client.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

public class ProgressDialogFragment extends DialogFragment
{
	/**
	 * Builder for progress dialog fragment
	 **/
	public static class Builder
	{
		private final Context c;
		private String title;
		private String message;
		private boolean cancelableOnTouchOutside = true;

		public Builder(Context c)
		{
			this.c = c;
		}

		public ProgressDialogFragment.Builder setTitle(String title)
		{
			this.title = title;
			return this;
		}

		public ProgressDialogFragment.Builder setMessage(String message)
		{
			this.message = message;
			return this;
		}

		public ProgressDialogFragment.Builder setCancelableOnTouchOutside(boolean cancelable)
		{
			this.cancelableOnTouchOutside = cancelable;
			return this;
		}

		public ProgressDialogFragment build()
		{
			return ProgressDialogFragment.newInstance(title, message, cancelableOnTouchOutside);
		}
	}

	protected static ProgressDialogFragment newInstance()
	{
		return newInstance("", "", true);
	}

	protected static ProgressDialogFragment newInstance(String title, String message, boolean cancelableOnTouchOutside)
	{
		ProgressDialogFragment frag = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", title);
		args.putString("message", message);
		args.putBoolean("cancelableOnTouchOutside", cancelableOnTouchOutside);
		frag.setArguments(args);
		return frag;
	}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		ProgressDialog progress;
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			int theme = AlertDialog.THEME_HOLO_LIGHT;

			try
			{
				String styleName = SettingsManager.getThemeName();
				int styleRes = getActivity().getResources().getIdentifier(styleName, "style", getActivity().getPackageName());
				if (styleRes == R.style.DefaultDark)
				{
					theme = AlertDialog.THEME_HOLO_DARK;
				}
			}
			catch (Exception e){}

			progress = new ProgressDialog(getActivity(), theme);
		}
		else
		{
			progress = new ProgressDialog(getActivity());
		}

		String title = getArguments().getString("title");
		String message = getArguments().getString("message");

		if (!TextUtils.isEmpty(title))
		{
			progress.setTitle(title);
		}

		if (!TextUtils.isEmpty(message))
		{
			progress.setMessage(message);
		}

		return progress;
	}

	@Override public void show(FragmentManager manager, String tag)
	{
		if (manager.findFragmentByTag(tag) == null)
		{
			super.show(manager, tag);
		}
	}
}