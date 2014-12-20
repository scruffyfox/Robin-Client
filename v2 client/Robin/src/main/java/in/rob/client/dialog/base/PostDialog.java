package in.rob.client.dialog.base;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import in.lib.utils.Views;
import in.lib.utils.Views.Injectable;
import in.lib.utils.Views.OnClick;
import lombok.Getter;

@Injectable
public abstract class PostDialog extends Activity
{
	@Getter private Context context = this;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(getContentView());
		Views.inject(this);
		retrieveArguments(savedInstanceState == null ? getIntent().getExtras() : savedInstanceState);
		setWindowMode();
	}

	protected void setWindowMode()
	{
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	public void retrieveArguments(Bundle args)
	{

	}

	public abstract int getContentView();
	@OnClick public abstract void onPositiveButtonClick(View view);
	@OnClick public abstract void onNegativeButtonClick(View view);
}
