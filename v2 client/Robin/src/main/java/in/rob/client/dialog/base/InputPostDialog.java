package in.rob.client.dialog.base;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.data.annotation.FileAnnotation;
import in.data.annotation.LocationAnnotation;
import in.data.entity.Entity;
import in.data.entity.LinkEntity;
import in.lib.Constants;
import in.lib.adapter.AccountAdapter;
import in.lib.adapter.AutoCompleteAdapter;
import in.lib.builder.DialogBuilder;
import in.lib.helper.LocationHelper;
import in.lib.helper.LocationHelper.Accuracy;
import in.lib.helper.LocationHelper.LocationResponse;
import in.lib.manager.CacheManager;
import in.lib.manager.UserManager;
import in.lib.type.TListWrapper;
import in.lib.utils.Debug;
import in.lib.utils.Regex;
import in.lib.utils.SpaceTokenizer;
import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.utils.Views.OnClick;
import in.lib.view.AutoSuggestView;
import in.lib.view.AvatarImageView;
import in.lib.view.LinkedAutoCompleteTextView;
import in.lib.view.LinkedAutoCompleteTextView.OnSpannableClickedListener;
import in.lib.view.spannable.MarkDownClickableSpan;
import in.model.AdnModel;
import in.model.SimpleUser;
import in.model.User;
import in.model.base.Draft;
import in.rob.client.AuthenticationActivity;
import in.rob.client.R;
import in.rob.client.StorageBrowserActivity;
import lombok.Getter;
import lombok.Setter;

@Injectable
public abstract class InputPostDialog extends PostDialog
{
	@Getter @InjectView private View positiveButton;
	@Getter @InjectView private View negativeButton;
	@Getter @InjectView private TextView postInput;
	@Getter @InjectView private TextView counter;
	@Getter @InjectView private LinearLayout imageContainer;
	@Getter @InjectView private ImageButton actionImage;
	@Getter @InjectView private ImageButton actionLocation;
	@Getter @InjectView private AvatarImageView actionAccount;
	@InjectView private TextView title;

	@Getter private LocationHelper locationHelper;
	@Getter @Setter private Draft draft;
	@Getter @Setter private int maxChars = 256;
	private boolean finish = false;

	@Getter private AutoCompleteAdapter adapter;
	private Uri tempImagePath;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		initialiseDraft();

		super.onCreate(savedInstanceState);
		Views.inject(this);

		locationHelper = new LocationHelper(getContext());
		initialiseDialog();
	}

	@Override public int getContentView()
	{
		return R.layout.post_dialog;
	}

	@Override public void onBackPressed()
	{
		onNegativeButtonClick(negativeButton);
	}

	@Override public void setTitle(int res)
	{
		this.title.setText(res);
	}

	@Override public void setTitle(CharSequence title)
	{
		this.title.setText(title);
	}

	/**
	 * Override this method to create the draft
	 */
	public abstract void initialiseDraft();

	public void initialiseDialog()
	{
		final Pattern mdLinkMatcher = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
		final TextWatcher textWatcher = new TextWatcher()
		{
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after){}

			@Override public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				Matcher matcher = mdLinkMatcher.matcher(s);
				while (matcher.find())
				{
					String match = matcher.group();
					String link = matcher.group(2);
					String anchor = matcher.group(1).trim();

					if (Regex.REGEX_URL.matcher(link).find())
					{
						int pos = s.toString().indexOf(match);

						SpannableStringBuilder newLink = new SpannableStringBuilder(anchor);
						newLink.setSpan(new MarkDownClickableSpan(link, anchor), 0, anchor.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

						SpannableStringBuilder newText = new SpannableStringBuilder(s);

						newText.replace(pos, pos + match.length(), newLink);
						newText.append(' ');

						getPostInput().setText(newText);
						((EditText)getPostInput()).setSelection(getPostInput().getText().length());
					}
				}
			}

			@Override public void afterTextChanged(Editable s)
			{
				MarkDownClickableSpan[] links = s.getSpans(0, s.length(), MarkDownClickableSpan.class);

				for (MarkDownClickableSpan l : links)
				{
					int spanStart = s.getSpanStart(l);
					int spanEnd = s.getSpanEnd(l);
					int anchorLen = l.getAnchor().length();
					int newLen = spanEnd - spanStart;

					if (newLen < anchorLen)
					{
						String spanText = s.subSequence(spanStart, spanEnd).toString().substring(0, newLen);
						s.removeSpan(l);

						if (l.getAnchor().startsWith(spanText))
						{
							s.replace(spanStart, spanStart + spanText.length(), "[" + l.getAnchor() + "](" + l.getUrl());
						}
					}
				}

				int remaining = maxChars - getPostInput().getText().toString().trim().length();
				setRemainingChars(remaining);
			}
		};

		getPostInput().addTextChangedListener(textWatcher);

		if (getPostInput() instanceof AutoSuggestView)
		{
			List<SimpleUser> items = new ArrayList<SimpleUser>();

			adapter = new AutoCompleteAdapter(getContext(), items);
			((AutoSuggestView)getPostInput()).setAdapter(adapter);
			((AutoSuggestView)getPostInput()).setTokenizer(new SpaceTokenizer());
			((AutoSuggestView)getPostInput()).setThreshold(2);
			((AutoSuggestView)getPostInput()).setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			((AutoSuggestView)getPostInput()).addTextChangedListener(textWatcher);

			TListWrapper usernames = CacheManager.getInstance().readFile(Constants.CACHE_AUTOCOMPLETE_USERNAMES, TListWrapper.class);
			TListWrapper hashtags = CacheManager.getInstance().readFile(Constants.CACHE_AUTOCOMPLETE_HASHTAGS, TListWrapper.class);

			List<AdnModel> newList = new ArrayList<AdnModel>(usernames.getList());
			newList.addAll(hashtags.getList());

			adapter.setItems(newList);
			adapter.notifyDataSetChanged();
		}

		if (draft != null)
		{
			getPostInput().setText(draft.getPostText());
			setLinkData();

			if (getPostInput() instanceof LinkedAutoCompleteTextView)
			{
				((LinkedAutoCompleteTextView)getPostInput()).setOnSpannableClickedListener(new OnSpannableClickedListener()
				{
					@Override public void onSpannableClicked(ClickableSpan spannable)
					{
						if (spannable instanceof MarkDownClickableSpan)
						{
							handleAnchoredLink((MarkDownClickableSpan)spannable);
						}
					}
				});
			}
			
			((EditText)getPostInput()).setSelection(getPostInput().getText().length());
		}

		if (counter != null)
		{
			setRemainingChars(maxChars - getPostInput().getText().length());
		}

		//if (UserManager.getInstance().getLinkedUserIds().size() > 1 && getDraft() != null)
		{
			User user = UserManager.getInstance().getUser();

			if (getDraft() != null)
			{
				if (TextUtils.isEmpty(getDraft().getSelectedAccountId()))
				{
					getDraft().setSelectedAccountId(user.getId());
				}
				else
				{
					user = User.load(getDraft().getSelectedAccountId());
				}
			}
			
			actionAccount.setUser(user);
			actionAccount.setVisibility(View.VISIBLE);
		}
	}

	public void updateDraft()
	{
		if (draft != null)
		{
			if (getPostInput() instanceof EditText)
			{
				Spannable text = ((EditText)getPostInput()).getText();
				MarkDownClickableSpan[] links = text.getSpans(0, text.length(), MarkDownClickableSpan.class);
				getDraft().getLinkEntities().clear();

				for (MarkDownClickableSpan linkSpan : links)
				{
					int start = text.getSpanStart(linkSpan);
					int end = text.getSpanEnd(linkSpan);

					LinkEntity link = new LinkEntity();
					link.setPos(start);
					link.setLength(end - start);
					link.setUrl(linkSpan.getUrl());

					if (!linkSpan.getUrl().equals(linkSpan.getAnchor()))
					{
						try
						{
							link.setAmendedLength(link.getLength() + Uri.parse(linkSpan.getUrl()).getHost().length() + 3);
						}
						catch (Exception e)
						{
							Debug.out(e);
						}
					}

					getDraft().getLinkEntities().add(link);
				}
			}

			getDraft().setPostText(getPostInput().getText().toString());
		}
	}

	public void setLinkData()
	{
		if (getPostInput() instanceof EditText)
		{
			SpannableStringBuilder newText = new SpannableStringBuilder(getPostInput().getText());

			if (getDraft().getLinkEntities() != null)
			{
				for (Entity entity : getDraft().getLinkEntities())
				{
					LinkEntity link = (LinkEntity)entity;

					if (link.getAmendedLength() > -1)
					{
						String anchor = newText.toString().substring(link.getPos(), link.getPos() + link.getLength());
						SpannableStringBuilder newLink = new SpannableStringBuilder(anchor);
						newLink.setSpan(new MarkDownClickableSpan(link.getUrl(), anchor), 0, anchor.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						newText.replace(link.getPos(), link.getPos() + link.getLength(), newLink);
					}
					else
					{
						String anchor = newText.toString().substring(link.getPos(), link.getPos() + link.getLength());
						SpannableStringBuilder newLink = new SpannableStringBuilder(anchor);
						newLink.setSpan(new MarkDownClickableSpan(link.getUrl(), anchor), 0, link.getLength(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						newText.replace(link.getPos(), link.getPos() + link.getLength(), newLink);
					}
				}

				getPostInput().setText(newText);
			}
		}
	}

	public void setRemainingChars(int remaining)
	{
		counter.setText(String.valueOf(remaining));

		if (remaining >= 0)
		{
			positiveButton.setVisibility(View.VISIBLE);

			int colour = 0xCCCCCC;
			int progress = 0xFF - (int)(((1f / (float)maxChars) * (float)remaining) * 0xFF);
			progress = Math.max(0xCC, progress);
			colour = (progress << 16) | (colour & 0x00FFFF);
			colour = (0xFF << 24) | (colour & 0x00FFFFFF);

			counter.setTextColor(Math.min(0xFFFFCCCC, colour));
		}
		else
		{
			positiveButton.setVisibility(View.INVISIBLE);
		}
	}

	public void handleAnchoredLink(final MarkDownClickableSpan spannable)
	{
		final View view = LayoutInflater.from(getContext()).inflate(R.layout.markdown_link_edit_layout, null, false);

		if (spannable != null)
		{
			((TextView)view.findViewById(R.id.url_anchor)).setText(spannable.getAnchor());
			((TextView)view.findViewById(R.id.url)).setText(spannable.getUrl());
			((EditText)view.findViewById(R.id.url_anchor)).setSelection(spannable.getAnchor().length());
			((EditText)view.findViewById(R.id.url)).setSelection(spannable.getUrl().length());
		}

		final AlertDialog d = DialogBuilder.create(getContext())
			.setTitle(spannable == null ? R.string.add_link : R.string.edit_link)
			.setView(view)
			.setPositiveButton(R.string.done, null)
			.setNegativeButton(R.string.close, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					((EditText)getPostInput()).setSelection(getPostInput().getText().length());
				}
			})
			.create();
		d.show();

		Button button = d.getButton(AlertDialog.BUTTON_POSITIVE);
		if (button != null)
		{
			button.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					String anchor = ((TextView)view.findViewById(R.id.url_anchor)).getText().toString().trim();
					String url = ((TextView)view.findViewById(R.id.url)).getText().toString().trim();

					if (!Regex.REGEX_URL.matcher(url).find())
					{
						Toast.makeText(getContext(), R.string.invalid_url, Toast.LENGTH_LONG).show();
						return;
					}

					SpannableStringBuilder newText = new SpannableStringBuilder(getPostInput().getText());
					SpannableStringBuilder newLink = new SpannableStringBuilder(anchor);
					newLink.setSpan(new MarkDownClickableSpan(url, anchor), 0, anchor.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

					if (spannable == null)
					{
						if (newText.length() > 0 && newText.charAt(newText.length() - 1) != ' ')
						{
							newText.append(' ');
						}

						newText.append(newLink).append(' ');
					}
					else
					{
						int start = ((EditText)getPostInput()).getText().getSpanStart(spannable);
						int end = ((EditText)getPostInput()).getText().getSpanEnd(spannable);

						newText.removeSpan(spannable);
						newText.replace(start, end, newLink);
					}

					((EditText)getPostInput()).setText(newText);
					((EditText)getPostInput()).setSelection(getPostInput().getText().length());

					d.dismiss();
				}
			});
		}
	}

	public void addImage(final Uri contentUri)
	{
		if (!getDraft().getImages().contains(contentUri.toString()))
		{
			getDraft().getImages().add(contentUri.toString());
			addImageToView(contentUri.toString());
		}
	}

	protected void addImageToView(final String imageUrl)
	{
		View view = LayoutInflater.from(getContext()).inflate(R.layout.post_image_stub, imageContainer, false);
		ImageView image = (ImageView)view.findViewById(R.id.image);
		ImageLoader.getInstance().displayImage(imageUrl, image);

		imageContainer.addView(view);
		imageContainer.post(new Runnable()
		{
			@Override public void run()
			{
				((HorizontalScrollView)imageContainer.getParent()).smoothScrollTo(imageContainer.getMeasuredWidth(), 0);
			}
		});

		final int position = imageContainer.getChildCount() - 1;
		view.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				new Builder(getContext())
					.setTitle(R.string.confirm)
					.setMessage(R.string.remove_image)
					.setPositiveButton(R.string.yes, new OnClickListener()
					{
						@Override public void onClick(DialogInterface dialog, int which)
						{
							getDraft().getImages().remove(imageUrl);
							imageContainer.removeViewAt(position);

							if (imageContainer.getChildCount() == 0)
							{
								((View)imageContainer.getParent()).setVisibility(View.GONE);
							}
						}
					})
					.setNegativeButton(R.string.no, null)
				.show();
			}
		});

		((View)imageContainer.getParent()).setVisibility(View.VISIBLE);
	}

	@Override public void onPositiveButtonClick(View view)
	{
		updateDraft();
		finish();
	}

	@Override public void onNegativeButtonClick(View view)
	{
		updateDraft();

		if (finish || TextUtils.isEmpty(getDraft().getPostText()))
		{
			finish();
			return;
		}

		AlertDialog.Builder builder = DialogBuilder.create(getContext());
		builder.setTitle(R.string.confirm);
		builder.setMessage(R.string.save_to_drafts);
		builder.setPositiveButton(R.string.yes, new OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				finish = true;
				getDraft().save();
				onNegativeButtonClick(null);
			}
		});
		builder.setNegativeButton(R.string.no, new OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				finish = true;
				onNegativeButtonClick(null);
			}
		});
		builder.setNeutralButton(R.string.cancel, null);
		builder.show();
	}

	public int getAnnotationIndexOfLocation()
	{
		for (int index = 0, count = getDraft().getAnnotations().size(); index < count; index++)
		{
			if (getDraft().getAnnotations().get(index) instanceof LocationAnnotation)
			{
				return index;
			}
		}

		return -1;
	}

	@OnClick public void onActionLinkClick(View view)
	{
		handleAnchoredLink(null);
	}

	@OnClick public void onActionMoreClick(View view)
	{
		final PopupMenu options = new PopupMenu(this, view);
		options.getMenuInflater().inflate(R.menu.menu_post_dialog, options.getMenu());

		options.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			@Override public boolean onMenuItemClick(MenuItem menuItem)
			{
				if (menuItem.getItemId() == R.id.menu_at)
				{
					getPostInput().append(" @");
				}
				else if (menuItem.getItemId() == R.id.menu_hash)
				{
					getPostInput().append(" #");
				}

				options.dismiss();
				return true;
			}
		});

		options.show();
	}

	@OnClick public void onActionAccountClick(View view)
	{
		List<String> users = UserManager.getInstance().getLinkedUserIds();
		final ArrayList<User> loadedUsers = new ArrayList<User>();

		for (int index = 0; index < users.size(); index++)
		{
			User u = new User().load(users.get(index));

			if (u != null)
			{
				loadedUsers.add(u);
			}
		}

		DialogBuilder.create(getContext())
			.setTitle(getString(R.string.select_account))
			.setAdapter(new AccountAdapter(getContext(), loadedUsers), new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					getDraft().setSelectedAccountId(loadedUsers.get(which).getId());
					actionAccount.setUser(loadedUsers.get(which));
				}
			})
			.setPositiveButton(R.string.add_account, new OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					Intent loginIntent = new Intent(getContext(), AuthenticationActivity.class);
					loginIntent.putExtra(Constants.EXTRA_NEW_USER, true);
					loginIntent.putExtra(Constants.EXTRA_FINISH, true);
					startActivityForResult(loginIntent, Constants.REQUEST_ADD_ACCOUNT);
					dialog.dismiss();
				}
			})
			.setNegativeButton(R.string.close, null)
		.show();
	}

	@OnClick public void onActionLocationClick(ImageView view)
	{
		int index = -1;
		if ((index = getAnnotationIndexOfLocation()) > -1)
		{
			getDraft().getAnnotations().remove(index);
			return;
		}

		getLocationHelper().fetchLocation(10000, Accuracy.FINE, new LocationResponse()
		{
			@Override public void onRequest()
			{
				Toast.makeText(getContext(), R.string.fetching_location, Toast.LENGTH_SHORT).show();
			}

			@Override public void onLocationAcquired(Location l)
			{
				Toast.makeText(getContext(), R.string.location_aquired, Toast.LENGTH_SHORT).show();

				// remove any current location annotations
				int index = -1;
				if ((index = getAnnotationIndexOfLocation()) > -1)
				{
					getDraft().getAnnotations().remove(index);
				}

				LocationAnnotation location = new LocationAnnotation(l);
				getDraft().getAnnotations().add(location);
			}

			@Override public void onTimeout()
			{
				Location cached = getLocationHelper().getCachedLocation();
				if (cached != null)
				{
					onLocationAcquired(cached);
				}
				else
				{
					Toast.makeText(getContext(), R.string.location_failed, Toast.LENGTH_SHORT).show();
				}
			}

			@Override public void onLocationFailed(String message, int messageId)
			{
				onTimeout();
			}
		});
	}

	@OnClick public void onActionImageClick(ImageView view)
	{
		CharSequence[] items = getResources().getStringArray(R.array.image_choice);

		AlertDialog.Builder builder = DialogBuilder.create(getContext());
		builder.setItems(items, new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				// Camera
				if (which == 0)
				{
					File photo = null;
					Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
					if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
					{
						String folder = Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM) + "/Camera/";
						if (!new File(folder).exists())
						{
							new File(folder).mkdirs();
						}

						photo = new File(folder, "robin_" + System.currentTimeMillis() + ".jpg");
					}
					else
					{
						photo = new File(getCacheDir(), "robin_" + System.currentTimeMillis() + ".jpg");
					}

					if (photo != null)
					{
						tempImagePath = Uri.fromFile(photo);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImagePath);

						try
						{
							startActivityForResult(intent, Constants.REQUEST_CAMERA);
						}
						catch (Exception e)
						{
							Toast.makeText(getContext(), R.string.camera_failed, Toast.LENGTH_SHORT).show();
						}
					}
				}
				// Gallery
				else if (which == 1)
				{
					if (Build.VERSION.SDK_INT < 19)
					{
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						intent.setType("image/jpeg");
						startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), Constants.REQUEST_GALLERY);
					}
					else
					{
						Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
						intent.addCategory(Intent.CATEGORY_OPENABLE);
						intent.setType("image/jpeg");
						startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), Constants.REQUEST_GALLERY);
					}
				}
				// App.net storage
				else if (which == 2)
				{
					Intent browser = new Intent(getContext(), StorageBrowserActivity.class);
					startActivityForResult(browser, Constants.REQUEST_STORAGE);
				}
				// Last taken photo
				else if (which == 3)
				{
					String[] projection = new String[]{MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DATE_TAKEN};
					Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

					if (cursor.moveToFirst())
					{
						String imageLocation = cursor.getString(0);
						File imageFile = new File(imageLocation);
						addImage(Uri.fromFile(imageFile));
						cursor.close();
					}
					else
					{
						Toast.makeText(getContext(), R.string.image_not_found, Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		builder.setTitle(getString(R.string.please_select));
		builder.show();
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK)
		{
			if (requestCode == Constants.REQUEST_CAMERA)
			{
				Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				mediaScanIntent.setData(tempImagePath);
				sendBroadcast(mediaScanIntent);

				addImage(tempImagePath);
			}
			else if (requestCode == Constants.REQUEST_GALLERY)
			{
				if (data != null)
				{
					Uri selectedUri = data.getData();
					if (Build.VERSION.SDK_INT >= 19)
					{
						final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
						getContentResolver().takePersistableUriPermission(selectedUri, takeFlags);
					}

					addImage(selectedUri);
				}
			}
			else if (requestCode == Constants.REQUEST_STORAGE)
			{
				FileAnnotation file = (FileAnnotation)data.getParcelableExtra(Constants.EXTRA_FILE);
				getDraft().getAnnotations().add(file);

				addImageToView(file.getThumbUrl());
			}
			else if (requestCode == Constants.REQUEST_ADD_ACCOUNT)
			{
				onActionAccountClick(actionAccount);
			}
		}
	}
}
