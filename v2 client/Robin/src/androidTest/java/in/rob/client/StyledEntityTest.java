package in.rob.client;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import java.util.List;

import in.data.entity.BoldStyledEntity;
import in.data.entity.ItalicStyledEntity;

public class StyledEntityTest extends AndroidTestCase
{
	public void testItalicEntity()
	{
		String input = "test *italic* text";

		List<ItalicStyledEntity> styles = new ItalicStyledEntity().createListFrom(input);
		Assert.assertEquals(5, styles.get(0).getPos());
		Assert.assertEquals(8, styles.get(0).getLength());
	}

	public void testBoldEntity()
	{
		String input = "test **bold** text";

		List<BoldStyledEntity> styles = new BoldStyledEntity().createListFrom(input);
		Assert.assertEquals(5, styles.get(0).getPos());
		Assert.assertEquals(8, styles.get(0).getLength());
	}
}