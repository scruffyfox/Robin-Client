package in.rob.client.fragment.settings;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.lib.builder.DialogBuilder;
import in.lib.manager.SettingsManager;
import in.lib.utils.Views;
import in.lib.utils.Views.Injectable;
import in.lib.utils.Views.OnClick;
import in.lib.view.LinkTouchMovementMethod;
import in.lib.view.SettingContainerView;
import in.rob.client.R;
import in.rob.client.fragment.base.BaseFragment;

@Injectable
public class AppearanceSettingsFragment extends BaseFragment
{
	public static interface OnMultiChoiceDialogDismissed
	{
		public void onDialogDismissed(int finalBit);
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.appearance_settings_view, container, false);
		Views.inject(this, view);
		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		((SettingContainerView)getView().findViewById(R.id.web_readability_mode)).getCheckableView().setChecked(SettingsManager.getInstance().isWebReadabilityModeEnabled());
		((SettingContainerView)getView().findViewById(R.id.non_following)).getCheckableView().setChecked(SettingsManager.getInstance().isNonFollowingMentionEnabled());
	}

	private void createMultiChoiceDialog(int valuesRes, int optionsRes, int settingBit, final OnMultiChoiceDialogDismissed listener)
	{
		final int[] options = getResources().getIntArray(valuesRes);
		final boolean[] selectedItems = new boolean[options.length];

		for (int index = 0; index < selectedItems.length; index++)
		{
			selectedItems[index] = (settingBit & options[index]) == options[index];
		}

		Builder builder = DialogBuilder.create(getContext());
		builder.setTitle(R.string.please_select);
		builder.setMultiChoiceItems(optionsRes, selectedItems, new OnMultiChoiceClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which, boolean isChecked)
			{
				selectedItems[which] = isChecked;
			}
		});
		builder.setPositiveButton(R.string.done, new OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				int finalBit = 0;
				for (int index = 0; index < selectedItems.length; index++)
				{
					if (selectedItems[index])
					{
						finalBit |= options[index];
					}
				}

				listener.onDialogDismissed(finalBit);
			}
		});
		builder.setNegativeButton(R.string.cancel, null);
		builder.show();
	}

	@OnClick public void onNonFollowingClick(SettingContainerView view)
	{
		SettingsManager.getInstance().setNonFollowingMentionEnabled(view.getCheckableView().isChecked());
	}

	@OnClick public void onSingleClickLinksClick(SettingContainerView view)
	{
		createMultiChoiceDialog(R.array.single_click_values, R.array.single_click_options, SettingsManager.getInstance().getSingleClickBit(), new OnMultiChoiceDialogDismissed()
		{
			@Override public void onDialogDismissed(int finalBit)
			{
				SettingsManager.getInstance().setSingleClickBit(finalBit);
				LinkTouchMovementMethod.getInstance().recreate();
			}
		});
	}

	@OnClick public void onShowElementsClick(SettingContainerView view)
	{
		createMultiChoiceDialog(R.array.show_hide_element_values, R.array.show_hide_element_options, SettingsManager.getInstance().getShowHideBit(), new OnMultiChoiceDialogDismissed()
		{
			@Override public void onDialogDismissed(int finalBit)
			{
				SettingsManager.getInstance().setShowHideBit(finalBit);
			}
		});
	}

	@OnClick public void onInAppViewerClick(SettingContainerView view)
	{
		createMultiChoiceDialog(R.array.in_app_viewer_values, R.array.in_app_viewer_options, SettingsManager.getInstance().getInAppViewerBit(), new OnMultiChoiceDialogDismissed()
		{
			@Override public void onDialogDismissed(int finalBit)
			{
				SettingsManager.getInstance().setInAppViewerBit(finalBit);
			}
		});
	}

	@OnClick public void onWebReadabilityModeClick(SettingContainerView view)
	{
		SettingsManager.getInstance().setWebReadabilityModeEnabled(view.getCheckableView().isChecked());
	}

	@OnClick public void onPostEmphasisClick(SettingContainerView view)
	{
		createMultiChoiceDialog(R.array.emphasis_values, R.array.emphasis_options, SettingsManager.getInstance().getEmphasisBit(), new OnMultiChoiceDialogDismissed()
		{
			@Override public void onDialogDismissed(int finalBit)
			{
				SettingsManager.getInstance().setEmphasisBit(finalBit);
			}
		});
	}

	@Override public void onStop()
	{
		super.onStop();
		SettingsManager.getInstance().save();
	}
}
