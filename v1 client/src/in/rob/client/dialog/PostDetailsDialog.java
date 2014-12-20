package in.rob.client.dialog;

import in.lib.Constants;
import in.lib.adapter.AccountAdapter;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.utils.Views;
import in.model.Post;
import in.model.SimpleUser;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.ThreadActivity;
import in.rob.client.base.RobinDialogActivity;
import in.rob.client.dialog.base.DialogBuilder;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class PostDetailsDialog extends RobinDialogActivity implements OnClickListener, OnLongClickListener
{
	private Post mPost;
	@OnClick @InjectView(R.id.cancel) public View mCancel;
	@OnClick @InjectView(R.id.user_container) public View mUserContainer;
	@OnClick @InjectView(R.id.client_container) public View mClientContainer;
	@OnClick @InjectView(R.id.link_container) public View mLinkContainer;
	@OnClick @InjectView(R.id.repost_container) public View mRepostContainer;
	@OnClick @InjectView(R.id.stars_container) public View mStarsContainer;
	@OnClick @InjectView(R.id.replies_container) public View mRepliesContainer;
	@InjectView(R.id.posted_by_text) public TextView mPostedBy;
	@InjectView(R.id.stars_text) public TextView mStarsText;
	@InjectView(R.id.reposts_text) public TextView mRepostsText;
	@InjectView(R.id.replies_text) public TextView mRepliesText;
	@InjectView(R.id.link_text) public TextView mLinkText;
	@InjectView(R.id.client_text) public TextView mClientText;
	@InjectView(R.id.timezone_text) public TextView mTimezoneText;
	@InjectView(R.id.date_text) public TextView mDateText;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.more_post_view);
		Views.inject(this);

		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_bg));

		mPost = (Post)getIntent().getExtras().getParcelable(Constants.EXTRA_POST);
		if (mPost == null) finish();
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(this);
		DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);

		findViewById(R.id.link_container).setOnLongClickListener(this);

		mPostedBy.setText("@" + mPost.getPoster().getMentionName() + " (" + mPost.getPoster().getUserName() + ")");
		mStarsText.setText("" + mPost.getStarCount());
		mRepostsText.setText("" + mPost.getRepostCount());
		mRepliesText.setText("" + mPost.getReplyCount());
		mTimezoneText.setText(mPost.getTimeZone());
		mClientText.setText(mPost.getClientName());
		mLinkText.setText(mPost.getCanonicalUrl());
		mDateText.setText(dateFormat.format(new Date(mPost.getDate())) + " " + timeFormat.format(new Date(mPost.getDate())));
	}

	@Override public void onClick(View v)
	{
		if (v.getId() == R.id.cancel)
		{
			finish();
		}
		else if (v.getId() == R.id.user_container)
		{
			// open profile
			Intent intent = new Intent(this, ProfileActivity.class);
			intent.putExtra(Constants.EXTRA_USER, mPost.getPoster());
			startActivityForResult(intent, Constants.REQUEST_PROFILE);
		}
		else if (v.getId() == R.id.client_container)
		{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(mPost.getClientLink()));
			startActivity(intent);
		}
		else if (v.getId() == R.id.link_container)
		{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(mPost.getCanonicalUrl()));
			startActivity(intent);
		}
		else if (v.getId() == R.id.repost_container)
		{
			if (mPost.getRepostCount() > 0)
			{
				showReposters();
			}
		}
		else if (v.getId() == R.id.stars_container)
		{
			if (mPost.getStarCount() > 0)
			{
				showStarredBy();
			}
		}
		else if (v.getId() == R.id.replies_container)
		{
			Intent in = new Intent(this, ThreadActivity.class);
			in.putExtra(Constants.EXTRA_POST, mPost);
			startActivity(in);
		}
	}

	@Override public boolean onLongClick(View v)
	{
		if (v.getId() == R.id.link_container)
		{
			DialogBuilder.create(getContext())
			.setTitle(getString(R.string.pick_option))
			.setItems(new CharSequence[]{getString(R.string.open_url), getString(R.string.copy_url), getString(R.string.share_url)}, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					if (which == 0)
					{
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(Uri.parse(mPost.getCanonicalUrl()));
						startActivity(intent);
					}
					else if (which == 1)
					{
						if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
						{
							android.text.ClipboardManager clipboard = (android.text.ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
							clipboard.setText(mPost.getCanonicalUrl());
						}
						else
						{
							android.content.ClipboardManager clipboard = (android.content.ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
							android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", mPost.getCanonicalUrl());
							clipboard.setPrimaryClip(clip);
						}

						Toast.makeText(getContext(), getString(R.string.copy_url_success), Toast.LENGTH_SHORT).show();
					}
					else if (which == 2)
					{
						Intent shareIntent = new Intent(Intent.ACTION_SEND);
						shareIntent.putExtra(Intent.EXTRA_TEXT, mPost.getCanonicalUrl());
						shareIntent.setType("text/plain");
						startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)));
					}
				}
			})
			.show();

			return true;
		}

		return false;
	}

	public void showReposters()
	{
		final ArrayList<SimpleUser> loadedUsers = new ArrayList<SimpleUser>();
		for (SimpleUser u : mPost.getReposters())
		{
			if (u != null)
			{
				loadedUsers.add(u);
			}
		}

		DialogBuilder.create(getContext())
			.setTitle(R.string.reposted_by)
			.setAdapter(new AccountAdapter(getContext(), R.layout.user_dialog_list_item, loadedUsers), new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
					profileIntent.putExtra(Constants.EXTRA_USER, loadedUsers.get(which));
					getContext().startActivity(profileIntent);
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.close, null)
		.show();
	}

	public void showStarredBy()
	{
		final ArrayList<SimpleUser> loadedUsers = new ArrayList<SimpleUser>();
		for (SimpleUser u : mPost.getStarrers())
		{
			if (u != null)
			{
				loadedUsers.add(u);
			}
		}

		DialogBuilder.create(getContext())
			.setTitle(R.string.starred_by)
			.setAdapter(new AccountAdapter(getContext(), R.layout.user_dialog_list_item, loadedUsers), new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
					profileIntent.putExtra(Constants.EXTRA_USER, loadedUsers.get(which));
					getContext().startActivity(profileIntent);
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.close, null)
		.show();
	}
}