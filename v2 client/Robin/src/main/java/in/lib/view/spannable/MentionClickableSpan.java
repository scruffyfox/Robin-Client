package in.lib.view.spannable;

import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;

import in.data.entity.MentionEntity;
import in.rob.client.ProfileActivity;

public class MentionClickableSpan extends NotUnderlinedClickableSpan
{
	private final MentionEntity mention;

	public MentionClickableSpan(MentionEntity mention)
	{
		this.mention = mention;
	}

	@Override public void onSimpleClick(View widget)
	{
		super.onSimpleClick(widget);
		openProfile(widget.getContext());
	}

	@Override public boolean onTouch(View widget, MotionEvent m)
	{
		return super.onTouch(widget, m);
	}

	private void openProfile(Context ctx)
	{
		Intent profile = new Intent(ctx, ProfileActivity.class);
		ctx.startActivity(profile);
	}

	@Override public void onLongClick(View widget)
	{
		super.onLongClick(widget);
	}
}