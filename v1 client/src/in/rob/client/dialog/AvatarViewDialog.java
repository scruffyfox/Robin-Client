package in.rob.client.dialog;

import in.lib.Constants;
import in.rob.client.R;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

public class AvatarViewDialog extends DialogFragment
{
	private String mUrl;
	private ImageView mFsImage;

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.avatar_lightbox, container);
	}

	@Override public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_bg));

		return dialog;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (getArguments() != null && getArguments().containsKey(Constants.EXTRA_IMAGE))
		{
			mUrl = getArguments().getString(Constants.EXTRA_IMAGE);
		}
		else
		{
			return;
		}

		View root = getView();
		mFsImage = (ImageView)root.findViewById(R.id.fullscreen_image);
		ImageLoader.getInstance().displayImage(mUrl, mFsImage);
	}
}