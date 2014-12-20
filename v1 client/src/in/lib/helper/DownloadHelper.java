package in.lib.helper;

import in.rob.client.R;
import in.rob.client.dialog.base.DialogBuilder;

import java.io.File;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.CacheResponseHandler;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class DownloadHelper
{
	public static void showMediaDownloadPopup(final Context c, final String url)
	{
		DialogBuilder.create(c)
			.setTitle(c.getString(R.string.pick_option))
			.setItems(new CharSequence[]
			{
				c.getString(R.string.save_image),
				c.getString(R.string.copy_url),
				c.getString(R.string.share_image)
			},
			new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					if (which == 0)
					{
						if (url == null)
						{
							Toast.makeText(c, R.string.image_download_generic_failure, Toast.LENGTH_SHORT).show();
							return;
						}

						final AsyncHttpClient downloader = new AsyncHttpClient(url);

						Uri uri = Uri.parse(url);
						String remoteFileName = uri.toString().replaceAll("[^0-9A-Za-z]", "_");

						// Convert filename to nice readable one..
						final String filename = "image_" + remoteFileName + ".png";
						File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
						final String path = (downloadFolder.getAbsolutePath() + "/" + filename);

						downloadNotificationAnimationState = 0;
						downloadNotificationCompletionPercent = 0;

						applicationContext = c.getApplicationContext();
						mNotificationManager = (NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
						downloadFilename = path;

						notificationUpdateRunnable.run();

						downloader.get(new CacheResponseHandler(path)
						{
							@Override public void onSuccess(){}

							@Override public void onPublishedDownloadProgressUI(long totalProcessed, long totalLength)
							{
								downloadNotificationCompletionPercent = (int)(totalProcessed / totalLength);
							}

							@Override public void onFinish(boolean failed)
							{
								if (failed)
								{
									Toast.makeText(c, c.getString(R.string.error), Toast.LENGTH_LONG).show();
								}
								else
								{
									Toast.makeText(c, c.getString(R.string.download_saved, path), Toast.LENGTH_LONG).show();
								}

								notificationLoopHandler.removeCallbacks(notificationUpdateRunnable);
								completeDownloadNotification();
							}
						});
					}
					else if (which == 1)
					{
						if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
						{
							android.text.ClipboardManager clipboard = (android.text.ClipboardManager)c.getSystemService(Context.CLIPBOARD_SERVICE);
							clipboard.setText(url);
						}
						else
						{
							android.content.ClipboardManager clipboard = (android.content.ClipboardManager)c.getSystemService(Context.CLIPBOARD_SERVICE);
							android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", url);
							clipboard.setPrimaryClip(clip);
						}

						Toast.makeText(c, c.getString(R.string.copy_url_success), Toast.LENGTH_SHORT).show();
					}
					else
					{
						Intent shareIntent = new Intent(Intent.ACTION_SEND);
						shareIntent.putExtra(Intent.EXTRA_TEXT, url);
						shareIntent.setType("text/plain");
						c.startActivity(shareIntent);
					}
				}
			})
		.show();
	}

	private static int downloadNotificationAnimationState;
	private static int downloadNotificationCompletionPercent;

	private static Context applicationContext;
	private static NotificationManager mNotificationManager;
	private static String downloadFilename;

	private static Handler notificationLoopHandler = new Handler();
	private static Runnable notificationUpdateRunnable = new Runnable()
	{
		@Override public void run()
		{
			updateDownloadNotification();

			++downloadNotificationAnimationState;
			if (downloadNotificationAnimationState == 5) downloadNotificationAnimationState = 0;

			notificationLoopHandler.postDelayed(notificationUpdateRunnable, 250);
		}
	};

	private static void updateDownloadNotification()
	{
		Notification notification = new NotificationCompat.Builder(applicationContext)
			.setContentTitle(downloadFilename)
			.setProgress(100, downloadNotificationCompletionPercent, false)
			.setContentText("Downloading..")
			.setSmallIcon(android.R.drawable.stat_sys_download, downloadNotificationAnimationState)
			.setOngoing(true)
		.build();

		mNotificationManager.notify(downloadFilename.hashCode(), notification);
	}

	private static void completeDownloadNotification()
	{
		Intent view = new Intent(Intent.ACTION_VIEW);
		view.setDataAndType(Uri.fromFile(new File(downloadFilename)), "image/jpeg");
		PendingIntent viewIntent = PendingIntent.getActivity(applicationContext, 1, view, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification = new NotificationCompat.Builder(applicationContext)
			.setContentTitle("Downloaded complete")
			.setOngoing(false)
			.setContentText(downloadFilename)
			.setContentIntent(viewIntent)
			.setSmallIcon(android.R.drawable.stat_sys_download_done)
		.build();

		mNotificationManager.notify(downloadFilename.hashCode(), notification);
	}
}