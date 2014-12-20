package in.lib.view;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.MovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

import in.data.Text;
import in.data.entity.HashEntity;
import in.data.entity.LinkEntity;
import in.data.entity.MentionEntity;
import in.data.entity.StyledEntity;
import in.data.entity.StyledEntity.Type;
import in.lib.manager.SettingsManager;
import in.lib.view.spannable.HashTagClickableSpan;
import in.lib.view.spannable.LinkClickableSpan;
import in.lib.view.spannable.MentionClickableSpan;
import in.lib.view.spannable.NotUnderlinedClickableSpan;
import in.rob.client.R;
import lombok.Setter;

public class LinkedTextView extends TextView implements OnLongClickListener
{
	@Setter private boolean linkHit = false;

	public LinkedTextView(Context context)
	{
		super(context);
	}

	public LinkedTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public void setText(Text text)
	{
		if (text != null && text.getText() != null)
		{
			int maxLength = text.getText().length();
			SpannableStringBuilder stringBuilder = new SpannableStringBuilder(text.getText());

			if (text.getMentions() != null)
			{
				for (MentionEntity mention : text.getMentions())
				{
					stringBuilder.setSpan(new MentionClickableSpan(mention), mention.getPos(), Math.min(maxLength, mention.getPos() + mention.getLength()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}

			if (text.getLinks() != null)
			{
				for (LinkEntity link : text.getLinks())
				{
					stringBuilder.setSpan(new LinkClickableSpan(link), link.getPos(), Math.min(maxLength, link.getPos() + link.getLength()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}

			if (text.getHashTags() != null)
			{
				for (HashEntity tag : text.getHashTags())
				{
					stringBuilder.setSpan(new HashTagClickableSpan(tag), tag.getPos(), Math.min(maxLength, tag.getPos() + tag.getLength()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}

			if (text.getEmphasisStyles() != null)
			{
				for (StyledEntity entity : text.getEmphasisStyles())
				{
					int len = Math.min(maxLength, entity.getPos() + entity.getLength());
					CharacterStyle span = null;

					if (entity.getType() == Type.ITALIC)
					{
						String oldStr = text.getText().substring(entity.getPos() + 1, len - 1);
						stringBuilder.replace(entity.getPos(), len, String.format("\u200B%s\u200B", oldStr));
						span = new StyleSpan(Typeface.ITALIC);
					}

					if (entity.getType() == Type.BOLD)
					{
						String oldStr = text.getText().substring(entity.getPos() + 2, len - 2);
						stringBuilder.replace(entity.getPos(), len, String.format("\u200B\u200B%s\u200B\u200B", oldStr));
						span = new StyleSpan(Typeface.BOLD);
					}

					if (entity.getType() == Type.UNDERLINE)
					{
						String oldStr = text.getText().substring(entity.getPos() + 1, len - 1);
						stringBuilder.replace(entity.getPos(), len, String.format("\u200B%s\u200B", oldStr));
						span = new UnderlineSpan();
					}

					if (span != null)
					{
						stringBuilder.setSpan(span, entity.getPos(), len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
				}
			}

			setText(stringBuilder);
		}
	}

	@Override public boolean onTouchEvent(MotionEvent event)
	{
		linkHit = false;
		boolean res = super.onTouchEvent(event);

		if (!linkHit)
		{
			return false;
		}

		return res;
	}

	/**
	 * Will set the link movement method based on the current settings set for Single Click Options
	 */
	public void setLinkMovementMethod()
	{
		if (SettingsManager.getInstance().getSingleClickBit() > 0)
		{
			setLinkMovementMethod(LinkTouchMovementMethod.getInstance());
		}
//		else
//		{
//			setLinkMovementMethod(getDefaultMovementMethod());
//		}
	}

	public void setLinkMovementMethod(MovementMethod movement)
	{
		setMovementMethod(movement);
		setCustomSelectionActionModeCallback(new Callback()
		{
			@Override public boolean onCreateActionMode(ActionMode mode, Menu menu)
			{
				return true;
			}

			@Override public boolean onPrepareActionMode(ActionMode mode, Menu menu)
			{
				return false;
			}

			@Override public boolean onActionItemClicked(ActionMode mode, MenuItem item)
			{
				if (item.getItemId() == android.R.id.copy)
				{
					Toast.makeText(getContext(), getContext().getString(R.string.copy_text_success), Toast.LENGTH_SHORT).show();
				}

				return false;
			}

			@Override public void onDestroyActionMode(ActionMode mode)
			{
				setSelected(false);
				clearFocus();

				MotionEvent m = MotionEvent.obtain(0, System.currentTimeMillis(), MotionEvent.ACTION_UP, 0f, 0f, 0);
				dispatchTouchEvent(m);
			}
		});

		if (movement != null)
		{
			setOnLongClickListener(this);
		}
		else
		{
			setOnLongClickListener(null);
		}
	}

	@Override public boolean onLongClick(View v)
	{
		NotUnderlinedClickableSpan[] spans = ((Spannable)getText()).getSpans(getSelectionStart(), getSelectionEnd(), NotUnderlinedClickableSpan.class);

		if (spans.length > 0)
		{
			NotUnderlinedClickableSpan span = spans[0];
			span.onLongClick(v);

			return true;
		}

		return false;
	}
}
