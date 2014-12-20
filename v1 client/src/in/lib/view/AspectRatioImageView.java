package in.lib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class AspectRatioImageView extends ImageView
{
	public AspectRatioImageView(Context context)
	{
		super(context);
	}

	public AspectRatioImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (getDrawable() != null)
		{
			int width = MeasureSpec.getSize(widthMeasureSpec);
			int height = Math.max(1, (width * getDrawable().getIntrinsicHeight())) / Math.max(1, getDrawable().getIntrinsicWidth());
			setMeasuredDimension(width, height);
		}
	}
}