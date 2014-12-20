package in.rob.client.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.lib.manager.SettingsManager;
import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.utils.Views.OnClick;
import in.lib.view.SettingContainerView;
import in.rob.client.R;
import in.rob.client.fragment.base.BaseFragment;

@Injectable
public class GeneralSettingsFragment extends BaseFragment
{
	@InjectView(R.id.quick_post) private SettingContainerView quickPost;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.general_settings_view, container, false);
		Views.inject(this, view);
		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		quickPost.getCheckableView().setChecked(SettingsManager.getInstance().isQuickPostEnabled());
	}

	@OnClick public void onImageProviderClick(SettingContainerView v)
	{

	}

	@OnClick public void onShakeRefreshClick(SettingContainerView v)
	{

	}

	@OnClick public void onQuickPostClick(SettingContainerView v)
	{
		SettingsManager.getInstance().setQuickPostEnabled(v.getCheckableView().isChecked());
		SettingsManager.getInstance().toggleQuickPost();
	}

	@OnClick public void onInlineWifiClick(SettingContainerView v)
	{

	}

	@OnClick public void onStreamMarkersClick(SettingContainerView v)
	{

	}

	@Override public void onStop()
	{
		super.onStop();
		SettingsManager.getInstance().save();
	}
}
