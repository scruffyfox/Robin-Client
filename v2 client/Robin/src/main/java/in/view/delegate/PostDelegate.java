package in.view.delegate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

import java.util.Random;

import in.controller.adapter.base.RobinAdapter;
import in.controller.handler.PostDialogResponseHandler;
import in.lib.Constants;
import in.lib.manager.APIManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.ViewUtils;
import in.model.Post;
import in.rob.client.R;
import in.rob.client.dialog.DeletePostDialog;
import in.rob.client.dialog.QuotePostDialog;
import in.rob.client.dialog.ReplyPostDialog;
import in.view.delegate.base.AdapterDelegate;
import in.view.holder.PostHolder;

public class PostDelegate extends AdapterDelegate<Post> implements OnClickListener
{
	public PostDelegate(RobinAdapter<Post> adapter)
	{
		super(adapter);
	}

	public int getLayout()
	{
		return R.layout.post_view;
	}

	@Override public View getView(Post item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		PostHolder holder;
		if (convertView == null)
		{
			convertView = inflater.inflate(getLayout(), parent, false);

			holder = new PostHolder(convertView);
			holder.getReply().setOnClickListener(this);
			holder.getReplyAll().setOnClickListener(this);
			holder.getRepost().setOnClickListener(this);
			holder.getShare().setOnClickListener(this);
			holder.getMore().setOnClickListener(this);

			convertView.setTag(holder);
		}
		else
		{
			holder = (PostHolder)convertView.getTag();
		}

		convertView.setTag(R.id.TAG_POSITION, position);
		holder.populate(item);

		return convertView;
	}

	@Override public void onClick(final View v)
	{
		final int position = (Integer)ViewUtils.getParentWithId(R.id.root_view, v).getTag(R.id.TAG_POSITION);
		final Post item = getAdapter().getItem(position);

		if (v.getId() == R.id.reply || v.getId() == R.id.reply_all)
		{
			Intent replyIntent = new Intent(v.getContext(), ReplyPostDialog.class);
			replyIntent.putExtra(Constants.EXTRA_POST, (Parcelable)item);
			replyIntent.putExtra(Constants.EXTRA_REPLY_ALL, v.getId() == R.id.reply_all);
			v.getContext().startActivity(replyIntent);
		}
		else if (v.getId() == R.id.repost)
		{
			onRepostClick(v, item);
		}
		else if (v.getId() == R.id.share)
		{
			String originalText = item.getPostText().getText();
			String shareText = originalText + " via @" + item.getPoster().getUsername() + " " + item.getCanonicalUrl();
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
			shareIntent.setType("text/plain");
			v.getContext().startActivity(Intent.createChooser(shareIntent, v.getContext().getString(R.string.share_via)));
		}
		else if (v.getId() == R.id.more)
		{
			onMoreClick(v, item);
		}
	}

	protected void onMoreClick(final View view, final Post item)
	{
		final PopupMenu options = new PopupMenu(view.getContext(), view);
		options.getMenuInflater().inflate(R.menu.menu_post_more, options.getMenu());

		if (item.isMention())
		{
			options.getMenu().findItem(R.id.menu_mute).setVisible(true);
		}

		if (item.getHasReplies())
		{
			options.getMenu().findItem(R.id.menu_collapse).setVisible(true);
		}

		if (item.getPoster().equals(UserManager.getInstance().getUser())
		|| UserManager.getInstance().getLinkedUserIds().contains(item.getPoster().getId())
		|| item.getReposters().contains(UserManager.getInstance().getUser()))
		{
			options.getMenu().findItem(R.id.menu_delete).setVisible(true);
		}

		options.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			@Override public boolean onMenuItemClick(MenuItem menuItem)
			{
				if (menuItem.getItemId() == R.id.menu_collapse)
				{
					SettingsManager.getInstance().collapseThread(item.getThreadId());
					getAdapter().notifyDataSetChanged();
				}
				else if (menuItem.getItemId() == R.id.menu_delete)
				{
					Intent deletePost = new Intent(view.getContext(), DeletePostDialog.class);
					deletePost.putExtra(Constants.EXTRA_POST, item);
					view.getContext().startActivity(deletePost);
				}
				else if (menuItem.getItemId() == R.id.menu_translate)
				{
					String originalText = item.getPostText().getText();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse("http://translate.google.com/?q=" + Uri.encode(originalText)));
					view.getContext().startActivity(intent);
				}
				else if (menuItem.getItemId() == R.id.menu_copy_text)
				{
					String originalText = item.getPostText().getText();
					android.content.ClipboardManager clipboard = (android.content.ClipboardManager)view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
					android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", originalText);
					clipboard.setPrimaryClip(clip);

					Toast.makeText(view.getContext(), view.getContext().getString(R.string.copy_text_success), Toast.LENGTH_SHORT).show();
				}

				options.dismiss();
				return true;
			}
		});

		options.show();
	}

	protected void onRepostClick(final View v, final Post item)
	{
		final PopupMenu options = new PopupMenu(v.getContext(), v);

		if (!item.getPoster().equals(UserManager.getInstance().getUser())
		&& !UserManager.getInstance().getLinkedUserIds().contains(item.getPoster().getId())
		&& !item.getReposters().contains(UserManager.getInstance().getUser()))
		{
			options.getMenu().add(0, 0, 0, "Repost");
		}

		options.getMenu().add(0, 1, 0, "Quote");
		options.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			@Override public boolean onMenuItemClick(MenuItem menuItem)
			{
				if (menuItem.getItemId() == 0)
				{
					APIManager.getInstance().postRepost(item.getId(), new PostDialogResponseHandler(v.getContext(), new Random().nextInt())
					{
						@Override public String getNotificationTitle()
						{
							return getContext().getString(R.string.reposting_title);
						}

						@Override public String getNotificationText()
						{
							return getContext().getString(R.string.reposting);
						}

						@Override public String getNotificationFinishText()
						{
							return getContext().getString(R.string.repost_success);
						}
					});
				}
				else if (menuItem.getItemId() == 1)
				{
					Intent quoteIntent = new Intent(v.getContext(), QuotePostDialog.class);
					quoteIntent.putExtra(Constants.EXTRA_POST, (Parcelable)item);
					v.getContext().startActivity(quoteIntent);
				}

				options.dismiss();
				return true;
			}
		});

		options.show();
	}

	@Override public boolean onItemLongClick(int position, View view)
	{
		toggleOptions(position, view);
		return true;
	}

	public void toggleOptions(int position, View view)
	{
		Post item = getAdapter().getItem(position);
		PostHolder holder = (PostHolder)view.getTag();

		int count = getAdapter().getListView().getChildCount();
		for (int index = 0; index < count; index++)
		{
			View options;
			if ((options = getAdapter().getListView().getChildAt(index).findViewById(R.id.options_container)) != null)
			{
				options.setVisibility(View.GONE);
			}
		}

		int visibility = view.findViewById(R.id.options_container).getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
		view.findViewById(R.id.options_container).setVisibility(visibility);
	}
}
