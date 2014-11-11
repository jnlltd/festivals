package ie.festivals.util

import org.apache.commons.lang.StringUtils as ApacheStringUtils

import java.text.Normalizer
import java.util.regex.Pattern

class StringUtils {

    private StringUtils() {}

    private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")

    /**
     * URL-encode a string and also perform some SEO-friendly transformations
     * @param str string to be encoded
     * @param searchChars ever occurrence of each character in this string will be replaced by
     * @param replaceChars the corresponding character (by position) in this string
     * @return
     */
    static String seoUrlEncode(String str, String searchChars, String replaceChars) {

        assert searchChars.size() == replaceChars.size(), 'A replacement char must be specified for each replaceable char'
        str = str.trim()

        // replace internal spaces with a '-' and remove all other chars in the 2nd argument
        str = ApacheStringUtils.replaceChars(str, "$searchChars'():", replaceChars)

        // convert accented characters to their non-accented equivalent
        str = Normalizer.normalize(str, Normalizer.Form.NFD)
        str = DIACRITICAL_MARKS.matcher(str).replaceAll("")

        // encode as URL, just in case there are any remaining chars that aren't allowed in URLs
        str.toLowerCase().encodeAsURL()
    }

    /**
     * Indicates whether a string is a valid HTTP/HTTPS URL
     * @param str
     * @param allowNull
     * @return
     */
    static boolean isWebUrl(String str, boolean allowNull = true) {
        try {
            if (str == null) {
                return allowNull
            }

            str = str.trim()

            def validProtocol = ['http://', 'https://'].any {
                str.toLowerCase().startsWith(it)
            }

            if (!validProtocol) {
                return false
            }

            URL url = new URL(str)
            url.toURI()

        } catch (MalformedURLException | URISyntaxException ex) {
            return false
        }
    }
}
