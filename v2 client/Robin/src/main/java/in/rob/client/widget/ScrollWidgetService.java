package in.rob.client.widget;

import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import in.data.stream.PostStream;
import in.lib.Constants;
import in.lib.manager.CacheManager;
import in.lib.manager.UserManager;
import in.model.Post;
import in.rob.client.R;

public class ScrollWidgetService extends RemoteViewsService
{
	public class WidgetViewsFactory implements RemoteViewsFactory
	{
		private Context context;
		private final int widgetID;
		private int listItemRes = R.layout.widget_message_list_item ;
		private PostStream stream;

		public WidgetViewsFactory(Context context, Intent intent)
		{
			this.context = context;
			this.widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			stream = CacheManager.getInstance().readFile(String.format(Constants.CACHE_TIMELINE, UserManager.getInstance().getUser().getId()), PostStream.class);
		}

		@Override public int getCount()
		{
			return stream == null ? 0 : stream.getItems().size();
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
			final RemoteViews row = new RemoteViews(context.getPackageName(), listItemRes);

			if (position < getCount())
			{
				Post post = ((Post)stream.getItems().get(position));

				row.setTextViewText(R.id.title, post.getPoster().getFormattedMentionNameTitle());
				row.setTextViewText(R.id.sub_title, post.getPoster().getFormattedMentionNameSubTitle());
				row.setTextViewText(R.id.time, post.getDateStr());
				row.setTextViewText(R.id.text, post.getPostText().getText());
				row.setImageViewResource(R.id.avatar, R.drawable.default_avatar);

				Intent intent = new Intent();
				intent.putExtra(Constants.EXTRA_POST, post);
				row.setOnClickFillInIntent(R.id.root_view, intent);
			}

			return row;
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
		}

		@Override public void onCreate(){}

		@Override public void onDestroy(){}
	}

	@Override public RemoteViewsFactory onGetViewFactory(Intent intent)
	{
		return new WidgetViewsFactory(getApplicationContext(), intent);
	}
}