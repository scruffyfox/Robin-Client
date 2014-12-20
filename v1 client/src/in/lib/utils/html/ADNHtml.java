package in.lib.utils.html;

import in.lib.view.spannable.HashtagClickableSpan;
import in.lib.view.spannable.MarkDownClickableSpan;
import in.lib.view.spannable.MentionClickableSpan;
import in.lib.view.spannable.UrlClickableSpan;

import java.io.IOException;
import java.io.StringReader;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.graphics.Typeface;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ParagraphStyle;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;

public class ADNHtml
{
	// can't inherit android.text.Html, the constructor is private

	private static class HtmlParser
	{
		private static final HTMLSchema schema = new HTMLSchema();
	}

	public static Spanned fromHtml(String source)
	{
		return fromHtml(source, null, null);
	}

	public static Spanned fromHtml(String source, ImageGetter imageGetter, TagHandler tagHandler)
	{
		Parser parser = new Parser();

		try
		{
			parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
		}
		catch (org.xml.sax.SAXNotRecognizedException e)
		{
			// Should not happen.
			throw new RuntimeException(e);
		}
		catch (org.xml.sax.SAXNotSupportedException e)
		{
			// Should not happen.
			throw new RuntimeException(e);
		}

		HtmlToSpannedConverter converter = new HtmlToSpannedConverter(source, imageGetter, tagHandler, parser);
		return converter.convert();
	}
}

class HtmlToSpannedConverter implements ContentHandler
{
	private final String mSource;
	private final XMLReader mReader;
	private final SpannableStringBuilder mSpannableStringBuilder;
	private final Html.TagHandler mTagHandler;

	public HtmlToSpannedConverter(String source, Html.ImageGetter imageGetter, Html.TagHandler tagHandler, Parser parser)
	{
		mSource = source;
		mSpannableStringBuilder = new SpannableStringBuilder();
		mTagHandler = tagHandler;
		mReader = parser;
	}

	public Spanned convert()
	{
		mReader.setContentHandler(this);

		try
		{
			mReader.parse(new InputSource(new StringReader(mSource)));
		}
		catch (IOException e)
		{
			// We are reading from a string. There should not be IO problems.
			throw new RuntimeException(e);
		}
		catch (SAXException e)
		{
			// TagSoup doesn't throw parse exceptions.
			throw new RuntimeException(e);
		}

		// Fix flags and range for paragraph-type markup.
		Object[] obj = mSpannableStringBuilder.getSpans(0, mSpannableStringBuilder.length(), ParagraphStyle.class);
		for (int i = 0; i < obj.length; i++)
		{
			int start = mSpannableStringBuilder.getSpanStart(obj[i]);
			int end = mSpannableStringBuilder.getSpanEnd(obj[i]);

			// If the last line of the range is blank, back off by one.
			if (end - 2 >= 0)
			{
				if (mSpannableStringBuilder.charAt(end - 1) == '\n' && mSpannableStringBuilder.charAt(end - 2) == '\n')
				{
					end--;
				}
			}

			if (end == start)
			{
				mSpannableStringBuilder.removeSpan(obj[i]);
			}
			else
			{
				mSpannableStringBuilder.setSpan(obj[i], start, end, Spannable.SPAN_PARAGRAPH);
			}
		}

		return mSpannableStringBuilder;
	}

	private void handleStartTag(String tag, Attributes attributes)
	{
		if (tag.equalsIgnoreCase("strong"))
		{
			start(mSpannableStringBuilder, new Bold());
		}
		else if (tag.equalsIgnoreCase("b"))
		{
			start(mSpannableStringBuilder, new Bold());
		}
		else if (tag.equalsIgnoreCase("em"))
		{
			start(mSpannableStringBuilder, new Italic());
		}
		else if (tag.equalsIgnoreCase("i"))
		{
			start(mSpannableStringBuilder, new Italic());
		}
		else if (tag.equalsIgnoreCase("a"))
		{
			startA(mSpannableStringBuilder, attributes);
		}
		else if (tag.equalsIgnoreCase("md"))
		{
			startMd(mSpannableStringBuilder, attributes);
		}
		else if (tag.equalsIgnoreCase("u"))
		{
			start(mSpannableStringBuilder, new Underline());
		}
		else if (tag.equalsIgnoreCase("sub"))
		{
			start(mSpannableStringBuilder, new Sub());
		}
		else if (tag.equalsIgnoreCase("span") && attributes.getValue("", "itemprop") != null)
		{
			if (attributes.getValue("", "itemprop").equals("mention"))
			{
				startADNMention(mSpannableStringBuilder, attributes);
			}
			else if (attributes.getValue("", "itemprop").equals("hashtag"))
			{
				startADNHashtag(mSpannableStringBuilder, attributes);
			}
		}
		else if (mTagHandler != null)
		{
			mTagHandler.handleTag(true, tag, mSpannableStringBuilder, mReader);
		}
	}

	private void handleEndTag(String tag)
	{
		if (tag.equalsIgnoreCase("br"))
		{
			handleBr(mSpannableStringBuilder);
		}
		else if (tag.equalsIgnoreCase("strong"))
		{
			end(mSpannableStringBuilder, Bold.class, new StyleSpan(Typeface.BOLD));
		}
		else if (tag.equalsIgnoreCase("b"))
		{
			end(mSpannableStringBuilder, Bold.class, new StyleSpan(Typeface.BOLD));
		}
		else if (tag.equalsIgnoreCase("em"))
		{
			end(mSpannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
		}
		else if (tag.equalsIgnoreCase("i"))
		{
			end(mSpannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
		}
		else if (tag.equalsIgnoreCase("a"))
		{
			endADNTag(mSpannableStringBuilder);
		}
		else if (tag.equalsIgnoreCase("md"))
		{
			endADNTag(mSpannableStringBuilder);
		}
		else if (tag.equalsIgnoreCase("u"))
		{
			end(mSpannableStringBuilder, Underline.class, new UnderlineSpan());
		}
		else if (tag.equalsIgnoreCase("sup"))
		{
			end(mSpannableStringBuilder, Super.class, new SuperscriptSpan());
		}
		else if (tag.equalsIgnoreCase("sub"))
		{
			end(mSpannableStringBuilder, Sub.class, new SubscriptSpan());
		}
		else if (tag.equalsIgnoreCase("span"))
		{
			endADNTag(mSpannableStringBuilder);
		}
		else if (mTagHandler != null)
		{
			mTagHandler.handleTag(false, tag, mSpannableStringBuilder, mReader);
		}
	}

	private static void handleBr(SpannableStringBuilder text)
	{
		text.append("\n");
	}

	private static Object getLast(Spanned text, Class kind)
	{
		/*
		 * This knows that the last returned object from getSpans() will be the
		 * most recently added.
		 */
		Object[] objs = text.getSpans(0, text.length(), kind);

		if (objs.length == 0)
		{
			return null;
		}
		else
		{
			return objs[objs.length - 1];
		}
	}

	private static void start(SpannableStringBuilder text, Object mark)
	{
		int len = text.length();
		text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK);
	}

	private static void end(SpannableStringBuilder text, Class kind, Object repl)
	{
		int len = text.length();
		Object obj = getLast(text, kind);
		int where = text.getSpanStart(obj);

		text.removeSpan(obj);

		if (where != len)
		{
			text.setSpan(repl, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return;
	}

	private static void startADNMention(SpannableStringBuilder text, Attributes attributes)
	{
		String username = attributes.getValue("", "data-mention-name");
		String userid = attributes.getValue("", "data-mention-id");

		int len = text.length();
		text.setSpan(new Mention(username, userid), len, len, Spannable.SPAN_MARK_MARK);
	}

	private static void startADNHashtag(SpannableStringBuilder text, Attributes attributes)
	{
		String tag = attributes.getValue("", "data-hashtag-name");

		int len = text.length();
		text.setSpan(new Hashtag(tag), len, len, Spannable.SPAN_MARK_MARK);
	}

	private static void startA(SpannableStringBuilder text, Attributes attributes)
	{
		String href = attributes.getValue("", "href");

		int len = text.length();
		text.setSpan(new Href(href), len, len, Spannable.SPAN_MARK_MARK);
	}

	private static void startMd(SpannableStringBuilder text, Attributes attributes)
	{
		String href = attributes.getValue("", "href");
		String anchor = attributes.getValue("", "data-anchor");

		int len = text.length();
		text.setSpan(new MDLink(href, anchor), len, len, Spannable.SPAN_MARK_MARK);
	}

	private static void endADNTag(SpannableStringBuilder text)
	{
		int len = text.length();
		Object obj = getLast(text, ADNTag.class);
		int where = text.getSpanStart(obj);

		text.removeSpan(obj);

		if (where != len)
		{
			if (obj instanceof Mention)
			{
				Mention m = (Mention)obj;
				text.setSpan(new MentionClickableSpan(m.mUsername, m.mUserid), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			else if (obj instanceof Hashtag)
			{
				Hashtag h = (Hashtag)obj;
				text.setSpan(new HashtagClickableSpan(h.mTag), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			else if (obj instanceof Href)
			{
				Href h = (Href)obj;
				text.setSpan(new UrlClickableSpan(h.mHref), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			else if (obj instanceof MDLink)
			{
				MDLink h = (MDLink)obj;
				text.setSpan(new MarkDownClickableSpan(h.mHref, h.text), where, where + h.text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
	}

	@Override public void setDocumentLocator(Locator locator)
	{
	}

	@Override public void startDocument() throws SAXException
	{
	}

	@Override public void endDocument() throws SAXException
	{
	}

	@Override public void startPrefixMapping(String prefix, String uri) throws SAXException
	{
	}

	@Override public void endPrefixMapping(String prefix) throws SAXException
	{
	}

	@Override public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		handleStartTag(localName, attributes);
	}

	@Override public void endElement(String uri, String localName, String qName) throws SAXException
	{
		handleEndTag(localName);
	}

	@Override public void characters(char ch[], int start, int length) throws SAXException
	{
		StringBuilder sb = new StringBuilder();

		/*
		 * Ignore whitespace that immediately follows other whitespace; newlines
		 * count as spaces.
		 */

		for (int i = 0; i < length; i++)
		{
			char c = ch[i + start];

			if (c == ' ' || c == '\n')
			{
				char pred;
				int len = sb.length();

				if (len == 0)
				{
					len = mSpannableStringBuilder.length();

					if (len == 0)
					{
						pred = '\n';
					}
					else
					{
						pred = mSpannableStringBuilder.charAt(len - 1);
					}
				}
				else
				{
					pred = sb.charAt(len - 1);
				}

				if (pred != ' ' && pred != '\n')
				{
					sb.append(' ');
				}
			}
			else
			{
				sb.append(c);
			}
		}

		mSpannableStringBuilder.append(sb);
	}

	@Override public void ignorableWhitespace(char ch[], int start, int length) throws SAXException
	{
	}

	@Override public void processingInstruction(String target, String data) throws SAXException
	{
	}

	@Override public void skippedEntity(String name) throws SAXException
	{
	}

	private static class Bold{}
	private static class Italic{}
	private static class Underline{}
	private static class Super{}
	private static class Sub{}

	private static class ADNTag{}

	private static class Href extends ADNTag
	{
		public String mHref;

		public Href(String href)
		{
			mHref = href;
		}
	}

	private static class MDLink extends ADNTag
	{
		public String mHref;
		public String text;

		public MDLink(String href, String text)
		{
			mHref = href;
			this.text = text;
		}
	}

	private static class Mention extends ADNTag
	{
		public String mUsername;
		public String mUserid;

		public Mention(String username, String userid)
		{
			mUsername = username;
			mUserid = userid;
		}
	}

	private static class Hashtag extends ADNTag
	{
		public String mTag;

		public Hashtag(String tag)
		{
			mTag = tag;
		}
	}
}