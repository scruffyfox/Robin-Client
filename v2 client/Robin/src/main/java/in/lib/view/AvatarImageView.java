package in.lib.view;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import in.lib.Constants;
import in.lib.manager.ImageOptionsManager;
import in.lib.manager.SettingsManager;
import in.lib.utils.BitUtils;
import in.model.SimpleUser;
import in.model.User;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.dialog.PopupProfileDialog;
import lombok.Getter;

public class AvatarImageView extends ImageView implements OnClickListener, OnLongClickListener
{
	@Getter private boolean clickable = true;
	@Getter private boolean longClickable = true;
	@Getter private SimpleUser user;

	public AvatarImageView(Context context)
	{
		super(context);
		init();
	}

	public AvatarImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public AvatarImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	public void init()
	{
		clickable = super.isClickable();
		longClickable = super.isLongClickable();

		setOnClickListener(clickable ? this : null);
		setOnLongClickListener(longClickable ? this : null);
	}

	@Override public void setClickable(boolean clickable)
	{
		super.setClickable(clickable);
		this.clickable = clickable;

		setOnClickListener(clickable ? this : null);
	}

	@Override public void setLongClickable(boolean longClickable)
	{
		super.setLongClickable(longClickable);
		this.longClickable = true;

		setOnLongClickListener(clickable ? this : null);
	}

	public void setUser(SimpleUser user)
	{
		setUser(user, false);
	}

	public void setUser(SimpleUser user, boolean force)
	{
		this.user = user;

		if (force || BitUtils.contains(SettingsManager.getInstance().getShowHideBit(), Constants.BIT_SHOWHIDE_AVATARS))
		{
			setImageResource(R.drawable.default_avatar);
			ImageLoader.getInstance().cancelDisplayTask(this);

			if (user instanceof User && !((User)user).isAvatarDefault())
			{
				ImageLoader.getInstance().displayImage(user.getAvatarUrl() + "?w=" + SettingsManager.getInstance().getAvatarSize() + "&avatar=1&id=" + user.getId(), this, ImageOptionsManager.getInstance().getAvatarImageOptions());
			}

			setVisibility(View.VISIBLE);
		}
		else
		{
			setVisibility(View.GONE);
		}
	}

	@Override public void onClick(View v)
	{
		if (user != null && user instanceof User)
		{
			Intent intent = new Intent(getContext(), ProfileActivity.class);
			intent.putExtra(Constants.EXTRA_USER, (Parcelable)user);
			getContext().startActivity(intent);
		}
	}

	@Override public boolean onLongClick(View v)
	{
		if (user != null && user instanceof User)
		{
			Intent intent = new Intent(getContext(), PopupProfileDialog.class);
			intent.putExtra(Constants.EXTRA_USER, (Parcelable)user);
			getContext().startActivity(intent);

			return true;
		}

		return false;
	}

	@Override public boolean onTouchEvent(MotionEvent event)
	{
		if (getDrawable() != null && (clickable || isClickable()) && user instanceof User)
		{
			if (event.getAction() == MotionEvent.ACTION_DOWN)
			{
				getDrawable().setAlpha(120);
			}
			else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_OUTSIDE)
			{
				getDrawable().setAlpha(255);
			}
		}

		return super.onTouchEvent(event);
	}
}