package in.lib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SquareLayout extends LinearLayout
{
	/**
	 * Default Constructor
	 * @param context The application's context
	 */
	public SquareLayout(Context context)
	{
		super(context);
	}
	
	/**
	 * Default constructor
	 * @param context The context of the application/activity
	 * @param attrs The attribute set gathered from the XML
	 */
	public SquareLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	/**
	 * Measures the view and forces it to be square
	 * @param widthMeasureSpec The width spec to use when measuring
	 * @param heightMeasureSpec The height spec to use when measuring
	 */
	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{	
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int mScale = 1;
				
		if (width < (int)(mScale * height + 0.5))
		{
			width = (int)(mScale * height + 0.5);
		}
		else
		{
			height = (int)(width / mScale + 0.5);
		}
		
		super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
	}
}