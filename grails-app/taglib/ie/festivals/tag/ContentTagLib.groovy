package ie.festivals.tag


class ContentTagLib {

    static namespace = "content"

    def pluralize = {attrs ->
        Integer count = attrs.count
        def singular = attrs.singular
        def suffix = attrs.suffix ?: 's'
        String text = count == 1 ? "$count $singular" : "$count $singular$suffix"
        out << text
    }

    /**
     * Generate the page title element
     */
    def title = {attrs, body ->

        // don't change this so that it also outputs the <title> tag, it doesn't work
        out << "${body().encodeAsHTML()} | Festivals.ie"
    }
}
