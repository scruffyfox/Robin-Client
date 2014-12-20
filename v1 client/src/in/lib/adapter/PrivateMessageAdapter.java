package in.lib.adapter;

import in.lib.Constants;
import in.lib.adapter.base.MessageAdapter;
import in.lib.holder.CenterPrivateMessageHolder;
import in.lib.holder.PrivateMessageHolder;
import in.lib.manager.UserManager;
import in.model.PrivateMessage;
import in.obj.entity.MentionEntity;
import in.rob.client.R;
import in.rob.client.dialog.DeleteMessageDialog;
import in.rob.client.dialog.ReplyMessageDialog;

import java.util.List;

import lombok.Getter;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

public class PrivateMessageAdapter extends MessageAdapter
{
	@Getter private final int postLayoutResource, mentionLayoutResource, centerLayoutResource;

	public PrivateMessageAdapter(Context context, List<PrivateMessage> items, PrivateMessage centralMessage)
	{
		this(context, items, centralMessage, Order.DESC);
	}

	public PrivateMessageAdapter(Context context, List<PrivateMessage> items)
	{
		this(context, items, null, Order.DESC);
	}

	public PrivateMessageAdapter(Context context, List<PrivateMessage> items, PrivateMessage centralMessage, Order order)
	{
		super(context, items);

		this.postLayoutResource = R.layout.message_list_item;
		this.mentionLayoutResource = R.layout.message_list_item_mention;
		this.centerLayoutResource = R.layout.message_list_item_center;
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		int viewType = getItemViewType(position);

		PrivateMessage message = (PrivateMessage)getItem(position);
		PrivateMessageHolder currentHolder;

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
				convertView = getLayoutInflater().inflate(this.centerLayoutResource, parent, false);
			}

			if (viewType == TYPE_CENTER)
			{
				currentHolder = new CenterPrivateMessageHolder(convertView);
				((CenterPrivateMessageHolder)currentHolder).userContainer.setOnClickListener(this);
			}
			else
			{
				currentHolder = new PrivateMessageHolder(convertView);
			}

			convertView.setTag(R.id.TAG_VIEW_HOLDER, currentHolder);

			// set the button listeners for options
			currentHolder.replyButton.setOnClickListener(this);
			currentHolder.replyAllButton.setOnClickListener(this);
			currentHolder.shareButton.setOnClickListener(this);
			currentHolder.deleteButton.setOnClickListener(this);
			currentHolder.moreButton.setOnClickListener(this);
			currentHolder.avatar.setOnClickListener(this);
			currentHolder.avatar.setOnLongClickListener(this);
		}
		else
		{
			currentHolder = (PrivateMessageHolder)convertView.getTag(R.id.TAG_VIEW_HOLDER);
		}

		// set defaults
		currentHolder.replyAllButton.setVisibility(View.VISIBLE);
		currentHolder.optionsContainer.setVisibility(View.GONE);

		if (viewType == TYPE_STANDARD || viewType == TYPE_MENTION)
		{
			currentHolder.populate(message, this);
			currentHolder.media.setOnClickListener(this);
			currentHolder.media.setOnLongClickListener(this);
			currentHolder.media.setTag(position);
		}
		else if (viewType == TYPE_CENTER)
		{
			((CenterPrivateMessageHolder)currentHolder).populate(message, this);
			currentHolder.locationImage.setTag(position);
			currentHolder.locationImage.setOnClickListener(this);
			currentHolder.text.setTag(R.id.TAG_ENTITY, message.getAnnotations());
			currentHolder.media.setOnClickListener(this);
			currentHolder.media.setOnLongClickListener(this);
			currentHolder.media.setTag(position);

			((CenterPrivateMessageHolder)currentHolder).userContainer.setTag(position);
		}

		currentHolder.avatar.setTag(position);
		currentHolder.deleteButton.setTag(position);
		currentHolder.shareButton.setTag(position);
		currentHolder.moreButton.setTag(position);
		currentHolder.replyButton.setTag(position);
		currentHolder.replyAllButton.setTag(position);

		super.getView(position, convertView, parent);
		return convertView;
	}

	@Override public void onClick(View v)
	{
		if (v.getTag() == null) return;
		final PrivateMessage post = (PrivateMessage)getItem((Integer)v.getTag());

		if (v.getId() == R.id.delete)
		{
			Intent intent = new Intent(getContext(), DeleteMessageDialog.class);
			intent.putExtra(Constants.EXTRA_MESSAGE, post);
			getContext().startActivity(intent);
			return;
		}
		else if (v.getId() == R.id.reply)
		{
			Intent inReply = new Intent(getContext(), ReplyMessageDialog.class);
			inReply.putExtra(Constants.EXTRA_CHANNEL_ID, post.getChannelId());
			inReply.putExtra(Constants.EXTRA_REPLY_TO, post);
			getContext().startActivity(inReply);
			return;
		}
		else if (v.getId() == R.id.reply_all)
		{
			// get post participants
			String myUserId = UserManager.getUserId();
			String participants = "@" + post.getPoster().getMentionName() + " ";
			for (MentionEntity mention : post.getMentions())
			{
				if (!participants.contains(mention.getName()) && !mention.getName().equals(UserManager.getUser().getMentionName()))
				{
					participants += "@" + mention.getName() + " ";
				}
			}

			Intent in = new Intent(getContext(), ReplyMessageDialog.class);
			in.putExtra(Constants.EXTRA_CHANNEL_ID, post.getChannelId());
			in.putExtra(Constants.EXTRA_REPLY_TO, post);
			in.putExtra(Constants.EXTRA_REPLY_TO_EXTRA, participants);
			getContext().startActivity(in);
			return;
		}

		super.onClick(v);
	}
}