package in.rob.client.dialog.base;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.Regex;
import in.lib.SpaceTokenizer;
import in.lib.adapter.AccountAdapter;
import in.lib.adapter.AutoCompleteAdapter;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.helper.LocationHelper;
import in.lib.helper.LocationHelper.Accuracy;
import in.lib.helper.LocationHelper.LocationResponse;
import in.lib.loader.base.Loader;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.BitmapUtils;
import in.lib.utils.Views;
import in.lib.view.AutoSuggestView;
import in.lib.view.LinkifiedAutoCompleteTextView;
import in.lib.view.LinkifiedAutoCompleteTextView.OnSpannableClickedListener;
import in.lib.view.spannable.MarkDownClickableSpan;
import in.model.DraftPost;
import in.model.SimpleUser;
import in.model.Stream;
import in.model.User;
import in.model.base.NetObject;
import in.obj.annotation.Annotation;
import in.obj.annotation.LocationAnnotation;
import in.obj.entity.Entity;
import in.obj.entity.Entity.Type;
import in.obj.entity.LinkEntity;
import in.rob.client.AuthenticateActivity;
import in.rob.client.MainApplication;
import in.rob.client.R;
import in.rob.client.base.RobinDialogActivity;
import in.rob.client.page.UserFriendsPage;

import java.io.File;
import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * This is the super class for the post dialogs and should not be implemented directly.
 *
 * You should not start any of these subclasses for result as the APIManager will initiate a
 * service broadcast which you should implement and catch instead.
 */
public abstract class PostDialog extends RobinDialogActivity implements OnClickListener
{
	@InjectView(R.id.title) public TextView mTitle;
	@InjectView(R.id.remaining_characters) public TextView mRemainingCharacters;
	@Getter @InjectView(R.id.new_post_input) public TextView input;
	@OnClick @InjectView(R.id.account_container) public View mAccountContainer;
	@OnClick @InjectView(R.id.post) public ImageView mPostBtn;
	@OnClick @InjectView(R.id.cancel_post) public ImageView mCancelBtn;
	@OnClick @InjectView(R.id.add_hash) public ImageView mTagBtn;
	@OnClick @InjectView(R.id.add_at) public ImageView mMentionBtn;
	@OnClick @InjectView(R.id.add_link) public ImageView mLinkBtn;
	@OnClick @InjectView(R.id.add_photo) public ImageView mImageButton;
	@OnClick @InjectView(R.id.add_location) public ImageView mLocationButton;
	@InjectView(R.id.image_thumb_scroll) public View thumbScroller;
	@InjectView(R.id.image_thumb_container) public LinearLayout imageContainer;

	@Getter private LocationHelper locationHelper;
	@Getter @Setter private Integer maxChars = SettingsManager.getPostLength();
	@Getter @Setter private DraftPost currentPost = new DraftPost();
	@Setter @Getter private User selectedUser = UserManager.getUser();
	@Getter @Setter private LocationAnnotation location;
	@Getter @Setter private boolean usingLocation = false;
	@Getter @Setter private AutoCompleteAdapter adapter;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (!UserManager.isLoggedIn())
		{
			Intent auth = new Intent(getContext(), AuthenticateActivity.class);
			auth.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			auth.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			auth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(auth);
			finish();
			return;
		}

		retrieveArguments(savedInstanceState == null ? getIntent().getExtras() : savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(getContentView());
		Views.inject(this);

		setWindowMode();
		getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_bg));

		// Get the location data if any
		locationHelper = new LocationHelper(getContext());
		location = CacheManager.getInstance().readFileAsObject(Constants.CACHE_CURRENT_LOCATION, new LocationAnnotation());
		usingLocation = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getBoolean(Constants.PREFS_POST_USE_LOCATION, false);

		initDialog();
	}

	@Override public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);

		setWindowMode();
	}

	private void setWindowMode()
	{
		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE)
		{
			getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
		else
		{
			getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		}

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}
		else
		{
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		}
	}

	/**
	 * Use this to retrieve any arguments in the saved instance/extras bundle.
	 * You should <b>NOT</b> be modifying anything on the UI here
	 * @param instances Is savedinstances when orientation happens, else it will be getIntent().getExtras()
	 */
	public void retrieveArguments(Bundle instances)
	{
		currentPost.setSelectedAccountId(selectedUser.getId());

		if (instances != null)
		{
			if (instances.containsKey(Constants.EXTRA_NEW_POST_DRAFT))
			{
				currentPost = DraftPost.deserialize(instances.getByteArray(Constants.EXTRA_NEW_POST_DRAFT));

				if (currentPost == null)
				{
					currentPost = new DraftPost();
				}
				else
				{
					selectedUser = User.loadUser(currentPost.getSelectedAccountId());
				}
			}

			if (instances.containsKey(Constants.EXTRA_USER))
			{
				selectedUser = (User)instances.getParcelable(Constants.EXTRA_USER);
				currentPost.setSelectedAccountId(selectedUser.getId());
			}

			if (instances.containsKey(Constants.EXTRA_SELECT_USER))
			{
				String id = instances.getString(Constants.EXTRA_SELECT_USER);
				User u = new User().loadUser(id);

				if (u == null)
				{
					u = new User();
					u.setId(id);
				}

				selectedUser = u;
				currentPost.setSelectedAccountId(id);
			}
		}
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		outState.putSerializable(Constants.EXTRA_NEW_POST_DRAFT, currentPost.serialize());
		outState.putParcelable(Constants.EXTRA_USER, selectedUser);
		super.onSaveInstanceState(outState);
	}

	@Override public void setTitle(CharSequence title)
	{
		mTitle.setText(title);
	}

	@Override public void setTitle(int titleId)
	{
		mTitle.setText(titleId);
	}

	/**
	 * Called when the cancel button has been pressed
	 */
	public void negativeControl()
	{
		if (this.input != null)
		{
			InputMethodManager m = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			m.hideSoftInputFromWindow(this.input.getApplicationWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}

		currentPost = null;
		finish();
	}

	/**
	 * Sets the remaining character count
	 * @param remaining The remaining count
	 */
	public void setRemainingChars(int remaining)
	{
		mRemainingCharacters.setText(remaining + "");

		if (remaining >= 0)
		{
			mRemainingCharacters.setTextColor(getResources().getColor(R.color.dark_grey));
			mPostBtn.setVisibility(View.VISIBLE);
		}
		else
		{
			mRemainingCharacters.setTextColor(getResources().getColor(R.color.light_dialog_text_color_alert));
			mPostBtn.setVisibility(View.INVISIBLE);
		}
	}

	@Override public void onClick(View v)
	{
		if (v == mAccountContainer)
		{
			List<String> users = UserManager.getLinkedUserIds(getContext());
			final ArrayList<User> loadedUsers = new ArrayList<User>();

			for (int index = 0; index < users.size(); index++)
			{
				User u = User.loadUser(users.get(index));

				if (u != null)
				{
					loadedUsers.add(u);
				}
			}

			DialogBuilder.create(getContext())
				.setTitle(R.string.select_account)
				.setAdapter(new AccountAdapter(getContext(), loadedUsers), new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						selectedUser = loadedUsers.get(which);
						currentPost.setSelectedAccountId(selectedUser.getId());
						ImageLoader.getInstance().displayImage(selectedUser.getAvatarUrl(), ((ImageView)mAccountContainer.findViewById(R.id.account_avatar)), MainApplication.getAvatarImageOptions());
						((TextView)mAccountContainer.findViewById(R.id.account_name)).setText("@" + selectedUser.getMentionName());
					}
				})
				.setNegativeButton(R.string.close, null)
			.show();
		}
		else if (v.getId() == R.id.image)
		{
			removeImage(v);
		}
		else if (v == mImageButton)
		{
			addImage();
		}
		else if (v == mLinkBtn)
		{
			addLink();
		}
		else if (v == mLocationButton)
		{
			fetchLocation(true);
		}
		else if (v == mTagBtn)
		{
			insertChar(getString(R.string.hash));
		}
		else if (v == mMentionBtn)
		{
			insertChar(getString(R.string.at));
		}
		else if (v == mPostBtn || v == mCancelBtn)
		{
			controlsClick(v);
		}
	}

	/**
	 * Sets up al lof the listeners and post properties
	 *
	 * If overriden, you <b>must</b> call super
	 */
	protected void initDialog()
	{
		if (mRemainingCharacters != null)
		{
			mRemainingCharacters.setText(maxChars - currentPost.getPostText().length() + "");
		}

		final Pattern mdLinkMatcher = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
		final TextWatcher textWatcher = new TextWatcher()
		{
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after){}

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

				int remaining = maxChars - input.getText().toString().trim().length() - (currentPost.getImagePath() == null ? 0 : SettingsManager.getImageProvider().getUrlLength());
				setRemainingChars(remaining);
			}

			@Override public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				Matcher matcher = mdLinkMatcher.matcher(s);
				while (matcher.find())
				{
					String match = matcher.group();
					String link = matcher.group(2);
					String anchor = matcher.group(1);

					if (Regex.VALID_URL.matcher(link).find())
					{
						int pos = input.getText().toString().indexOf(match);

						SpannableStringBuilder newLink = new SpannableStringBuilder(anchor);
						newLink.setSpan(new MarkDownClickableSpan(link, anchor), 0, anchor.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

						SpannableStringBuilder newText = new SpannableStringBuilder(input.getText());
						newText.replace(pos, pos + match.length(), newLink);
						newText.append(' ');

						input.setText(newText);
						((EditText)input).setSelection(input.getText().length());
					}
				}
			}
		};

		if (!TextUtils.isEmpty(currentPost.getImagePath()) && thumbScroller != null)
		{
			addImage(Uri.parse(currentPost.getImagePath()));
		}

		if (mAccountContainer != null)
		{
			if (UserManager.getLinkedUserIds(getContext()).size() > 1)
			{
				mAccountContainer.setVisibility(View.VISIBLE);
				ImageLoader.getInstance().displayImage(selectedUser.getAvatarUrl(), ((ImageView)mAccountContainer.findViewById(R.id.account_avatar)), MainApplication.getAvatarImageOptions());
				((TextView)mAccountContainer.findViewById(R.id.account_name)).setText("@" + selectedUser.getMentionName());
			}
		}

		if (input != null)
		{
			this.input.setText(currentPost.getPostText());

			if (input instanceof LinkifiedAutoCompleteTextView)
			{
				setLinkData();
				((LinkifiedAutoCompleteTextView)input).setOnSpannableClickedListener(new OnSpannableClickedListener()
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

			if (input instanceof AutoSuggestView)
			{
				List<NetObject> items = new ArrayList<NetObject>();
				items.add(SimpleUser.parseFromUser(UserManager.getUser()));

				adapter = new AutoCompleteAdapter(getContext(), items);
				((AutoSuggestView)input).setAdapter(adapter);
				((AutoSuggestView)input).setTokenizer(new SpaceTokenizer());
				((AutoSuggestView)input).setThreshold(2);
				((AutoSuggestView)input).setDropDownBackgroundResource(R.drawable.profile_avatar_fade);
				((AutoSuggestView)input).setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
				((AutoSuggestView)input).setSelection(currentPost.getPostText().length());
				((AutoSuggestView)input).addTextChangedListener(textWatcher);

				new SuggestCacheLoader().execute();
			}

			this.input.requestFocus();
		}

		int remaining = maxChars - input.getText().length() - (currentPost.getImagePath() == null ? 0 : SettingsManager.getImageProvider().getUrlLength());
		setRemainingChars(remaining);

		if (mLocationButton != null)
		{
			PackageManager packageManager = getPackageManager();
			boolean hasGPS = packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);

			if (hasGPS)
			{
				if (isUsingLocation())
				{
					setUsingLocation(!isUsingLocation());
					fetchLocation(false);
				}
			}
			else
			{
				mLocationButton.setVisibility(View.GONE);
			}
		}
	}

	public void addLink()
	{
		handleAnchoredLink(null);
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
					((EditText)getInput()).setSelection(input.getText().length());
				}
			})
		.create();
		d.show();
		d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener()
		{
			@Override public void onClick(View v)
			{
				String anchor = ((TextView)view.findViewById(R.id.url_anchor)).getText().toString().trim();
				String url = ((TextView)view.findViewById(R.id.url)).getText().toString().trim();

				if (!Regex.VALID_URL.matcher(url).find())
				{
					Toast.makeText(getContext(), R.string.invalid_url, Toast.LENGTH_LONG).show();
					return;
				}

				SpannableStringBuilder newText = new SpannableStringBuilder(((EditText)getInput()).getText());
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
					int start = ((EditText)getInput()).getText().getSpanStart(spannable);
					int end = ((EditText)getInput()).getText().getSpanEnd(spannable);

					newText.removeSpan(spannable);
					newText.replace(start, end, newLink);
				}

				((EditText)getInput()).setText(newText);
				((EditText)getInput()).setSelection(input.getText().length());

				d.dismiss();
			}
		});
	}

	public void setLinkData()
	{
		if (input instanceof EditText)
		{
			SpannableStringBuilder newText = new SpannableStringBuilder(((EditText)getInput()).getText());

			if (getCurrentPost().getEntities().get(Type.LINK) != null)
			{
				for (Entity entity : getCurrentPost().getEntities().get(Type.LINK))
				{
					LinkEntity link = (LinkEntity)entity;

					if (link.getAmendedLen() > -1)
					{
						String anchor = newText.toString().substring(link.getPos(), link.getPos() + link.getLen());
						SpannableStringBuilder newLink = new SpannableStringBuilder(anchor);
						newLink.setSpan(new MarkDownClickableSpan(link.getUrl(), anchor), 0, anchor.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						newText.replace(link.getPos(), link.getPos() + link.getLen(), newLink);
					}
					else
					{
						String anchor = newText.toString().substring(link.getPos(), link.getPos() + link.getLen());
						SpannableStringBuilder newLink = new SpannableStringBuilder(anchor);
						newLink.setSpan(new MarkDownClickableSpan(link.getUrl(), anchor), 0, link.getLen(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						newText.replace(link.getPos(), link.getPos() + link.getLen(), newLink);
					}
				}

				((EditText)getInput()).setText(newText);
			}
		}
	}

	private void insertChar(String string)
	{
		int selStart = getInput().getSelectionStart();

		SpannableStringBuilder builder = new SpannableStringBuilder(getInput().getText());
		builder.insert(selStart, string);
		getInput().setText(builder);

		if (getInput() instanceof EditText)
		{
			((EditText)getInput()).setSelection(selStart + string.length());
		}
	}

	public void updateDraftPost()
	{
		if (input instanceof EditText)
		{
			Spannable text = ((EditText)input).getText();
			MarkDownClickableSpan[] links = text.getSpans(0, text.length(), MarkDownClickableSpan.class);
			currentPost.getEntities().clear();

			ArrayList<Entity> linksArr = new ArrayList<Entity>();
			for (MarkDownClickableSpan l : links)
			{
				int start = text.getSpanStart(l);
				int end = text.getSpanEnd(l);

				LinkEntity link = new LinkEntity();
				link.setPos(start);
				link.setLen(end - start);
				link.setUrl(l.getUrl());

				if (!l.getUrl().equals(l.getAnchor()))
				{
					try
					{
						link.setAmendedLen(link.getLen() + Uri.parse(l.getUrl()).getHost().length() + 3);
					}
					catch (Exception e){}
				}

				linksArr.add(link);
			}

			currentPost.getEntities().put(Type.LINK, linksArr);
		}

		currentPost.setPostText(input.getText().toString());
	}

	public void controlsClick(View v)
	{
		updateDraftPost();
		if (v.getId() == R.id.cancel_post)
		{
			negativeControl();
		}
		else if (v.getId() == R.id.post)
		{
			currentPost.setPostText(currentPost.getPostText().trim());
			positiveControl();
			finish();
		}
	}

	public void removeImage(final View v)
	{
		DialogBuilder.create(getContext())
			.setTitle(R.string.confirm)
			.setMessage(R.string.remove_image)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					currentPost.setImagePath(null);
					thumbScroller.setVisibility(View.GONE);
					imageContainer.removeAllViews();
					setRemainingChars(maxChars - getInput().getText().length());
				}
			})
			.setNegativeButton(R.string.no, null)
		.show();
	}

	public void addImage()
	{
		CharSequence[] items = getResources().getStringArray(R.array.image_choice);

		DialogBuilder.create(getContext())
		.setItems(items, new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				//	Camera
				if (which == 0)
				{
					File photo = null;
					Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
					if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
					{
						String folder = Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM) + "/100ROBIN/";
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
						Uri path = Uri.fromFile(photo);

						// add the uri to the preferences because shitty phones like the Droid, destroy the activity
						// and all the references get set to null.
						SharedPreferences prefs = getSharedPreferences(Constants.PREFS_POST, Context.MODE_PRIVATE);
						prefs.edit().putString(Constants.PREFS_POST_IMAGE_KEY, path.toString()).apply();

						intent.putExtra(MediaStore.EXTRA_OUTPUT, path);

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
				//	Gallery
				else if (which == 1)
				{
					if (Build.VERSION.SDK_INT < 19)
					{
						Intent intent = new Intent();
						intent.setType("image/jpeg");
						intent.setAction(Intent.ACTION_GET_CONTENT);
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
				else if (which == 2)
				{
					String[] projection = new String[]{MediaStore.Images.ImageColumns.DATA, MediaStore.Images.ImageColumns.DATE_TAKEN};
					Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
					if (cursor.moveToFirst())
					{
						String imageLocation = cursor.getString(0);
						File imageFile = new File(imageLocation);
						addImage(Uri.fromFile(imageFile));

						int remaining = maxChars - input.getText().length() - (currentPost.getImagePath() == null ? 0 : SettingsManager.getImageProvider().getUrlLength());
						setRemainingChars(remaining);
					}
					else
					{
						Toast.makeText(getContext(), R.string.image_not_found, Toast.LENGTH_SHORT).show();
					}
				}
			}
		})
		.setTitle(getString(R.string.please_select))
		.show();
	}

	public void addImage(Uri imageUri)
	{
		currentPost.setImagePath(imageUri.toString());
		ImageView image = (ImageView)LayoutInflater.from(getContext()).inflate(R.layout.dialog_post_image_stub, imageContainer, false);
		image.setImageBitmap(loadImageThumb(imageUri));
		image.setOnClickListener(this);
		imageContainer.removeAllViews();
		imageContainer.addView(image);
		thumbScroller.setVisibility(View.VISIBLE);
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK) return;

		// check the shared prefs for our image uri
		SharedPreferences prefs = getSharedPreferences(Constants.PREFS_POST, Context.MODE_PRIVATE);
		Uri imagePath = Uri.parse(prefs.getString(Constants.PREFS_POST_IMAGE_KEY, ""));

		prefs.edit().clear().apply();

		if (requestCode == Constants.REQUEST_CAMERA && imagePath != null)
		{
			addImage(imagePath);

			int remaining = maxChars - input.getText().length() - (currentPost.getImagePath() == null ? 0 : SettingsManager.getImageProvider().getUrlLength());
			setRemainingChars(remaining);
		}
		else if (data != null)
		{
			Uri selectedUri = data.getData();
			if (Build.VERSION.SDK_INT >= 19)
			{
				final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				getContentResolver().takePersistableUriPermission(selectedUri, takeFlags);
			}

			addImage(selectedUri);
			int remaining = maxChars - input.getText().length() - (currentPost.getImagePath() == null ? 0 : SettingsManager.getImageProvider().getUrlLength());
			setRemainingChars(remaining);
		}
	}

	public int getOrientation(Uri photoUri)
	{
		Cursor cursor = getContentResolver().query(photoUri, new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

		if (cursor.getCount() != 1)
		{
			return -1;
		}

		cursor.moveToFirst();
		return cursor.getInt(0);
	}

	/**
	 * Loads an image as a thumbnail
	 * @param image The path to the image
	 * @return Bitmap of the thumbnail
	 */
	public Bitmap loadImageThumb(Uri imageUri)
	{
		try
		{
			if (imageUri.getScheme().startsWith("content"))
			{
				ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(imageUri, "r");
				FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

				int sample = BitmapUtils.recursiveSample(fileDescriptor, 200, 200);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = sample;

				int orientation = getOrientation(imageUri);
				Bitmap image = BitmapUtils.fixOrientation(BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options), orientation);
				parcelFileDescriptor.close();

				return image;
			}
			else if (imageUri.getScheme().startsWith("file"))
			{
				int orientation = 0;
				String image = imageUri.getPath();

				try
				{
					ExifInterface exif = new ExifInterface(image);
					orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
				}
				catch (Exception e)
				{
					Debug.out(e);
				}

				int sample = BitmapUtils.recursiveSample(image, 200, 200);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = sample;
				return BitmapUtils.fixOrientation(BitmapFactory.decodeFile(image, options), orientation);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Gets the file path of a media URI
	 * @param uri the uri of the media
	 * @return The string path of the media, or null
	 */
	public String getPath(Uri uri)
	{
		String[] projection = {MediaStore.Images.Media.DATA};
		Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
		if (cursor != null)
		{
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			String s = cursor.getString(column_index);

			return s;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Fetches the user's location
	 */
	public void fetchLocation(final boolean showToast)
	{
		// only fetch for posts with the location button
		if (mLocationButton == null) return;

		if (!isUsingLocation())
		{
			if (getLocation() != null
			&& getLocation().hasAccuracy()
			&& getLocation().getAccuracy() <= 500.0f
			&& System.currentTimeMillis() - getLocation().getTime() < 60 * 15 * 1000)
			{
				mLocationButton.setImageResource(R.drawable.dialog_ic_location_found);

				List<Annotation> embeddables = currentPost.getAnnotations();

				for (Annotation b : embeddables)
				{
					if (b instanceof LocationAnnotation)
					{
						currentPost.getAnnotations().remove(b);
						break;
					}
				}

				currentPost.getAnnotations().add(getLocation());
			}
			else
			{
				getLocationHelper().fetchLocation(20000, Accuracy.COARSE, new LocationResponse()
				{
					@Override public void onLocationAquired(Location arg0)
					{
						setLocation(new LocationAnnotation(arg0));
						mLocationButton.setEnabled(true);
						mLocationButton.setImageResource(R.drawable.dialog_ic_location_found);

						if (showToast && !isFinishing())
						{
							Toast.makeText(getContext(), R.string.location_aquired, Toast.LENGTH_SHORT).show();
						}

						CacheManager.getInstance().writeFile(Constants.CACHE_CURRENT_LOCATION, getLocation());

						if (currentPost == null)
						{
							currentPost = new DraftPost();
						}

						List<Annotation> embeddables = currentPost.getAnnotations();

						for (Annotation b : embeddables)
						{
							if (b instanceof LocationAnnotation)
							{
								currentPost.getAnnotations().remove(b);
								break;
							}
						}

						currentPost.getAnnotations().add(getLocation());
						getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().putBoolean(Constants.PREFS_POST_USE_LOCATION, isUsingLocation()).apply();
					}

					@Override public void onLocationChanged(Location l)
					{
						if (l.getAccuracy() < 500.0f)
						{
							onLocationAquired(l);
							getLocationHelper().stopFetch();
						}
					}

					@Override public void onLocationFailed(String message, int messageId)
					{
						mLocationButton.setEnabled(true);
						mLocationButton.setImageResource(R.drawable.dialog_ic_location);

						if (showToast && !isFinishing())
						{
							Toast.makeText(getContext(), R.string.location_failed, Toast.LENGTH_SHORT).show();
						}

						setUsingLocation(false);
						getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().putBoolean(Constants.PREFS_POST_USE_LOCATION, isUsingLocation()).apply();
					}
				});

				mLocationButton.setImageResource(R.drawable.dialog_ic_location_active);
				mLocationButton.setEnabled(false);

				if (showToast)
				{
					Toast.makeText(getContext(), R.string.fetching_location, Toast.LENGTH_SHORT).show();
				}
			}
		}
		else
		{
			for (Annotation b : currentPost.getAnnotations())
			{
				if (b instanceof LocationAnnotation)
				{
					currentPost.getAnnotations().remove(b);
					break;
				}
			}

			mLocationButton.setImageResource(R.drawable.dialog_ic_location);
		}

		setUsingLocation(!isUsingLocation());
		getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).edit().putBoolean(Constants.PREFS_POST_USE_LOCATION, isUsingLocation()).apply();
	}

	/**
	 * Called when setting the content view
	 * @return The layout resource of the content view
	 */
	public abstract int getContentView();

	/**
	 * Called when the send button has been pressed
	 *
	 * Note: {@link Activity.finish()} is called after this method
	 */
	public abstract void positiveControl();

	/**
	 * Class to load in the users and tags for the autocompelete filter
	 */
	private class SuggestCacheLoader extends Loader<List<NetObject>>
	{
		public SuggestCacheLoader()
		{
			super("");
		}

		@Override public List<NetObject> doInBackground()
		{
			List<SimpleUser> users = CacheManager.getInstance().readFileAsObject(Constants.CACHE_USERNAMES, new ArrayList<SimpleUser>());
			List<NetObject> tags = CacheManager.getInstance().readFileAsObject(Constants.CACHE_HASHTAGS, new ArrayList<NetObject>());

			if (users.size() < 1)
			{
				// load default followers/following list and add them to the autocomplete
				Stream following = CacheManager.getInstance().readFileAsObject(String.format(Constants.CACHE_USER_LIST_NAME, UserFriendsPage.Mode.FOLLOWING.getModeText(), UserManager.getUserId()), Stream.class);
				Stream followers = CacheManager.getInstance().readFileAsObject(String.format(Constants.CACHE_USER_LIST_NAME, UserFriendsPage.Mode.FOLLOWERS.getModeText(), UserManager.getUserId()), Stream.class);

				if (following != null)
				{
					for (NetObject object : following.getObjects())
					{
						users.add(SimpleUser.parseFromUser((User)object));
					}
				}
				else
				{
					CacheManager.getInstance().removeFile(String.format(Constants.CACHE_USER_LIST_NAME, UserFriendsPage.Mode.FOLLOWING.getModeText(), UserManager.getUserId()));
				}

				if (followers != null)
				{
					for (NetObject object : followers.getObjects())
					{
						users.add(SimpleUser.parseFromUser((User)object));
					}
				}
				else
				{
					CacheManager.getInstance().removeFile(String.format(Constants.CACHE_USER_LIST_NAME, UserFriendsPage.Mode.FOLLOWERS.getModeText(), UserManager.getUserId()));
				}

				CacheManager.getInstance().asyncWriteFile(Constants.CACHE_USERNAMES, users);
			}

			tags.addAll(users);

			for (String recent : SettingsManager.getRecentSearches())
			{
				NetObject tag = new NetObject();
				recent = !recent.startsWith("#") ? "#" + recent : recent;
				tag.setFilterTag(recent);
				tags.add(tag);
			}

			for (String saved : SettingsManager.getSavedTags())
			{
				NetObject tag = new NetObject();
				saved = !saved.startsWith("#") ? "#" + saved : saved;
				tag.setFilterTag(saved);
				tags.add(tag);
			}

			return tags;
		}

		@Override public void onPostExecute(List<NetObject> tags)
		{
			adapter.setItems(tags);
			adapter.notifyDataSetChanged();
		}
	}
}