package in.view.delegate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import in.controller.adapter.base.RobinAdapter;
import in.lib.Constants;
import in.lib.builder.DialogBuilder;
import in.lib.utils.ViewUtils;
import in.model.DraftPost;
import in.model.base.Draft;
import in.rob.client.R;
import in.rob.client.dialog.NewPostDialog;
import in.view.delegate.base.AdapterDelegate;
import in.view.holder.DraftPostHolder;

public class DraftPostDelegate extends AdapterDelegate<Draft> implements OnClickListener
{
	public DraftPostDelegate(RobinAdapter<Draft> adapter)
	{
		super(adapter);
	}

	public int getLayout()
	{
		return R.layout.draft_post_view;
	}

	@Override public View getView(Draft item, int position, View convertView, ViewGroup parent, LayoutInflater inflater)
	{
		DraftPostHolder holder;
		if (convertView == null)
		{
			convertView = inflater.inflate(getLayout(), parent, false);

			holder = new DraftPostHolder(convertView);
			holder.getSend().setOnClickListener(this);
			holder.getEdit().setOnClickListener(this);
			holder.getDuplicate().setOnClickListener(this);
			holder.getDelete().setOnClickListener(this);

			convertView.setTag(holder);
		}
		else
		{
			holder = (DraftPostHolder)convertView.getTag();
		}

		convertView.setTag(R.id.TAG_POSITION, position);
		holder.populate((DraftPost)item);

		return convertView;
	}

	@Override public void onClick(View view)
	{
		final int position = (Integer)ViewUtils.getParentWithId(R.id.root_view, view).getTag(R.id.TAG_POSITION);
		final Draft item = getAdapter().getItem(position);

		if (view.getId() == R.id.send)
		{
			Intent postIntent = new Intent(view.getContext(), NewPostDialog.class);
			postIntent.putExtra(Constants.EXTRA_DRAFT_POST, (Parcelable)item);
			postIntent.putExtra(Constants.EXTRA_SEND, true);
			view.getContext().startActivity(postIntent);

			// TODO: Send event
		}
		else if (view.getId() == R.id.edit)
		{
			Intent postIntent = new Intent(view.getContext(), NewPostDialog.class);
			postIntent.putExtra(Constants.EXTRA_DRAFT_POST, (Parcelable)item);
			view.getContext().startActivity(postIntent);
		}
		else if (view.getId() == R.id.duplicate)
		{
			DraftPost post = (DraftPost)item;
			post.setDate(System.currentTimeMillis());
			post.save();
			post = null;

			// TODO: Send event
		}
		else if (view.getId() == R.id.delete)
		{
			AlertDialog.Builder builder = DialogBuilder.create(view.getContext());
			builder.setTitle(R.string.confirm);
			builder.setMessage(R.string.confirm_delete);
			builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					item.delete();

					// TODO: Send event
				}
			});
			builder.setNegativeButton(R.string.no, null);
			builder.setNeutralButton(R.string.cancel, null);
			builder.show();
		}
	}
}
