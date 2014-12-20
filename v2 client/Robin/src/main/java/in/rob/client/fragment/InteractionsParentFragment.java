package in.rob.client.fragment;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.utils.Views.OnClick;
import in.rob.client.R;
import in.rob.client.fragment.base.BaseFragment;

@Injectable
public class InteractionsParentFragment extends BaseFragment
{
	@InjectView private FrameLayout fragmentHolder;
	@InjectView private TextView mentionsButton;
	@InjectView private TextView interactionsButton;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.interactions_switch_layout, container, false);
		Views.inject(this, view);

		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		onMentionsButtonClick(null);
	}

	@OnClick public void onMentionsButtonClick(View v)
	{
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragment_holder, new MentionsFragment()).commit();
		interactionsButton.setTextColor(0xffaaaaaa);
		mentionsButton.setTextColor(0xff000000);
	}

	@OnClick public void onInteractionsButtonClick(View v)
	{
		FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
		fragmentTransaction.replace(R.id.fragment_holder, new InteractionsFragment()).commit();
		interactionsButton.setTextColor(0xff000000);
		mentionsButton.setTextColor(0xffaaaaaa);
	}
}
