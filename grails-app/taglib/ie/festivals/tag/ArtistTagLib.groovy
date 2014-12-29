package ie.festivals.tag

import grails.util.GrailsUtil
import groovy.xml.MarkupBuilder
import ie.festivals.Artist

class ArtistTagLib {

    static namespace = "artist"

    def track = { attrs ->
        def lengthInSeconds = attrs.duration?.toInteger()

        if (!lengthInSeconds) {
            return
        }

        def minutes = lengthInSeconds / 60
        minutes = Math.floor(minutes) as Integer
        String seconds = lengthInSeconds % 60

        log.trace "length of $seconds is ${seconds.length()}"
        if (seconds.length() == 1) {
            seconds += "0"
        }

        out << "($minutes:$seconds)"
    }

    /**
     * Renders an artist's image. Any additional attributes provided will be passed through to the generated image
     * HTML tag.
     *
     * @attr url REQUIRED absolute or relative path to image. URL should be absolute if image is stored remotely (e.g. LastFM)
     * or relative if image is stored on the server (e.g. resized images chosen via image search).
     * @attr thumb If truthy, a shadow image will be displayed if the url attribute is falsey
     * @attr dimensions specifies the size of the image. If provided, width and height attributes will be added to the
     * <img> tag to improve rendering time
     */
    def img = { attrs ->

        String imageUrl = attrs.remove('url')
        def showThumb = attrs.remove('thumb')

        if (imageUrl?.startsWith('http')) {
            attrs.src = imageUrl

        } else if (imageUrl) {
            // Render a link that will call back to the controller to get the image
            attrs.src = g.createLink(controller: 'artist', action: 'getLocalImage', params: [path: imageUrl])

        } else if (showThumb) {
            // If the artist was added without an image and a thumbnail is requested, show a silhouette - #128
            attrs.src = asset.assetPath(src: 'images/silhouette.png')
        }

        if (attrs.src) {
            def dimensions = attrs.remove('dimensions')
            if (dimensions) {
                attrs.width = dimensions.width
                attrs.height = dimensions.height
            }
            new MarkupBuilder(out).img(attrs)
        }
    }

    /**
     * Generates a link to the artists page
     * @attr id If truthy the body will be wrapped in a link to this artist's page. If falsey, only the body will be rendered
     * @attr name name of the artist. If not provided the artist's name will be set by loading the artist from the database
     */
    def show = { attrs, body ->

        def id = attrs.remove('id')
        def name = attrs.remove('name')

        if (!name) {
            name = Artist.createCriteria().get {
                idEq(id)
                projections {
                    property "name"
                }
            }

            // Ignore attempts to show missing artists in dev mode
            if (GrailsUtil.developmentEnv && !name) {
                log.warn "<$namespace:show> invoked with invalid ID: $attrs.id"
                return
            }
        }

        def params = [id: id, name: name.encodeAsSeoName()]

        attrs += [action: 'show', controller: 'artist', params: params]
        def linkedContent = body ? body() : name.encodeAsHTML()

        // don't wrap a "show artist" link around the content if the artist is transient
        out << (id ? g.link(attrs, linkedContent) : linkedContent)
    }
}
