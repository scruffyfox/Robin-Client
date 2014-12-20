package in.lib.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.MultiAutoCompleteTextView;

import in.data.entity.HashEntity;
import in.model.SimpleUser;

public class AutoSuggestView extends MultiAutoCompleteTextView
{
	public AutoSuggestView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	@Override protected CharSequence convertSelectionToString(Object selectedItem)
	{
		if (selectedItem instanceof SimpleUser)
		{
			SimpleUser item = (SimpleUser)selectedItem;
			return String.format("@%s", ((SimpleUser)item).getUsername());
		}
		else if (selectedItem instanceof HashEntity)
		{
			HashEntity item = (HashEntity)selectedItem;
			return String.format("#%s", ((HashEntity)item).getName());
		}

		return "";
	}
}