package in.lib.view;

import lombok.Getter;
import lombok.Setter;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SwipableViewPager extends ViewPager
{
	@Getter @Setter public Boolean canSwipe = true;

	public SwipableViewPager(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override public boolean onInterceptTouchEvent(MotionEvent arg0)
	{
		if (canSwipe)
		{
			return super.onInterceptTouchEvent(arg0);
		}

		return false;
	}

	@Override public boolean onTouchEvent(MotionEvent event)
	{
		if (canSwipe)
		{
			return super.onTouchEvent(event);
		}

		return false;
	}
}