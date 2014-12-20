package in.rob.client.dialog;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import in.controller.handler.PostDialogResponseHandler;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.view.AvatarImageView;
import in.lib.view.LinkedTextView;
import in.lib.view.TextChronometer;
import in.model.Post;
import in.rob.client.R;
import in.rob.client.dialog.base.InputPostDialog;
import lombok.Getter;

@Injectable
public class DeletePostDialog extends InputPostDialog
{
	@InjectView private AvatarImageView avatar;
	@InjectView private TextChronometer date;
	@InjectView private TextView usernameTitle;
	@InjectView private TextView usernameSubtitle;
	@Getter private Post post;

	@Override public void initialiseDraft(){}

	@Override public int getContentView()
	{
		return R.layout.post_delete_dialog;
	}

	@Override public TextView getPostInput()
	{
		return (TextView)findViewById(R.id.post_text);
	}

	@Override protected void setWindowMode()
	{
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	}

	@Override public void initialiseDialog()
	{
		super.initialiseDialog();

		setTitle(R.string.confirm);
		Views.inject(this);
	}

	@Override public void retrieveArguments(Bundle args)
	{
		super.retrieveArguments(args);

		if (args != null)
		{
			post = (Post)args.getParcelable(Constants.EXTRA_POST);

			avatar.setUser(post.getPoster());
			usernameTitle.setText(post.getPoster().getFormattedMentionNameTitle());
			usernameSubtitle.setText(post.getPoster().getFormattedMentionNameSubTitle());
			date.setTime(post.getDate());
			((LinkedTextView)getPostInput()).setText(post.getPostText());
		}
	}

	@Override public void onPositiveButtonClick(View view)
	{
		finish();
		APIManager.getInstance().deletePost(getPost(), new PostDialogResponseHandler(getContext(), getPost().hashCode())
		{
			@Override public String getNotificationTitle()
			{
				return getContext().getString(R.string.deleting_post_title);
			}

			@Override public String getNotificationText()
			{
				return getContext().getString(R.string.deleting_post);
			}

			@Override public String getNotificationFinishText()
			{
				return getContext().getString(R.string.post_delete_success);
			}
		});
	}

	@Override public void onNegativeButtonClick(View view)
	{
		finish();
	}
}
