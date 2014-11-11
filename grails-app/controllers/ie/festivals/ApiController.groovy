package ie.festivals

import grails.converters.JSON
import grails.gsp.PageRenderer
import grails.plugin.cache.Cacheable
import ie.festivals.command.FestivalFilterCommand
import ie.festivals.command.FestivalGeoSearchCommand
import ie.festivals.enums.Priority
import ie.festivals.map.MapFocalPoint

import javax.servlet.http.HttpServletResponse

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST

class ApiController {

    FestivalService festivalService
    PageRenderer groovyPageRenderer

    static allowedMethods = [
            festivalGeoSearch: 'GET',
            festivalSearch: 'GET',
            festivalDetail: 'GET',
            festivalsByArtist: 'GET'
    ]

    @Lazy
    private String dateFormat = grailsApplication.config.festival.dateFormat
    private static final TIME_FORMAT = 'HH:mm'

    def beforeInterceptor = {
        boolean validUser = User.countByApiKeyAndUsername(params.key, params.user) == 1

        if (!validUser) {
            renderError(HttpServletResponse.SC_FORBIDDEN, 'api.unauthorised')
        }
        log.debug "${validUser ? 'Valid' : 'Invalid'} user $params.user with key $params.key"
        validUser
    }

    /**
     * Will handle any uncaught exceptions thrown by this controller's actions
     * http://grails.org/doc/latest/guide/single.html#controllerExceptionHandling
     * @param ex
     * @return
     */
    def handleApiException(Exception ex) {
        log.error "Error processing API request with params: $request.queryString", ex
        renderError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 'api.error', [getApiDocsLink()])
    }

    def festivalGeoSearch(FestivalGeoSearchCommand command) {

        if (command.validate()) {
            List<Festival> nearbyFestivals = festivalService.getNearbyFestivals(command)
            renderAsJson(nearbyFestivals)
        } else {
            renderError(SC_BAD_REQUEST, 'api.invalidParams', [request.queryString, getApiDocsLink()])
        }
    }

    def festivalsByArtist() {

        Artist artist = Artist.read(params.id)

        if (artist) {
            List<Festival> festivals = festivalService.getPerformances(artist, true)
            renderAsJson(festivals)

        } else {
            renderError(SC_BAD_REQUEST, 'api.invalidArtistId', [getApiDocsLink()])
        }
    }

    def festivalDetail() {
        Long festivalId = params.long('id')
        Festival festival = festivalId ? Festival.findByIdAndApproved(festivalId, true) : null

        if (festival) {
            Map festivalProperties = getFestivalProperties(festival)

            if (festival.synopsis) {
                festivalProperties.synopsis = groovyPageRenderer.render(
                        template: '/api/synopsis', model: [synopsis: festival.synopsis])
            } else {
                festivalProperties.synopsis = null
            }

            List<Performance> performances = festivalService.getLineup(festival)

            festivalProperties.lineup = performances.collect { Performance performance ->
                getArtistProperties(performance)
            }
            render contentType: 'application/json', text: festivalProperties as JSON
        } else {
            renderError(SC_BAD_REQUEST, 'api.missingFestivalId', [getApiDocsLink()])
        }
    }

    private Map getArtistProperties(Performance performance) {
        Artist artist = performance.artist

        def artistProperties = [
                id: artist.id,
                name: artist.name,
                headline: performance.priority == Priority.HEADLINE,
        ]

        artistProperties.performanceDate = performance.date?.format(dateFormat)
        artistProperties.performanceTime = performance.hasPerformanceTime ? performance.date.format(TIME_FORMAT) : null

        if (artist.hasLocalImages()) {
            artistProperties.image = getLocalImageUrl(artist.image)
            artistProperties.thumbnail = getLocalImageUrl(artist.thumbnail)

        } else {
            artistProperties.image = artist.image
            artistProperties.thumbnail = artist.thumbnail
        }
        artistProperties
    }

    private getLocalImageUrl(String localImagePath) {
        createLink(absolute: true, controller: 'artist', action: 'getLocalImage', params: [path: localImagePath])
    }

    @Cacheable('festivalGroup')
    def festivalSearch(ApiSearchCommand command) {

        if (command.validate()) {
            def searchCriteria = [
                    freeOnly: command.freeOnly,
                    countryCode: command.location?.countryCode,
                    types: command.types,
                    futureOnly: command.futureOnly,
                    max: command.max,
                    offset: command.offset
            ]

            if (command.hasDates()) {
                searchCriteria.dateRange = command.start..command.end
            }

            def festivals = festivalService.findAll(searchCriteria)
            renderAsJson(festivals)
        } else {
            renderError(SC_BAD_REQUEST, 'api.invalidParams', [request.queryString, getApiDocsLink()])
        }
    }

    private getApiDocsLink() {
        g.createLink(absolute: true, uri: '/apiDocs')
    }

    private renderError(Integer httpStatus, String msgCode, List msgArgs = Collections.emptyList()) {
        def message = g.message(code: msgCode, args: msgArgs)
        render contentType: 'application/json', text: [errorMessage: message] as JSON, status: httpStatus
    }

    private renderAsJson(List<Festival> festivals) {

        def festivalResults = festivals.collect { Festival fvl ->

            def festivalProperties = getFestivalProperties(fvl)

            if (fvl.distance != null) {
                festivalProperties.distance = fvl.distance
            }
            festivalProperties
        }
        render contentType: 'application/json', text: festivalResults as JSON
    }

    private Map getFestivalProperties(Festival fvl) {
        def festivalUrl = festival.showFestivalUrl(festival: fvl, absolute: true)

        return [id: fvl.id,
                name: fvl.name,
                start: fvl.start.format(dateFormat),
                end: fvl.end.format(dateFormat),
                type: fvl.type.toString(),
                url: festivalUrl,
                website: fvl.website,
                address: fvl.fullAddress,
                latitude: fvl.latitude,
                longitude: fvl.longitude,
                lineupAllowed: fvl.hasLineup]
    }
}

class ApiSearchCommand extends FestivalFilterCommand {
    Date start
    Date end

    /**
     * The parent class defaults to Ireland, but in the API we default to anywhere
     */
    MapFocalPoint location = MapFocalPoint.EUROPE

    void setStart(Date start) {
        this.start = start?.clearTime()
    }

    void setEnd(Date end) {
        this.end = end?.clearTime()
    }

    boolean hasDates() {
        start || end
    }

    static constraints = {
        start nullable: true
        types nullable: true
        end nullable: true, validator: {end, self ->

            if (self.hasDates()) {
                // if one date is provided then the other date must also be provided
                self.start && end && self.start <= end
            }
        }
    }
}
