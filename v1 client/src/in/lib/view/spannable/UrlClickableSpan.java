package in.lib.view.spannable;

import in.lib.Constants;
import in.lib.URLMatcher;
import in.lib.helper.ThemeHelper;
import in.lib.manager.SettingsManager;
import in.lib.utils.URLUtils;
import in.obj.annotation.Annotation;
import in.obj.annotation.Annotation.Type;
import in.obj.annotation.ImageAnnotation;
import in.rob.client.R;
import in.rob.client.dialog.ImageLightboxDialog;
import in.rob.client.dialog.base.DialogBuilder;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class UrlClickableSpan extends NotUnderlinedClickableSpan
{
	private String mUrl;

	public UrlClickableSpan(String url)
	{
		mUrl = url;
		if (!mUrl.startsWith("http://") && !url.startsWith("https://"))
		{
			mUrl = "http://" + mUrl;
		}
	}

	@Override public boolean onTouch(View widget, MotionEvent m)
	{
		return false;
	}

	@Override public void onSimpleClick(View widget)
	{
		super.onSimpleClick(widget);

		HashMap<Type, ArrayList<Annotation>> annotations = (HashMap<Type, ArrayList<Annotation>>)widget.getTag(R.id.TAG_ENTITY);
		Context ctx = widget.getContext();
		Intent intent = null;
		boolean isImage = URLUtils.isImage(Uri.parse(mUrl));

		// check if the url exists in annotations as an embeddable image link
		// e.g. photos.app.net
		if (annotations != null)
		{
			for (int index = 0, count = annotations.get(Type.IMAGE).size(); index < count; index++)
			{
				if (((ImageAnnotation)annotations.get(Type.IMAGE).get(index)).getEmbeddableUrl().equals(mUrl))
				{
					mUrl = ((ImageAnnotation)annotations.get(Type.IMAGE).get(index)).getUrl();
					isImage = true;
					break;
				}
			}
		}

		if (isImage)
		{
			if (SettingsManager.isImageViewerEnabled())
			{
				if (annotations != null)
				{
					int pos = 0;
					ArrayList<String> imageUrl = new ArrayList<String>();
					ArrayList<String> webUrl = new ArrayList<String>();

					for (int index = 0, count = annotations.get(Type.IMAGE).size(); index < count; index++)
					{
						String url = ((ImageAnnotation)annotations.get(Type.IMAGE).get(index)).getUrl();
						String wUrl = ((ImageAnnotation)annotations.get(Type.IMAGE).get(index)).getEmbeddableUrl();
						//if (!imageUrl.contains(url) && !webUrl.contains(wUrl) && !webUrl.contains(url))
						if (!imageUrl.contains(url) && !webUrl.contains(wUrl))
						{
							imageUrl.add(url);
							webUrl.add(wUrl);

							if (((ImageAnnotation)annotations.get(Type.IMAGE).get(index)).getUrl().equals(mUrl))
							{
								pos = index;
							}
						}
					}

					intent = new Intent(ctx, ImageLightboxDialog.class);
					intent.putExtra(Constants.EXTRA_WEB_URL, webUrl.toArray(new String[webUrl.size()]));
					intent.putExtra(Constants.EXTRA_PREVIEW_URL, imageUrl.toArray(new String[imageUrl.size()]));
					intent.putExtra(Constants.EXTRA_IMAGE_POSITION, pos);
				}
				else
				{
					intent = new Intent(ctx, ImageLightboxDialog.class);
					intent.putExtra(Constants.EXTRA_PREVIEW_URL, mUrl);
				}
			}
			else
			{
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(mUrl));
			}

			ctx.startActivity(intent);
		}
		else
		{
			openUrl(ctx);
		}
	}

	public void openUrl(Context ctx)
	{
		Intent intent = null;

		if (SettingsManager.isLightboxEnabled())
		{
			if (URLUtils.isYoutubeVideo(Uri.parse(mUrl)))
			{
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(mUrl));
			}
			else
			{
				intent = new Intent(ctx, URLMatcher.class);
				intent.setData(Uri.parse(mUrl));
			}
		}

		if (intent == null)
		{
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(mUrl));
		}

		ctx.startActivity(intent);
	}

	@Override public void onLongClick(View widget)
	{
		final Context ctx = widget.getContext();

		DialogBuilder.create(ctx)
		.setIcon(ThemeHelper.getDrawableResource(ctx, R.attr.rbn_dialog_icon_link))
		.setTitle(mUrl)
		.setItems(new CharSequence[]
		{
			ctx.getString(R.string.open_url),
			ctx.getString(R.string.copy_url),
			ctx.getString(R.string.share_url)
		},
		new OnClickListener()
		{
			@Override @TargetApi(11) public void onClick(DialogInterface dialog, int which)
			{
				if (which == 0)
				{
					openUrl(ctx);
				}
				else if (which == 1)
				{
					if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
					{
						android.text.ClipboardManager clipboard = (android.text.ClipboardManager)ctx.getSystemService(Context.CLIPBOARD_SERVICE);
						clipboard.setText(mUrl);
					}
					else
					{
						android.content.ClipboardManager clipboard = (android.content.ClipboardManager)ctx.getSystemService(Context.CLIPBOARD_SERVICE);
						android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", mUrl);
						clipboard.setPrimaryClip(clip);
					}

					Toast.makeText(ctx, ctx.getString(R.string.copy_url_success), Toast.LENGTH_SHORT).show();
				}
				else if (which == 2)
				{
					Intent shareIntent = new Intent(Intent.ACTION_SEND);
					shareIntent.putExtra(Intent.EXTRA_TEXT, mUrl);
					shareIntent.setType("text/plain");
					((Activity)ctx).startActivity(Intent.createChooser(shareIntent, ctx.getString(R.string.share_via)));
				}
			}
		})
		.show();
	}
}