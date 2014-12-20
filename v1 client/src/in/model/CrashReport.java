package in.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.callumtaylor.asynchttp.obj.entity.JsonEntity;
import android.util.Log;

import com.google.gson.JsonObject;

@ToString public class CrashReport implements Serializable
{
	@Getter @Setter private String userId = "unknown";

	// App information
	@Getter @Setter private String version = "unknown";
	@Getter @Setter private String packageName = "unknown";
	@Getter @Setter private String versionCode = "0";

	// User information
	@Getter @Setter private String deviceId = "";
	@Getter @Setter private String pushId = "";

	// Device information
	@Getter @Setter private String model = "unknown";
	@Getter @Setter private String manufacturer = "unknown";
	@Getter @Setter private String osVersion = "unknown";
	@Getter @Setter private String screenSize = "0x0";
	@Getter @Setter private String screenDensity = "0x0";

	@Getter @Setter private Throwable exception = new Throwable();
	@Getter @Setter private String additionalMessage = "";
	@Getter @Setter private long timestamp = 0L;

	public JsonEntity toEntity()
	{
		try
		{
			JsonObject postData = new JsonObject();
			postData.addProperty("user_id", userId);
			postData.addProperty("app_version", version);
			postData.addProperty("app_build", versionCode);
			postData.addProperty("device_id", deviceId);
			postData.addProperty("device_make", manufacturer + " " + model);
			postData.addProperty("device_version", osVersion);
			postData.addProperty("report", Log.getStackTraceString(exception) + "\r\n" + additionalMessage);
			postData.addProperty("date", timestamp / 1000);

			JsonEntity entity = new JsonEntity(postData);
			return entity;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}