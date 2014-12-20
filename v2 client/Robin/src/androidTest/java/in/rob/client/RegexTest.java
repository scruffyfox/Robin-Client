package in.rob.client;

import junit.framework.Assert;
import junit.framework.TestCase;

import in.lib.utils.Regex;

public class RegexTest extends TestCase
{
	/**
	 * Will test the validity of various URLs
	 */
	public void testUrlValidity()
	{
		String[] validInputs = {
			"google.com",
			"http://google.com",
			"https://google.com",
			"google.com/",
			"http://google.com/",
			"https://google.com/",
			"https://www.google.co.uk/search?q=robin+for+app.net",

//			"t.co",
//			"http://t.co",
//			"t.co/",
//			"http://t.co/",
//			"http://t.co/123456"
		};

		String[] invalidInputs = {
			"google.invalid",
			"ftp://google.com",
			"ssh://google.com",
			"not a url"
		};

		for (String validInput : validInputs)
		{
			Assert.assertTrue(validInput, Regex.REGEX_URL.matcher(validInput).matches());
		}

		for (String invalidInput : invalidInputs)
		{
			Assert.assertFalse(invalidInput, Regex.REGEX_URL.matcher(invalidInput).matches());
		}
	}
}
