package ie.festivals.job

import grails.gsp.PageRenderer
import grails.plugin.geocode.Point
import ie.festivals.ArtistService
import ie.festivals.Festival
import grails.plugin.geocode.GeocodingService

import ie.festivals.ImportAudit
import ie.festivals.enums.FestivalSource
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.gmock.WithGMock

import static java.util.Collections.emptyList
import static org.hamcrest.Matchers.anything

@WithGMock
class ImportSkiddleFeedJobTests extends GroovyTestCase {

    GrailsApplication grailsApplication
    PageRenderer groovyPageRenderer

    private ImportSkiddleFeedJob createJobInstance(String responseFile) {
        URL fileURL = getClass().getResource(responseFile)
        grailsApplication.config.festival.skiddle.feedUrl = fileURL.toString()

        ArtistService stubArtistService = mock(ArtistService)
        stubArtistService.festivalLineupSearch(anything(), anything(), anything()).returns(emptyList()).stub()

        Point location = new Point(latitude: 0F, longitude: 0F)
        GeocodingService stubGeocodingService = mock(GeocodingService)
        stubGeocodingService.getPoint(anything()).returns(location).stub()

        new ImportSkiddleFeedJob(
                geocodingService: stubGeocodingService,
                grailsApplication: grailsApplication,
                groovyPageRenderer: groovyPageRenderer,
                artistService: stubArtistService)
    }

    void testMultiDayFestivalImport() {

        ImportSkiddleFeedJob job = createJobInstance('multiDayFestival.xml')

        play {
            job.execute()

            // This is listed as two <event> entries in the file, but we should
            // parse it to a single festival of 2-days duration
            def rugbyRocksLondon = Festival.findAllByName('RugbyRocks London 2013')
            assertEquals 1, rugbyRocksLondon.size()

            Festival multiDayFestival = rugbyRocksLondon.first()
            String dateFormat = 'yyyy-MM-dd'
            assertEquals Date.parse(dateFormat, '2113-06-01'), multiDayFestival.start
            assertEquals Date.parse(dateFormat, '2113-06-02'), multiDayFestival.end

            // skiddle eventId of both days should be saved in SkiddleLog
            def actualEventIds = ImportAudit.findAllBySource(FestivalSource.SKIDDLE).eventId

            [11745690L, 11745688L].each {
                assertTrue it in actualEventIds
            }
        }
    }

    void testFestivalWithLongNameImport() {

        ImportSkiddleFeedJob job = createJobInstance('longNameFestival.xml')

        play {
            job.execute()
            def festival = Festival.first()
            def longName = festival.name

            def maxNameLength = grailsApplication.config.festival.utf8mb4MaxLength
            assertEquals maxNameLength, longName.size()
            assertEquals Festival.ELLIPSE, longName[-1]
        }
    }
}
