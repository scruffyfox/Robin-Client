package in.lib.view.spannable;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

public abstract class NotUnderlinedClickableSpan extends ClickableSpan
{
	@Override public void onClick(View widget)
	{
	}

	public boolean onTouch(View widget, MotionEvent m)
	{
		return false;
	}

	public void onSimpleClick(View widget)
	{
		//widget.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
		widget.playSoundEffect(SoundEffectConstants.CLICK);
	}

	public void onLongClick(View widget)
	{
		MotionEvent m = MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0f, 0f, 0);
		widget.dispatchTouchEvent(m);
	}

	@Override public void updateDrawState(TextPaint ds)
	{
		super.updateDrawState(ds);
		ds.setUnderlineText(false);
	}
}