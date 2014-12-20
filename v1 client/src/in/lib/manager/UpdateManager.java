package in.lib.manager;

import in.lib.Constants;
import in.rob.client.R;
import in.rob.client.base.RobinDialogActivity;
import in.rob.client.dialog.base.DialogBuilder;
import in.rob.client.dialog.base.ProgressBuilder;

import java.io.File;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.CacheResponseHandler;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;

public class UpdateManager extends RobinDialogActivity
{
	private ProgressDialog progress;
	private String updateUrl = "https://beta.robinapp.net/robin.apk";

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getIntent().getExtras() != null)
		{
			updateUrl = getIntent().getExtras().getString(Constants.EXTRA_WEB_URL);
		}

		progress = ProgressBuilder.create(getContext());
		getWindow().setBackgroundDrawable(null);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setVisible(false);
		downloadNewBuild();
	}

	@Override protected void onDestroy()
	{
		if (progress != null && progress.isShowing())
		{
			progress.dismiss();
		}

		super.onDestroy();
	}

	public void downloadNewBuild()
	{
		final AsyncHttpClient client = new AsyncHttpClient(updateUrl);
		progress.setCanceledOnTouchOutside(false);
		progress.setMessage(getString(R.string.downloading));
		progress.setOnCancelListener(new OnCancelListener()
		{
			@Override public void onCancel(DialogInterface dialog)
			{
				client.cancel();
			}
		});

		if (!getExternalCacheDir().exists())
		{
			getExternalCacheDir().mkdir();
		}

		new File(getExternalCacheDir().getAbsolutePath() + "/update.apk").delete();
		client.get(new CacheResponseHandler(getExternalCacheDir().getAbsolutePath() + "/update.apk")
		{
			@Override public void onSend()
			{
				progress.show();
			}

			@Override public void onSuccess()
			{
				File f = getContent();

				NotificationManager m = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
				m.cancel(65834);

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
				startActivity(intent);
				finish();
			}

			@Override public void onFinish(boolean failed)
			{
				if (failed)
				{
					DialogBuilder.create(UpdateManager.this)
						.setTitle(R.string.error)
						.setMessage(R.string.update_failed)
						.setPositiveButton(R.string.close, null)
					.show();
				}

				if (progress != null && progress.isShowing())
				{
					progress.dismiss();
				}

				finish();
			}
		});
	}
}