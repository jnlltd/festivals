package ie.festivals.parser.muzu

import groovy.util.slurpersupport.GPathResult
import ie.festivals.parser.XmlResponseParser

class MuzuParserTests extends GroovyTestCase {

    private GPathResult loadXml(String path) {
        InputStream inputStream = getClass().getResourceAsStream(path)
        new XmlSlurper().parse(inputStream)
    }

    void testVideoParsingEmbedCode() {
        def expectedEmbedCode = '<iframe frameborder="0" width="570" height="340" src="http://player.muzu.tv/player/getPlayer/i/2065/vidId=807765&amp;la=n&amp;ps=b" allowfullscreen></iframe>'
        testParser('muzuVideoSearchResults.xml', MuzuParser.ARTIST_VIDEO, expectedEmbedCode)
    }

    void testArtistParsingEmbedCode() {
        def expectedEmbedCode = '<iframe frameborder="0" width="570" height="340" src="http://player.muzu.tv/player/getPlayer/i/33302/vidId=735820&amp;la=n&amp;ps=b" allowfullscreen></iframe>'
        testParser('muzuPlaylistResults.xml', MuzuParser.ARTIST_PLAYLIST, expectedEmbedCode)
    }

    void testNoVideoResults() {
        testParser('noVideoResults.xml', MuzuParser.ARTIST_VIDEO)
    }

    void testNoPlaylistResults() {
        testParser('noPlaylistResults.xml', MuzuParser.ARTIST_PLAYLIST)
    }

    private testParser(String xmlFile, XmlResponseParser parser, String expectedResult = null) {
        def xmlResult = loadXml(xmlFile)
        def actualEmbedCode = parser.parse(xmlResult)
        assertEquals expectedResult, actualEmbedCode
    }
}
