package in.lib.view.spannable;

import java.util.Locale;

import lombok.Getter;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;

public class MarkDownClickableSpan extends NotUnderlinedClickableSpan
{
	@Getter private String url;
	@Getter private String anchor;

	public MarkDownClickableSpan(String url, String anchor)
	{
		this.url = url;
		this.anchor = anchor;

		if (!url.toLowerCase(Locale.getDefault()).startsWith("http://") && !url.toLowerCase(Locale.getDefault()).startsWith("https://"))
		{
			this.url = "http://" + url;
		}
	}

	@Override public void updateDrawState(TextPaint ds)
	{
		super.updateDrawState(ds);
		ds.setUnderlineText(true);
	}

	@Override public boolean onTouch(View widget, MotionEvent m)
	{
		return false;
	}

	@Override public void onSimpleClick(View widget)
	{
		super.onSimpleClick(widget);
	}

	@Override public void onLongClick(View widget)
	{
	}
}