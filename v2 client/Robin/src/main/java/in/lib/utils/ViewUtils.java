/**
 * @brief x util is the utility library which includes the method extentions for common data types
 *
 * @author Callum Taylor
**/
package in.lib.utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;

/**
 * @brief Utilities for doing operations on Views
 */
public class ViewUtils
{
	/**
	 * Gets all views of a parent that match an instance (recursive)
	 * @param parent The parent view
	 * @param instance The instance to check
	 * @return An array of views
	 */
	public static ArrayList<View> getAllChildrenByInstance(ViewGroup parent, Class instance)
	{
		ArrayList<View> views = new ArrayList<View>();
		int childCount = parent.getChildCount();

		for (int childIndex = 0; childIndex < childCount; childIndex++)
		{
			View child = parent.getChildAt(childIndex);

			if (child instanceof ViewGroup)
			{
				views.addAll(getAllChildrenByInstance((ViewGroup)child, instance));
			}
			else
			{
				if (instance.isInstance(child))
				{
					views.add(child);
				}
			}
		}

		return views;
	}

	/**
	 * Gets the first parent view with a tag matching the id.
	 * Note: the tag stored in the view must <b>not</b> be null
	 * @param tagId The tag id to match
	 * @return The found view, or null
	 */
	public static View getParentWithTag(int tagId, View child)
	{
		View parent = child;
		while ((parent = (View)parent.getParent()) != null)
		{
			if (parent.getTag(tagId) != null)
			{
				return parent;
			}
		}

		return null;
	}

	/**
	 * Gets the first parent view with an id matching the id.
	 *
	 * @param id The id to match
	 * @return The found view, or null
	 */
	public static View getParentWithId(int id, View child)
	{
		View found = null;
		ViewParent parent = child.getParent();
		while (parent != null)
		{
			if (parent instanceof View)
			{
				if (((View)parent).getId() == id)
				{
					found = (View)parent;
				}

				parent = parent.getParent();
			}
			else
			{
				break;
			}
		}

		return found;
	}

	/**
	 * Gets the first child it finds in a parent matched from an instance (recursive)
	 * @param parent The parent view
	 * @param instance The instance to check
	 * @return The found view, or null
	 */
	public static View getFirstChildByInstance(ViewGroup parent, Class instance)
	{
		View retView = null;
		int childCount = parent.getChildCount();

		for (int childIndex = 0; childIndex < childCount; childIndex++)
		{
			View child = parent.getChildAt(childIndex);

			if (instance.isAssignableFrom(child.getClass()))
			{
				return child;
			}

			if (child instanceof ViewGroup)
			{
				View v = getFirstChildByInstance((ViewGroup)child, instance);

				if (v != null)
				{
					return v;
				}
			}
		}

		return retView;
	}
}