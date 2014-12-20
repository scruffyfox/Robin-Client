package in.rob.client.dialog;

import in.lib.Constants;
import in.lib.utils.Dimension;
import in.rob.client.R;
import in.rob.client.base.RobinDialogActivity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

public class AboutDialog extends RobinDialogActivity implements OnClickListener
{
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about);

		Dimension d = new Dimension(this);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			getWindow().setLayout((int)(d.getScreenWidth() / 1.5), LayoutParams.WRAP_CONTENT);
		}
		else
		{
			getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		}

		getWindow().setGravity(Gravity.CENTER);
		getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_bg));

		((ImageButton)findViewById(R.id.cancel)).setOnClickListener(this);
		((Button)findViewById(R.id.about_donate)).setOnClickListener(this);
		((Button)findViewById(R.id.feedback)).setOnClickListener(this);
		((Button)findViewById(R.id.rate)).setOnClickListener(this);
	}

	@Override public void onClick(View v)
	{
		if (v.getId() == R.id.cancel)
		{
			finish();
		}
		else if (v.getId() == R.id.about_donate)
		{
			//startActivity(new Intent(getContext(), DonationsActivity.class));
		}
		else if (v.getId() == R.id.feedback)
		{
			Intent replyIntent = new Intent(getContext(), NewPostDialog.class);
			replyIntent.putExtra(Constants.EXTRA_MENTION_NAME, "scruffyfox");
			startActivity(replyIntent);
		}
		else
		{
			Intent replyIntent = new Intent(Intent.ACTION_VIEW);
			replyIntent.setData(Uri.parse("market://details?id=" + getPackageName()));
			startActivity(replyIntent);
		}
	}
}
