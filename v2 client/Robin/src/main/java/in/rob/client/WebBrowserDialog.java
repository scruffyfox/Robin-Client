package in.rob.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import in.lib.Constants;
import in.lib.manager.SettingsManager;
import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.utils.Views.OnClick;
import lombok.Getter;

@Injectable
public class WebBrowserDialog extends Activity
{
	private final String READABILITY_PREFIX = "http://www.instapaper.com/text?u=";
	private final String READABILITY_REGEX = "http(s?)://(www.)?instapaper.com";

	@Getter private Context context = this;
	private String mUrl;

	@Getter @InjectView(R.id.fullscreen_web) public WebView webView;
	@InjectView(R.id.icon_web) public View mWeb;
	@InjectView(R.id.icon_share) public View mShare;
	@InjectView(R.id.icon_close) public View mClose;
	@InjectView(R.id.icon_back) public View mBack;
	@InjectView(R.id.icon_forward) public View mForward;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.web_browser_view);
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
		settings.setDisplayZoomControls(false);

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
			if (SettingsManager.getInstance().isWebReadabilityModeEnabled() && !mUrl.matches(READABILITY_REGEX))
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
			if (SettingsManager.getInstance().isWebReadabilityModeEnabled() && !url.matches(READABILITY_REGEX))
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

		LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams)view1.getLayoutParams();
		lp1.weight = 100 - progress;

		LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams)view2.getLayoutParams();
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

	@OnClick public void onIconWebClick(View v)
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

	@OnClick public void onIconShareClick(View v)
	{
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_TEXT, mUrl);
		shareIntent.setType("text/plain");
		startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
	}

	@OnClick public void onIconBackClick(View v)
	{
		if (webView.canGoBack())
		{
			webView.goBack();
		}
	}

	@OnClick public void onIconForwardClick(View v)
	{
		webView.goForward();
	}

	@OnClick public void onIconCloseClick(View v)
	{
		finish();
	}
}