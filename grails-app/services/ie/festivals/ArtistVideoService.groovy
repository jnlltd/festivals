package ie.festivals

import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.URIBuilder
import ie.festivals.xmlparser.muzu.MuzuParser
import org.codehaus.groovy.grails.commons.GrailsApplication
import ie.festivals.xmlparser.XmlParser


class ArtistVideoService {

    static transactional = false

    GrailsApplication grailsApplication

    private static final MAX_MUZU_RESULTS = 1

    private GPathResult getXmlResponse(String url, Map params) {

        url = new URIBuilder(url).setQuery(params).toString()
        log.debug "Muzu video request URL: $url"
        new XmlSlurper().parse(url)
    }

    String getVideoEmbedCode(Artist artist) {
        getMuzuPlaylistEmbedCode(artist) ?: getSingleMuzuVideoEmbedCode(artist)
    }

    private String getMuzuPlaylistEmbedCode(Artist artist) {

        String embedCode = null
        String playlistUrl = 'http://www.muzu.tv/api/artist/details'

        XmlParser<String> playListParser = MuzuParser.ARTIST_PLAYLIST

        if (artist.mbid) {
            def params = getBaseMuzuParams() + [mbid: artist.mbid]
            def muzuResponse = getXmlResponse(playlistUrl, params)
            embedCode = playListParser.parse(muzuResponse)
        }

        if (!embedCode) {
            def params = getBaseMuzuParams() + [aname: artist.name]
            def muzuResponse = getXmlResponse(playlistUrl, params)
            embedCode = playListParser.parse(muzuResponse)
        }

        log.debug "Playlist embed code for artist ID $artist.id retrieved via artist details: $embedCode"
        embedCode
    }

    private String getSingleMuzuVideoEmbedCode(Artist artist) {

        // Muzu API docs for this method: http://www.muzu.tv/api/searchDoc/
        def params = getBaseMuzuParams() + [mySearch: artist.name]
        def muzuResponse = getXmlResponse('http://www.muzu.tv/api/search', params)
        String embedCode = MuzuParser.ARTIST_VIDEO.parse(muzuResponse)

        log.debug "Video embed code for artist ID $artist.id retrieved via artist search: $embedCode"
        embedCode
    }

    private Map getBaseMuzuParams() {
        [muzuid: grailsApplication.config.festival.muzuApiKey, format: 'xml', country: 'ie', noadult: 'y', l: MAX_MUZU_RESULTS]
    }
}
