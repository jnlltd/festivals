package ie.festivals.parser.lastfm

import ie.festivals.parser.ApiResponseParser

abstract class AbstractLastFmParser<T> implements ApiResponseParser<T> {

    protected String getImage(parentElement, size) {

        if (parentElement) {
            def img = parentElement.image.find { it.@size == size }
            img?.text()
        }
    }
}
