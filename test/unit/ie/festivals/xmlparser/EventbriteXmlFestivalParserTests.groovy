package ie.festivals.xmlparser

import grails.gsp.PageRenderer
import grails.plugin.geocode.Address
import grails.plugin.geocode.AddressComponent
import grails.plugin.geocode.GeocodingService
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import groovy.util.slurpersupport.GPathResult
import ie.festivals.Festival
import ie.festivals.enums.FestivalSource
import org.gmock.WithGMock
import org.hamcrest.Matchers
import org.junit.Before

@TestMixin(GrailsUnitTestMixin)
@WithGMock
@Mock([Festival])
class EventbriteXmlFestivalParserTests {

    private PageRenderer mockPageRenderer
    private GeocodingService mockGeocodingService

    @Before
    void mockPageRenderer() {
        mockPageRenderer = mock(PageRenderer)
        mockPageRenderer.render(Matchers.isA(Map)).returns('').stub()
        this.mockGeocodingService = mock(GeocodingService)

        def Address mockAddress = new Address(
            addressComponents: [
                    new AddressComponent(longName: 22, types: ['street_number']),
                    new AddressComponent(longName: 'Main St.', types: ['route']),
                    new AddressComponent(longName: 'Bray', types: ['locality'])
            ]
        )
        mockGeocodingService.getAddress(Matchers.anything(), Matchers.anything()).returns(mockAddress).stub()
    }

    void testEventbriteXmlParsing() {

        play {
	        Map<Long, Festival> parsedFestivals = parseFestivals('eventbriteFestivalsResponse.xml')

	        parsedFestivals.each {Long eventId, Festival festival ->
                assertNotNull eventId
	            assertFalse festival.approved
				assertNull festival.type
				assertFalse festival.hasErrors()
                assertEquals FestivalSource.EVENTBRITE, festival.source
	        }
		}
    }

    private Map<Long, Festival> parseFestivals(String fileName) {
        InputStream festivalFeed = getClass().getResourceAsStream(fileName)
        GPathResult festivalXml = new XmlSlurper().parse(festivalFeed)
        Integer expectedFestivals = festivalXml.event.size()

        def parser = new EventbriteXmlFestivalParser(mockPageRenderer, mockGeocodingService, null)
        Map<Long, Festival> parsedFestivals = parser.parse(festivalXml)
        assertEquals expectedFestivals, parsedFestivals.size()
        parsedFestivals
    }
}

