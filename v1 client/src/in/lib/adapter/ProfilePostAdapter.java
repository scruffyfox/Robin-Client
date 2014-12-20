package in.lib.adapter;

import in.model.Post;
import in.rob.client.R;

import java.util.List;

import lombok.Setter;
import android.content.Context;
import android.view.View;

/**
 * Extends post adapter so we can override what to do when the user clicks on
 * something I.E avatar
 */
public class ProfilePostAdapter extends PostAdapter
{
	@Setter private String userId;

	public ProfilePostAdapter(Context context, int resource, List<Post> objects, String userId)
	{
		super(context, objects);
		this.userId = userId;
	}

	@Override public void onClick(View v)
	{
		if (v.getId() == R.id.avatar)
		{
			// retrieve user id
			Post p = (Post)getItem((Integer)v.getTag());

			if (!this.userId.equals(p.getPoster().getId()))
			{
				super.onClick(v);
			}
		}
		else
		{
			super.onClick(v);
		}
	}

	@Override public boolean isEmpty()
	{
		return false;
	}
}