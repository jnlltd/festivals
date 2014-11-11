package ie.festivals.tag

class GetThereTagLib {

    static namespace = "getthere"

    private String getThereLink(festivalName, body, urlSuffix = null) {

        festivalName = festivalName.encodeAsGetThere()
        def url = "http://getthere.ie/$festivalName"

        if (urlSuffix) {
			// trailing '/' is required
            url += "/$urlSuffix/"
        }
        "<a target='blank' href='$url'>${body()}</a>"
    }

    def summary = {attrs, body ->
        out << getThereLink(attrs.name, body)
    }

    def offer = {attrs, body ->
        out << getThereLink(attrs.name, body, 'offer')
    }

    def request = {attrs, body ->
        out << getThereLink(attrs.name, body, 'request')
    }

}
