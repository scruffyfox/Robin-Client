package in.lib.view;

import in.model.SimpleUser;
import in.model.base.NetObject;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.MultiAutoCompleteTextView;

public class AutoSuggestView extends MultiAutoCompleteTextView
{
	public AutoSuggestView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	/* Overriding this method and returning String type solves the problem */
	@Override protected CharSequence convertSelectionToString(Object selectedItem)
	{
		NetObject item = (NetObject)selectedItem;
		return (item instanceof SimpleUser) ? "@" + ((SimpleUser)item).getMentionName() : item.getFilterTag().toString();
	}
}