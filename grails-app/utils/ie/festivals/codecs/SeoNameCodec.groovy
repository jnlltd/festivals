package ie.festivals.codecs

import ie.festivals.util.StringUtils

/**
 * Converts a name like <tt>Electric Picnic</tt> to <tt>electric-picnic</tt>
 *
 * Information about what kind of URLs Google likes is available
 * <a href="http://support.google.com/webmasters/bin/answer.py?hl=en&answer=76329">here</a>
 */
class SeoNameCodec {

	static encode = { str ->
        StringUtils.seoUrlEncode(str, ' _', '--')
	}
}

