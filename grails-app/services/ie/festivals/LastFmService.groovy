package ie.festivals

import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.URIBuilder
import ie.festivals.music.Album
import ie.festivals.music.Track
import ie.festivals.xmlparser.lastfm.ArtistInfoParser
import ie.festivals.xmlparser.lastfm.ArtistSearchParser
import ie.festivals.xmlparser.lastfm.TopAlbumsParser
import ie.festivals.xmlparser.lastfm.TopTracksParser
import org.codehaus.groovy.grails.commons.GrailsApplication

class LastFmService {

    static transactional = false

    GrailsApplication grailsApplication
    ImageService imageService

    @Lazy
    private lastFMConfig = grailsApplication.config.festival.lastFM

    private GPathResult submitLastFmRequest(String method, Map params = [:]) {
        def url = "http://ws.audioscrobbler.com/2.0/"
        params.method = method
        params.api_key = lastFMConfig.apiKey
        getXmlResponse(url, params)
    }

    private GPathResult getXmlResponse(String url, Map params) {

        url = new URIBuilder(url).setQuery(params).toString()
        log.debug "WS Request URL: $url"
        new XmlSlurper().parse(url)
    }

    private Map getBaseApiParams(Artist artist) {
        def apiParams = [artist: artist.name]

        if (artist.mbid) {
            apiParams.mbid = artist.mbid
        }
        apiParams
    }

    Artist updateArtistInfo(Artist artist) {
        Map apiParams = getBaseApiParams(artist)

        GPathResult artistInfoXml = submitLastFmRequest('artist.getinfo', apiParams)
        new ArtistInfoParser(artist, imageService).parse(artistInfoXml)
    }

    List<Track> getTopTracks(Artist artist) {
        Map apiParams = getBaseApiParams(artist)
        apiParams.limit = lastFMConfig.topTracksLimit
        GPathResult topTracksXml = submitLastFmRequest('artist.gettoptracks', apiParams)
        new TopTracksParser().parse(topTracksXml)
    }


    List<Album> getTopAlbums(Artist artist) {
        Map apiParams = getBaseApiParams(artist)
        apiParams.limit = lastFMConfig.topAlbumsLimit
        GPathResult topAlbumsXml = submitLastFmRequest('artist.gettopalbums', apiParams)
        new TopAlbumsParser(imageService).parse(topAlbumsXml)
    }

    List<Artist> searchArtists(String artistNameQuery, Boolean matchNameExactly) {
        def maxResults = lastFMConfig.artistSearchLimit
        def apiParams = [limit: maxResults, artist: artistNameQuery]

        GPathResult records = submitLastFmRequest('artist.search', apiParams)
        new ArtistSearchParser(artistNameQuery, matchNameExactly).parse(records)
    }
}
