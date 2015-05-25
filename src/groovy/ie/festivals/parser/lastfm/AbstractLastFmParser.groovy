package ie.festivals.parser.lastfm

import ie.festivals.parser.XmlResponseParser

abstract class AbstractLastFmParser<T> implements XmlResponseParser<T> {

    protected String getImage(parentElement, size) {

        if (parentElement) {
            def img = parentElement.image.find { it.@size == size }
            img?.text()
        }
    }
}
