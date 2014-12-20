package in.lib.utils;

import in.model.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

public class CodeUtils
{
	/**
	 * Parser for name and username in post items
	 * @param code The input string
	 * @param user The user object to match with
	 * @return The formatted name as array. First cell represents the first view
	 * and the send, represents the second view
	 */
	public static String[] nameOrderParse(String code, User user)
	{
		code = code.replaceAll("(\\{\\#username\\})", user.getMentionName());
		code = code.replaceAll("(\\{\\#firstname\\})", user.getFirstName());
		code = code.replaceAll("(\\{\\#lastname\\})", user.getLastName());
		code = code.replaceAll("(\\{\\#fullname\\})", user.getUserName());

		String functionPattern = "(\\w+)?(\\[[0-9,]+\\])";
		Pattern p = Pattern.compile(functionPattern);
		Matcher m = p.matcher(code);
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
				String formatted = "";

				for (String index : indexes)
				{
					try
					{
						int idx = Integer.parseInt(index);

						if (idx >= 0 && idx < str.length())
						{
							formatted += str.charAt(idx);
						}
					}
					catch (Exception e){}
				}

				code = code.replace(match, formatted);
			}
		}

		functionPattern = "(\\w+)\\s?\\((((\\w+)?(\\s+)?(,\\s?)*?)+)\\)";
		p = Pattern.compile(functionPattern);
		m = p.matcher(code);
		while (m.find())
		{
			String match = m.group();
			String function = match.substring(0, match.indexOf('('));
			String[] params = match.substring(match.indexOf('(') + 1, match.indexOf(')')).split("[,]");
			String formattedParam = "";

			if (params.length > 0)
			{
				if (function.equalsIgnoreCase("uc"))
				{
					for (String param : params)
					{
						if (TextUtils.isEmpty(param)) continue;
						formattedParam += ("" + param.charAt(0)).toUpperCase();

						if (param.length() > 1)
						{
							formattedParam += param.substring(1, param.length());
						}

						formattedParam += " ";
					}

					formattedParam = formattedParam.substring(0, formattedParam.length() - 1);
				}
				else if (function.equalsIgnoreCase("lc"))
				{
					for (String param : params)
					{
						if (TextUtils.isEmpty(param)) continue;
						formattedParam += param.toLowerCase() + " ";
					}

					formattedParam = formattedParam.substring(0, formattedParam.length() - 1);
				}
				else if (function.equalsIgnoreCase("cap"))
				{
					for (String param : params)
					{
						if (TextUtils.isEmpty(param)) continue;
						formattedParam += param.toUpperCase() + " ";
					}

					formattedParam = formattedParam.substring(0, formattedParam.length() - 1);
				}
			}

			code = code.replace(match, formattedParam);
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
			else
			{
				code = code.substring(0, code.indexOf('|'));
			}
		}

		return new String[]{code.trim(), ""};
	}
}