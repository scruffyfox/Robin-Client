package in.lib.utils;

import android.text.TextUtils;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.model.SimpleUser;

public class CodeUtils
{
	private static String ARRAY_PATTERN_REGEX = "(\\w+)?(\\[[0-9,]+\\])";
	private static Pattern ARRAY_PATTERN = Pattern.compile(ARRAY_PATTERN_REGEX);
	private static String FUNCTION_PATTERN_REGEX = "(\\w+)\\s?\\((((\\w+)?(\\s+)?(,\\s?)*?)+)\\)";
	private static Pattern FUNCTION_PATTERN = Pattern.compile(FUNCTION_PATTERN_REGEX);

	/**
	 * Parser for name and username in post items
	 * @param code The input string
	 * @param user The user object to match with
	 * @return The formatted name as array. First cell represents the first view
	 * and the send, represents the second view
	 */
	public static String[] compileUserTitle(String code, SimpleUser user)
	{
		code = code.replaceAll("(\\#\\{username\\})", user.getUsername());
		code = code.replaceAll("(\\#\\{firstname\\})", user.getFirstName());
		code = code.replaceAll("(\\#\\{lastname\\})", user.getLastName());
		code = code.replaceAll("(\\#\\{fullname\\})", user.getFullname());

		Matcher m = ARRAY_PATTERN.matcher(code);
		while (m.find())
		{
			String match = m.group();
			String str = match.substring(0, match.indexOf('['));
			if (TextUtils.isEmpty(str))
			{
				code = code.replace(match, "");
			}
			else
			{
				String[] indexes = match.substring(match.indexOf('[') + 1, match.indexOf(']')).split("[,]");
				StringBuilder formatted = new StringBuilder(indexes.length);

				for (String index : indexes)
				{
					try
					{
						int idx = Integer.parseInt(index);

						if (idx >= 0 && idx < str.length())
						{
							formatted.append(str.charAt(idx));
						}
					}
					catch (Exception e){}
				}

				code = code.replace(match, formatted.toString());
			}
		}

		m = FUNCTION_PATTERN.matcher(code);
		while (m.find())
		{
			String match = m.group();
			String function = match.substring(0, match.indexOf('('));
			String[] params = match.substring(match.indexOf('(') + 1, match.indexOf(')')).split("[,]");
			StringBuilder formattedParam = new StringBuilder(40);

			if (params.length > 0)
			{
				if (function.equalsIgnoreCase("uc"))
				{
					for (String param : params)
					{
						if (TextUtils.isEmpty(param)) continue;
						formattedParam.append(String.valueOf(param.charAt(0)).toUpperCase(Locale.getDefault()));

						if (param.length() > 1)
						{
							formattedParam.append(param.substring(1, param.length()));
						}

						formattedParam.append(" ");
					}

					formattedParam = formattedParam.replace(0, formattedParam.length() - 1, "");
				}
				else if (function.equalsIgnoreCase("lc"))
				{
					for (String param : params)
					{
						if (TextUtils.isEmpty(param)) continue;
						formattedParam.append(param.toLowerCase()).append(" ");
					}

					formattedParam = formattedParam.replace(0, formattedParam.length() - 1, "");
				}
				else if (function.equalsIgnoreCase("cap"))
				{
					for (String param : params)
					{
						if (TextUtils.isEmpty(param)) continue;
						formattedParam.append(param.toUpperCase(Locale.getDefault()));
					}

					formattedParam = formattedParam.replace(0, formattedParam.length() - 1, "");
				}
			}

			code = code.replace(match, formattedParam.toString());
		}

		if (code.indexOf('|') > -1)
		{
			String[] parts = code.split("[|]");

			if (parts.length > 2)
			{
				String[] finalParts = new String[2];
				finalParts[0] = parts[0].trim();

				for (int index = 1; index < parts.length; index++)
				{
					finalParts[1] = (parts[index] + "|");
				}

				finalParts[1] = finalParts[1].substring(0, finalParts[1].length() - 1).trim();

				return finalParts;
			}
			else if (parts.length == 2)
			{
				return new String[]{parts[0].trim(), parts[1].trim()};
			}
		}

		return new String[]{code.trim()};
	}
}