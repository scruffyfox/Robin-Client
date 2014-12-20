package in.lib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import in.lib.utils.ViewUtils;

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

	public CompoundButton getCheckableView()
	{
		return (CompoundButton)ViewUtils.getFirstChildByInstance(this, CompoundButton.class);
	}

	@Override public void onClick(View v)
	{
		CompoundButton box = getCheckableView();
		if (box != null)
		{
			box.toggle();
		}

		if (mOnClickListener != null)
		{
			mOnClickListener.onClick(v);
		}
	}
}