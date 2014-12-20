package in.rob.client.dialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import java.util.ArrayList;

import in.controller.handler.PostDialogResponseHandler;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.utils.Debug;
import in.lib.utils.Views.Injectable;
import in.model.DraftPost;
import in.rob.client.dialog.base.InputPostDialog;

@Injectable
public class NewPostDialog extends InputPostDialog
{
	private boolean resend = false;

	@Override public void initialiseDraft()
	{
		setDraft(new DraftPost());
	}

	@Override public void retrieveArguments(Bundle args)
	{
		super.retrieveArguments(args);

		if (args != null)
		{
			if (args.containsKey(Constants.EXTRA_DRAFT_POST))
			{
				setDraft((DraftPost)args.get(Constants.EXTRA_DRAFT_POST));
			}

			if (args.containsKey(Intent.EXTRA_STREAM))
			{
				Object stream = args.get(Intent.EXTRA_STREAM);
				ArrayList<Uri> uris;

				if (stream instanceof String)
				{
					uris = new ArrayList<Uri>();
					uris.add(Uri.parse(String.valueOf(stream)));
				}
				else if (stream instanceof Uri)
				{
					uris = new ArrayList<Uri>();
					uris.add((Uri)stream);
				}
				else if (stream instanceof ArrayList)
				{
					uris = (ArrayList<Uri>)stream;
				}
				else
				{
					// invalid extra passed
					Debug.out("ERROR: Invalid extras passed to share intent %s", stream.getClass());
					finish();
					return;
				}

				for (Uri uri : uris)
				{
					addImage(uri);
				}
			}

			if (args.containsKey(Intent.EXTRA_TEXT))
			{
				getDraft().setPostText(args.getString(Intent.EXTRA_TEXT));
			}

			if (args.containsKey(Constants.EXTRA_SEND))
			{
				resend = true;
				onPositiveButtonClick(null);
			}
		}
	}

	@Override public void onPositiveButtonClick(View view)
	{
		if (!resend)
		{
			super.onPositiveButtonClick(view);
		}
		else
		{
			finish();
		}

		if (!TextUtils.isEmpty(getDraft().getPostText()))
		{
			getDraft().setImageCount(getDraft().getImages().size());
			APIManager.getInstance().postPost(getContext(), (DraftPost)getDraft(), new PostDialogResponseHandler(getContext(), (int)(getDraft().getDate() / 1000L))
			{
				@Override public void onFinish(boolean failed)
				{
					super.onFinish(failed);

					if (!failed)
					{
						getDraft().delete();
					}
				}
			});
		}
	}
}
