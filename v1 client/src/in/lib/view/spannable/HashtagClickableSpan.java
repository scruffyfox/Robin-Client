package in.lib.view.spannable;

import in.lib.Constants;
import in.lib.helper.ThemeHelper;
import in.lib.manager.SettingsManager;
import in.rob.client.R;
import in.rob.client.SearchResultsActivity;
import in.rob.client.dialog.base.DialogBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.View;

public class HashtagClickableSpan extends NotUnderlinedClickableSpan
{
	private final String mTagName;

	public HashtagClickableSpan(String tagName)
	{
		mTagName = tagName;
	}

	@Override public void onSimpleClick(View widget)
	{
		super.onSimpleClick(widget);
		openHashtag(widget.getContext());
	}

	private void openHashtag(Context ctx)
	{
		Intent intent = new Intent(ctx, SearchResultsActivity.class);
		intent.putExtra(Constants.EXTRA_TAG_NAME, "#" + mTagName);

		SettingsManager manager = SettingsManager.getInstance();
		manager.addSearchHistory("#" + mTagName);

		ctx.startActivity(intent);
	}

	@Override public void onLongClick(View widget)
	{
		final Context ctx = widget.getContext();

		final Boolean saved = SettingsManager.isTagSaved(mTagName);
		final Boolean muted = SettingsManager.isTagMuted(mTagName);

		DialogBuilder.create(ctx)
			.setIcon(ThemeHelper.getDrawableResource(ctx, R.attr.rbn_dialog_icon_hashtag))
			.setTitle("#" + mTagName)
			.setItems(new CharSequence[]
			{
				ctx.getString(R.string.open_hashtag),
				saved ? ctx.getString(R.string.unsave_hashtag) : ctx.getString(R.string.save_hashtag),
				muted ? ctx.getString(R.string.unmute_hashtag) : ctx.getString(R.string.mute_hashtag)
			},
			new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					SettingsManager settings = SettingsManager.getInstance();

					if (which == 0)
					{
						openHashtag(ctx);
					}
					else if (which == 1)
					{
						if (saved)
						{
							settings.unsaveTag(mTagName);
						}
						else
						{
							settings.saveTag(mTagName);
						}
					}
					else if (which == 2)
					{
						if (muted)
						{
							settings.unmuteTag(mTagName);
						}
						else
						{
							settings.muteTag(mTagName);
						}

						//((Activity)ctx).setResult(Constants.RESULT_REFRESH, new Intent().putExtra(Constants.EXTRA_REFRESH_MUTED, true));
					}
				}
			})
		.show();
	}
}
