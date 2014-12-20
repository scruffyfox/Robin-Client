package in.lib.view;

import in.lib.manager.SettingsManager;
import in.rob.client.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class ResizableTextView extends TextView
{
	private float originalTextSize = 0f;
	private float textSizeAdjustment = 1.0f;

	public ResizableTextView(Context context)
	{
		super(context);
	}

	public ResizableTextView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public ResizableTextView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.ResizableTextView, defStyle, 0);
		originalTextSize = values.getDimension(R.styleable.ResizableTextView_textSize, 14.0f);
		values.recycle();

		refresh();
	}

	public void refresh()
	{
		textSizeAdjustment = SettingsManager.getFontSize();
		setAdjustment(textSizeAdjustment);
	}

	/**
	 * Sets the original text size of the view.
	 *
	 * @param original
	 *            The new original text size to be used on future adjustments
	 */
	public void setOriginalTextSize(float original)
	{
		setTextSize(original);
	}

	/**
	 * Sets the adjustment of the text view font size
	 *
	 * @param percent
	 *            The percentage increase of the original font size declared
	 *            from `app:textSize` or {@link setOriginalTextSize}
	 */
	public void setAdjustment(float percent)
	{
		textSizeAdjustment = percent;
		setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize * textSizeAdjustment);
	}

//	/**
//	 * Sets the text size of the text view and updates the original text size.
//	 * Any changes to the adjustment ratio, will be applied to this new size.
//	 *
//	 * @param size The new font size in pixels
//	 */
//	@Override public void setTextSize(float size)
//	{
//		originalTextSize = size;
//		setTextSize(TypedValue.COMPLEX_UNIT_PX, originalTextSize * textSizeAdjustment);
//	}
}