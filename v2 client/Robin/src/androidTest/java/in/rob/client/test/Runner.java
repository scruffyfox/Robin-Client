package in.rob.client.test;

import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;

import junit.framework.TestSuite;

import in.rob.client.BitTest;
import in.rob.client.RegexTest;
import in.rob.client.StyledEntityTest;

public class Runner extends InstrumentationTestRunner
{
	@Override public TestSuite getAllTests()
	{
		InstrumentationTestSuite suite = new InstrumentationTestSuite(this);
		suite.addTestSuite(RegexTest.class);
		suite.addTestSuite(BitTest.class);
		suite.addTestSuite(StyledEntityTest.class);

		return suite;
	}

	@Override public ClassLoader getLoader()
	{
		return Runner.class.getClassLoader();
	}
}