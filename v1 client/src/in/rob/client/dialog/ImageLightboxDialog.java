package in.rob.client.dialog;

import in.lib.Constants;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.helper.DownloadHelper;
import in.lib.utils.URLUtils;
import in.lib.utils.Views;
import in.lib.view.SwipableViewPager;
import in.lib.view.TouchImageView;
import in.lib.view.TouchImageView.OnScaleChangedListener;
import in.rob.client.MainApplication;
import in.rob.client.R;
import in.rob.client.base.RobinDialogActivity;
import lombok.Getter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class ImageLightboxDialog extends RobinDialogActivity implements OnClickListener
{
	private String[] imageUrls = {};
	private String[] webUrls = {};
	@Getter private Context context = this;

	@InjectView(R.id.pager) public SwipableViewPager pager;
	@InjectView(R.id.fullscreen_image) public ImageView mFsImage;
	@OnClick @InjectView(R.id.icon_web) public View mWeb;
	@OnClick @InjectView(R.id.icon_share) public View mShare;
	@OnClick @InjectView(R.id.icon_download) public View mDownload;
	@OnClick @InjectView(R.id.icon_copy) public View mCopy;
	@OnClick @InjectView(R.id.fail_message) public TextView failMessage;

	private int pagerPosition = 0;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_lightbox);

		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		getWindow().setGravity(Gravity.CENTER);
		getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.fullscreen_dialog_bg));

		if (getIntent().getExtras() != null)
		{
			if (getIntent().getExtras().containsKey(Constants.EXTRA_PREVIEW_URL))
			{
				Object urls = getIntent().getExtras().get(Constants.EXTRA_PREVIEW_URL);

				if (urls instanceof String[])
				{
					imageUrls = getIntent().getExtras().getStringArray(Constants.EXTRA_PREVIEW_URL);
				}
				else
				{
					imageUrls = new String[]{getIntent().getExtras().getString(Constants.EXTRA_PREVIEW_URL)};
				}
			}

			if (getIntent().getExtras().containsKey(Constants.EXTRA_WEB_URL))
			{
				Object urls = getIntent().getExtras().get(Constants.EXTRA_PREVIEW_URL);

				if (urls instanceof String[])
				{
					webUrls = getIntent().getExtras().getStringArray(Constants.EXTRA_WEB_URL);
				}
				else
				{
					webUrls = new String[]{getIntent().getExtras().getString(Constants.EXTRA_WEB_URL)};
				}
			}
			else
			{
				webUrls = imageUrls;
			}

			pagerPosition = getIntent().getExtras().getInt(Constants.EXTRA_IMAGE_POSITION, 0);
		}
		else
		{
			finish();
			return;
		}

		if (savedInstanceState != null)
		{
			pagerPosition = savedInstanceState.getInt(Constants.EXTRA_IMAGE_POSITION);
		}

		Views.inject(this);

		pager.setAdapter(new ImagePagerAdapter(imageUrls, pager));
		pager.setCurrentItem(pagerPosition);
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putInt(Constants.EXTRA_IMAGE_POSITION, pager.getCurrentItem());
		super.onSaveInstanceState(outState);
	}

	@Override public void onClick(View v)
	{
		if (v == mWeb)
		{
			try
			{
				Intent web = new Intent(Intent.ACTION_VIEW);
				web.setData(Uri.parse(webUrls[pager.getCurrentItem()]));
				startActivity(web);
			}
			catch (Exception e)
			{
				Toast.makeText(getContext(), R.string.failed_intent, Toast.LENGTH_LONG).show();
			}
		}
		else if (v == mShare)
		{
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, webUrls[pager.getCurrentItem()]);
			shareIntent.setType("text/plain");
			startActivity(shareIntent);
		}
		else if (v == mDownload)
		{
			DownloadHelper.showMediaDownloadPopup(getContext(), webUrls[pager.getCurrentItem()]);
		}
		else if (v == mCopy)
		{
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
			{
				android.text.ClipboardManager clipboard = (android.text.ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(webUrls[pager.getCurrentItem()]);
			}
			else
			{
				android.content.ClipboardManager clipboard = (android.content.ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
				android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", webUrls[pager.getCurrentItem()]);
				clipboard.setPrimaryClip(clip);
			}

			Toast.makeText(getContext(), getContext().getString(R.string.copy_url_success), Toast.LENGTH_SHORT).show();
		}
	}

	private class ImagePagerAdapter extends PagerAdapter
	{
		private String[] images;
		private LayoutInflater inflater;
		private SwipableViewPager pager;

		ImagePagerAdapter(String[] images, SwipableViewPager pager)
		{
			this.images = images;
			this.pager = pager;
			inflater = getLayoutInflater();
		}

		@Override public void destroyItem(ViewGroup container, int position, Object object)
		{
			((ViewPager)container).removeView((View)object);
		}

		@Override public void finishUpdate(View container)
		{
		}

		@Override public int getCount()
		{
			return images.length;
		}

		@Override public Object instantiateItem(ViewGroup view, int position)
		{
			final View imageLayout = inflater.inflate(R.layout.image_lightbox_image, view, false);
			loadImage(imageLayout, position);

			((ViewPager)view).addView(imageLayout, 0);
			return imageLayout;
		}

		private void loadImage(final View v, final int position)
		{
			final ImageView imageView = (ImageView)v.findViewById(R.id.fullscreen_image);
			final ProgressBar spinner = (ProgressBar)v.findViewById(R.id.progress);
			final TextView failedText = (TextView)v.findViewById(R.id.fail_message);
			failedText.setOnClickListener(new OnClickListener()
			{
				@Override public void onClick(View v2)
				{
					loadImage(v, position);
				}
			});

			((TouchImageView)imageView).setOnScaleChangedListener(new OnScaleChangedListener()
			{
				@Override public void onScaleChanged(float newScale)
				{
					if (newScale <= 1f)
					{
						pager.setCanSwipe(true);
						pager.requestDisallowInterceptTouchEvent(false);
					}
					else
					{
						pager.setCanSwipe(false);
						pager.requestDisallowInterceptTouchEvent(true);
					}
				}
			});

			ImageLoader.getInstance().displayImage(URLUtils.fixInlineImage(Uri.parse(images[position])), imageView, MainApplication.getMediaImageOptions(), new SimpleImageLoadingListener()
			{
				@Override public void onLoadingStarted(String imageUri, View view)
				{
					spinner.setVisibility(View.VISIBLE);
					failedText.setVisibility(View.GONE);
				}

				@Override public void onLoadingFailed(String imageUri, View view, FailReason failReason)
				{
					spinner.setVisibility(View.GONE);
					failedText.setVisibility(View.VISIBLE);
				}

				@Override public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
				{
					spinner.setVisibility(View.GONE);
					failedText.setVisibility(View.GONE);
				}

				@Override public void onLoadingCancelled(String imageUri, View view)
				{
					spinner.setVisibility(View.GONE);
				}
			});
		}

		@Override public boolean isViewFromObject(View view, Object object)
		{
			return view.equals(object);
		}

		@Override public void restoreState(Parcelable state, ClassLoader loader)
		{
		}

		@Override public Parcelable saveState()
		{
			return null;
		}

		@Override public void startUpdate(View container)
		{
		}
	}
}