package in.controller.adapter;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import in.controller.adapter.base.RobinAdapter;
import in.data.stream.base.Stream;
import in.model.Post;
import in.view.delegate.PostHiddenDelegate;
import in.view.delegate.ThreadPostCollapsedDelegate;
import in.view.delegate.ThreadPostDelegate;
import in.view.delegate.ThreadPostSelectedDelegate;
import lombok.Getter;
import lombok.Setter;

public class ThreadAdapter extends RobinAdapter<Post>
{
	public enum Mode
	{
		STANDARD,
		NESTED; // TODO: Add support for replies to deleted posts
	}

	public static final int TYPE_POST = 0;
	public static final int TYPE_POST_SELECTED = 1;
	public static final int TYPE_POST_COLLAPSED_HEADER = 2;
	public static final int TYPE_POST_COLLAPSED_HIDDEN = 3;

	@Getter @Setter private Mode mode = Mode.STANDARD;
	@Getter @Setter private Post selectedPost;

	@Getter @Setter private HashMap<String, Integer> indentSpec;
	@Getter private HashMap<String, ArrayList<String>> collapsedReference = new HashMap<String, ArrayList<String>>();

	public ThreadAdapter(Context context)
	{
		super(context);

		getItemTypes().put(TYPE_POST, new ThreadPostDelegate(this));
		getItemTypes().put(TYPE_POST_SELECTED, new ThreadPostSelectedDelegate(this));
		getItemTypes().put(TYPE_POST_COLLAPSED_HEADER, new ThreadPostCollapsedDelegate(this));
		getItemTypes().put(TYPE_POST_COLLAPSED_HIDDEN, new PostHiddenDelegate(this));
	}

	@Override public void setStream(Stream<Post> stream)
	{
		if (mode == Mode.STANDARD)
		{
			// ensure that the stream is in date order with newest first
			Collections.sort(stream.getItems(), new Comparator<Post>()
			{
				@Override public int compare(Post lhs, Post rhs)
				{
					return lhs.getDate() == rhs.getDate() ? 0 : (lhs.getDate() < rhs.getDate() ? 1 : -1);
				}
			});

			super.setStream(stream);
		}
		else
		{
			// calculate the post replies into a map based on their originalId
			ArrayList<Post> postsList = new ArrayList<Post>(stream.getItems().size());
			indentSpec = new HashMap<String, Integer>(stream.getItems().size());

			for (int streamIndex = stream.getItems().size() - 1; streamIndex > -1; streamIndex--)
			{
				Post post = stream.getItems().get(streamIndex);
				int insertIndex = postsList.indexOf(post);

				if (post.getReplyTo() == null)
				{
					indentSpec.put(post.getOriginalId(), 1);
				}

				if (insertIndex < 0)
				{
					postsList.add(post);
					insertIndex = postsList.size();
				}

				if (post.getReplyCount() > 0)
				{
					// Loop through each post in our list and find that post's replies
					ArrayList<Post> replies = new ArrayList<Post>();
					for (int i = stream.getItems().size() - 1; i > -1; i--)
					{
						Post toMatch = stream.getItems().get(i);
						if (toMatch == null || toMatch.getReplyTo() == null || toMatch.equals(post)) continue;

						if (toMatch.getReplyTo().equals(post.getOriginalId()) && !postsList.contains(toMatch))
						{
							replies.add(toMatch);
							int intIndent = indentSpec.get(toMatch.getReplyTo()) == null ? -1 : indentSpec.get(toMatch.getReplyTo());
							indentSpec.put(toMatch.getOriginalId(), intIndent + 1);
						}
					}

					postsList.addAll(Math.min(insertIndex + 1, postsList.size()), replies);
				}
			}

			stream.getItems().clear();

			for (int index = postsList.size() - 1; index > -1; index--)
			{
				stream.getItems().add(postsList.get(index));
			}

			super.setStream(stream);
		}
	}

	@Override public Post getItem(int position)
	{
		return getStream().getItems().get(getCount() - 1 - position);
	}

	@Override public int getItemViewType(int position)
	{
		Post item = getItem(position);

		if (mode == Mode.STANDARD)
		{
			if (getCount() == 1 || item.equals(selectedPost))
			{
				return TYPE_POST_SELECTED;
			}
		}
		else
		{
			Iterator<String> iterator = collapsedReference.keySet().iterator();
			while (iterator.hasNext())
			{
				String key = iterator.next();
				if (collapsedReference.get(key).contains(item.getOriginalId()))
				{
					return TYPE_POST_COLLAPSED_HIDDEN;
				}
			}

			if (collapsedReference.containsKey(item.getOriginalId()))
			{
				return TYPE_POST_COLLAPSED_HEADER;
			}
		}

		return TYPE_POST;
	}
}
