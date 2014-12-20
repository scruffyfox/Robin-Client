package in.lib.view.spannable;

import in.lib.Constants;
import in.lib.helper.ThemeHelper;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.dialog.NewPostDialog;
import in.rob.client.dialog.base.DialogBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;

public class MentionClickableSpan extends NotUnderlinedClickableSpan
{
	private final String mUsername;
	private final String mUserId;

	public MentionClickableSpan(String username, String userId)
	{
		mUsername = username;
		mUserId = userId;
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
		Intent intent = new Intent(ctx, ProfileActivity.class);
		intent.putExtra(Constants.EXTRA_USER_ID, mUserId);
		ctx.startActivity(intent);
	}

	@Override public void onLongClick(View widget)
	{
		final Context ctx = widget.getContext();

		DialogBuilder.create(ctx)
			.setIcon(ThemeHelper.getDrawableResource(ctx, R.attr.rbn_dialog_icon_profile))
			.setTitle(ctx.getString(R.string.pick_option))
			.setItems(new CharSequence[]{ctx.getString(R.string.open_profile), ctx.getString(R.string.mention_user, mUsername)}, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					switch (which)
					{
						case 0:
							openProfile(ctx);
							break;

						case 1:
							Intent mention = new Intent(ctx, NewPostDialog.class);
							mention.putExtra(Constants.EXTRA_MENTION_NAME, mUsername);
							ctx.startActivity(mention);
							break;

						default:
							break;
					}
				}
			})
		.show();
	}
}