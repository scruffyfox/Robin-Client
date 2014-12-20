package in.rob.client.dialog;

import in.lib.Constants;
import in.lib.annotation.InjectView;
import in.lib.handler.dialogs.NewPostDialogResponseHandler;
import in.lib.handler.dialogs.RepostDialogResponseHandler;
import in.lib.manager.APIManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.Views;
import in.model.Post;
import in.obj.annotation.Annotation;
import in.obj.entity.Entity;
import in.obj.entity.Entity.Type;
import in.obj.entity.LinkEntity;
import in.rob.client.MainApplication;
import in.rob.client.R;
import in.rob.client.dialog.base.PostDialog;

import java.util.ArrayList;
import java.util.Random;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Repost dialog for reposting a post.
 *
 * Required extra arguments:
 * <ul>
 * 	<li><b>{@linkplain Constants Constants.EXTRA_REPOST}</b>: A {@link Post} object of the post to repost</li>
 * </ul>
 */
public class RepostDialog extends PostDialog
{
	@InjectView(R.id.avatar) public ImageView mAvatar;
	private NotificationManager mNotificationManager;
	private Post mRepostee;
	private String originalPostText;
	private int mNotificationId;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationId = new Random().nextInt();

		setTitle(String.format(getString(R.string.repost), mRepostee.getPoster().getMentionName()));
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	}

	@Override protected void initDialog()
	{
		super.initDialog();

		Views.inject(this);
		ImageLoader.getInstance().displayImage(mRepostee.getPoster().getAvatarUrl() + "?avatar=1&id=" + mRepostee.getPoster().getId(), mAvatar, MainApplication.getAvatarImageOptions());

		final TextWatcher textWatcher = new TextWatcher()
		{
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			@Override public void afterTextChanged(Editable s)
			{
				if (s.toString().trim().replace(originalPostText.trim(), "").trim().length() < 1)
				{
					setRemainingChars(0);
				}
				else
				{
					int remaining = getMaxChars() - input.getText().toString().trim().length() - (getCurrentPost().getImagePath() == null ? 0 : SettingsManager.getImageProvider().getUrlLength());
					setRemainingChars(remaining);
				}
			}

			@Override public void onTextChanged(CharSequence s, int start, int before, int count){}
		};

		((EditText)getInput()).setSelection(0);
		((EditText)getInput()).addTextChangedListener(textWatcher);

		if (getCurrentPost().getPostText().trim().replace(originalPostText.trim(), "").trim().length() < 1)
		{
			setRemainingChars(0);
		}
	}

	@Override public void retrieveArguments(Bundle instances)
	{
		super.retrieveArguments(instances);

		if (instances != null)
		{
			if (instances.containsKey(Constants.EXTRA_REPOST))
			{
				mRepostee = (Post)instances.getParcelable(Constants.EXTRA_REPOST);
				String originalText = mRepostee.getOriginalText();
				String prefix = "RP @" + mRepostee.getPoster().getMentionName() + " ";
				getCurrentPost().setRepostId(mRepostee.getId());

				if (mRepostee.getAnnotations() != null)
				{
					ArrayList<Annotation> annotations = new ArrayList<Annotation>();

					if (mRepostee.getAnnotations().get(Annotation.Type.IMAGE) != null)
					{
						annotations.addAll(mRepostee.getAnnotations().get(Annotation.Type.IMAGE));
					}

					if (mRepostee.getAnnotations().get(Annotation.Type.RICH) != null)
					{
						annotations.addAll(mRepostee.getAnnotations().get(Annotation.Type.RICH));
					}

					if (mRepostee.getAnnotations().get(Annotation.Type.VIDEO) != null)
					{
						annotations.addAll(mRepostee.getAnnotations().get(Annotation.Type.VIDEO));
					}

					getCurrentPost().setAnnotations(annotations);
				}

				ArrayList<Entity> links = new ArrayList<Entity>();

				int offset = prefix.length();
				int offsetSet = 0;
				if (mRepostee.getLinks() != null)
				{
					for (LinkEntity link : mRepostee.getLinks())
					{
						link.setPos(link.getPos() - offsetSet);

						if (link.getAmendedLen() > -1)
						{
							int previewStart = link.getPos() + link.getLen() + 1;
							String start = originalText.substring(0, previewStart);
							int endLen = start.length() + (link.getAmendedLen() - link.getLen());
							endLen = Math.min(endLen, originalText.length());
							String end = originalText.substring(endLen);

							offsetSet += originalText.length() - start.length() - end.length();
							originalText = start + end;
						}

						link.setPos(link.getPos() + offset);
						links.add(link);
					}
				}

				originalPostText = prefix + originalText;
				getCurrentPost().setPostText(originalPostText);
				getCurrentPost().getEntities().put(Type.LINK, links);
			}
			else if (instances.containsKey(Constants.EXTRA_POST))
			{
				mRepostee = (Post)instances.getParcelable(Constants.EXTRA_REPOST);
				String originalText = Html.fromHtml(mRepostee.getFormattedText()).toString();
				originalPostText = "RP @" + mRepostee.getPoster().getMentionName() + " " + originalText;
			}
		}
	}

	/**
	 * Sets the remaining character count
	 * @param remaining The remaining count
	 */
	@Override public void setRemainingChars(int remaining)
	{
		mRemainingCharacters.setText(remaining + "");

		if (remaining >= 0)
		{
			mRemainingCharacters.setTextColor(getResources().getColor(R.color.dark_grey));
			mPostBtn.setVisibility(View.VISIBLE);
		}
		else
		{
			mRemainingCharacters.setTextColor(getResources().getColor(R.color.light_dialog_text_color_alert));
			mPostBtn.setVisibility(View.INVISIBLE);
		}
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putParcelable(Constants.EXTRA_POST, mRepostee);
		super.onSaveInstanceState(outState);
	}

	@Override public void positiveControl()
	{
		sendNotification(getString(R.string.reposting_title), getString(R.string.reposting));
		String token = UserManager.getAuths(getContext()).get(getSelectedUser().getId()).getAccessToken();

		if (getCurrentPost().getPostText().trim().replace(originalPostText.trim(), "").trim().length() > 0)
		{
			if (getCurrentPost().getPostText().length() > 256)
			{
				mRemainingCharacters.setVisibility(View.VISIBLE);
				return;
			}

			Intent failedIntent = new Intent(this, NewPostDialog.class);
			failedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			failedIntent.putExtra(Constants.EXTRA_NEW_POST_DRAFT, getCurrentPost().serialize());
			getCurrentPost().setReplyId(getCurrentPost().getRepostId());
			APIManager.getInstance().replyPost(token, getCurrentPost(), new NewPostDialogResponseHandler(getContext(), failedIntent, mNotificationId));
		}
		else
		{
			Intent failedIntent = new Intent(this, RepostDialog.class);
			failedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
			failedIntent.putExtra(Constants.EXTRA_REPOST, mRepostee);
			APIManager.getInstance().repost(token, mRepostee.getId(), new RepostDialogResponseHandler(getContext(), failedIntent, mNotificationId));
		}
	}

	public void sendNotification(String title, String content)
	{
		CharSequence contentTitle = getString(R.string.app_name);
		CharSequence contentText = content;

		Intent notificationIntent = new Intent();
		PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder notification = new NotificationCompat.Builder(getContext());
		notification.setContentTitle(contentTitle);
		notification.setContentText(contentText);
		notification.setTicker(content);
		notification.setSmallIcon(R.drawable.notif);
		notification.setProgress(0, 0, true);

		//notification.setOngoing(true);
		notification.setContentIntent(contentIntent);
		mNotificationManager.notify(mNotificationId, notification.build());
	}

	@Override public int getContentView()
	{
		return R.layout.repost_dialog;
	}
}