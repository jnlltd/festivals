package ie.festivals.xmlparser

import com.neovisionaries.i18n.CountryCode
import grails.gsp.PageRenderer
import grails.plugin.geocode.AddressComponent
import grails.plugin.geocode.GeocodingService
import grails.plugin.geocode.Point
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import ie.festivals.Festival
import ie.festivals.enums.FestivalSource
import ie.festivals.tag.EuropeTagLib
import ie.festivals.util.HtmlUtils

import java.text.NumberFormat

@Slf4j
class EventbriteXmlFestivalParser implements XmlParser<Map<Long, Festival>> {

    private static final DATE_FORMAT = 'yyyy-MM-dd hh:mm:ss'

    private final PageRenderer pageRenderer

    private final NumberFormat currencyParser = NumberFormat.getInstance(Locale.UK)
    private final GeocodingService geocodingService
    private final String geocodingKey

    private parseXmlDate(elementName, xmlNode) {
        def date = xmlNode."$elementName".text()
        Date.parse(DATE_FORMAT, date)
    }

    EventbriteXmlFestivalParser(PageRenderer pageRenderer, GeocodingService geocodingService, String geocodingKey) {
        this.pageRenderer = pageRenderer
        this.geocodingService = geocodingService
        this.geocodingKey = geocodingKey
    }

    @Override
    Map<Long, Festival> parse(GPathResult xmlResponse) {

        Map<Long, Festival> parsedFestivals = [:]

        xmlResponse.event.each { festivalXml ->

            Festival festival = new Festival(
                    name: festivalXml.title.text(),
                    source: FestivalSource.EVENTBRITE,
                    website: festivalXml.url.text())

            log.info "Parsing Eventbrite festival named '$festival.name'"

            def floatParser = { elementName, xmlNode = festivalXml ->

                log.trace "Parsing float from element '$elementName'"
                String elementContent = xmlNode."$elementName".text()

                if (elementContent) {
                    elementContent.toFloat()
                }
            }

            festival.start = parseXmlDate('start_date', festivalXml).clearTime()
            festival.end = parseXmlDate('end_date', festivalXml).clearTime()
            festival.latitude = floatParser('latitude')
            festival.longitude = floatParser('longitude')
            festival.synopsis = HtmlUtils.removeTags(festivalXml.description.text())

            def venue = festivalXml.venue

            // if lat/lng haven't been set use the venue's coordinates
            festival.latitude = festival.latitude ?: floatParser('latitude', venue)
            festival.longitude = festival.longitude ?: floatParser('longitude', venue)
            festival.addressLine1 = venue.name.text()

            // Eventbrite returns addresses in a crappy unpredictable format which is very hard to parse, so use
            // the geocodingService to get the address in a format that's easier to work with
            Point location = new Point(latitude: festival.latitude, longitude: festival.longitude)
            List<AddressComponent> components = reverseGeocode(location)

            if (components) {
                Closure findAddressComponent = { componentType ->

                    AddressComponent component = components.find {
                        componentType in it.types
                    }
                    component?.longName
                }

                def streetNumber = findAddressComponent('street_number')
                def streetName = findAddressComponent('route')

                festival.addressLine2 = [streetNumber, streetName].join(' ')
                festival.city = findAddressComponent('locality')
                festival.postCode = findAddressComponent('postal_code')
            } else {
                log.error "Failed to reverse geocode location: $location"
            }

            // convert 2-letter country code to 3-letter
            String twoLetterCode = venue.country_code.text()
            String threeLetterCode = CountryCode.getByCode(twoLetterCode, false).alpha3.toLowerCase()
            assert threeLetterCode in EuropeTagLib.ISO3166_3.keySet()

            festival.countryCode = threeLetterCode
            log.debug "Retrieved country name '$festival.countryName' for code '$threeLetterCode'"

            def visibleTickets = festivalXml.tickets.ticket.findAll { Boolean.parseBoolean(it.visible.text()) }
            List<EventbriteTicket> tickets = visibleTickets.collect { getTicket(it) }

            if (tickets?.every { it.price == 0F }) {
                festival.freeEntry = true
            }

            def ticketModel = [tickets: tickets, ticketUrl: festival.website]
            festival.ticketInfo = pageRenderer.render(template: '/festival/eventbriteTicketInfo', model: ticketModel)

            festival.approved = false

            Long eventbriteId = festivalXml.id.text().toLong()
            parsedFestivals[eventbriteId] = festival
        }
        parsedFestivals
    }

    private List<AddressComponent> reverseGeocode(Point location) {
        def optionalGeocodingArgs = [key: geocodingKey, result_type: 'street_address']

        // stay on the good side of Google's rate-limiting
        // https://developers.google.com/maps/documentation/geocoding/#Limits
        Thread.sleep(500)
        List<AddressComponent> components = geocodingService.getAddress(location, optionalGeocodingArgs)?.addressComponents

        if (!components) {
            // try and geocode it again with no restriction on the type of address returned
            optionalGeocodingArgs.remove('result_type')
            geocodingService.getAddress(location, optionalGeocodingArgs)?.addressComponents

        } else {
            components
        }
    }

    private EventbriteTicket getTicket(ticketXml) {

        String currencyCode = ticketXml.currency.text()
        String priceText = ticketXml.price.text()
        Float price = priceText.isFloat() ? currencyParser.parse(priceText) : null

        new EventbriteTicket(
                name: ticketXml.name.text(),
                currency: currencyCode,
                price: price,
                end: parseXmlDate('end_date', ticketXml)
        )
    }
}

class EventbriteTicket {
    String name
    String currency
    Float price
    Date end
}