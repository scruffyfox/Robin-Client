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

	public abstract void onLongClick(View widget);

	@Override public void updateDrawState(TextPaint ds)
	{
		super.updateDrawState(ds);
		ds.setUnderlineText(false);
	}
}