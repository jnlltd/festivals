package ie.festivals.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Whitelist

class HtmlUtils {

    private HtmlUtils() {}

    static removeTags(String html) {
        Jsoup.clean(html, Whitelist.none())
    }

    static String normalize(String html, String linkTarget = '_blank') {
        if (!html) {
            return html
        }

        html = Jsoup.clean(html, Whitelist.relaxed())

        if (linkTarget) {
            process html, { Document doc ->
                // open links in new window/tab
                doc.select("a").attr("target", linkTarget)
            }
        } else {
            html
        }
    }

    private static String process(String html, Closure processor) {
        if (!html) {
            return html
        }

        Document doc = Jsoup.parseBodyFragment(html)
        processor(doc)
        doc.body().html()
    }
}
