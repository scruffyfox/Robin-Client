package in.model;

import android.os.Parcel;
import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import in.lib.manager.SettingsManager;
import in.lib.utils.CodeUtils;
import in.lib.utils.Debug;
import in.lib.utils.SerialReaderUtil;
import in.lib.utils.SerialWriterUtil;
import lombok.Data;

@Data
public class SimpleUser extends AdnModel
{
	protected String username = "";
	protected String firstName = "";
	protected String lastName = "";
	protected String avatarUrl;
	protected String formattedMentionNameTitle;
	protected String formattedMentionNameSubTitle;

	public String getFullname()
	{
		return new StringBuilder().append(firstName).append(" ").append(lastName).toString().trim();
	}

	public SimpleUser createFrom(User user)
	{
		this.id = user.getId();
		this.username = user.getUsername();
		this.firstName = user.getFirstName();
		this.lastName = user.getLastName();
		this.avatarUrl = user.getAvatarUrl();
		this.formattedMentionNameTitle = user.getFormattedMentionNameTitle();
		this.formattedMentionNameSubTitle = user.getFormattedMentionNameSubTitle();

		return this;
	}

	@Override public SimpleUser createFrom(JsonElement element)
	{
		try
		{
			JsonObject userObject = element.getAsJsonObject();

			this.id = userObject.get("id").getAsString();
			this.username = userObject.get("username").getAsString();

			if (userObject.has("name"))
			{
				String fullName = userObject.get("name").getAsString();
				String[] parts = fullName.split("\\s");
				this.firstName = (parts.length > 0 ? parts[0] : getUsername());

				if (parts.length > 1)
				{
					StringBuilder lastNameBuilder = new StringBuilder();

					for (int index = 1; index < parts.length; index++)
					{
						if (lastNameBuilder.length() > 0)
						{
							lastNameBuilder.append(" ");
						}

						if (!TextUtils.isEmpty(parts[index]))
						{
							lastNameBuilder.append(parts[index]);
						}
					}

					this.lastName = lastNameBuilder.toString();
				}
			}
			else
			{
				this.firstName = "";
				this.lastName = "";
			}

			String[] formattedName = CodeUtils.compileUserTitle(SettingsManager.getInstance().getUserTitle(), this);
			this.formattedMentionNameTitle = formattedName[0];
			this.formattedMentionNameSubTitle = formattedName.length > 1 ? formattedName[1] : "";

			this.avatarUrl = userObject.get("avatar_image").getAsJsonObject().get("url").getAsString();

			return this;
		}
		catch (Exception e)
		{
			Debug.out(e);
		}

		return null;
	}

	@Override public SimpleUser createFrom(Parcel parcel)
	{
		super.createFrom(parcel);
		return this;
	}

	@Override public List<? extends SimpleUser> createListFrom(JsonElement element)
	{
		if (element != null)
		{
			try
			{
				JsonArray userArray = element.getAsJsonArray();
				ArrayList<SimpleUser> users = new ArrayList<SimpleUser>(userArray.size());

				for (JsonElement userElement : userArray)
				{
					SimpleUser user = new SimpleUser().createFrom(userElement);

					if (user != null)
					{
						users.add(user);
					}
				}

				return users;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public String getVersion()
	{
		return "1a16bdf8-2bff-4705-8098-23b6531fb8e7";
	}

	@Override public SimpleUser read(SerialReaderUtil util)
	{
		if (super.read(util) != null)
		{
			try
			{
				String version = util.readString();
				if (!version.equals(getVersion())) return null;

				this.username = util.readString();
				this.firstName = util.readString();
				this.lastName = util.readString();
				this.avatarUrl = util.readString();
				this.formattedMentionNameTitle = util.readString();
				this.formattedMentionNameSubTitle = util.readString();

				return this;
			}
			catch (Exception e)
			{
				Debug.out(e);
			}
		}

		return null;
	}

	@Override public void write(SerialWriterUtil util)
	{
		super.write(util);

		try
		{
			util.writeString(getVersion());
			util.writeString(username);
			util.writeString(firstName);
			util.writeString(lastName);
			util.writeString(avatarUrl);
			util.writeString(formattedMentionNameTitle);
			util.writeString(formattedMentionNameSubTitle);
		}
		catch (Exception e)
		{
			Debug.out(e);
		}
	}

	@Override public boolean equals(Object object)
	{
		if (object == null)
		{
			return false;
		}

		if ((object == this) || (object instanceof SimpleUser && ((SimpleUser)object).getId().equals(getId())))
		{
			return true;
		}

		return false;
	}

	public static final Creator<SimpleUser> CREATOR = new Creator<SimpleUser>()
	{
		@Override public SimpleUser[] newArray(int size)
		{
			return new SimpleUser[size];
		}

		@Override public SimpleUser createFromParcel(Parcel source)
		{
			return new SimpleUser().createFrom(source);
		}
	};
}
