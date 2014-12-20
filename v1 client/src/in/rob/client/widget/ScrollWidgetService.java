package in.rob.client.widget;

import in.lib.Constants;
import in.lib.Constants.StreamList;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.html.ADNHtml;
import in.model.Post;
import in.model.Stream;
import in.model.User;
import in.rob.client.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import lombok.Getter;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class ScrollWidgetService extends RemoteViewsService
{
	public class WidgetViewsFactory implements RemoteViewsService.RemoteViewsFactory
	{
		@Getter private final Context context;
		private final int widgetID;
		private int listItemRes = -1;
		private Stream timelineStream;
		private String streamStr;
		private String accountId;

		public WidgetViewsFactory(Context ctx, Intent intent)
		{
			this.context = ctx;
			this.widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			streamStr = prefs.getString("scroller_stream_id_" + widgetID, StreamList.TIMELINE.toString());
			accountId = prefs.getString(Constants.PREFS_SCROLL_WIDGET_USER_ID + widgetID, UserManager.getUserId());
			listItemRes = prefs.getString("scroller_theme_" + widgetID, "Light").equals("Light") ? R.layout.widget_message_list_item : R.layout.widget_message_list_item_dark;

			timelineStream = CacheManager.getInstance().readFileAsObject(String.format(streamStr, accountId), new Stream());
		}

		@Override public int getCount()
		{
			return timelineStream.getObjects().size();
		}

		@Override public long getItemId(int position)
		{
			return position;
		}

		@Override public RemoteViews getLoadingView()
		{
			return null;
		}

		@Override public RemoteViews getViewAt(int position)
		{
			final RemoteViews row = new RemoteViews(getContext().getPackageName(), listItemRes);

			if (position < getCount())
			{
				Post post = ((Post)timelineStream.getObjects().get(position));

				String dateStr = calculateDateString(post.getDate());

				row.setTextViewText(R.id.title, "@" + post.getPoster().getMentionName());
				row.setTextViewText(R.id.sub_title, post.getPoster().getUserName());
				row.setTextViewText(R.id.time, dateStr);
				row.setTextViewText(R.id.text, ADNHtml.fromHtml(post.getFormattedText()));

				Bitmap b = User.loadAvatar(getContext(), post.getPoster().getId());

				if (b != null)
				{
					row.setImageViewBitmap(R.id.avatar, b);
				}
				else
				{
					row.setImageViewResource(R.id.avatar, R.drawable.default_avatar);
				}

				Intent intent = new Intent();
				intent.putExtra(Constants.EXTRA_POST, post);
				row.setOnClickFillInIntent(R.id.root_view, intent);
			}

			return row;
		}

		public String calculateDateString(long date)
		{
			GregorianCalendar cal = new GregorianCalendar();
			GregorianCalendar todayDate = new GregorianCalendar(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE));
			long startDate = todayDate.getTimeInMillis();

			String time = SettingsManager.getDateFormat().format(new Date(date)) + "\n" + SettingsManager.getTimeFormat().format(new Date(date));
			return time;
		}

		@Override public int getViewTypeCount()
		{
			return 1;
		}

		@Override public boolean hasStableIds()
		{
			return true;
		}

		@Override public void onDataSetChanged()
		{
			NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			manager.cancel(widgetID);

			if (!TextUtils.isEmpty(streamStr))
			{
				timelineStream = CacheManager.getInstance().readFileAsObject(String.format(streamStr, accountId), new Stream());
			}
		}

		@Override public void onCreate(){}

		@Override public void onDestroy(){}
	}

	@Override public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		return new WidgetViewsFactory(getApplicationContext(), intent);
	}
}