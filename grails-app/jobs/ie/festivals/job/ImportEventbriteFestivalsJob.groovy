package ie.festivals.job

import com.neovisionaries.i18n.CountryCode
import grails.gsp.PageRenderer
import grails.plugin.geocode.GeocodingService
import groovy.json.JsonSlurper
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.HTTPBuilder
import ie.festivals.Festival
import ie.festivals.ImportAudit
import ie.festivals.tag.EuropeTagLib
import ie.festivals.parser.EventbriteJsonFestivalParser
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.transaction.TransactionStatus

import static com.neovisionaries.i18n.CountryCode.Assignment.OFFICIALLY_ASSIGNED
import static groovyx.net.http.ContentType.TEXT
import static groovyx.net.http.Method.GET
import static ie.festivals.enums.FestivalSource.EVENTBRITE

/**
 * Quartz job that imports festivals from the <a href="http://www.eventbrite.com/developer/v3/endpoints/events/">Eventbrite event search API</a>
 */
class ImportEventbriteFestivalsJob {

    static triggers = {
        cron cronExpression: '0 0 7 * * ?'
    }

    def concurrent = false

    GrailsApplication grailsApplication
    PageRenderer groovyPageRenderer

    /**
     * Append our access token to the following URL to verify this ID
     * https://www.eventbriteapi.com/v3/formats/?token=
     */
    private FESTIVAL_FORMAT_ID = 5

    private Map getRequestParams(String threeLetterCountryCode) {

        String todayStart = new Date().format('yyyy-MM-dd') + 'T00:00:00Z'

        Map params = [
                formats                 : FESTIVAL_FORMAT_ID,
                format                  : 'json',
                'start_date.range_start': todayStart
        ]

        // we need to check the assignment type as well because some countries (e.g. Finland) have more than one code
        CountryCode countryCode = CountryCode.values().find {
            it.alpha3 == threeLetterCountryCode && it.assignment == OFFICIALLY_ASSIGNED
        }

        String twoLetterCountryCode = countryCode.alpha2
        params['venue.country'] = twoLetterCountryCode

        // only include popular events if the country is not ireland
        if (twoLetterCountryCode != CountryCode.IE.alpha2) {
            params.popular = true
        }

        params
    }

    def execute() {
        log.info "$ImportEventbriteFestivalsJob.simpleName started at ${new Date()}"

        def eventbriteConfig = grailsApplication.config.festival.eventbrite
        String accessToken = eventbriteConfig.accessToken
        String feedUrl = eventbriteConfig.feedUrl
        def beforeFestivalCount = countEventbriteFestivals()

        EuropeTagLib.ISO3166_3.keySet().each { countryCode ->

            def requestParams = getRequestParams(countryCode.toUpperCase())

            // TODO refactor to expect a JSON response, rather than TEXT
            new HTTPBuilder().request(feedUrl, GET, TEXT) { req ->

                uri.query = requestParams
                log.info "Eventbrite request URL: $feedUrl, params: $requestParams"

                headers.Authorization = "Bearer $accessToken"
                headers.Accept = 'application/xml'

                response.success = { resp, Reader reader ->

                    Map response = new JsonSlurper().parse(reader)

                    def jsonParser = new EventbriteJsonFestivalParser(groovyPageRenderer)
                    Map<Long, Festival> parsedFestivals = jsonParser.parse(response)
                    log.info "Successfully parsed ${parsedFestivals.size()} festival(s) from Eventbrite for country $countryCode"

                    parsedFestivals.each { Long eventbriteId, Festival festival ->
                        saveFestival(eventbriteId, festival)
                    }
                }

                response.failure = { resp, Reader reader ->
                    // In practice the response to most invalid requests is 200, so we need to check the JSON response
                    // for an error field
                    Map response = new JsonSlurper().parse(reader)
                    log.error "Eventbrite event search error for request params: $requestParams. $response.error: ${response.'error_description'}"
                }
            }
        }
        log.info "Eventbrite import completed ${countEventbriteFestivals() - beforeFestivalCount} festivals imported"
    }

    private Integer countEventbriteFestivals() {
        Festival.countBySource(EVENTBRITE)
    }

    private saveFestival(Long eventId, Festival festival) {

        // If we've previously parsed this event, ignore it
        if (!ImportAudit.findByEventIdAndSource(eventId, EVENTBRITE)) {

            // It would be preferable to use withNewTransaction instead of withTransaction, because the latter allegedly
            // does not necessarily leave the session in a valid state after a rollback. However withNewTransaction does
            // not participate in integration test transactions, so it's use can lead to test pollution. Check the JIRA
            // I opened for a solution: https://jira.grails.org/browse/GRAILS-11721
            Festival.withTransaction { TransactionStatus status ->

                try {
                    if (festival.save()) {
                        new ImportAudit(eventId: eventId, source: EVENTBRITE).save(failOnError: true)
                        log.info "Saved Eventbrite festival named: $festival.name"

                    } else {
                        log.error "Cannot save Eventbrite festival due to errors: $festival.errors"
                    }
                } catch (ex) {
                    status.setRollbackOnly()
                    log.error "Failed to import '$festival.name' with Eventbrite ID: $eventId due to errors: $festival.errors", ex
                }
            }
        }
    }
}