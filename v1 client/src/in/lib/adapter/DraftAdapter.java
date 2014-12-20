package in.lib.adapter;

import in.lib.Constants;
import in.lib.adapter.base.RobinAdapter;
import in.lib.helper.AnimationHelper;
import in.lib.holder.DraftPostHolder;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.lib.utils.Dimension;
import in.model.DraftPost;
import in.rob.client.R;
import in.rob.client.dialog.NewPostDialog;
import in.rob.client.dialog.base.DialogBuilder;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DraftAdapter extends RobinAdapter
{
	protected final int TYPE_STANDARD = 0;

	@Getter private final Context context;
	@Getter private final Dimension dimension;
	@Getter private final LayoutInflater layoutInflater;

	@Getter @Setter private int lastPositionAnimated = 5;

	public DraftAdapter(Context context, List<DraftPost> items)
	{
		this(context, items, Order.DESC);
	}

	public DraftAdapter(Context context, List<DraftPost> items, Order order)
	{
		super(context, items);

		this.context = context;
		this.dimension = new Dimension(context);
		this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.setOrder(order);
		this.setItems(items);
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		DraftPost post = (DraftPost)getItem(position);
		DraftPostHolder currentHolder;

		if (convertView == null)
		{
			convertView = getLayoutInflater().inflate(R.layout.draft_post_list_item, parent, false);
			currentHolder = new DraftPostHolder(convertView);
			convertView.setTag(R.id.TAG_VIEW_HOLDER, currentHolder);

			currentHolder.editButton.setOnClickListener(this);
			currentHolder.duplicateButton.setOnClickListener(this);
			currentHolder.sendButton.setOnClickListener(this);
			currentHolder.deleteButton.setOnClickListener(this);
		}
		else
		{
			currentHolder = (DraftPostHolder)convertView.getTag(R.id.TAG_VIEW_HOLDER);
		}

		currentHolder.populate(post, this);
		currentHolder.media.setOnClickListener(this);
		currentHolder.media.setTag(position);

		currentHolder.editButton.setTag(position);
		currentHolder.duplicateButton.setTag(position);
		currentHolder.sendButton.setTag(position);
		currentHolder.deleteButton.setTag(position);

		/**
		 * Set the animation if it hasn't been played
		 */
		if ((lastPositionAnimated >= 0 && lastPositionAnimated < position) && (isAnimationsEnabled() && SettingsManager.isListAnimationEnabled()))
		{
			AnimationHelper.slideUp(convertView);
		}

		if (position > lastPositionAnimated)
		{
			lastPositionAnimated = position;
		}

		super.getView(position, convertView, parent);
		return convertView;
	}

	@Override public void onClick(final View v)
	{
		if (v.getTag() == null) return;
		final DraftPost post = (DraftPost)getItem((Integer)v.getTag());

		if (v.getId() == R.id.send)
		{
			Intent sendIntent = new Intent(getContext(), NewPostDialog.class);
			sendIntent.putExtra(Constants.EXTRA_NEW_POST_DRAFT, post.serialize());
			sendIntent.putExtra(Constants.EXTRA_RESEND, true);
			getContext().startActivity(sendIntent);
		}
		else if (v.getId() == R.id.edit)
		{
			Intent editIntent = new Intent(getContext(), NewPostDialog.class);
			editIntent.putExtra(Constants.EXTRA_NEW_POST_DRAFT, post.serialize());
			getContext().startActivity(editIntent);
		}
		else if (v.getId() == R.id.duplicate)
		{
			DraftPost duplicate = new DraftPost();
			duplicate.setAnnotations(post.getAnnotations());
			duplicate.setChannelId(post.getChannelId());
			duplicate.setDate(System.currentTimeMillis());
			duplicate.setImagePath(post.getImagePath());
			duplicate.setPostText(post.getPostText());
			duplicate.setReplyId(post.getReplyId());
			duplicate.setSelectedAccountId(post.getSelectedAccountId());
			prependItem(duplicate);
			notifyDataSetChanged();

			CacheManager.getInstance().writeFile(String.format(Constants.CACHE_DRAFT_POST, duplicate.getSelectedAccountId(), duplicate.getDate()), duplicate);
		}
		else if (v.getId() == R.id.delete)
		{
			DialogBuilder.create(getContext())
				.setTitle(R.string.confirm)
				.setMessage(R.string.confirm_delete)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						removeItem(post);
						notifyDataSetChanged();
						CacheManager.getInstance().removeFile(String.format(Constants.CACHE_DRAFT_POST, post.getSelectedAccountId(), post.getDate()));
					}
				})
				.setNegativeButton(R.string.no, null)
			.show();
		}
	}
}