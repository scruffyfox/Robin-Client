package in.view.delegate;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;

import java.util.ArrayList;

import in.controller.adapter.ThreadAdapter;
import in.controller.adapter.ThreadAdapter.Mode;
import in.controller.adapter.base.RobinAdapter;
import in.lib.utils.Debug;
import in.lib.utils.StringUtils;
import in.model.Post;
import in.rob.client.R;
import in.view.holder.ThreadPostHolder;

public class ThreadPostDelegate extends PostDelegate
{
	private static final int MAX_INDENT = 8;

	public ThreadPostDelegate(RobinAdapter<Post> adapter)
	{
		super(adapter);
	}

	public int getLayout()
	{
		return R.layout.thread_post_view;
	}

	@Override public View getView(Post item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		ThreadPostHolder holder;
		if (convertView == null)
		{
			convertView = inflater.inflate(getLayout(), parent, false);

			holder = new ThreadPostHolder(convertView);
			holder.getReply().setOnClickListener(this);
			holder.getReplyAll().setOnClickListener(this);
			holder.getRepost().setOnClickListener(this);
			holder.getMore().setOnClickListener(this);

			convertView.setTag(holder);
		}
		else
		{
			holder = (ThreadPostHolder)convertView.getTag();
		}

		convertView.setTag(R.id.TAG_POSITION, position);
		holder.populate(item);

		if (((ThreadAdapter)getAdapter()).getMode() == Mode.NESTED)
		{
			setIndentation(convertView, item);
		}
		else
		{
			((ViewGroup)convertView).getChildAt(0).setVisibility(View.GONE);
		}

		return convertView;
	}

	@Override public boolean onItemLongClick(int position, View view)
	{
		if (((ThreadAdapter)getAdapter()).getMode() == Mode.NESTED)
		{
			String id = getAdapter().getItem(position).getOriginalId();
			int indent = ((ThreadAdapter)getAdapter()).getIndentSpec().get(id);

			for (int index = position, count = getAdapter().getCount(); index < count; index++)
			{
				String childId = getAdapter().getItem(index).getOriginalId();
				int childIndent = ((ThreadAdapter)getAdapter()).getIndentSpec().get(childId);

				if (childIndent <= indent && index != position)
				{
					break;
				}

				if (index == position)
				{
					((ThreadAdapter)getAdapter()).getCollapsedReference().put(id, new ArrayList<String>());
				}
				else
				{
					((ThreadAdapter)getAdapter()).getCollapsedReference().get(id).add(childId);
				}
			}

			getAdapter().notifyDataSetChanged();
			return true;
		}
		else
		{
			getAdapter().getListView().getOnItemClickListener().onItemClick(getAdapter().getListView(), view, position, getAdapter().getItemId(position));
			return true;
		}
	}

	public void setIndentation(View convertView, Post item)
	{
		((LayoutParams)((ViewGroup)convertView).getChildAt(0).getLayoutParams()).width = item.getReplyTo() == null ? 0 : convertView.getContext().getResources().getDimensionPixelSize(R.dimen.nested_post_indent_width);

		if (((ThreadAdapter)getAdapter()).getIndentSpec() != null)
		{
			Integer indent = ((ThreadAdapter)getAdapter()).getIndentSpec().get(item.getOriginalId());
			indent = indent == null ? 0 : indent;
			indent = Math.min(MAX_INDENT, indent - 2);
			((LayoutParams)((ViewGroup)convertView).getChildAt(0).getLayoutParams()).leftMargin = Math.max(0, (indent * convertView.getContext().getResources().getDimensionPixelSize(R.dimen.nested_post_indent_margin)));

			int colourHash = item.getReplyTo() == null ? item.getOriginalId().hashCode() : item.getReplyTo().hashCode();
			colourHash = colourHash >= 0xffffffff ? colourHash / 2 : colourHash;

			try
			{
				int color = 0x7F000000;

				if (indent < MAX_INDENT)
				{
					String colourStr = Integer.toHexString(colourHash);

					if (colourStr.length() > 8)
					{
						colourStr = colourStr.substring(0, 7);
					}
					else if (colourStr.length() < 8)
					{
						colourStr = StringUtils.padTo(colourStr, 8, "F");
					}

					color = Color.parseColor("#" + colourStr);
					color = (0x7F << 24) | (color & 0x00ffffff);
				}

				((ViewGroup)convertView).getChildAt(0).setBackgroundColor(color);
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		((ViewGroup)convertView).getChildAt(0).setVisibility(View.VISIBLE);
	}
}
