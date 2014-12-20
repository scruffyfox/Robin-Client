package in.rob.client.page;

import in.lib.Constants;
import in.lib.Debug;
import in.lib.annotation.InjectView;
import in.lib.handler.streams.TrendingStreamResponseHandler;
import in.lib.manager.APIManager;
import in.lib.manager.SettingsManager;
import in.lib.thread.FragmentRunnable;
import in.lib.utils.Views;
import in.rob.client.R;
import in.rob.client.SearchResultsActivity;
import in.rob.client.base.RobinFragment;
import in.rob.client.dialog.base.DialogBuilder;
import in.rob.client.page.base.StreamFragment;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchPage extends RobinFragment implements OnEditorActionListener, OnClickListener, OnLongClickListener
{
	public View rootView;
	@InjectView(R.id.search_box) public EditText mSearchBox;
	@InjectView(R.id.trending_searches) public LinearLayout mTrendingSearches;
	@InjectView(R.id.saved_search_container) public LinearLayout mSavedSearches;
	@InjectView(R.id.recent_search_container) public LinearLayout mRecentSearches;
	@InjectView(R.id.muted_search_container) public LinearLayout mMutedSearches;

	public static final int TYPE_SAVED = 0;
	public static final int TYPE_RECENT = 1;
	public static final int TYPE_MUTED = 2;
	public static final int TYPE_TRENDING = 3;

	private List<String> trendingTags;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		rootView = inflater.inflate(R.layout.search_view, container, false);
		Views.inject(this, rootView);

		return rootView;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		setupTrendingSearches();

		mSearchBox.setOnEditorActionListener(this);

		if (savedInstanceState != null)
		{
			if (savedInstanceState.containsKey("trending"))
			{
				trendingTags = (ArrayList<String>)savedInstanceState.getSerializable("trending");
			}
		}
	}

	public void setupTrendingSearches()
	{
		if (trendingTags != null)
		{
			for (String tag : trendingTags)
			{
				if (TextUtils.isEmpty(tag)) continue;

				tag = "#" + tag;
				LinearLayout tagView = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.search_item_stub, null);
				((TextView)tagView.findViewById(R.id.title)).setText(tag);
				tagView.setOnClickListener(this);
				tagView.setOnLongClickListener(this);
				tagView.setTag(tag);
				tagView.setTag(R.id.TAG_TYPE, TYPE_TRENDING);
				mTrendingSearches.addView(tagView);
			}
		}
		else
		{
			if (rootView.findViewById(R.id.trending_loading) != null)
			{
				rootView.findViewById(R.id.trending_loading).setVisibility(View.VISIBLE);
			}

			//TODO: Move this response into a handler
			APIManager.getInstance().getTrending(new TrendingStreamResponseHandler(getContext())
			{
				@Override public void onCallback()
				{
					if (getActivity() != null)
					{
						getActivity().runOnUiThread(responseRunner);
					}
				}

				private FragmentRunnable<StreamFragment> responseRunner = new FragmentRunnable<StreamFragment>()
				{
					@Override public void run()
					{
						if (getTrending().size() > 0)
						{
							mTrendingSearches.removeAllViews();

							if (rootView.findViewById(R.id.trending_loading) != null)
							{
								rootView.findViewById(R.id.trending_loading).setVisibility(View.GONE);
							}

							for (String tag : getTrending())
							{
								if (TextUtils.isEmpty(tag)) continue;

								tag = "#" + tag;
								LinearLayout tagView = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.search_item_stub, null);
								((TextView)tagView.findViewById(R.id.title)).setText(tag);
								tagView.setOnClickListener(SearchPage.this);
								tagView.setOnLongClickListener(SearchPage.this);
								tagView.setTag(tag);
								tagView.setTag(R.id.TAG_TYPE, TYPE_TRENDING);
								mTrendingSearches.addView(tagView);
							}

							if (mTrendingSearches.getChildCount() > 0)
							{
								mTrendingSearches.getChildAt(mTrendingSearches.getChildCount() - 1).findViewById(R.id.divider).setVisibility(View.GONE);
							}

							trendingTags = getTrending();
						}
					}
				};

				@Override public void onFinish(boolean failed)
				{
					Debug.out(getConnectionInfo());

					if (failed && rootView.findViewById(R.id.trending_loading) != null)
					{
						rootView.findViewById(R.id.trending_loading).findViewById(R.id.load_text).setVisibility(View.VISIBLE);
						rootView.findViewById(R.id.trending_loading).findViewById(R.id.progress).setVisibility(View.GONE);
					}
				}

				@Override public StreamFragment getFragment()
				{
					return null;
				}
			});
		}
	}

	public void setupSavedSearches()
	{
		mSavedSearches.removeAllViews();
		String[] tags = SettingsManager.getSavedTags();

		for (String tag : tags)
		{
			if (TextUtils.isEmpty(tag)) continue;

			tag = "#" + tag;
			tag = tag.replace("#@", "@");
			tag = tag.replace("##", "#");
			LinearLayout tagView = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.search_item_stub, null);
			((TextView)tagView.findViewById(R.id.title)).setText(tag);
			tagView.setOnClickListener(this);
			tagView.setOnLongClickListener(this);
			tagView.setTag(tag);
			tagView.setTag(R.id.TAG_TYPE, TYPE_SAVED);
			mSavedSearches.addView(tagView);
		}

		if (mSavedSearches.getChildCount() > 0)
		{
			mSavedSearches.getChildAt(mSavedSearches.getChildCount() - 1).findViewById(R.id.divider).setVisibility(View.GONE);
			((View)mSavedSearches.getParent()).setVisibility(View.VISIBLE);
		}
	}

	public void setupRecentSearches()
	{
		mRecentSearches.removeAllViews();
		ArrayList<String> tags = SettingsManager.getRecentSearches();
		int size = tags.size();

		for (int index = size - 1; index > -1; index--)
		{
			if (TextUtils.isEmpty(tags.get(index))) continue;

			String tag = tags.get(index);
			//tag = "#" + tag;
			tag = tag.replace("#@", "@");
			tag = tag.replace("##", "#");
			LinearLayout tagView = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.search_item_stub, null);
			((TextView)tagView.findViewById(R.id.title)).setText(tag);
			tagView.setOnClickListener(this);
			tagView.setOnLongClickListener(this);
			tagView.setTag(tag);
			tagView.setTag(R.id.TAG_TYPE, TYPE_RECENT);
			mRecentSearches.addView(tagView);

			if (index == 0)
			{
				tagView.findViewById(R.id.divider).setVisibility(View.GONE);
			}
		}

		if (mRecentSearches.getChildCount() > 0) ((View)mRecentSearches.getParent()).setVisibility(View.VISIBLE);
	}

	public void setupMutedSearches()
	{
		mMutedSearches.removeAllViews();
		String[] tags = SettingsManager.getMutedTags();

		for (String tag : tags)
		{
			if (TextUtils.isEmpty(tag)) continue;

			LinearLayout tagView = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.search_item_stub, null);
			tag = "#" + tag;
			tag = tag.replace("#@", "@");
			tag = tag.replace("##", "#");
			((TextView)tagView.findViewById(R.id.title)).setText(tag);
			tagView.setOnClickListener(this);
			tagView.setOnLongClickListener(this);
			tagView.setTag(tag);
			tagView.setTag(R.id.TAG_TYPE, TYPE_MUTED);
			mMutedSearches.addView(tagView);
		}

		if (mMutedSearches.getChildCount() > 0)
		{
			mMutedSearches.getChildAt(mMutedSearches.getChildCount() - 1).findViewById(R.id.divider).setVisibility(View.GONE);
			((View)mMutedSearches.getParent()).setVisibility(View.VISIBLE);
		}
	}

	@Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
	{
		if (actionId == EditorInfo.IME_ACTION_SEARCH)
		{
			search();
		}

		return false;
	}

	public void search()
	{
		String searchText = ((TextView)rootView.findViewById(R.id.search_box)).getText().toString();
		searchText = searchText.trim();

		if (searchText.length() < 1) return;

		SettingsManager manager = SettingsManager.getInstance();
		manager.addSearchHistory(searchText);
		Intent searchIntent = new Intent(getContext(), SearchResultsActivity.class);
		searchIntent.putExtra(Constants.EXTRA_TAG_NAME, searchText);
		startActivityForResult(searchIntent, 1);
	}

	@Override public void onClick(View v)
	{
		if (v.getId() == R.id.up_button)
		{
			getActivity().finish();
		}
		else
		{
			SettingsManager manager = SettingsManager.getInstance();
			manager.addSearchHistory(v.getTag().toString());
			Intent searchIntent = new Intent(getContext(), SearchResultsActivity.class);
			searchIntent.putExtra(Constants.EXTRA_TAG_NAME, v.getTag().toString());
			startActivity(searchIntent);
		}
	}

	@Override public boolean onLongClick(final View v)
	{
		final int mode = (Integer)v.getTag(R.id.TAG_TYPE);

		String[] options;

		if (mode == TYPE_TRENDING)
		{
			options = new String[]{getString(R.string.open_tag_formatted, (String)v.getTag()), getString(R.string.save)};
		}
		else
		{
			options = new String[]{getString(R.string.open_tag_formatted, (String)v.getTag()), getString(R.string.remove)};
		}

		DialogBuilder.create(getContext())
			.setTitle(R.string.please_select)
			.setItems(options, new DialogInterface.OnClickListener()
			{
				@Override public void onClick(DialogInterface arg0, int index)
				{
					if (index == 0)
					{
						v.performClick();
						return;
					}

					SettingsManager settings = SettingsManager.getInstance();
					if (mode == TYPE_MUTED)
					{
						settings.unmuteTag((String)v.getTag());
						mMutedSearches.removeView(v);

						if (mMutedSearches.getChildCount() < 1) ((View)mMutedSearches.getParent()).setVisibility(View.GONE);
					}
					else if (mode == TYPE_RECENT)
					{
						settings.removeSearchHistory((String)v.getTag());
						mRecentSearches.removeView(v);

						if (mRecentSearches.getChildCount() < 1) ((View)mRecentSearches.getParent()).setVisibility(View.GONE);
					}
					else if (mode == TYPE_SAVED)
					{
						settings.unsaveTag((String)v.getTag());
						mSavedSearches.removeView(v);

						if (mSavedSearches.getChildCount() < 1) ((View)mSavedSearches.getParent()).setVisibility(View.GONE);
					}
					else if (mode == TYPE_TRENDING)
					{
						settings.saveTag((String)v.getTag());
					}
				}
			})
		.show();

		return true;
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		outState.putSerializable("trending", (ArrayList<?>)trendingTags);
		super.onSaveInstanceState(outState);
	}

	@Override public void onResume()
	{
		super.onResume();

		setupSavedSearches();
		setupRecentSearches();
		setupMutedSearches();
	}
}