package ie.festivals.parser.lastfm

import groovy.util.slurpersupport.GPathResult
import ie.festivals.Artist
import ie.festivals.ImageService
import ie.festivals.music.Album
import ie.festivals.music.Track
import org.gmock.WithGMock
import org.hamcrest.Matchers

import java.awt.image.BufferedImage

import static org.apache.commons.lang.StringUtils.isNotBlank

@WithGMock
class LastFMParsingTests extends GroovyTestCase {

    private GPathResult loadXml(String path) {
        InputStream inputStream = getClass().getResourceAsStream(path)
        new XmlSlurper().parse(inputStream)
    }

    void testArtistSearch() {
        def inexactNameMatcher = new ArtistSearchParser("U2", false)
        def xmlResults = loadXml('artistSearchQueryU2.xml')

        List<Artist> parsedArtists = inexactNameMatcher.parse(xmlResults)
        assertEquals 5, parsedArtists.size()

        def exactNameMatcher = new ArtistSearchParser("U2", true)
        parsedArtists = exactNameMatcher.parse(xmlResults)
        assertEquals 1, parsedArtists.size()
        assertEquals "U2", parsedArtists[0].name
    }

    void testTopTracks() {
        def tracksParser = new TopTracksParser()
        def xmlResults = loadXml('topTracksBobDylan.xml')
        List<Track> parsedTracks = tracksParser.parse(xmlResults)
        assertEquals 10, parsedTracks.size()
    }

    private ImageService mockImageService(boolean valid) {
        ImageService imageService = mock(ImageService)
        imageService.isValidImageUrl(Matchers.any(String)).returns(valid).stub()

        BufferedImage mockBufferedImage = new BufferedImage(10, 20, BufferedImage.TYPE_INT_RGB)

        imageService.read(Matchers.any(String)).returns(mockBufferedImage).stub()
        imageService
    }

    void testTopAlbums() {
        ImageService validImageService = mockImageService(true)
        ImageService invalidImageService = mockImageService(false)

        play {
            parseAlbums(validImageService, 6)
            parseAlbums(invalidImageService, 0)
        }
    }

    private parseAlbums(ImageService imageService, Integer expectedAlbums) {
        def albumsParser = new TopAlbumsParser(imageService)
        def xmlResults = loadXml('topAlbumsBobDylan.xml')
        List<Album> parsedAlbums = albumsParser.parse(xmlResults)
        assertEquals expectedAlbums, parsedAlbums.size()
    }

    void testArtistInfo() {
        ImageService validImageService = mockImageService(true)
        Artist tomWaits = new Artist(name: 'Tom Waits')

        play {
            def artistInfoParser = new ArtistInfoParser(tomWaits, validImageService)
            def xmlResults = loadXml('artistInfoQueryTomWaits.xml')
            tomWaits = artistInfoParser.parse(xmlResults)

            assertEquals tomWaits.image, 'http://userserve-ak.last.fm/serve/252/207221.jpg'
            assertEquals tomWaits.thumbnail, 'http://userserve-ak.last.fm/serve/126/207221.jpg'

            assertTrue isNotBlank(tomWaits.bioFull) && isNotBlank(tomWaits.bioSummary)
        }
    }
}