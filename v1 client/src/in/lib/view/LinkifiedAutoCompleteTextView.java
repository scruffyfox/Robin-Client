package in.lib.view;

import in.lib.utils.html.ADNHtml;
import in.lib.view.spannable.NotUnderlinedClickableSpan;
import lombok.Setter;
import android.content.Context;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;

public class LinkifiedAutoCompleteTextView extends AutoSuggestView implements OnLongClickListener, OnClickListener
{
	private SpannableStringBuilder mStrBuilder;
	@Setter private OnSpannableClickedListener onSpannableClickedListener;

	public interface OnSpannableClickedListener
	{
		public void onSpannableClicked(ClickableSpan spannable);
	}

	public LinkifiedAutoCompleteTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public void setText(String text)
	{
		if (!TextUtils.isEmpty(text))
		{
			mStrBuilder = new SpannableStringBuilder(ADNHtml.fromHtml(text));
			setText(mStrBuilder);
		}
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

	@Override public boolean onTouchEvent(MotionEvent event)
	{
		int x = (int)event.getX();
		int y = (int)event.getY();

		x -= getTotalPaddingLeft();
		y -= getTotalPaddingTop();

		x += getScrollX();
		y += getScrollY();

		Layout layout = getLayout();
		int line = layout.getLineForVertical(y);
		int off = layout.getOffsetForHorizontal(line, x);

		setSelection(off);

		/**
		 * get you interest span
		 */
		NotUnderlinedClickableSpan[] link = getText().getSpans(off, off, NotUnderlinedClickableSpan.class);
		if (link.length != 0)
		{
			if (event.getAction() == MotionEvent.ACTION_UP)
			{
				if (event.getEventTime() - event.getDownTime() <= ViewConfiguration.getTapTimeout())
				{
					if (onSpannableClickedListener != null)
					{
						onSpannableClickedListener.onSpannableClicked(link[0]);
					}

					link[0].onSimpleClick(this);
				}
			}

			return true;
		}

		return super.onTouchEvent(event);
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