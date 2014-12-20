package in.rob.client.dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import in.controller.handler.ChannelMessageDialogResponseHandler;
import in.lib.Constants;
import in.lib.builder.DialogBuilder;
import in.lib.manager.APIManager;
import in.lib.utils.Views.Injectable;
import in.model.Channel;
import in.model.DraftMessage;
import in.rob.client.R;
import in.rob.client.dialog.base.InputPostDialog;

@Injectable
public class NewMessageDialog extends InputPostDialog
{
	private boolean finish = false;

	@Override public void initialiseDraft()
	{
		setDraft(new DraftMessage());
	}

	@Override public void initialiseDialog()
	{
		super.initialiseDialog();

		getActionAccount().setOnClickListener(null);
	}

	@Override public void retrieveArguments(Bundle args)
	{
		super.retrieveArguments(args);

		if (args != null)
		{
			if (args.containsKey(Constants.EXTRA_CHANNEL))
			{
				((DraftMessage)getDraft()).setChannelId(((Channel)args.getParcelable(Constants.EXTRA_CHANNEL)).getId());
			}

			if (args.containsKey(Constants.EXTRA_CHANNEL_ID))
			{
				((DraftMessage)getDraft()).setChannelId(args.getString(Constants.EXTRA_CHANNEL_ID));
			}
		}

		if (TextUtils.isEmpty(((DraftMessage)getDraft()).getChannelId()))
		{
			// TODO: Add no channel id set error
			finish();
			return;
		}

		setTitle(R.string.new_message);
		setMaxChars(2048); // TODO: Replace this with config
	}

	@Override public void onPositiveButtonClick(View view)
	{
		super.onPositiveButtonClick(view);

		if (!TextUtils.isEmpty(getDraft().getPostText()))
		{
			getDraft().setImageCount(getDraft().getImages().size());
			APIManager.getInstance().postMessage(getContext(), (DraftMessage)getDraft(), new ChannelMessageDialogResponseHandler(getContext(), (int)(getDraft().getDate() / 1000L)));
		}
	}

	@Override public void onNegativeButtonClick(View view)
	{
		if (finish)
		{
			finish();
			return;
		}

		AlertDialog.Builder builder = DialogBuilder.create(getContext());
		builder.setTitle(R.string.confirm);
		builder.setMessage(R.string.discard_changes);
		builder.setPositiveButton(R.string.yes, new OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				finish = true;
				onNegativeButtonClick(null);
			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}
}
