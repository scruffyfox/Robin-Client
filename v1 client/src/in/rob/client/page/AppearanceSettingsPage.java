package in.rob.client.page;

import in.lib.Constants;
import in.lib.annotation.InjectView;
import in.lib.annotation.OnClick;
import in.lib.manager.CacheManager;
import in.lib.manager.SettingsManager;
import in.lib.manager.UserManager;
import in.lib.utils.CodeUtils;
import in.lib.utils.Views;
import in.rob.client.R;
import in.rob.client.SettingsActivity;
import in.rob.client.base.RobinFragment;
import in.rob.client.dialog.base.DialogBuilder;

import java.util.ArrayList;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

public class AppearanceSettingsPage extends RobinFragment implements OnCheckedChangeListener, OnSeekBarChangeListener, OnClickListener
{
	@InjectView(R.id.timeline_cover) public CheckBox mTimelineCover;
	@InjectView(R.id.timeline_break) public CheckBox mTimelineBreak;
	@InjectView(R.id.unified_stream) public CheckBox mUnifiedStream;
	@InjectView(R.id.post_times) public CheckBox mLongDates;
	@InjectView(R.id.avatar_images) public CheckBox mShowAvatars;
	@InjectView(R.id.a_directed_msgs) public CheckBox mToggleDirectedMsgs;
	@InjectView(R.id.mention_directed_msgs) public CheckBox mToggleDirectedMentions;
	@InjectView(R.id.inline_images) public CheckBox mToggleInlineImages;
	@InjectView(R.id.global_page) public CheckBox mGlobalStream;
	@InjectView(R.id.font_size) public SeekBar mFontSize;
	@InjectView(R.id.font_size_text) public TextView mFontSizeTv;
	@InjectView(R.id.name_order) public TextView mNameOrder;
	@InjectView(R.id.invert_post_checkbox) public CheckBox mInvertPost;
	@InjectView(R.id.lightbox_checkbox) public CheckBox mLigtbox;
	@InjectView(R.id.image_viewer_checkbox) public CheckBox mImagebox;
	@InjectView(R.id.custom_fonts) public CheckBox mCustomFonts;
	@InjectView(R.id.web_readability) public CheckBox mWebReadability;
	@OnClick @InjectView(R.id.name_order_container) public View mNameOrderContainer;
	@OnClick @InjectView(R.id.theme_container) public View mThemeContainer;
	@OnClick @InjectView(R.id.locale_container) public View mLocaleContainer;
	@OnClick @InjectView(R.id.animation_options) public View mAnimations;
	@OnClick @InjectView(R.id.emphasis_options) public View mEmphasis;
	@OnClick @InjectView(R.id.single_click_link) public View mSingleClickLink;

	private ArrayList<String> mNameOrderHistory;
	private SettingsManager mSettingsManager;

	private Intent mIntent = new Intent();
	private String[] mFontSizeOpts;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.appearance_settings_view, null);
		Views.inject(this, view);
		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		mSettingsManager = SettingsManager.getInstance();

		mFontSizeOpts = new String[]
		{
			getString(R.string.small),
			getString(R.string.normal),
			getString(R.string.medium),
			getString(R.string.large),
			getString(R.string.larger),
			getString(R.string.larger_still),
			getString(R.string.am_blind)
		};

		// init inputs
		mFontSizeTv.setText(mFontSizeOpts[SettingsManager.getFontSizeIndex()]);
		mFontSize.setProgress((SettingsManager.getFontSizeIndex() * 10) + 9);
		mFontSize.setOnSeekBarChangeListener(this);

		mUnifiedStream.setChecked(SettingsManager.isUsingUnified());
		mUnifiedStream.setOnCheckedChangeListener(this);

		mTimelineBreak.setChecked(SettingsManager.isTimelineBreakEnabled());
		mTimelineBreak.setOnCheckedChangeListener(this);

		mTimelineCover.setChecked(SettingsManager.getShowTimelineCover());
		mTimelineCover.setOnCheckedChangeListener(this);

		mToggleDirectedMsgs.setChecked(SettingsManager.getShowDirectedPosts());
		mToggleDirectedMsgs.setOnCheckedChangeListener(this);

		mToggleInlineImages.setChecked(SettingsManager.isInlineImagesEnabled());
		mToggleInlineImages.setOnCheckedChangeListener(this);

		mShowAvatars.setChecked(SettingsManager.getShowAvatars());
		mShowAvatars.setOnCheckedChangeListener(this);

		mLongDates.setChecked(SettingsManager.getShowLongDates());
		mLongDates.setOnCheckedChangeListener(this);

		mGlobalStream.setChecked(SettingsManager.isGlobalEnabled());
		mGlobalStream.setOnCheckedChangeListener(this);

		mNameOrder.setText(SettingsManager.getNameDisplayOrder());

		mInvertPost.setChecked(SettingsManager.isInvertPostClick());
		mInvertPost.setOnCheckedChangeListener(this);

		mLigtbox.setChecked(SettingsManager.isLightboxEnabled());
		mLigtbox.setOnCheckedChangeListener(this);

		mImagebox.setChecked(SettingsManager.isImageViewerEnabled());
		mImagebox.setOnCheckedChangeListener(this);

		mToggleDirectedMentions.setChecked(SettingsManager.getShowDirectedMentions());
		mToggleDirectedMentions.setOnCheckedChangeListener(this);

		mCustomFonts.setChecked(SettingsManager.isCustomFontsEnabled());
		mCustomFonts.setOnCheckedChangeListener(this);

		mWebReadability.setChecked(SettingsManager.isWebReadabilityEnabled());
		mWebReadability.setOnCheckedChangeListener(this);
	}

	@Override public void onStart()
	{
		super.onStart();
		mNameOrderHistory = CacheManager.getInstance().readFileAsObject(Constants.CACHE_NAME_HISTORY, new ArrayList<String>());
	}

	@Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if (buttonView == mTimelineBreak)
		{
			mSettingsManager.setTimelineBreakEnabled(isChecked);
		}
		else if (buttonView == mWebReadability)
		{
			mSettingsManager.setWebReadabilityEnabled(isChecked);
		}
		else if (buttonView == mCustomFonts)
		{
			mSettingsManager.setCustomFontsEnabled(isChecked);
			showRestartToast();
		}
		else if (buttonView == mToggleDirectedMentions)
		{
			mSettingsManager.setShowDirectedMentions(isChecked);
		}
		else if (buttonView == mLigtbox)
		{
			mSettingsManager.setLightboxEnabled(isChecked);
		}
		else if (buttonView == mImagebox)
		{
			mSettingsManager.setImageViewerEnabled(isChecked);
		}
		else if (buttonView == mInvertPost)
		{
			mSettingsManager.setInvertPostEnabled(isChecked);
		}
		else if (buttonView == mTimelineCover)
		{
			mSettingsManager.setTimelineCoverEnabled(isChecked);
			mIntent.putExtra(Constants.EXTRA_REFRESH_TIMELINE_COVER, true);
			showRestartToast();
		}
		else if (buttonView == mUnifiedStream)
		{
			mSettingsManager.setUsingUnified(isChecked);
			mIntent.putExtra(Constants.EXTRA_REFRESH_TIMELINE, true);
		}
		else if (buttonView == mGlobalStream)
		{
			mSettingsManager.setGlobalEnabled(isChecked);
			mIntent.putExtra(Constants.EXTRA_REFRESH_GLOBAL, true);
		}
		else if (buttonView == mLongDates)
		{
			mSettingsManager.setShowLongDates(isChecked);
			mIntent.putExtra(Constants.EXTRA_REFRESH_TIMES, true);
		}
		else if (buttonView == mToggleInlineImages)
		{
			mSettingsManager.setInlineImagesEnabled(isChecked);
			mIntent.putExtra(Constants.EXTRA_REFRESH_INLINE, true);
		}
		else if (buttonView == mToggleDirectedMsgs)
		{
			mSettingsManager.setShowDirectedPosts(isChecked);
			mIntent.putExtra(Constants.EXTRA_REFRESH_TIMELINE, true);
		}
		else if (buttonView == mShowAvatars)
		{
			mSettingsManager.setShowAvatars(isChecked);
			mIntent.putExtra(Constants.EXTRA_REFRESH_LIST, true);
		}

		if (mIntent.getExtras() != null && !mIntent.getExtras().isEmpty())
		{
			getActivity().setResult(Constants.RESULT_REFRESH, mIntent);
		}
	}

	public void showRestartToast()
	{
		Toast.makeText(getContext(), R.string.setting_restart_message, Toast.LENGTH_SHORT).show();

		if (getActivity() instanceof SettingsActivity)
		{
			((SettingsActivity)getActivity()).setRestartRequired(true);
		}
	}

	@Override public void onClick(View v)
	{
		if (v == mAnimations)
		{
			showAnimationOptions();
		}
		else if (v == mEmphasis)
		{
			showEmphasisOptions();
		}
		else if (v == mSingleClickLink)
		{
			showSingleClickOptions();
		}
		else if (v == mLocaleContainer)
		{
			showLocaleOptions();
		}
		else if (v == mNameOrderContainer)
		{
			((SettingsActivity)getActivity()).lockOrientation();
			final ArrayList<CharSequence> items = new ArrayList<CharSequence>();
			CharSequence[] arr = getResources().getStringArray(R.array.name_orders_values);
			for (CharSequence s : arr)
			{
				String[] parts = CodeUtils.nameOrderParse(s.toString(), UserManager.getUser());
				items.add(Html.fromHtml("<b>" + parts[0] + "</b> " + parts[1]));
			}

			items.addAll(mNameOrderHistory);
			items.add(getString(R.string.custom));

			DialogBuilder.create(getContext())
			.setTitle(R.string.pick_option)
			.setItems(items.toArray(new CharSequence[items.size()]), new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					Tracker t = GoogleAnalytics.getInstance(getContext()).getTracker(getString(R.string.ga_trackingId));
					String[] vals = getResources().getStringArray(R.array.name_orders_values);

					if (which > vals.length - 1 && which != items.size() - 1)
					{
						mSettingsManager.setNameDisplayOrder(items.get(which).toString());
						mNameOrder.setText(items.get(which));
						getActivity().setResult(Constants.RESULT_REFRESH, mIntent.putExtra(Constants.EXTRA_REFRESH_NAMES, true));
						t.trackEvent("settings", "custom name", mNameOrder.getText().toString(), System.currentTimeMillis());
					}
					else if (which == items.size() - 1)
					{
						showCustomOrders();
					}
					else
					{
						mSettingsManager.setNameDisplayOrder(vals[which]);
						mNameOrder.setText(vals[which]);
						getActivity().setResult(Constants.RESULT_REFRESH, mIntent.putExtra(Constants.EXTRA_REFRESH_NAMES, true));
						t.trackEvent("settings", "custom name", mNameOrder.getText().toString(), System.currentTimeMillis());
					}

					if (SettingsManager.isAnalyticsEnabled())
					{
						GAServiceManager.getInstance().dispatch();
					}

					getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
					dialog.dismiss();
				}
			})
			.show();
		}
		else if (v == mThemeContainer)
		{
			((SettingsActivity)getActivity()).lockOrientation();
			CharSequence[] options = {"Light", "Dark"};
			final String[] res = {getResources().getResourceEntryName(R.style.DefaultLight), getResources().getResourceEntryName(R.style.DefaultDark)};

			DialogBuilder.create(getContext())
			.setTitle(R.string.pick_option)
			.setItems(options, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					mSettingsManager.setAppTheme(res[which]);
					dialog.dismiss();
					showRestartToast();
				}
			})
			.show();
		}
	}

	/**
	 * Popup for animation settings
	 */
	public void showAnimationOptions()
	{
		final boolean[] options = new boolean[getResources().getStringArray(R.array.animation_options).length];
		final int[] ints = getResources().getIntArray(R.array.animation_setting_choice_mask);

		for (int index = 0; index < ints.length; index++)
		{
			options[index] = (SettingsManager.getAnimations() & ints[index]) == ints[index];
		}

		DialogBuilder.create(getContext())
			.setTitle(R.string.select_animation_option)
			.setMultiChoiceItems(R.array.animation_options, options, new DialogInterface.OnMultiChoiceClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which, boolean isChecked)
				{
					options[which] = isChecked;
				}
			})
			.setPositiveButton(R.string.done, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					int finalInt = 0;
					for (int index = 0; index < options.length; index++)
					{
						if (options[index])
						{
							finalInt |= ints[index];
						}
					}

					mSettingsManager.setAnimationOptions(finalInt);
					mIntent.putExtra(Constants.EXTRA_REFRESH_ANIMATIONS, true);
					getActivity().setResult(Constants.RESULT_REFRESH, mIntent);
				}
			})
			.setNegativeButton(R.string.cancel, null)
		.show();
	}

	/**
	 * Popup for emphasis settings
	 */
	public void showEmphasisOptions()
	{
		final boolean[] options = new boolean[getResources().getStringArray(R.array.emphasis_options).length];
		final int[] ints = getResources().getIntArray(R.array.emphasis_setting_choice_mask);

		for (int index = 0; index < ints.length; index++)
		{
			options[index] = (SettingsManager.getPostEmphasis() & ints[index]) == ints[index];
		}

		DialogBuilder.create(getContext())
			.setTitle(R.string.select_emphasis_option)
			.setMultiChoiceItems(R.array.emphasis_options, options, new DialogInterface.OnMultiChoiceClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which, boolean isChecked)
				{
					options[which] = isChecked;
				}
			})
			.setPositiveButton(R.string.done, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					int finalInt = 0;
					for (int index = 0; index < options.length; index++)
					{
						if (options[index])
						{
							finalInt |= ints[index];
						}
					}

					mSettingsManager.setPostEmphasisOptions(finalInt);
					mIntent.putExtra(Constants.EXTRA_REFRESH_ALL_DATA, true);
					getActivity().setResult(Constants.RESULT_REFRESH, mIntent);
				}
			})
			.setNegativeButton(R.string.cancel, null)
		.show();
	}

	/**
	 * Popup for single click settings
	 */
	public void showSingleClickOptions()
	{
		final boolean[] options = new boolean[getResources().getStringArray(R.array.single_click_options).length];
		final int[] ints = getResources().getIntArray(R.array.single_click_setting_choice_mask);

		for (int index = 0; index < ints.length; index++)
		{
			options[index] = (SettingsManager.getSingleClickLinks() & ints[index]) == ints[index];
		}

		DialogBuilder.create(getContext())
			.setTitle(R.string.please_select)
			.setMultiChoiceItems(R.array.single_click_options, options, new DialogInterface.OnMultiChoiceClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which, boolean isChecked)
				{
					options[which] = isChecked;
				}
			})
			.setPositiveButton(R.string.done, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					int finalInt = 0;
					for (int index = 0; index < options.length; index++)
					{
						if (options[index])
						{
							finalInt |= ints[index];
						}
					}

					mSettingsManager.setSingleClickOptions(finalInt);
					mIntent.putExtra(Constants.EXTRA_REFRESH_LIST, true);
					getActivity().setResult(Constants.RESULT_REFRESH, mIntent);
				}
			})
			.setNegativeButton(R.string.cancel, null)
		.show();
	}

	/**
	 * Popup for animation settings
	 */
	public void showLocaleOptions()
	{
		final String[] options = getResources().getStringArray(R.array.locale_options);
		final String[] locales = getResources().getStringArray(R.array.locales);

		DialogBuilder.create(getContext())
			.setTitle(R.string.please_select)
			.setItems(options, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					mSettingsManager.setLocale(locales[which]);
					showRestartToast();
				}
			})
			.setNegativeButton(R.string.cancel, null)
		.show();
	}

	/**
	 * Used to show the popup to create custom name orders
	 */
	public void showCustomOrders()
	{
		String instr = "Note: To seperate the names, use the | (pipe) character" +
						"<br /><br />Variables:" +
						"<br /><b>{#username}</b> = " + UserManager.getUser().getMentionName() +
						"<br /><b>{#firstname}</b> = " + UserManager.getUser().getFirstName() +
						"<br /><b>{#lastname}</b> = " + UserManager.getUser().getLastName() +
						"<br /><b>{#fullname}</b> = " + UserManager.getUser().getUserName() +
						"<br /><br />Functions:" +
						"<br />Note: you can comma-seperate parameters, a blank paramter counts as a space" +
						"<br /><b>uc()</b>: Upper Case" +
						"<br /><b>lc()</b>: lower case" +
						"<br /><b>cap()</b>: CAPITIALIZE" +
						"<br /><br />Operators:" +
						"<br /><b>[0-100]</b>: Character at index (starts at 0) E.G {#username}[0,1,2,3,4,5,6] = \"" + UserManager.getUser().getMentionName().substring(0, Math.min(6, UserManager.getUser().getMentionName().length())) + "\"";

		View view = LayoutInflater.from(getContext()).inflate(R.layout.custom_name_view, null, false);

		TextView instructions = (TextView)view.findViewById(R.id.instructions);
		final TextView preview = (TextView)view.findViewById(R.id.preview);
		final EditText input = (EditText)view.findViewById(R.id.input);

		instructions.setText(Html.fromHtml(instr));

		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(60);
		input.setFilters(FilterArray);

		input.addTextChangedListener(new TextWatcher()
		{
			@Override public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				String[] format = CodeUtils.nameOrderParse(s.toString(), UserManager.getUser());

				if (format.length > 0)
				{
					format[0] = "<b>" + format[0] + "</b>&nbsp;";
					String string = "";
					for (String str : format)
					{
						string += str.replaceAll("[\\s]", "&nbsp;");
					}

					preview.setText(Html.fromHtml(string));
				}
			}

			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			@Override public void afterTextChanged(Editable s){}
		});
		input.setText(SettingsManager.getNameDisplayOrder());

		((SettingsActivity)getActivity()).lockOrientation();
		DialogBuilder.create(getContext())
			.setTitle(R.string.create_custom_order)
			.setView(view)
			.setPositiveButton(R.string.done, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface dialog, int which)
				{
					mSettingsManager.setNameDisplayOrder(input.getText().toString());

					if (!mNameOrderHistory.contains(input.getText().toString()))
					{
						mNameOrderHistory.add(input.getText().toString());
					}

					mNameOrder.setText(input.getText().toString());
					CacheManager.getInstance().writeFile(Constants.CACHE_NAME_HISTORY, mNameOrderHistory);

					if (SettingsManager.isAnalyticsEnabled())
					{
						Tracker t = GoogleAnalytics.getInstance(getContext()).getTracker(getString(R.string.ga_trackingId));
						t.trackEvent("settings", "custom name", mNameOrder.getText().toString(), System.currentTimeMillis() / 10000L);
						GAServiceManager.getInstance().dispatch();
					}

					getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
					mIntent.putExtra(Constants.EXTRA_REFRESH_NAMES, true);
					getActivity().setResult(Constants.RESULT_REFRESH, mIntent);
				}
			})
			.setNegativeButton(R.string.cancel, null)
		.show();
	}

	@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		if (seekBar == mFontSize)
		{
			mSettingsManager.setFontSize(progress / 10);
			mFontSizeTv.setText(mFontSizeOpts[progress / 10]);
			mIntent.putExtra(Constants.EXTRA_REFRESH_FONTS, true);
			getActivity().setResult(Constants.RESULT_REFRESH, mIntent);
		}
	}

	@Override public void onStartTrackingTouch(SeekBar seekBar){}
	@Override public void onStopTrackingTouch(SeekBar seekBar){}
}