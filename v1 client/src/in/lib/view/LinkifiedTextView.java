package in.lib.view;

import in.lib.utils.html.ADNHtml;
import in.lib.view.spannable.NotUnderlinedClickableSpan;
import lombok.Setter;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public class LinkifiedTextView extends ResizableTextView implements OnLongClickListener, OnClickListener
{
	private SpannableStringBuilder mStrBuilder;
	private String cachedString;
	@Setter private boolean linkHit = false;

	public LinkifiedTextView(Context context)
	{
		super(context);
	}

	public LinkifiedTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public void setText(String text)
	{
		if (!TextUtils.isEmpty(text))
		{
			if (mStrBuilder == null || !cachedString.equals(text))
			{
				cachedString = text;
				mStrBuilder = new SpannableStringBuilder(ADNHtml.fromHtml(cachedString));
			}

			setText(mStrBuilder);
		}
	}

	@Override public boolean onTouchEvent(MotionEvent event)
	{
		linkHit = false;
		boolean res = super.onTouchEvent(event);

		if (!linkHit)
		{
			return false;
		}

		return res;
	}

	public void setLinkMovementMethod(MovementMethod movement)
	{
		setMovementMethod(movement);

		if (movement != null)
		{
			setOnLongClickListener(this);
			//setOnClickListener(this);
		}
	}

	@Override public boolean onLongClick(View v)
	{
		if (mStrBuilder != null)
		{
			NotUnderlinedClickableSpan[] spans = mStrBuilder.getSpans(getSelectionStart(), getSelectionEnd(), NotUnderlinedClickableSpan.class);

			if (spans.length == 1)
			{
				NotUnderlinedClickableSpan span = spans[0];
				span.onLongClick(v);
				return true;
			}
		}

		return false;
	}

	@Override public void onClick(View v)
	{
		if (mStrBuilder != null)
		{
			NotUnderlinedClickableSpan[] spans = mStrBuilder.getSpans(getSelectionStart(), getSelectionEnd(), NotUnderlinedClickableSpan.class);

			if (spans.length > 0)
			{
				NotUnderlinedClickableSpan span = spans[0];
				span.onSimpleClick(v);
			}
		}
	}
}