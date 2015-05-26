package ie.festivals.parser

import grails.gsp.PageRenderer
import grails.test.mixin.Mock
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.GPathResult
import ie.festivals.Festival
import ie.festivals.enums.FestivalSource
import org.gmock.WithGMock
import org.hamcrest.Matchers
import org.junit.Before

@TestMixin(GrailsUnitTestMixin)
@WithGMock
@Mock([Festival])
class EventbriteJsonFestivalParserTests {

    private PageRenderer mockPageRenderer

    @Before
    void mockPageRenderer() {
        mockPageRenderer = mock(PageRenderer)
        mockPageRenderer.render(Matchers.isA(Map)).returns('').stub()
    }

    void testEventbriteXmlParsing() {

        play {
	        Map<Long, Festival> parsedFestivals = parseFestivals('eventbriteFestivalsResponse.json')

	        parsedFestivals.each { Long eventId, Festival festival ->
                assertNotNull eventId
	            assertFalse festival.approved
				assertFalse festival.hasErrors()
                assertEquals FestivalSource.EVENTBRITE, festival.source
	        }
		}
    }

    private Map<Long, Festival> parseFestivals(String fileName) {
        InputStream festivalFeed = getClass().getResourceAsStream(fileName)
        Map festivalJson = new JsonSlurper().parse(festivalFeed)
        Integer expectedFestivals = festivalJson.pagination.page_size

        def parser = new EventbriteJsonFestivalParser(mockPageRenderer)
        Map<Long, Festival> parsedFestivals = parser.parse(festivalJson)
        assertEquals expectedFestivals, parsedFestivals.size()
        parsedFestivals
    }
}

