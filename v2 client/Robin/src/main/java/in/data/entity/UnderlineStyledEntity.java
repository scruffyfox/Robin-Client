package in.data.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import in.lib.utils.Regex;

public class UnderlineStyledEntity extends StyledEntity
{
	@Override public List<UnderlineStyledEntity> createListFrom(String text)
	{
		List<UnderlineStyledEntity> entities = new ArrayList<UnderlineStyledEntity>();

		Matcher slash = Regex.MATCH_UNDERSCORE.matcher(text);
		while (slash.find())
		{
			String group = slash.group();
			int start = slash.start();
			int end = slash.end();

			UnderlineStyledEntity entity = new UnderlineStyledEntity();
			entity.setPos(start);
			entity.setLength(end - start);
			entity.setType(Type.UNDERLINE);

			entities.add(entity);
		}

		return entities;
	}
}
