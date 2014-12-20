package in.lib.view;

import in.lib.Constants;
import in.model.User;
import in.rob.client.dialog.PopupProfileDialog;
import lombok.Getter;
import lombok.Setter;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class AvatarView extends ImageView
{
	@Getter @Setter private boolean clickable = false;

	public AvatarView(Context context)
	{
		super(context);
	}

	public AvatarView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public AvatarView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	/**
	 * Used to triger the standard user options
	 * @param user
	 */
	public void triggerLongPress(final User user)
	{
		Intent intent = new Intent(getContext(), PopupProfileDialog.class);
		intent.putExtra(Constants.EXTRA_USER, user);
		getContext().startActivity(intent);
	}

	@Override public void setLongClickable(boolean longClickable)
	{
		clickable = true;
		super.setLongClickable(longClickable);
	}

	@Override public void setOnClickListener(OnClickListener l)
	{
		clickable = true;
		super.setOnClickListener(l);
	}

	@Override public boolean onTouchEvent(MotionEvent event)
	{
		if (getDrawable() == null || !clickable) return super.onTouchEvent(event);

		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			getDrawable().setAlpha(120);
		}
		else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_OUTSIDE)
		{
			getDrawable().setAlpha(255);
		}

		return super.onTouchEvent(event);
	}
}