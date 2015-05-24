package ie.festivals.parser

import grails.gsp.PageRenderer
import grails.plugin.geocode.GeocodingService
import grails.plugin.geocode.Point
import groovy.util.slurpersupport.GPathResult
import ie.festivals.Artist
import ie.festivals.ArtistService
import ie.festivals.Festival
import ie.festivals.Performance
import ie.festivals.enums.FestivalSource
import ie.festivals.enums.Priority
import ie.festivals.tag.EuropeTagLib
import org.apache.commons.lang.StringUtils

import java.text.NumberFormat

class SkiddleXmlFestivalParser implements ApiResponseParser<Map<Long, Festival>> {

    private static final UK_COUNTRY_CODE = 'gbr'

    /**
     * Ticket status:
     * 1   => available
     * 300 => end date has passed (e.g. early bird)
     *
     * There are probably various other values, but I've no idea what they are or what they might mean
     */
    private static final TICKET_STATUS_AVAILABLE = 1

    private ArtistService artistService
    private String skiddleId
    private GeocodingService geocodingService
    private PageRenderer pageRenderer

    private NumberFormat ukNumberFormatter = NumberFormat.getInstance(Locale.UK)

    SkiddleXmlFestivalParser(ArtistService artistService, GeocodingService geocodingService, skiddleConfig,
                             PageRenderer pageRenderer) {
        this.skiddleId = skiddleConfig.tag
        this.artistService = artistService
        this.geocodingService = geocodingService
        this.pageRenderer = pageRenderer
    }

    private String parseAddressField(field) {
        String addressField = field.text().trim()

        // sometimes an address field just contains a '#'
        if (addressField == '#') {
            return null
        }
        StringUtils.removeEnd(addressField, ',')
    }

    @Override
    Map<Long, Festival> parse(GPathResult xmlResponse) {

        Map<Long, Festival> parsedFestivals = [:]

        xmlResponse.events.event.each {festivalXml ->

            Festival festival = new Festival(name: festivalXml.name.text(), source: FestivalSource.SKIDDLE)

            // add our tracking code to the skiddle URL
            String skiddleUrl = festivalXml.@link.text()
            festival.website = StringUtils.replaceOnce(skiddleUrl, 'sktag=XXX', "sktag=$skiddleId").replace(' ', '%20')

            Long skiddleEventId = festivalXml.@id.text().toLong()
            String date = festivalXml.date.text()
            festival.start = festival.end = Date.parse('yyyy-MM-dd', date)

            festival.synopsis = festivalXml.shortdesc.text()

            // address
            def venue = festivalXml.venue
            festival.addressLine2 = parseAddressField(venue.address)

            def city = parseAddressField(venue.city)
            def town = parseAddressField(venue.town)

            // The address does not include a country. Festivals outside UK have the following retarded address format
            //
            // <name>The Garden</name>
            // <address>Tisno, Petrica Glava 34 22240</address>
            // <city>Croatia</city>
            // <town>Tisno</town>
            // <citycode>AB10</citycode>
            // <postcode/>

            def nonUkCountry = EuropeTagLib.ISO3166_3.find {countryCode, countryName ->
                countryName.equalsIgnoreCase(city)
            }

            if (nonUkCountry) {
                festival.countryCode = nonUkCountry.key
                festival.countryName = nonUkCountry.value
                festival.city = town

            } else {
                festival.countryCode = UK_COUNTRY_CODE
                festival.countryName = EuropeTagLib.ISO3166_3[UK_COUNTRY_CODE]

                // if no city is provided, set the city to the town
                festival.city = (town as Boolean ^ city as Boolean) ? (town ?: city) : city
            }

            // ticket info
            List tickets = []
            festivalXml.tickets.ticket.each { addTicket(it, tickets) }

            def ticketModel = [tickets: tickets, ticketUrl: festival.website, date: festival.start]
            festival.ticketInfo = pageRenderer.render(template: '/festival/skiddleTicketInfo', model: ticketModel)

            festivalXml.artists.artist.each {artist ->
                addPerformance(artist, festival)
            }

            try {
                // stay on the good side of Google's rate-limiting
                // https://developers.google.com/maps/documentation/geocoding/#Limits
                Thread.sleep(500)
                Point location = geocodingService.getPoint(festival.fullAddress)

                if (location) {
                    festival.latitude = location.latitude
                    festival.longitude = location.longitude
                }
            } catch (ex) {
                log.warn "Error geolocating address $festival.fullAddress", ex
            }

            // include the venue name in the address after the address has been geocoded
            festival.addressLine1 = parseAddressField(venue.name)

            festival.approved = false
            parsedFestivals[skiddleEventId] = festival
        }
        parsedFestivals
    }

    private addPerformance(artistXml, Festival festival) {
        String artistName = artistXml.name.text()

        // Don't pass the Festival because it's transient, so we can't run the queries that require it to
        // have an ID
        List<Artist> artists = artistService.festivalLineupSearch(artistName, null, true)

        // If there's more than one match I've no idea which one to choose
        if (artists.size() == 1) {

            // Artist might have been retrieved from Lucene index, so re-read it so that it is
            // recognised as a persistent object
            Artist artist = artists[0]
            artist = artist.id ? Artist.get(artist.id) : artist

            Performance performance = new Performance(artist: artist, festival: festival, priority: Priority.HEADLINE)
            festival.addToPerformances(performance)
        }
    }

    private void addTicket(ticket, List<Ticket> allTickets) {

        Integer ticketStatus = ticket.status.@statuscode.text().toInteger()

        if (ticketStatus == TICKET_STATUS_AVAILABLE) {
            String ticketDescription = ticket.name.text()
            def faceValue = ukNumberFormatter.parse(ticket.faceValue.text())
            def bookingFee = ukNumberFormatter.parse(ticket.bookingFee.text())
            def totalCost = faceValue + bookingFee

            allTickets << new Ticket(description: ticketDescription, cost: "£$totalCost")
        }
    }
}

class Ticket {
    def description
    def cost
}
