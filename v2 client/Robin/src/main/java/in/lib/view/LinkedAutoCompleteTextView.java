package in.lib.view;

import android.content.Context;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import in.lib.view.spannable.NotUnderlinedClickableSpan;
import lombok.Setter;

public class LinkedAutoCompleteTextView extends AutoSuggestView implements OnLongClickListener, OnClickListener
{
	@Setter private OnSpannableClickedListener onSpannableClickedListener;

	public static interface OnSpannableClickedListener
	{
		public void onSpannableClicked(ClickableSpan spannable);
	}

	public LinkedAutoCompleteTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setOnClickListener(this);
	}

	@Override public boolean onLongClick(View v)
	{
		NotUnderlinedClickableSpan[] spans = getText().getSpans(getSelectionStart(), getSelectionEnd(), NotUnderlinedClickableSpan.class);

		if (spans.length == 1)
		{
			NotUnderlinedClickableSpan span = spans[0];
			span.onLongClick(v);
			return true;
		}

		return false;
	}

	@Override public void onClick(View v)
	{
		NotUnderlinedClickableSpan[] spans = getText().getSpans(getSelectionStart(), getSelectionEnd(), NotUnderlinedClickableSpan.class);

		if (spans.length > 0)
		{
			NotUnderlinedClickableSpan span = spans[0];
			span.onSimpleClick(v);

			if (onSpannableClickedListener != null)
			{
				onSpannableClickedListener.onSpannableClicked(span);
			}
		}
	}
}