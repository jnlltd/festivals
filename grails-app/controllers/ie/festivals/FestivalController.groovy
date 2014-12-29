package ie.festivals

import grails.converters.JSON
import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import grails.plugin.geocode.GeocodingException
import grails.plugin.geocode.Point
import grails.plugins.springsecurity.Secured
import grails.util.GrailsUtil
import grails.web.JSONBuilder
import ie.festivals.command.AddRatingCommand
import ie.festivals.command.CalendarEventCommand
import ie.festivals.command.FestivalFilterCommand
import ie.festivals.enums.FestivalSource
import ie.festivals.enums.FestivalType
import ie.festivals.enums.Priority
import ie.festivals.enums.RatingType
import ie.festivals.map.MapFocalPoint
import ie.festivals.notify.FestivalSubscription
import ie.festivals.notify.NotificationService
import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.plugin.geocode.GeocodingService

import javax.servlet.http.HttpServletResponse

class FestivalController extends AbstractController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    static defaultAction = "map"

    private static final MAX_REMINDER_DAYS = 10
    private static final DEFAULT_ZOOM_LEVEL = 7

    @Lazy
    private String dateFormat = grailsApplication.config.festival.dateFormat

    def springSecurityService

    ArtistService artistService
    FestivalService festivalService
    GeocodingService geocodingService
    GrailsApplication grailsApplication
    UserRegistrationService userRegistrationService
    NotificationService notificationService

    def list(FestivalFilterCommand command) {

        def festivals = festivalService.findAll(
                freeOnly: command.freeOnly,
                futureOnly: command.futureOnly,
                countryCode: command.location.countryCode,
                types: command.types)

        // If command.types is bound to null by a URL such as /festival/list, the query will include all festival types.
        // Set command.types to all types to ensure that the selected types in the filter match the query's defaults
        command.types = command.types ?: FestivalType.values()
        [festivalInstanceList: festivals, command: command]
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def create() {
        [festivalInstance: new Festival()]
    }

    private guessLocationIfNecessary(Festival festival) {

        // To determine if lat/long were provided, read the values from params rather than festival, because
        // if non-numeric values are provided, the binding to festival will have failed
        boolean locationMissing = !params.latitude && !params.longitude
        String fullAddress = festival.fullAddress

        if (fullAddress && locationMissing) {
            try {
                Point location = geocodingService.getPoint(fullAddress)
                if (location) {
                    festival.latitude = location.latitude
                    festival.longitude = location.longitude
                } else {
                    log.warn "failed to guess lat/long coordinates of address '$fullAddress'"
                }
            } catch (GeocodingException ex) {
                log.error "failed to guess lat/long coordinates of '$fullAddress'", ex
            }
        }
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    @CacheEvict(value = 'festivalGroup', allEntries = true)
    def save() {
        Festival festival = new Festival(params)

        if (!isAdmin()) {
            festival.videoUrl = null
            festival.approved = false
            festival.hasLineup = festival.type?.hasLineup()
        }

        guessLocationIfNecessary(festival)

        if (!festival.save()) {
            flashHelper.warn 'default.invalid': 'Festival'
            render(view: "create", model: [festivalInstance: festival])
            return
        }

        if (festival.approved) {
            flashHelper.info 'default.created': festival.name
        } else {
            flashHelper.info 'festival.user.created': festival.name
        }
        redirect(uri: getFestivalUrl(festival, [absolute: true]))
    }

    def showSimilarFestivals() {

        Festival festival = getSafelyById(Festival, true)

        if (festival) {
            def similarFestivals = festivalService.getSimilarFestivals(festival)

            // This command specifies the initial values that will be shown in the filter.
            def command = new FestivalFilterCommand(
                    location: MapFocalPoint.fromCountryCode(festival.countryCode),
                    types: similarFestivals.type.unique(),
                    futureOnly: true,
                    freeOnly: false)

            def heading = message(code: 'similar.festival.heading', args: [festival.name])
            render view: 'list', model:  [festivalInstanceList: similarFestivals, command: command, heading: heading]

        } else {
            flashHelper.warn 'festival.similar.notFound'
            log.warn "No festival found with ID: $params.id"
            redirect controller: 'home'
        }
    }

    def show() {

        if (isAdmin()) {
            // Don't let the browser cache this page if making changes to the lineup: https://pl3.projectlocker.com/SummerFestivals/Weceem/trac/ticket/199
            cache false
        }

        Festival fvl = getSafelyById(Festival)

        // Only admins can view unapproved festivals
        if (!fvl || !festivalService.isAccessible(fvl)) {
            flashHelper.warn 'default.not.found.message': ['Festival', params.id]
            redirect(controller: "home")
            return
        }

        def mapData = [
                center: new Point(latitude: fvl.latitude, longitude: fvl.longitude),
                zoom: DEFAULT_ZOOM_LEVEL,
        ]

        log.debug "returning map data for festival $mapData"

        User user = springSecurityService.currentUser
        def (avgTicketRating, ticketRateable) = getRating(user, fvl, RatingType.TICKET_PRICE)
        def (avgLineupRating, lineupRateable) = getRating(user, fvl, RatingType.LINEUP)

        Map<Priority, List<Performance>> festivalLineup = Collections.emptyMap()

        // Optimisation: avoid running the lineup queries unnecessarily
        if (fvl.hasLineup) {
            festivalLineup = festivalService.getPrioritisedLineup(fvl)
        }

        SortedSet<Date> lineupFilterDates = getLineupFilterDates(fvl, festivalLineup)

        // Optimisation: avoid running the queries if user not logged in
        boolean isSubscribed = user && FestivalSubscription.countByFestivalAndUser(fvl, user)
        boolean isFavorite = user && FavoriteFestival.countByFestivalAndUser(fvl, user)
        Reminder reminder = user ? Reminder.findByFestivalAndUser(fvl, user) : null

        def countdown = countdownDays(fvl)
        def similarFestivals = festivalService.getSimilarFestivals(fvl)

        // Optimisation: avoid running subscription query if there are no performers
        def subscribedArtistIds = festivalLineup ? Collections.emptyList() : artistService.currentSubscriptions()*.id

        // Can't invoke this taglib in the GSP because it's namespace (festival) is shadowed
        // by a model variable
        String canonicalUrl = festival.showFestivalUrl(festival: fvl, absolute: true)
        String description = festival.getDescription(festival: fvl)

        // If we were redirected here from ReviewController.submit and the review was not saved
        // we should populate the review form with it
        Review review = flash.invalidReview ?: new Review()
        List<Review> approvedReviews = Review.findAllByApprovedAndFestival(true, fvl)

        // accommodation start/end dates cannot be in the past, leave them blank if the festival is over
        Date today = new Date()
        Date accomStart = fvl.isFinished() ? null : today > fvl.start ? today : fvl.start
        Date accomEnd = fvl.isFinished() ? null : fvl.end + 1

        return [festival: fvl,
                accomStart: accomStart,
                accomEnd: accomEnd,
                prioritisedLineup: festivalLineup,
                mapData: mapData,
                filterDates: lineupFilterDates,
                ticketRateable: ticketRateable, ticketRating: avgTicketRating,
                lineupRateable: lineupRateable, lineupRating: avgLineupRating,
                subscribed: isSubscribed,
                countdown: countdown,
                reminderRange: getReminderRange(fvl),
                reminder: reminder,
                similarFestivals: similarFestivals,
                subscribedArtistIds: subscribedArtistIds,
                canonicalUrl: canonicalUrl,
                approvedReviews: approvedReviews,
                review: review,
                description: description,
                isFavorite: isFavorite]
    }

    /**
     * Returns the dates that should appear in the festival lineup filter.
     */
    private SortedSet<Date> getLineupFilterDates(Festival festival, Map<Priority, List<Performance>> festivalLineup) {

        Set<Date> filterDates = new TreeSet<>()

        if (!festival.multiDayDuration) {
            return filterDates
        }

        festivalLineup.each {Priority priority, List<Performance> performances ->

            // clone the date before clearing the time or we'll clear the date of the Performance objects
            List<Date> performanceDates = performances.collect { it.date?.clone()?.clearTime() }

            // filter out any null values
            filterDates.addAll(performanceDates.findAll())
        }
        filterDates
    }

    private Range getReminderRange(Festival festival) {

        def tomorrow = new Date().clearTime() + 1
        def festivalStart = festival.start

        if (tomorrow > festivalStart) {
            return null
        }

        def maxReminderDays = Math.min(festivalStart - tomorrow, MAX_REMINDER_DAYS)
        0..maxReminderDays
    }

    /**
     * calculate the number of days until the festival starts
     * @param festival
     * @return null if the festival is over, 0 if it's currently happening, or the number of days until it begins
     */
    private Integer countdownDays(Festival festival) {

        Date today = new Date().clearTime()

        if (festival.end >= today) {
            today in festival.start..festival.end ? 0 : festival.start - today
        }
    }

    /**
     * Returns festival's average rating and a boolean that indicates whether it can be rated by the current user
     * @param festival
     * @param type
     * @return
     */
    private getRating(User currentUser, Festival festival, RatingType type) {

        def averageRating = festivalService.getAverageRating(festival, type)
        boolean rateable = springSecurityService.isLoggedIn() && !festivalService.getRating(festival, currentUser, type)

        [averageRating, rateable]
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def addRating(AddRatingCommand ratingCommand) {
        Festival festival = Festival.findByIdAndApproved(ratingCommand.festivalId, true)
        def averageRating

        if (festival) {
            User user = springSecurityService.currentUser
            averageRating = festivalService.addRating(festival, user, ratingCommand.type, ratingCommand.score)
        } else {
            averageRating = festivalService.getAverageRating(festival, ratingCommand.type)
        }
        render([updatedAverage: averageRating] as JSON)
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def edit() {
        def festivalInstance = getSafelyById(Festival)

        if (!festivalInstance || !festivalService.isAccessible(festivalInstance)) {
            flashHelper.warn 'default.not.found.message': ['Festival', params.id]
            redirect(action: "list")
            return
        }

        [festivalInstance: festivalInstance]
    }

    @Secured(['ROLE_ADMIN'])
    def createClone() {
        def festival = getSafelyById(Festival)
        render view: 'create', model: [festivalInstance: festival.clone()]
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    @CacheEvict(value = 'festivalGroup', allEntries = true)
    def update() {
        Festival festival = getSafelyById(Festival)
        boolean wasApproved = festival.approved

        if (!festival || !festivalService.isAccessible(festival)) {
            flashHelper.warn 'default.not.found.message': ['Festival', params.id]
            redirect(action: "list")
            return
        }

        if (isStale(festival)) {
            render(view: "edit", model: [festivalInstance: festival])
            return
        }

        String previousFestivalName = festival.name
        festival.properties = params

        // The name of a skiddle festival cannot be changed or the event merging won't work
        if (festival.source == FestivalSource.SKIDDLE && previousFestivalName != festival.name) {
            festival.name = previousFestivalName
            flashHelper.warn 'festival.skiddleName': previousFestivalName
            render(view: "edit", model: [festivalInstance: festival])
            return
        }

        guessLocationIfNecessary(festival)

        if (!festival.save(flush: true)) {
            flashHelper.warn 'default.invalid': 'Festival'
            render(view: "edit", model: [festivalInstance: festival])
            return
        }

        // if the festival was created by a user and has been approved for the first time
        // send the user an email notifying them
        if (!wasApproved && festival.approved && !festival.source == FestivalSource.SKIDDLE) {

            String adminRoleName = grailsApplication.config.festival.adminRoleName
            List<Long> adminIds = userRegistrationService.findAllByRole(adminRoleName).id

            User festivalAuthor = festival.createdBy

            if (!(festivalAuthor.id in adminIds)) {
                notificationService.sendFestivalApprovedNotification(festivalAuthor, festival)
            }
        }

        redirect(uri: getFestivalUrl(festival, [absolute: true]))
    }

    @Secured(['ROLE_ADMIN'])
    @CacheEvict(value = 'festivalGroup', allEntries = true)
    def delete(Long id) {

        if (festivalService.delete(id)) {

            // deletion is invoked via AJAX from http://festivals.ie/admin/listUnapproved
            if (request.xhr) {
                render status: HttpServletResponse.SC_OK
                return
            }
            flashHelper.info 'festival.deleted': id

        } else {
            flashHelper.warn 'default.not.found.message': ['Festival', params.id]
        }

        redirect uri: "/"
    }

    /**
     * Extract the festival types from the params, because I don't know how to get Grails to bind a collection of enums
     * @param types may be an array, List or String
     * @return
     */
    @Cacheable('festivalGroup')
    def map(FestivalFilterCommand mapCommand) {

        log.debug "Rendering map for location '$mapCommand.location' and festival types $mapCommand.types"

        // we never filter by country on the map page, because the user could zoom out or pan to see other countries
        List<Festival> allFestivals = festivalService.findAll(
                freeOnly: mapCommand.freeOnly,
                futureOnly: mapCommand.futureOnly,
                types: mapCommand.types)

        // Even though MapCommand.location has a default value, it can still be bound to null for some URLs, e.g. /festival/map?location
        // If these properties are bound to null, the query will use defaults for them. We need to set
        // matching defaults in the command object to ensure that the inputs in the filter match the query's defaults
        mapCommand.location = mapCommand.location ?: MapFocalPoint.EUROPE
        mapCommand.types = mapCommand.types ?: FestivalType.values()

        def jsonMapData = buildMapJSON(mapCommand.location.location, mapCommand.location.zoomLevel, allFestivals)

        log.debug "returning JSON map data: $jsonMapData"
        [mapData: jsonMapData.toString(), command: mapCommand, festivalCount: allFestivals.size()]
    }

    private buildMapJSON(Point location, Integer zoomLevel, Collection<Festival> allFestivals) {

        def jsonMapData = new JSONBuilder().build {
            center = location
            zoom = zoomLevel

            festivals = array {
                allFestivals.each { fvl ->

                    def url = getFestivalUrl(fvl)

                    // if the festival type hasn't been set (Skiddle) show the warning image
                    def markerImagePath = fvl.type ? "map/${fvl.type.id}.png" : "map/warning.png"
                    def resolvedMarkerImagePath = asset.assetPath(src: markerImagePath)
                    def start = g.formatDate(date: fvl.start)
                    def end = g.formatDate(date: fvl.end)

                    festival name: fvl.name,
                            url: url,
                            latitude: fvl.latitude,
                            longitude: fvl.longitude,
                            markerImage: resolvedMarkerImagePath,
                            start: start,
                            end: end
                }
            }
        }

        jsonMapData.toString(GrailsUtil.isDevelopmentEnv())
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def requestReminder() {
        Festival festival = Festival.findByIdAndApproved(params.festivalId, true)

        // TODO Exception will be thrown if the query above returns null
        Reminder reminder = Reminder.findOrCreateByFestivalAndUser(festival, springSecurityService.currentUser)
        reminder.daysAhead = params.int('daysAhead')
        reminder.save(failOnError: true)

        def model = [reminder: reminder, reminderRange: getReminderRange(festival), festival: festival]
        render status: HttpServletResponse.SC_OK, template: 'reminder', model: model
    }


    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def cancelReminder() {
        Festival festival = getSafelyById(Festival)
        def user = springSecurityService.currentUser

        // TODO Exception will be thrown if the query above returns null
        Integer deletedRows = Reminder.executeUpdate(
                "delete Reminder r where r.user = ? and r.festival = ?", [user, festival])

        if (deletedRows != 1) {
            log.error "No reminder found for user ID $user.id and festival ID $festival.id"
        }

        def model = [reminderRange: getReminderRange(festival), festival: festival]
        render status: HttpServletResponse.SC_OK, template: 'reminder', model: model
    }

    def calendar() {
        MapFocalPoint location = MapFocalPoint.find { it.name() == params.location } ?: MapFocalPoint.EUROPE
        [location: location]
    }

    def getCalendarEvents(CalendarEventCommand calendarEventCommand) {

        if (calendarEventCommand.hasErrors()) {
            // I've seen in Airbrake that occasionally this action is being called without any params.
            // I don't know how this is happening - maybe a crawler or a manually entered URL
            log.warn "$CalendarEventCommand.simpleName validation errors: $calendarEventCommand.errors"
            redirect action: 'calendar'
            return
        }

        ObjectRange dateRange = calendarEventCommand.start..calendarEventCommand.end
        String countryCode = calendarEventCommand.location?.countryCode

        log.debug "Loading festivals between $dateRange.from and $dateRange.to"
        List<Festival> festivalEvents = festivalService.findAll(
                types: calendarEventCommand.types,
                dateRange: dateRange,
                countryCode: countryCode,
                freeOnly: calendarEventCommand.freeOnly)

        // convert to JSON format expected by calendar plugin
        def jsonFestivalEvents = new JSONBuilder().build {
            for (fvl in festivalEvents) {
                def festivalUrl = getFestivalUrl(fvl)
                def start = g.formatDate(date: fvl.start, format: dateFormat)
                def end = g.formatDate(date: fvl.end, format: dateFormat)
                def cssClassName = fvl.type.id

                element url: festivalUrl, start: start, end: end, title: fvl.name, allDay: true, className: cssClassName
            }
        }

        if (log.debugEnabled && jsonFestivalEvents) {
            String jsonResponse = jsonFestivalEvents.toString(true)
            log.debug "Calendar JSON response: $jsonResponse"
        }
        render(contentType: "application/json", text: jsonFestivalEvents.toString())
    }

    /**
     * Returns URL of festival page
     * @param fvl
     * @param additionalArgs be sure to add <tt>absolute: true</tt> to this Map if redirecting to the URL
     * @return
     */
    private String getFestivalUrl(Festival fvl, Map additionalArgs = [:]) {

        additionalArgs.festival = fvl
        festival.showFestivalUrl(additionalArgs)
    }
}