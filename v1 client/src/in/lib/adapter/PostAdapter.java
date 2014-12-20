package in.lib.adapter;

import in.lib.Constants;
import in.lib.adapter.base.MessageAdapter;
import in.lib.handler.PostStarResponseHandler;
import in.lib.handler.PostUnStarResponseHandler;
import in.lib.helper.ThemeHelper;
import in.lib.holder.CenterPostHolder;
import in.lib.holder.PostHolder;
import in.lib.manager.APIManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.model.Post;
import in.model.SimpleUser;
import in.model.base.NetObject;
import in.obj.annotation.Annotation;
import in.obj.annotation.Annotation.Type;
import in.obj.annotation.LinkAnnotation;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.dialog.DeletePostDialog;
import in.rob.client.dialog.PostDetailsDialog;
import in.rob.client.dialog.RepostDialog;
import in.rob.client.dialog.base.DialogBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

public class PostAdapter extends MessageAdapter<Post>
{
	@Getter @Setter private boolean ready = true;
	@Getter private final int postLayoutResource, mentionLayoutResource, centerPostLayoutResource, hiddenPostLayoutResource;

	public PostAdapter(Context context, List<Post> items, Post centralPost)
	{
		this(context, items, centralPost, Order.DESC);
	}

	public PostAdapter(Context context, List<Post> items)
	{
		this(context, items, null, Order.DESC);
	}

	public PostAdapter(Context context, List<Post> items, Post centralPost, Order order)
	{
		super(context, items);

		this.postLayoutResource = R.layout.post_list_item;
		this.mentionLayoutResource = R.layout.post_list_item_mention;
		this.centerPostLayoutResource = R.layout.post_list_item_center;
		this.hiddenPostLayoutResource = R.layout.post_list_item_hidden;
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		int viewType = getItemViewType(position);
		Post post = (Post)getItem(position);
		PostHolder currentHolder;

		if (convertView == null)
		{
			if (viewType == TYPE_STANDARD)
			{
				convertView = getLayoutInflater().inflate(this.postLayoutResource, parent, false);
			}
			else if (viewType == TYPE_MENTION)
			{
				convertView = getLayoutInflater().inflate(this.mentionLayoutResource, parent, false);
			}
			else if (viewType == TYPE_CENTER)
			{
				convertView = getLayoutInflater().inflate(this.centerPostLayoutResource, parent, false);
			}
			else if (viewType == TYPE_HIDDEN)
			{
				convertView = getLayoutInflater().inflate(this.hiddenPostLayoutResource, parent, false);
			}

			if (viewType == TYPE_CENTER)
			{
				currentHolder = new CenterPostHolder(convertView);
				((CenterPostHolder)currentHolder).userContainer.setOnClickListener(this);
			}
			else
			{
				currentHolder = new PostHolder(convertView);
			}

			convertView.setTag(R.id.TAG_VIEW_HOLDER, currentHolder);
			convertView.setTag(R.id.TAG_POST_HIDDEN, viewType == TYPE_HIDDEN);

			// set the button listeners for options
			currentHolder.replyButton.setOnClickListener(this);
			currentHolder.replyAllButton.setOnClickListener(this);
			currentHolder.starButton.setOnClickListener(this);
			currentHolder.shareButton.setOnClickListener(this);
			currentHolder.moreButton.setOnClickListener(this);
			currentHolder.repostButton.setOnClickListener(this);
			currentHolder.avatar.setOnClickListener(this);
			currentHolder.avatar.setOnLongClickListener(this);

			if (convertView.findViewById(R.id.missing_posts) != null)
			{
				convertView.findViewById(R.id.missing_posts).setOnClickListener(this);
			}
		}
		else
		{
			currentHolder = (PostHolder)convertView.getTag(R.id.TAG_VIEW_HOLDER);
		}

		// set defaults
		currentHolder.repostButton.setVisibility(View.VISIBLE);
		currentHolder.replyAllButton.setVisibility(View.VISIBLE);
		currentHolder.optionsContainer.setVisibility(View.GONE);

		if (viewType == TYPE_CENTER)
		{
			((CenterPostHolder)currentHolder).populate(post, this, true);
			((CenterPostHolder)currentHolder).locationImage.setTag(position);
			((CenterPostHolder)currentHolder).locationImage.setOnClickListener(this);
			currentHolder.crosspost.setTag(position);
			currentHolder.crosspost.setOnClickListener(this);
			currentHolder.text.setTag(R.id.TAG_ENTITY, post.getAnnotations());
			currentHolder.media.setOnClickListener(this);
			currentHolder.media.setOnLongClickListener(this);
			currentHolder.media.setTag(position);

			((CenterPostHolder)currentHolder).userContainer.setTag(position);
		}
		else if (viewType == TYPE_STANDARD || viewType == TYPE_MENTION)
		{
			currentHolder.populate(post, this, getCenter() != null);
			currentHolder.media.setOnClickListener(this);
			currentHolder.media.setOnLongClickListener(this);
			currentHolder.media.setTag(position);

			if (position == getBreakPosition())
			{
				currentHolder.missingPosts.setTag(position);
				currentHolder.missingPosts.setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.divider).setVisibility(View.GONE);

				if (getResetBreak())
				{
					currentHolder.missingPosts.setTag(R.id.TAG_IS_LOADING, null);
					currentHolder.missingPosts.setTag(R.id.TAG_POST_POSITION, null);
					setResetBreak(false);
				}

				if ((currentHolder.missingPosts.getTag(R.id.TAG_IS_LOADING) != null && (Integer)currentHolder.missingPosts.getTag(R.id.TAG_POST_POSITION) == (Integer)currentHolder.missingPosts.getTag()))
				{
					currentHolder.missingPosts.findViewById(R.id.load_text).setVisibility(View.GONE);
					currentHolder.missingPosts.findViewById(R.id.progress).setVisibility(View.VISIBLE);
				}
				else
				{
					currentHolder.missingPosts.findViewById(R.id.load_text).setVisibility(View.VISIBLE);
					currentHolder.missingPosts.findViewById(R.id.progress).setVisibility(View.GONE);
				}
			}
			else
			{
				currentHolder.missingPosts.setVisibility(View.GONE);
				convertView.findViewById(R.id.divider).setVisibility(View.VISIBLE);
			}

			if (position == getBreakPosition() + 1)
			{
				currentHolder.missingPostsTop.setVisibility(View.VISIBLE);
			}
			else
			{
				currentHolder.missingPostsTop.setVisibility(View.GONE);
			}
		}
		else if (viewType == TYPE_HIDDEN)
		{
			return convertView;
		}

		currentHolder.avatar.setTag(position);
		currentHolder.moreButton.setTag(position);
		currentHolder.replyButton.setTag(position);
		currentHolder.replyAllButton.setTag(position);
		currentHolder.starButton.setTag(position);
		currentHolder.shareButton.setTag(position);
		currentHolder.repostButton.setTag(position);

		super.getView(position, convertView, parent);
		return convertView;
	}

	/**
	 * Gets an item from it's ID
	 * @param id the ID to search for
	 * @return The object, or null
	 */
	public NetObject getItemByOriginalId(String id)
	{
		for (NetObject obj : getStream().getObjects())
		{
			if (((Post)obj).getOriginalId().equals(id))
			{
				return obj;
			}
		}

		return null;
	}

	@Override public void onClick(final View v)
	{
		super.onClick(v);

		if (v.getTag() == null) return;
		final Post post = (Post)getItem((Integer)v.getTag());

		if (v.getId() == R.id.missing_posts && v.getTag(R.id.TAG_IS_LOADING) == null)
		{
			if (onPagerListener != null)
			{
				Post loadFrom = (Post)getItem((Integer)v.getTag() + 1);
				v.setTag(R.id.TAG_IS_LOADING, true);
				v.setTag(R.id.TAG_POST_POSITION, v.getTag());
				v.findViewById(R.id.load_text).setVisibility(View.GONE);
				v.findViewById(R.id.progress).setVisibility(View.VISIBLE);
				onPagerListener.onBreakClicked(loadFrom);
			}
		}

		if (v.getId() == R.id.more)
		{
			final LinkedHashMap<Integer, String> options = new LinkedHashMap<Integer, String>();
			options.put(2, getContext().getString(R.string.details));

			if (post.getAnnotations() != null && post.getAnnotations().containsKey(Type.LINK) && post.getAnnotations().get(Type.LINK).size() > 0)
			{
				options.put(1, getContext().getString(R.string.links));
			}

			if (post.getRepostCount() > 0)
			{
				options.put(5, getContext().getString(R.string.show_reposters));
			}

			if (post.getStarCount() > 0)
			{
				options.put(9, getContext().getString(R.string.show_starred_by));
			}

			options.put(6, getContext().getString(R.string.copy_text));
			options.put(8, getContext().getString(R.string.translate_post));

			if (SettingsManager.isThreadMuted(post.getThreadId()))
			{
				options.put(4, getContext().getString(R.string.unmute_thread));
			}
			else
			{
				options.put(4, getContext().getString(R.string.mute_thread));
			}

			List<String> ids = UserManager.getLinkedUserIds(getContext());
			if (ids.contains(post.getPoster().getId()))
			{
				options.put(3, getContext().getString(R.string.delete));
			}

			if (!post.getPoster().isYou())
			{
				options.put(7, getContext().getString(R.string.report));
			}

			CharSequence[] items = options.values().toArray(new CharSequence[options.size()]);
			DialogBuilder.create(getContext())
				.setIcon(ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_dialog_icon_misc))
				.setTitle(R.string.pick_option)
				.setItems(items, new OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						Integer[] keyset = options.keySet().toArray(new Integer[options.size()]);

						if (keyset[which] == 1)
						{
							List<Annotation> entities = post.getAnnotations().get(Annotation.Type.LINK);
							final CharSequence[] keys = new CharSequence[entities.size()];
							final CharSequence[] links = new CharSequence[entities.size()];

							for (int index = 0; index < links.length; index++)
							{
								keys[index] = ((LinkAnnotation)entities.get(index)).getUrl();
								links[index] = ((LinkAnnotation)entities.get(index)).getUrl();
							}

							if (links.length > 1)
							{
								DialogBuilder.create(getContext())
								.setTitle(getContext().getString(R.string.select_link))
								.setItems(keys, new DialogInterface.OnClickListener()
								{
									@Override public void onClick(DialogInterface dialog, int which)
									{
										Intent browserIntent = new Intent(Intent.ACTION_VIEW);
										browserIntent.setData(Uri.parse(links[which].toString()));
										getContext().startActivity(browserIntent);

										dialog.dismiss();
									}
								}).show();
							}
							else
							{
								Intent browserIntent = new Intent(Intent.ACTION_VIEW);
								browserIntent.setData(Uri.parse(links[0].toString()));
								getContext().startActivity(browserIntent);
							}
						}
						else if (keyset[which] == 2)
						{
							Intent inReply = new Intent(getContext(), PostDetailsDialog.class);
							inReply.putExtra(Constants.EXTRA_POST, post);
							getContext().startActivity(inReply);
						}
						else if (keyset[which] == 3)
						{
							Intent inDelete = new Intent(getContext(), DeletePostDialog.class);
							inDelete.putExtra(Constants.EXTRA_POST, post);
							getContext().startActivity(inDelete);
						}
						else if (keyset[which] == 4)
						{
							if (SettingsManager.isThreadMuted(post.getThreadId()))
							{
								SettingsManager.getInstance(getContext()).unmuteThread(post.getThreadId());
							}
							else
							{
								SettingsManager.getInstance(getContext()).muteThread(post.getThreadId());
							}
						}
						else if (keyset[which] == 5)
						{
							showReposters(post);
						}
						else if (keyset[which] == 6)
						{
							String originalText = Html.fromHtml(post.getFormattedText()).toString();

							if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB)
							{
								android.text.ClipboardManager clipboard = (android.text.ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
								clipboard.setText(originalText);
							}
							else
							{
								android.content.ClipboardManager clipboard = (android.content.ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
								android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", originalText);
								clipboard.setPrimaryClip(clip);
							}

							Toast.makeText(getContext(), getContext().getString(R.string.copy_text_success), Toast.LENGTH_SHORT).show();
						}
						else if (keyset[which] == 7)
						{
							DialogBuilder.create(getContext())
								.setTitle(R.string.confirm)
								.setMessage(R.string.confirm_report)
								.setPositiveButton(R.string.yes, new OnClickListener()
								{
									@Override public void onClick(DialogInterface dialog, int which)
									{
										APIManager.getInstance().report(post.getId(), null);
										Toast.makeText(getContext(), getContext().getString(R.string.post_reported), Toast.LENGTH_LONG).show();
									}
								})
								.setNegativeButton(R.string.no, null)
							.show();
						}
						else if (keyset[which] == 8)
						{
							String originalText = Html.fromHtml(post.getFormattedText()).toString();
							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setData(Uri.parse("http://translate.google.com/?q=" + Uri.encode(originalText)));
							getContext().startActivity(intent);
						}
						else if (keyset[which] == 9)
						{
							showStarredBy(post);
						}

						dialog.dismiss();
					}
				})
				.show();
		}
		else if (v.getId() == R.id.crosspost)
		{
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(post.getCrossPostUrl()));
			getContext().startActivity(intent);
		}
		else if (v.getId() == R.id.repost)
		{
			Intent inRepost = new Intent(getContext(), RepostDialog.class);
			inRepost.putExtra(Constants.EXTRA_REPOST, post);
			getContext().startActivity(inRepost);
		}
		else if (v.getId() == R.id.star)
		{
			APIManager manager = APIManager.getInstance();
			if (post.isStarred())
			{
				post.setStarred(false);
				int unstarred = ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_icon_unstarred);
				((ImageView)v).setImageResource(unstarred);
				manager.unstarPost(post.getId(), new PostUnStarResponseHandler());
			}
			else
			{
				post.setStarred(true);

				int starred = ThemeHelper.getDrawableResource(getContext(), R.attr.rbn_icon_starred);
				((ImageView)v).setImageResource(starred);
				manager.starPost(post.getId(), new PostStarResponseHandler());
			}
		}
	}

	public void showReposters(Post post)
	{
		final ArrayList<SimpleUser> loadedUsers = new ArrayList<SimpleUser>();
		for (SimpleUser u : post.getReposters())
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
					profileIntent.putExtra(Constants.EXTRA_USER_ID, loadedUsers.get(which).getId());
					getContext().startActivity(profileIntent);
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.close, null)
		.show();
	}

	public void showStarredBy(Post post)
	{
		final ArrayList<SimpleUser> loadedUsers = new ArrayList<SimpleUser>();
		for (SimpleUser u : post.getStarrers())
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
					profileIntent.putExtra(Constants.EXTRA_USER_ID, loadedUsers.get(which).getId());
					getContext().startActivity(profileIntent);
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.close, null)
		.show();
	}
}