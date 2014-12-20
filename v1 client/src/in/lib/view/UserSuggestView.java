package in.lib.view;

import in.model.SimpleUser;
import lombok.Getter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;

public class UserSuggestView extends AutoCompleteTextView implements OnItemClickListener
{
	@Getter private SimpleUser selectedUser;
	private OnItemClickListener listener;

	public UserSuggestView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setOnItemClickListener(this);
	}

	@Override public void setOnItemClickListener(OnItemClickListener l)
	{
		if (l == this)
		{
			super.setOnItemClickListener(l);
		}
		else
		{
			listener = l;
		}
	}

	@Override public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		if (listener != null)
		{
			listener.onItemClick(arg0, arg1, arg2, arg3);
		}

		selectedUser = (SimpleUser)getAdapter().getItem(arg2);
	}

	/* Overriding this method and returning String type solves the problem */
	@Override protected CharSequence convertSelectionToString(Object selectedItem)
	{
		SimpleUser selectedUser = (SimpleUser)selectedItem;
		return "@" + selectedUser.getMentionName();
	}
}