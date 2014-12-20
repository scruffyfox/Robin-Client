package in.lib.adapter.base;

import in.lib.Constants;
import in.lib.URLMatcher;
import in.lib.helper.AnimationHelper;
import in.lib.helper.DownloadHelper;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.Dimension;
import in.lib.view.AvatarView;
import in.lib.view.LinkifiedTextView;
import in.model.User;
import in.model.base.Message;
import in.model.base.NetObject;
import in.obj.annotation.Annotation;
import in.obj.annotation.Annotation.Type;
import in.obj.annotation.ImageAnnotation;
import in.obj.annotation.LinkAnnotation;
import in.obj.annotation.LocationAnnotation;
import in.obj.annotation.RichAnnotation;
import in.obj.annotation.VideoAnnotation;
import in.obj.entity.MentionEntity;
import in.rob.client.ProfileActivity;
import in.rob.client.R;
import in.rob.client.dialog.ImageLightboxDialog;
import in.rob.client.dialog.ReplyPostDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;

public abstract class MessageAdapter<T extends Message> extends RobinAdapter implements OnLongClickListener
{
	protected final int TYPE_STANDARD = 0;
	protected final int TYPE_MENTION = 1;
	protected final int TYPE_CENTER = 2;
	protected final int TYPE_HIDDEN = 3;

	@Getter private final Context context;
	@Getter private final Dimension dimension;
	@Getter private final LayoutInflater layoutInflater;
	@Getter private T center;
	@Getter @Setter private boolean showMuted = false;

	@Getter private int centerPosition = Integer.MIN_VALUE;
	@Getter private View currentOptionsView;
	@Getter @Setter private Boolean resetBreak = false;

	public MessageAdapter(Context context, List<T> items, T centralPost)
	{
		this(context, items, centralPost, Order.DESC);
	}

	public MessageAdapter(Context context, List<T> items)
	{
		this(context, items, null, Order.DESC);
	}

	public MessageAdapter(Context context, List<T> items, T central, Order order)
	{
		super(context, items);

		this.context = context;
		this.center = central;
		this.dimension = new Dimension(context);
		this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.setOrder(order);
		this.setItems(items);
	}

	@Override public void setItems(List<? extends NetObject> items)
	{
		super.setItems(items);
		setCenterPosition();
	}

	@Override public void prependItems(List<? extends NetObject> items)
	{
		for (NetObject p : getItems())
		{
			((Message)p).setDateStr(((Message)p).calculateDateString());
		}

		super.prependItems(items);
	}

	@Override public void addItems(List<? extends NetObject> items)
	{
		for (NetObject p : getItems())
		{
			((Message)p).setDateStr(((Message)p).calculateDateString());
		}

		super.addItems(items);
	}

	@Override public void addItem(int position, NetObject item)
	{
		super.addItem(position, item);
		setCenterPosition();
	}

	@Override public View getView(int position, View convertView, ViewGroup parent)
	{
		super.getView(position, convertView, parent);

		/**
		 * Set the animation if it hasn't been played
		 */
		if ((getLastPositionAnimated() >= 0 && getLastPositionAnimated() < position) && (isAnimationsEnabled() && SettingsManager.isListAnimationEnabled()))
		{
			if (center == null || position > centerPosition)
			{
				AnimationHelper.slideUp(convertView);
			}
			else if (position < centerPosition)
			{
				AnimationHelper.slideDown(convertView);
			}
		}

		if (position > getLastPositionAnimated())
		{
			setLastPositionAnimated(position);
		}

		return convertView;
	}

	public void resetBreak()
	{
		resetBreak = true;
	}

	@Override public int getViewTypeCount()
	{
		return 4;
	}

	@Override public int getItemViewType(int position)
	{
		Message message = (Message)getItem(position);
		if (message == null) return 0;

		if (center != null && isCentral(message))
		{
			return TYPE_CENTER;
		}

		if ((!showMuted && message.isMuted()) || message.isMachinePost())
		{
			return TYPE_HIDDEN;
		}

		if (message.isMention())
		{
			return TYPE_MENTION;
		}
		else
		{
			return TYPE_STANDARD;
		}
	}

	@Override public void onClick(final View v)
	{
		if (v.getTag() == null) return;
		final Message post = (Message)getItem((Integer)v.getTag());

		if (v.getId() == R.id.share)
		{
			String originalText = Html.fromHtml(post.getFormattedText()).toString();
			String shareText = originalText + " via @" + post.getPoster().getMentionName();
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
			shareIntent.setType("text/plain");
			context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_via)));
		}
		else if (v.getId() == R.id.avatar || v.getId() == R.id.user_container)
		{
			Intent intent = new Intent(context, ProfileActivity.class);
			intent.putExtra(Constants.EXTRA_USER, post.getPoster());
			context.startActivity(intent);
		}
		else if (v.getId() == R.id.reply)
		{
			Intent inReply = new Intent(context, ReplyPostDialog.class);
			inReply.putExtra(Constants.EXTRA_POST_ID, post.getId());
			inReply.putExtra(Constants.EXTRA_REPLY_TO_EXTRA, post.getPoster().getMentionName());
			context.startActivity(inReply);
		}
		else if (v.getId() == R.id.reply_all)
		{
			String participants = "@" + post.getPoster().getMentionName() + " ";
			for (MentionEntity mention : post.getMentions())
			{
				if (!participants.contains(mention.getName()) && !mention.getName().equals(UserManager.getUser().getMentionName()))
				{
					participants += "@" + mention.getName() + " ";
				}
			}

			participants = participants.trim();
			Intent in = new Intent(context, ReplyPostDialog.class);
			in.putExtra(Constants.EXTRA_POST_ID, post.getId());
			in.putExtra(Constants.EXTRA_TEXT, participants);
			in.putExtra(Constants.EXTRA_TITLE, getContext().getString(R.string.reply_post));
			context.startActivity(in);
		}
		else if (v.getId() == R.id.media_image)
		{
			Annotation entity = (Annotation)v.getTag(R.id.TAG_ENTITY);

			String url = "";
			Intent intent = null;

			if (entity.getClass().equals(ImageAnnotation.class))
			{
				if (SettingsManager.isImageViewerEnabled())
				{
					int pos = 0;
					ArrayList<String> imageUrl = new ArrayList<String>();
					ArrayList<String> webUrl = new ArrayList<String>();

					for (int index = 0, count = post.getAnnotations().get(Type.IMAGE).size(); index < count; index++)
					{
						String iUrl = ((ImageAnnotation)post.getAnnotations().get(Type.IMAGE).get(index)).getUrl();
						String wUrl = ((ImageAnnotation)post.getAnnotations().get(Type.IMAGE).get(index)).getEmbeddableUrl();

						if (!imageUrl.contains(iUrl) && !webUrl.contains(wUrl))
						{
							imageUrl.add(iUrl);
							webUrl.add(wUrl);

							if (((ImageAnnotation)post.getAnnotations().get(Type.IMAGE).get(index)) == entity)
							{
								pos = index;
							}
						}
					}

					intent = new Intent(getContext(), ImageLightboxDialog.class);
					intent.putExtra(Constants.EXTRA_WEB_URL, webUrl.toArray(new String[webUrl.size()]));
					intent.putExtra(Constants.EXTRA_PREVIEW_URL, imageUrl.toArray(new String[imageUrl.size()]));
					intent.putExtra(Constants.EXTRA_IMAGE_POSITION, pos);
				}
			}
			else if (SettingsManager.isLightboxEnabled())
			{
				if (entity.getClass().equals(LinkAnnotation.class))
				{
					url = ((LinkAnnotation)entity).getUrl();
					intent = new Intent(getContext(), URLMatcher.class);
					intent.setData(Uri.parse(url));
				}
				else if (entity.getClass().equals(VideoAnnotation.class))
				{
					url = ((VideoAnnotation)entity).getUrl();
					intent = new Intent(getContext(), URLMatcher.class);

					if (!TextUtils.isEmpty(((VideoAnnotation)entity).getEmbeddableUrl()))
					{
						url = ((VideoAnnotation)entity).getEmbeddableUrl();
					}

					intent.setData(Uri.parse(url));
				}
				else if (entity.getClass().equals(RichAnnotation.class))
				{
					url = ((RichAnnotation)entity).getEmbeddableUrl();
					intent = new Intent(getContext(), URLMatcher.class);
					intent.setData(Uri.parse(url));
				}
				else
				{
					url = entity.getPreviewUrl();
					intent = new Intent(getContext(), URLMatcher.class);
					intent.setData(Uri.parse(url));
				}

				if (url.toLowerCase(Locale.US).endsWith("gif"))
				{
					intent = new Intent(getContext(), URLMatcher.class);
					intent.setData(Uri.parse(url));
				}

				intent.putExtra(Constants.EXTRA_PREVIEW_URL, url);
			}

			if (intent == null)
			{
				if (entity.getClass().equals(ImageAnnotation.class))
				{
					url = ((ImageAnnotation)entity).getUrl();
					if (!TextUtils.isEmpty(((ImageAnnotation)entity).getEmbeddableUrl()))
					{
						url = ((ImageAnnotation)entity).getEmbeddableUrl();
					}
				}
				else if (entity.getClass().equals(VideoAnnotation.class) || entity.getClass().equals(LinkAnnotation.class))
				{
					url = ((VideoAnnotation)entity).getUrl();
				}
				else
				{
					url = entity.getPreviewUrl();
				}

				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
			}

			getContext().startActivity(intent);
		}
		else if (v.getId() == R.id.location_image)
		{
			try
			{
				Annotation entity = (Annotation)v.getTag(R.id.TAG_ENTITY);
				LocationAnnotation loc = (LocationAnnotation)entity;
				String url = "geo:" + loc.getLat() + "," + loc.getLng() + "?z=15";
				Intent browserIntent = new Intent(Intent.ACTION_VIEW);
				browserIntent.setData(Uri.parse(url));
				getContext().startActivity(Intent.createChooser(browserIntent, getContext().getString(R.string.pick_option)));
			}
			catch (Exception e)
			{

			}
		}
	}

	public boolean isCentral(NetObject post)
	{
		return (center != null && (center.getId().equals(post.getId()) || center == post));
	}

	public void setCenter(T p)
	{
		center = p;
		setCenterPosition();
	}

	private void setCenterPosition()
	{
		if (center != null)
		{
			int itemSize = getCount();
			for (int i = 0; i < itemSize; i++)
			{
				if (isCentral(getItem(i)))
				{
					centerPosition = i;
					break;
				}
			}
		}
	}

	@Override public boolean onItemLongClick(AdapterView<?> arg0, View v, int position, long arg3)
	{
		// weird conflicts with LinkifiedTextView on Jelly Bean
		if (v instanceof LinkifiedTextView)
		{
			return false;
		}

		Message post = (Message)getItem(position - getListView().getHeaderViewsCount());
		if (center != null && !isCentral(post))
		{
			setCenter((T)post);
			notifyDataSetChanged();
		}
		else
		{
			if (!SettingsManager.isInvertPostClick() || (SettingsManager.isInvertPostClick() && v.getTag(R.id.TAG_STOP_STACK_OVERFLOW) != null))
			{
				v.setTag(R.id.TAG_STOP_STACK_OVERFLOW, null);

				currentOptionsView = v.findViewById(R.id.options_container);
				if (currentOptionsView != null)
				{
					if ((currentOptionsView != null && currentOptionsView.getVisibility() == View.VISIBLE))
					{
						currentOptionsView.setVisibility(View.GONE);
					}
					else
					{
						currentOptionsView.setVisibility(View.VISIBLE);

						if (position - getListView().getHeaderViewsCount() > getCount())
						{
							currentOptionsView.setTag(post);
						}
					}
				}
			}
			else
			{
				v.setTag(R.id.TAG_STOP_STACK_OVERFLOW, true);
				getListView().performItemClick(v, position, arg3);
			}
		}

		return true;
	}

	@Override public boolean onLongClick(View v)
	{
		if (v.getId() == R.id.avatar)
		{
			User user = ((Message)getItem((Integer)v.getTag())).getPoster();
			((AvatarView)v).triggerLongPress(user);

			return true;
		}
		else if (v.getId() == R.id.media_image)
		{
			String url = (String)v.getTag(R.id.TAG_IMAGE_URL);
			DownloadHelper.showMediaDownloadPopup(getContext(), url);
			return true;
		}

		return false;
	}
}