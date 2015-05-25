package ie.festivals.parser

import com.neovisionaries.i18n.CountryCode
import grails.gsp.PageRenderer
import groovy.util.logging.Slf4j
import groovy.util.slurpersupport.GPathResult
import ie.festivals.Festival
import ie.festivals.enums.FestivalSource
import ie.festivals.enums.FestivalType
import ie.festivals.tag.EuropeTagLib
import org.apache.commons.lang.WordUtils

@Slf4j
class EventbriteJsonFestivalParser implements JsonResponseParser<Map<Long, Festival>> {

    private static final DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss"
    private static final TimeZone UTC = TimeZone.getTimeZone('UTC')

    private final PageRenderer pageRenderer

    EventbriteJsonFestivalParser(PageRenderer pageRenderer) {
        this.pageRenderer = pageRenderer
    }

    /**
     * Maps the short code of the Eventbrite festival types, to those used on festivals.ie. A list of the latter is
     * available by appending our access token to the following URL https://www.eventbriteapi.com/v3/categories/?token=
     */
    private static Map<String, FestivalType> FESTIVAL_TYPE_MAPPING = [
            Music: FestivalType.MUSIC,
            'Food & Drink': FestivalType.FOOD_AND_DRINK,
            Arts: FestivalType.ARTS,
            'Film & Media': FestivalType.FILM,
            'Sports & Fitness': FestivalType.SPORT,
            Other: FestivalType.OTHER
    ]

    private parseDate(elementName, node) {
        def date = node."$elementName"
        Date.parse(DATE_FORMAT, date, UTC)
    }

    @Override
    Map<Long, Festival> parse(Map response) {

        Map<Long, Festival> parsedFestivals = [:]

        response.events.each { festivalJson ->

            if (festivalJson.status != 'live') {
                // skip festivals that are cancelled, completed, etc.
                return
            }

            Festival festival = new Festival(
                    name: festivalJson.name.text,
                    source: FestivalSource.EVENTBRITE,
                    website: festivalJson.url)

            festival.type = FESTIVAL_TYPE_MAPPING[festivalJson.category.short_name]

            log.info "Parsing Eventbrite festival named '$festival.name'"

            def floatParser = { elementName, node = festivalJson ->

                log.trace "Parsing float from element '$elementName'"
                String elementContent = node."$elementName"

                if (elementContent) {
                    elementContent.toFloat()
                }
            }

            def venue = festivalJson.venue

            festival.start = parseDate('utc', festivalJson.start)
            festival.end = parseDate('utc', festivalJson.end)

            // there's also a lat-lng coordinate in the venue.address node, but it appears to be the same as that
            // in the venue node
            festival.latitude = floatParser('latitude', venue)
            festival.longitude = floatParser('longitude', venue)
            festival.synopsis = festivalJson.description.text

            def address = venue.address
            festival.addressLine1 = address.address_1
            festival.addressLine2 = address.address_2
            festival.city = address.city
            festival.region = address.region
            festival.postCode = address.postal_code

            // convert 2-letter country code to 3-letter
            String twoLetterCode = address.country
            String threeLetterCode = CountryCode.getByCode(twoLetterCode, false).alpha3.toLowerCase()
            assert threeLetterCode in EuropeTagLib.ISO3166_3.keySet()

            festival.countryCode = threeLetterCode
            log.debug "Retrieved country name '$festival.countryName' for code '$threeLetterCode'"

            def availableTickets = festivalJson.ticket_classes
            List<EventbriteTicket> tickets = availableTickets.collect { getTicket(it) }

            // "as boolean" converts null to false
            festival.freeEntry = tickets?.every { it.free } as boolean

            def ticketModel = [tickets: tickets, ticketUrl: festival.website]
            festival.ticketInfo = pageRenderer.render(template: '/festival/eventbriteTicketInfo', model: ticketModel)

            festival.approved = false

            Long eventbriteId = festivalJson.id.toLong()
            parsedFestivals[eventbriteId] = festival
        }
        parsedFestivals
    }

    private EventbriteTicket getTicket(ticketJson) {

        // convert SOLD_OUT to Sold Out
        String status = ticketJson.on_sale_status.toString().replace('_', ' ')
        status = WordUtils.capitalizeFully(status)

        new EventbriteTicket(
                name: ticketJson.name,
                price: ticketJson.cost?.display,
                status: status,
                free: ticketJson.free
        )
    }
}

class EventbriteTicket {
    Boolean free
    String name
    String price
    String status
}