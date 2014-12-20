package in.rob.client;

import in.rob.client.base.RobinActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;

public class Licences extends RobinActivity
{
	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ImageButton upBtn = (ImageButton)getSupportActionBar().getCustomView().findViewById(R.id.up_button);
		upBtn.setVisibility(View.GONE);

		setTitle(R.string.licenses);
		WebView view = new WebView(this);
		view.loadUrl("file:///android_asset/licences.html");
		setContentView(view);
	}
}