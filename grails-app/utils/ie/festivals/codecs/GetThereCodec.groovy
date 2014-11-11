package ie.festivals.codecs

import ie.festivals.util.StringUtils

/**
 * Converts a festival name to the ID expected by getthere.ie, e.g. from <tt>Electric Picnic</tt> to <tt>electric_picnic</tt>
 */
class GetThereCodec {

	static encode = { str ->
        StringUtils.seoUrlEncode(str, ' ', '_')
	}
}

