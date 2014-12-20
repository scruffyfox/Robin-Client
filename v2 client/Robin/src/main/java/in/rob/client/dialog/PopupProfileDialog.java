package in.rob.client.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import in.lib.Constants;
import in.lib.manager.ImageOptionsManager;
import in.lib.utils.Views;
import in.lib.utils.Views.InjectView;
import in.lib.utils.Views.Injectable;
import in.lib.view.AvatarImageView;
import in.lib.view.LinkTouchMovementMethod;
import in.lib.view.LinkedTextView;
import in.model.User;
import in.rob.client.R;
import lombok.Getter;

@Injectable
public class PopupProfileDialog extends Activity
{
	@InjectView private AvatarImageView avatar;
	@InjectView private ImageView cover;
	@InjectView private LinkedTextView bio;
	@InjectView private TextView followCount;
	@InjectView private TextView fullName;
	@InjectView private TextView username;
	@Getter private User user;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		retrieveArguments(savedInstanceState == null ? getIntent().getExtras() : savedInstanceState);
		setContentView(R.layout.popup_profile_dialog);
		Views.inject(this);
		setWindowMode();

		if (user != null)
		{
			populateView();
		}
	}

	private void setWindowMode()
	{
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	}

	public void retrieveArguments(Bundle args)
	{
		user = (User)args.getParcelable(Constants.EXTRA_USER);
	}

	@Override protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		if (user != null)
		{
			outState.putParcelable(Constants.EXTRA_USER, user);
		}
	}

	private void populateView()
	{
		bio.setText(getUser().getDescription());
		bio.setLinkMovementMethod(LinkTouchMovementMethod.getInstance());
		username.setText(getUser().getFormattedMentionNameTitle());
		fullName.setText(getUser().getFormattedMentionNameSubTitle());
		followCount.setText(getString(R.string.follows_following, getUser().getFollowingCount(), getUser().getFollowerCount()));

		avatar.setUser(getUser());

		if (!getUser().isCoverDefault())
		{
			ImageLoader.getInstance().displayImage(getUser().getCoverUrl(), cover, ImageOptionsManager.getInstance().getCoverImageOptions());
		}
	}
}