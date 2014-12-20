package in.data.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import in.lib.utils.Regex;

public class ItalicStyledEntity extends StyledEntity
{
	@Override public List<ItalicStyledEntity> createListFrom(String text)
	{
		List<ItalicStyledEntity> entities = new ArrayList<ItalicStyledEntity>();

		Matcher slash = Regex.MATCH_SLASH.matcher(text);
		while (slash.find())
		{
			String group = slash.group();
			int start = slash.start();
			int end = slash.end();

			ItalicStyledEntity entity = new ItalicStyledEntity();
			entity.setPos(start);
			entity.setLength(end - start);
			entity.setType(Type.ITALIC);

			entities.add(entity);
		}

		return entities;
	}
}
