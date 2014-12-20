package in.lib.utils;

import java.util.regex.Pattern;

public class Regex
{
	private static final String UNICODE_SPACES = "[" +
			"\\u0009-\\u000d" +     //  # White_Space # Cc   [5] <control-0009>..<control-000D>
			"\\u0020" +             // White_Space # Zs       SPACE
			"\\u0085" +             // White_Space # Cc       <control-0085>
			"\\u00a0" +             // White_Space # Zs       NO-BREAK SPACE
			"\\u1680" +             // White_Space # Zs       OGHAM SPACE MARK
			"\\u180E" +             // White_Space # Zs       MONGOLIAN VOWEL SEPARATOR
			"\\u2000-\\u200a" +     // # White_Space # Zs  [11] EN QUAD..HAIR SPACE
			"\\u2028" +             // White_Space # Zl       LINE SEPARATOR
			"\\u2029" +             // White_Space # Zp       PARAGRAPH SEPARATOR
			"\\u202F" +             // White_Space # Zs       NARROW NO-BREAK SPACE
			"\\u205F" +             // White_Space # Zs       MEDIUM MATHEMATICAL SPACE
			"\\u3000" +              // White_Space # Zs       IDEOGRAPHIC SPACE
	"]";

	private static String LATIN_ACCENTS_CHARS = "\\u00c0-\\u00d6\\u00d8-\\u00f6\\u00f8-\\u00ff" + // Latin-1
											"\\u0100-\\u024f" + // Latin Extended A and B
											"\\u0253\\u0254\\u0256\\u0257\\u0259\\u025b\\u0263\\u0268\\u026f\\u0272\\u0289\\u028b" + // IPA Extensions
											"\\u02bb" + // Hawaiian
											"\\u0300-\\u036f" + // Combining diacritics
											"\\u1e00-\\u1eff"; // Latin Extended Additional (mostly for Vietnamese)
	/* URL related hash regex collection */
	private static final String URL_VALID_PRECEEDING_CHARS = "(?:[^A-Z0-9@＠$#＃\u202A-\u202E]|^)";

	private static final String URL_VALID_CHARS = "[\\p{Alnum}" + LATIN_ACCENTS_CHARS + "]";
	private static final String URL_VALID_SUBDOMAIN = "(?:(?:" + URL_VALID_CHARS + "[" + URL_VALID_CHARS + "\\-_]*)?" + URL_VALID_CHARS + "\\.)";
	private static final String URL_VALID_DOMAIN_NAME = "(?:(?:" + URL_VALID_CHARS + "[" + URL_VALID_CHARS + "\\-]*)?" + URL_VALID_CHARS + "\\.)";
	/* Any non-space, non-punctuation characters. \p{Z} = any kind of whitespace or invisible separator. */
	private static final String URL_VALID_UNICODE_CHARS = "[.[^\\p{Punct}\\s\\p{Z}\\p{InGeneralPunctuation}]]";

	private static final String URL_VALID_GTLD =
			"(?:(?:aero|asia|biz|cat|com|coop|edu|gov|info|int|jobs|mil|mobi|museum|name|net|org|" +
					"pro|tel|travel|xxx)(?=\\P{Alnum}|$))";
	private static final String URL_VALID_TLD =
			"(?:(?:ac|academy|actor|ad|ae|aero|af|ag|agency|ai|al|am|an|ao|aq|ar|arpa|as|asia|at|au|" +
					"aw|ax|az|ba|bar|bargains|bb|bd|be|berlin|best|bf|bg|bh|bi|bid|bike|biz|bj|blue|" +
					"bm|bn|bo|boutique|br|bs|bt|build|builders|buzz|bv|bw|by|bz|ca|cab|camera|camp|" +
					"cards|careers|cat|catering|cc|cd|center|ceo|cf|cg|ch|cheap|christmas|ci|ck|cl|" +
					"cleaning|clothing|club|cm|cn|co|codes|coffee|com|community|company|computer|" +
					"condos|construction|contractors|cool|coop|cr|cruises|cu|cv|cw|cx|cy|cz|dance|" +
					"dating|de|democrat|diamonds|directory|dj|dk|dm|dnp|do|domains|dz|ec|edu|" +
					"education|ee|eg|email|enterprises|equipment|er|es|estate|et|eu|events|expert|" +
					"exposed|farm|fi|fish|fj|fk|flights|florist|fm|fo|foundation|fr|futbol|ga|" +
					"gallery|gb|gd|ge|gf|gg|gh|gi|gift|gl|glass|gm|gn|gov|gp|gq|gr|graphics|gs|gt|" +
					"gu|guitars|guru|gw|gy|hk|hm|hn|holdings|holiday|house|hr|ht|hu|id|ie|il|im|" +
					"immobilien|in|industries|info|ink|institute|int|international|io|iq|ir|is|it|" +
					"je|jetzt|jm|jo|jobs|jp|kaufen|ke|kg|kh|ki|kim|kitchen|kiwi|km|kn|koeln|kp|kr|" +
					"kred|kw|ky|kz|la|land|lb|lc|li|lighting|limo|link|lk|lr|ls|lt|lu|luxury|lv|ly|" +
					"ma|maison|management|mango|marketing|mc|md|me|menu|mg|mh|mil|mk|ml|mm|mn|mo|" +
					"mobi|moda|monash|mp|mq|mr|ms|mt|mu|museum|mv|mw|mx|my|mz|na|nagoya|name|nc|ne|" +
					"net|neustar|nf|ng|ni|ninja|nl|no|np|nr|nu|nz|okinawa|om|onl|org|pa|partners|" +
					"parts|pe|pf|pg|ph|photo|photography|photos|pics|pink|pk|pl|plumbing|pm|pn|post|" +
					"pr|pro|productions|properties|ps|pt|pub|pw|py|qa|qpon|re|recipes|red|rentals|" +
					"repair|report|reviews|rich|ro|rs|ru|ruhr|rw|sa|sb|sc|sd|se|sexy|sg|sh|shiksha|" +
					"shoes|si|singles|sj|sk|sl|sm|sn|so|social|solar|solutions|sr|st|su|supplies|" +
					"supply|support|sv|sx|sy|systems|sz|tattoo|tc|td|technology|tel|tf|tg|th|tienda|" +
					"tips|tj|tk|tl|tm|tn|to|today|tokyo|tools|tp|tr|training|travel|tt|tv|tw|tz|ua|" +
					"ug|uk|uno|us|uy|uz|va|vacations|vc|ve|ventures|vg|vi|viajes|villas|vision|vn|" +
					"vote|voting|voto|voyage|vu|wang|watch|wed|wf|wien|wiki|works|ws|xxx|xyz|ye|yt|" +
					"za|zm|zone|zw)(?=\\P{Alnum}|$))";
	private static final String URL_PUNYCODE = "(?:xn--[0-9a-z]+)";

	private static final String URL_VALID_DOMAIN =
			"(?:" +                                                   // subdomains + domain + TLD
				URL_VALID_SUBDOMAIN + "+" + URL_VALID_DOMAIN_NAME +   // e.g. www.twitter.com, foo.co.jp, bar.co.uk
				"(?:" + URL_VALID_GTLD + "|" + URL_VALID_TLD + "|" + URL_PUNYCODE + ")" +
			")" +
		"|(?:" +                                                  // domain + gTLD
      URL_VALID_DOMAIN_NAME +                                 // e.g. twitter.com
      "(?:" + URL_VALID_GTLD + "|" + URL_PUNYCODE + ")" +
    ")" +
    "|(?:" + "(?<=https?://)" +
      "(?:" +
        "(?:" + URL_VALID_DOMAIN_NAME + URL_VALID_TLD + ")" +  // protocol + domain + ccTLD
        "|(?:" +
          URL_VALID_UNICODE_CHARS + "+\\." +                     // protocol + unicode domain + TLD
          "(?:" + URL_VALID_GTLD + "|" + URL_VALID_TLD + ")" +
        ")" +
      ")" +
    ")" +
    "|(?:" +                                                  // domain + ccTLD + '/'
      URL_VALID_DOMAIN_NAME + URL_VALID_TLD + "(?=/)" +     // e.g. t.co/
    ")";

	private static final String URL_VALID_PORT_NUMBER = "[0-9]++";

	private static final String URL_VALID_GENERAL_PATH_CHARS = "[a-z0-9!\\*';:=\\+,.\\$/%#\\[\\]\\-_~\\|&@" + LATIN_ACCENTS_CHARS + "]";
	/** Allow URL paths to contain balanced parens
	 *  1. Used in Wikipedia URLs like /Primer_(film)
	 *  2. Used in IIS sessions like /S(dfd346)/
	**/
	private static final String URL_BALANCED_PARENS = "\\(" + URL_VALID_GENERAL_PATH_CHARS + "+\\)";
	/** Valid end-of-path chracters (so /foo. does not gobble the period).
	 *   2. Allow =&# for empty URL parameters and other URL-join artifacts
	**/
	private static final String URL_VALID_PATH_ENDING_CHARS = "[a-z0-9=_#/\\-\\+" + LATIN_ACCENTS_CHARS + "]|(?:" + URL_BALANCED_PARENS +")";

	private static final String URL_VALID_PATH = "(?:" +
	"(?:" +
		URL_VALID_GENERAL_PATH_CHARS + "*" +
		"(?:" + URL_BALANCED_PARENS + URL_VALID_GENERAL_PATH_CHARS + "*)*" +
		URL_VALID_PATH_ENDING_CHARS +
		")|(?:@" + URL_VALID_GENERAL_PATH_CHARS + "+/)" +
	")";

	private static final String URL_VALID_URL_QUERY_CHARS = "[a-z0-9!?\\*'\\(\\);:&=\\+\\$/%#\\[\\]\\-_\\.,~\\|@]";
	private static final String URL_VALID_URL_QUERY_ENDING_CHARS = "[a-z0-9_&=#/]";
	public static final String VALID_URL_PATTERN_STRING =
	"(" +                                                            //  $1 total match
		"(" + URL_VALID_PRECEEDING_CHARS + ")" +                       //  $2 Preceeding chracter
		"(" +                                                          //  $3 URL
			"(https?://)?" +                                             //  $4 Protocol (optional)
			"(" + URL_VALID_DOMAIN + ")" +                               //  $5 Domain(s)
			"(?::(" + URL_VALID_PORT_NUMBER +"))?" +                     //  $6 Port number (optional)
			"(/" +
				URL_VALID_PATH + "*+" +
			")?" +                                                       //  $7 URL Path and anchor
			"(\\?" + URL_VALID_URL_QUERY_CHARS + "*" +                   //  $8 Query String
					URL_VALID_URL_QUERY_ENDING_CHARS + ")?" +
		")" +
	")";

	private static String AT_SIGNS_CHARS = "@\uFF20";

	private static final String DOLLAR_SIGN_CHAR = "\\$";
	private static final String CASHTAG = "[a-z]{1,6}(?:[._][a-z]{1,2})?";

	public static final Pattern REGEX_URL = Pattern.compile(VALID_URL_PATTERN_STRING, Pattern.CASE_INSENSITIVE);
	public static final int VALID_URL_GROUP_ALL          = 1;
	public static final int VALID_URL_GROUP_BEFORE       = 2;
	public static final int VALID_URL_GROUP_URL          = 3;
	public static final int VALID_URL_GROUP_PROTOCOL     = 4;
	public static final int VALID_URL_GROUP_DOMAIN       = 5;
	public static final int VALID_URL_GROUP_PORT         = 6;
	public static final int VALID_URL_GROUP_PATH         = 7;
	public static final int VALID_URL_GROUP_QUERY_STRING = 8;

	public static final Pattern INVALID_URL_WITHOUT_PROTOCOL_MATCH_BEGIN = Pattern.compile("[-_./]$");

	private static final String REGEX_ASTRIX = "(\\*\\*([^\\s][^.\\*\\*?$]+[^\\s])\\*\\*)";
	private static final String REGEX_SLASH = "(\\*([^\\s][^.*?$]+[^\\s])\\*)|(/([^\\s][^./?$]+[^\\s])/)";
	private static final String REGEX_UNDERSCORE = "(_([^\\s][^._?$]+[^\\s])_)";

	public static final Pattern MATCH_ASTRIX = Pattern.compile(REGEX_ASTRIX, Pattern.CASE_INSENSITIVE);
	public static final Pattern MATCH_SLASH = Pattern.compile(REGEX_SLASH, Pattern.CASE_INSENSITIVE);
	public static final Pattern MATCH_UNDERSCORE = Pattern.compile(REGEX_UNDERSCORE, Pattern.CASE_INSENSITIVE);
}