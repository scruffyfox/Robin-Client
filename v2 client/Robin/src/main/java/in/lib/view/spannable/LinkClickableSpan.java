package in.lib.view.spannable;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import in.data.entity.LinkEntity;
import in.lib.Constants;
import in.lib.builder.DialogBuilder;
import in.lib.manager.SettingsManager;
import in.lib.utils.BitUtils;
import in.lib.utils.URLUtils;
import in.rob.client.R;
import in.rob.client.URLMatcher;

public class LinkClickableSpan extends NotUnderlinedClickableSpan
{
	private final LinkEntity link;

	public LinkClickableSpan(LinkEntity link)
	{
		this.link = link;
	}

	@Override public boolean onTouch(View widget, MotionEvent m)
	{
		return super.onTouch(widget, m);
	}

	@Override public void onSimpleClick(View widget)
	{
		super.onSimpleClick(widget);

		Context ctx = widget.getContext();
		openUrl(ctx);
	}

	public void openUrl(Context ctx)
	{
		Intent intent = null;

		if (BitUtils.contains(SettingsManager.getInstance().getInAppViewerBit(), Constants.BIT_IN_APP_VIEWER_BROWSER))
		{
			if (URLUtils.isYoutubeVideo(Uri.parse(link.getUrl())))
			{
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(link.getUrl()));
			}
			else
			{
				intent = new Intent(ctx, URLMatcher.class);
				intent.setData(Uri.parse(link.getUrl()));
			}
		}

		if (intent == null)
		{
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(link.getUrl()));
		}

		ctx.startActivity(intent);
	}

	@Override public void onLongClick(View widget)
	{
		super.onLongClick(widget);
		final Context ctx = widget.getContext();

		DialogBuilder.create(ctx)
			.setTitle(link.getUrl())
			.setItems(new CharSequence[]{ctx.getString(R.string.open_url), ctx.getString(R.string.copy_url), ctx.getString(R.string.share_url)}, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					if (which == 0)
					{
						openUrl(ctx);
					}
					else if (which == 1)
					{
						android.content.ClipboardManager clipboard = (android.content.ClipboardManager)ctx.getSystemService(Context.CLIPBOARD_SERVICE);
						android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", link.getUrl());
						clipboard.setPrimaryClip(clip);

						Toast.makeText(ctx, ctx.getString(R.string.copy_url_success), Toast.LENGTH_SHORT).show();
					}
					else if (which == 2)
					{
						Intent shareIntent = new Intent(Intent.ACTION_SEND);
						shareIntent.putExtra(Intent.EXTRA_TEXT, link.getUrl());
						shareIntent.setType("text/plain");
						((Activity)ctx).startActivity(Intent.createChooser(shareIntent, ctx.getString(R.string.share_via)));
					}
				}
			})
		.show();
	}
}