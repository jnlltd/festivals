package ie.festivals.xmlparser

import grails.gsp.PageRenderer
import grails.plugin.geocode.Point
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import groovy.util.slurpersupport.GPathResult
import ie.festivals.ArtistService
import ie.festivals.Festival
import grails.plugin.geocode.GeocodingService

import ie.festivals.enums.FestivalSource
import org.gmock.WithGMock
import org.hamcrest.Matchers

@TestMixin(GrailsUnitTestMixin)
@WithGMock
@Mock([Festival])
class SkiddleXmlFestivalParserTests {

    void testSkiddleXmlParsing() {

        Point location = new Point(latitude: 0F, longitude: 0F)
        GeocodingService stubGeocodingService = mock(GeocodingService)
        stubGeocodingService.getPoint(Matchers.anything()).returns(location).stub()

        ArtistService stubArtistService = mock(ArtistService)
		stubArtistService.festivalLineupSearch(Matchers.anything(), Matchers.anything(), Matchers.anything()).returns(Collections.emptyList()).stub()

        PageRenderer mockPageRenderer = mock(PageRenderer)
        mockPageRenderer.render(Matchers.isA(Map)).returns('').stub()

        def mockSkiddleConfig = [tag: 'skiddleTag']

        play {
	        InputStream festivalFeed = loadResourceFromClasspath('skiddleFestivalFeed.xml')
            GPathResult skiddleXml = new XmlSlurper().parse(festivalFeed)

            def parser = new SkiddleXmlFestivalParser(stubArtistService, stubGeocodingService, mockSkiddleConfig, mockPageRenderer)
	        Map<Long, Festival> parsedFestivals = parser.parse(skiddleXml)
	        assertEquals 22, parsedFestivals.size()
	
	        parsedFestivals.each {Long eventId, Festival festival ->
                assertNotNull eventId
	            assertFalse festival.approved
				assertNull festival.type
				assertFalse festival.hasErrors()
                assertEquals FestivalSource.SKIDDLE, festival.source
	        }
		}
    }	
	
    private InputStream loadResourceFromClasspath(String path) {
        getClass().getResourceAsStream(path)
    }	
}

