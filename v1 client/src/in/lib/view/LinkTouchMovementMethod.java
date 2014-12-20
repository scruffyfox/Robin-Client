package in.lib.view;

import in.lib.view.spannable.NotUnderlinedClickableSpan;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

public class LinkTouchMovementMethod extends LinkMovementMethod
{
	private Class<? extends NotUnderlinedClickableSpan>[] mDisallowSpans = new Class[0];

	public LinkTouchMovementMethod()
	{

	}

	public LinkTouchMovementMethod(Class<? extends NotUnderlinedClickableSpan>... disallow)
	{
		mDisallowSpans = disallow;
	}

	@Override public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event)
	{
		int action = event.getAction();

		if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_CANCEL)
		{
			int x = (int)event.getX();
			int y = (int)event.getY();

			x -= widget.getTotalPaddingLeft();
			y -= widget.getTotalPaddingTop();

			x += widget.getScrollX();
			y += widget.getScrollY();

			Layout layout = widget.getLayout();
			int line = layout.getLineForVertical(y);
			int off = layout.getOffsetForHorizontal(line, x);

			NotUnderlinedClickableSpan[] link = buffer.getSpans(off, off, NotUnderlinedClickableSpan.class);

			if (link.length != 0)
			{
				if (mDisallowSpans.length > 0)
				{
					for (Class<? extends NotUnderlinedClickableSpan> disallow : mDisallowSpans)
					{
						if (link[0].getClass().equals(disallow))
						{
							return true;
						}
					}
				}

				if (action == MotionEvent.ACTION_DOWN)
				{
					int start = buffer.getSpanStart(link[0]);
					int end = buffer.getSpanEnd(link[0]);
					if (end > start)
					{
						Selection.setSelection(buffer, start, end);
						buffer.setSpan(new BackgroundColorSpan(0x7fF13D4D), buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					}
				}
				else if (action == MotionEvent.ACTION_UP)
				{
					if (event.getEventTime() - event.getDownTime() <= ViewConfiguration.getTapTimeout())
					{
						link[0].onSimpleClick(widget);
					}
				}

				if (widget.getClass().equals(LinkifiedTextView.class))
				{
					((LinkifiedTextView)widget).setLinkHit(true);
				}
			}

			if (action != MotionEvent.ACTION_DOWN)
			{
				BackgroundColorSpan[] toRemoveSpans = buffer.getSpans(0, widget.getText().length(), BackgroundColorSpan.class);
				ForegroundColorSpan[] toRemoveSpans2 = buffer.getSpans(0, widget.getText().length(), ForegroundColorSpan.class);
				for (BackgroundColorSpan s : toRemoveSpans)
				{
					buffer.removeSpan(s);
				}

				for (ForegroundColorSpan s : toRemoveSpans2)
				{
					buffer.removeSpan(s);
				}
			}
		}

		return super.onTouchEvent(widget, buffer, event);
	}
}