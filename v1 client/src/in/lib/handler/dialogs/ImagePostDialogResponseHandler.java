package in.lib.handler.dialogs;

import in.lib.exception.ExceptionHandler;
import in.lib.manager.ImageAPIManager;
import in.lib.manager.ImageAPIManager.Provider;
import in.lib.manager.SettingsManager;
import in.obj.annotation.Annotation;
import in.obj.annotation.ImageAnnotation;
import in.rob.client.MainApplication;
import in.rob.client.MainApplication.ApplicationType;

import java.util.List;

import net.callumtaylor.asynchttp.response.JsonResponseHandler;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class ImagePostDialogResponseHandler extends NewPostDialogResponseHandler
{
	public ImagePostDialogResponseHandler(Context context, Intent failIntent, int sendNotificationId)
	{
		super(context, failIntent, sendNotificationId);
	}

	@Override public void onSuccess()
	{
		super.onSuccess();

		if (SettingsManager.getImageProvider() == Provider.BLIMS)
		{
			String code = "";
			List<Annotation> images = getPost().getAnnotations().get(Annotation.Type.IMAGE);

			if (images != null)
			{
				for (Annotation img : images)
				{
					if (((ImageAnnotation)img).getTextUrl().toLowerCase().contains("http://bli.ms/") && ((ImageAnnotation)img).getTextUrl().length() < 20)
					{
						code = ((ImageAnnotation)img).getTextUrl().replace("http://bli.ms/", "");
						break;
					}
				}

				if (!TextUtils.isEmpty(code))
				{
					ImageAPIManager.getInstance().blimsSetPostThread(code, getPost().getId(), new JsonResponseHandler()
					{
						@Override public void onSuccess(){}
						@Override public void onFinish(boolean failed)
						{
							if (failed)
							{
								if (((MainApplication)getContext().getApplicationContext()).getApplicationType() == ApplicationType.BETA)
								{
									Exception e = new Exception(getConnectionInfo() + "\n" + getContent());
									ExceptionHandler.sendException(e);
								}
							}
						}
					});
				}
			}
		}
	}
}