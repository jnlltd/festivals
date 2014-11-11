package ie.festivals.job

import grails.gsp.PageRenderer
import grails.plugin.geocode.Address
import grails.plugin.geocode.AddressComponent
import grails.plugin.geocode.GeocodingService
import grails.plugin.geocode.Point
import ie.festivals.Festival
import ie.festivals.ImportAudit
import ie.festivals.enums.FestivalSource
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.gmock.WithGMock

import static org.hamcrest.Matchers.anything

@WithGMock
class ImportEventbriteFestivalsJobTests extends GroovyTestCase {

    GrailsApplication grailsApplication
    PageRenderer groovyPageRenderer

    void testFestivalImport() {

        // Mock the geocoding service because it's usage is only allowed from certain IPs
        Point location = new Point(latitude: 0F, longitude: 0F)
        GeocodingService stubGeocodingService = mock(GeocodingService)
        stubGeocodingService.getPoint(anything()).returns(location).stub()

        Address address = new Address(
                addressComponents: [
                        new AddressComponent(
                                types: ['street_number', 'route', 'locality', 'postal_code'],
                                longName: 'foo'
                        )
                ]
        )

        stubGeocodingService.getAddress(anything(), anything()).returns(address).stub()

        play {
            new ImportEventbriteFestivalsJob(
                    grailsApplication: grailsApplication,
                    groovyPageRenderer: groovyPageRenderer,
                    geocodingService: stubGeocodingService).execute()

            Integer savedFestivals = Festival.count()
            assertTrue savedFestivals > 0

            assertEquals savedFestivals, ImportAudit.countBySource(FestivalSource.EVENTBRITE)
        }
    }
}
