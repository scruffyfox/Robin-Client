package in.rob.client;

import android.test.AndroidTestCase;

import junit.framework.Assert;

import in.lib.utils.BitUtils;

public class BitTest extends AndroidTestCase
{
	public void testBitCheck()
	{
		/* OPTIONS	= 0b00000011
		 * BIT_1	= 0b00000001
		 * BIT_2	= 0b00000010
		 * BIT_3	= 0b00000100
		 */

		int OPTIONS = 0x3;
		int BIT_1 = 0x1;
		int BIT_2 = 0x2;
		int BIT_3 = 0x4;

		Assert.assertEquals(true, BitUtils.contains(OPTIONS, BIT_1));
		Assert.assertEquals(true, BitUtils.contains(OPTIONS, BIT_2));
		Assert.assertEquals(true, BitUtils.contains(OPTIONS, BIT_1, BIT_2));
		Assert.assertEquals(false, BitUtils.contains(OPTIONS, BIT_3));
	}

	public void testAndBit()
	{
		/* BIT_1	= 0b00000001
		 * BIT_2	= 0b00000010
		 * BIT_3	= 0b00000100
		 * OUTPUT	= 0b00000000
		 */

		int BIT_1 = 0x1;
		int BIT_2 = 0x2;
		int BIT_3 = 0x4;

		Assert.assertEquals(0x0, BitUtils.and(BIT_1, BIT_2, BIT_3));
		Assert.assertEquals(BIT_1, BitUtils.and(BIT_1, BIT_1));
	}

	public void testOrBit()
	{
		/* BIT_1	= 0b00000001
		 * BIT_2	= 0b00000010
		 * BIT_3	= 0b00000100
		 * OUTPUT	= 0b00000111
		 */

		int BIT_1 = 0x1;
		int BIT_2 = 0x2;
		int BIT_3 = 0x4;

		Assert.assertEquals(0x7, BitUtils.or(BIT_1, BIT_2, BIT_3));
		Assert.assertEquals(BIT_1, BitUtils.or(BIT_1, BIT_1));
		Assert.assertEquals(0x0, BitUtils.or(0x0, 0x0));
	}

	public void testXorBit()
	{
		/* BIT_1	= 0b00000001
		 * BIT_2	= 0b00000010
		 * BIT_3	= 0b00000100
		 * OUTPUT	= 0b00000111
		 */

		int BIT_1 = 0x1;
		int BIT_2 = 0x2;
		int BIT_3 = 0x4;

		Assert.assertEquals(0x0, BitUtils.xor(0x0, 0x0));
		Assert.assertEquals(BIT_1, BitUtils.xor(0x0, BIT_1));
		Assert.assertEquals(0x0, BitUtils.xor(BIT_1, BIT_1));
		Assert.assertEquals(0x7, BitUtils.xor(BIT_1, BIT_2, BIT_3));
	}
}