package in.rob.client.dialog;

import in.lib.Constants;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.manager.SettingsManager;
import in.lib.utils.Views;
import in.rob.client.R;
import in.rob.client.base.RobinDialogActivity;
import lombok.Getter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class WebLightboxDialog extends RobinDialogActivity implements OnClickListener
{
	private final String READABILITY_PREFIX = "http://www.instapaper.com/text?u=";
	private final String READABILITY_REGEX = "http(s?)://(www.)?instapaper.com";

	@Getter private Context context = this;
	private String mUrl;

	@Getter @InjectView(R.id.fullscreen_web) public WebView webView;
	@OnClick @InjectView(R.id.icon_web) public View mWeb;
	@OnClick @InjectView(R.id.icon_share) public View mShare;
	@OnClick @InjectView(R.id.icon_close) public View mClose;
	@OnClick @InjectView(R.id.icon_back) public View mBack;
	@OnClick @InjectView(R.id.icon_forward) public View mForward;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.web_lightbox);
		Views.inject(this);

		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		getWindow().setGravity(Gravity.CENTER);
		getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.fullscreen_dialog_bg));

		if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(Constants.EXTRA_PREVIEW_URL))
		{
			mUrl = getIntent().getExtras().getString(Constants.EXTRA_PREVIEW_URL);
		}
		else
		{
			finish();
			return;
		}

		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setBuiltInZoomControls(true);
		settings.setLoadWithOverviewMode(true);
		settings.setUseWideViewPort(true);

		if (Build.VERSION.SDK_INT >= 11)
		{
			settings.setDisplayZoomControls(false);
		}

		final ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress);
		webView.setWebViewClient(new CustomWebViewClient());
		webView.setWebChromeClient(new WebChromeClient()
		{
			@Override public void onProgressChanged(WebView view, int progress)
			{
				if (progress < 100)
				{
					setLoadProgress(progress);
				}
				else if (progress == 100)
				{
					setLoadProgress(100);
				}

				super.onProgressChanged(view, progress);
			}
		});

		if (savedInstanceState == null)
		{
			if (SettingsManager.isWebReadabilityEnabled() && !mUrl.matches(READABILITY_REGEX))
			{
				mUrl = READABILITY_PREFIX + Uri.encode(mUrl);
			}

			webView.loadUrl(mUrl);
		}
	}

	private class CustomWebViewClient extends WebViewClient
	{
		@Override public boolean shouldOverrideUrlLoading(WebView view, String url)
		{
			if (SettingsManager.isWebReadabilityEnabled() && !url.matches(READABILITY_REGEX))
			{
				view.loadUrl(READABILITY_PREFIX + Uri.encode(url));
				return true;
			}

			return false;
		}
	}

	private void removeWebView()
	{
		if (webView != null)
		{
			webView.stopLoading();
			webView.loadData("", "text/html", "utf-8");
		}
	}

	@Override public void finish()
	{
		super.finish();
		removeWebView();
	}

	@Override protected void onDestroy()
	{
		removeWebView();
		super.onDestroy();

		if (webView != null)
		{
			((ViewGroup)findViewById(R.id.web_holder)).removeAllViews();
			webView.destroy();
			webView = null;
		}
	}

	@Override public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	@Override public void onSaveInstanceState(Bundle savedInstanceState)
	{
		webView.saveState(savedInstanceState);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override protected void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		webView.restoreState(savedInstanceState);
	}

	public void setLoadProgress(int progress)
	{
		View view1 = ((ViewGroup)findViewById(R.id.progress_indicator)).getChildAt(0);
		View view2 = ((ViewGroup)findViewById(R.id.progress_indicator)).getChildAt(1);

		LinearLayout.LayoutParams lp1 = (android.widget.LinearLayout.LayoutParams)view1.getLayoutParams();
		lp1.weight = 100 - progress;

		LinearLayout.LayoutParams lp2 = (android.widget.LinearLayout.LayoutParams)view2.getLayoutParams();
		lp2.weight = progress;

		view1.setLayoutParams(lp1);
		view2.setLayoutParams(lp2);
	}

	@Override public void onBackPressed()
	{
		if (webView.canGoBack())
		{
			webView.goBack();
			return;
		}

		removeWebView();
		super.onBackPressed();
	}

	@Override public void onClick(View v)
	{
		if (v == mWeb)
		{
			Intent web = new Intent(Intent.ACTION_VIEW);

			if (webView.getUrl() == null)
			{
				web.setData(Uri.parse(mUrl));
			}
			else
			{
				web.setData(Uri.parse(webView.getUrl()));
			}

			startActivity(web);
		}
		else if (v == mShare)
		{
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, mUrl);
			shareIntent.setType("text/plain");
			startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
		}
		else if (v == mBack)
		{
			if (webView.canGoBack())
			{
				webView.goBack();
			}
		}
		else if (v == mForward)
		{
			webView.goForward();
		}
		else if (v == mClose)
		{
			finish();
		}
	}
}