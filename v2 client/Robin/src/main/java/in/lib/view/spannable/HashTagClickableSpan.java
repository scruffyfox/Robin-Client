package in.lib.view.spannable;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import in.data.entity.HashEntity;

public class HashTagClickableSpan extends NotUnderlinedClickableSpan
{
	private final HashEntity hashtag;

	public HashTagClickableSpan(HashEntity hashtag)
	{
		this.hashtag = hashtag;
	}

	@Override public void onSimpleClick(View widget)
	{
		super.onSimpleClick(widget);
		openTag(widget.getContext());
	}

	@Override public boolean onTouch(View widget, MotionEvent m)
	{
		return super.onTouch(widget, m);
	}

	private void openTag(Context ctx)
	{

	}

	@Override public void onLongClick(View widget)
	{
		super.onLongClick(widget);
	}
}