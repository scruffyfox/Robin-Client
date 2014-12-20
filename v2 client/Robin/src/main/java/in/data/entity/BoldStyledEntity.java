package in.data.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import in.lib.utils.Regex;

public class BoldStyledEntity extends StyledEntity
{
	@Override public List<BoldStyledEntity> createListFrom(String text)
	{
		List<BoldStyledEntity> entities = new ArrayList<BoldStyledEntity>();

		Matcher slash = Regex.MATCH_ASTRIX.matcher(text);
		while (slash.find())
		{
			String group = slash.group();
			int start = slash.start();
			int end = slash.end();

			BoldStyledEntity entity = new BoldStyledEntity();
			entity.setPos(start);
			entity.setLength(end - start);
			entity.setType(Type.BOLD);

			entities.add(entity);
		}

		return entities;
	}
}
