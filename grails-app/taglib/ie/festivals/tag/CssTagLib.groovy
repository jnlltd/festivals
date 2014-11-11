package ie.festivals.tag

import org.codehaus.groovy.grails.commons.GrailsApplication

class CssTagLib {

    GrailsApplication grailsApplication

    static namespace = "css"

    @Lazy
    private dateFormat = grailsApplication.config.festival.dateFormat

    /**
     * Converts a date to the corresponding CSS class name
     * @attr date REQUIRED the date to be converted to a CSS class name
     */
    def getDateClass = { attrs ->
        out << 'date-' + g.formatDate(date: attrs.date, format: dateFormat)
    }

    def getArtistClass = { attrs ->
        def artist = attrs.artist

        if (artist.id) {
            out << "artist-$artist.id saved"

        } else if (artist.mbid) {
            out << "artist-$artist.mbid"

        } else {
            out << 'artist-'
        }
    }
}
