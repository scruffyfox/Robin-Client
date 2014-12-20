package in.lib.view;

import in.lib.utils.ViewUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

public class SettingContainerView extends RelativeLayout implements OnClickListener
{
	private OnClickListener mOnClickListener;

	public SettingContainerView(Context context)
	{
		super(context);
		init();
	}

	public SettingContainerView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	private void init()
	{
		setOnClickListener(this);
	}

	@Override public void setOnClickListener(OnClickListener l)
	{
		if (l == this)
		{
			super.setOnClickListener(l);
		}
		else
		{
			mOnClickListener = l;
		}
	}

	@Override public void onClick(View v)
	{
		if (mOnClickListener != null)
		{
			mOnClickListener.onClick(v);
		}

		View box;
		if ((box = ViewUtils.getFirstChildByInstance(this, CheckBox.class)) != null)
		{
			((CheckBox)box).toggle();
		}
	}
}