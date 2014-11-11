package ie.festivals.xmlparser.lastfm

import ie.festivals.xmlparser.XmlParser

abstract class AbstractLastFmParser<T> implements XmlParser<T> {

    protected String getImage(parentElement, size) {

        if (parentElement) {
            def img = parentElement.image.find { it.@size == size }
            img?.text()
        }
    }
}
