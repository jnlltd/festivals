package ie.festivals

import grails.plugins.springsecurity.Secured
import ie.festivals.enums.ArtistAction
import ie.festivals.enums.Priority
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.servlet.http.HttpServletResponse

@Secured(['ROLE_ADMIN'])
class PerformanceController extends AbstractController {

    static allowedMethods = [delete: "POST"]

    PerformanceService performanceService
    ArtistService artistService
    GrailsApplication grailsApplication

    def performerSearch() {

        Festival festival = Festival.read(params.festivalId)
        List<Artist> searchResults = artistService.festivalLineupSearch(params.artistName, festival)

        render(template: '/artist/searchResultsWithCustomImageOption',
                model: [artistAction: ArtistAction.LINEUP_ADD,
                        artistInstanceList: searchResults,
                        festival: festival])
    }

    def add(AddPerformerCommand command) {

        if (validatePerformer(command)) {
            Performance performance = performanceService.addPerformer(command)
            def currentArtistSubscriptionIds = artistService.currentSubscriptions()*.id

            render(template: '/artist/artistListEntry', model: [
                    artist: performance.artist,
                    performance: performance,
                    subscribedArtistIds: currentArtistSubscriptionIds,
                    artistAction: ArtistAction.LINEUP_DELETE])
        }
    }

    def delete() {
        Performance performance = getSafelyById(Performance)

        if (performance) {
            performance.deleted = true
            performance.save(flush: true, failOnError: true)
        } else {
            log.error "Performance instance with ID '$params.id' not found"
        }
        render status: HttpServletResponse.SC_OK
    }

    /**
     * Creates a new artist, and adds them to a festival's lineup. A custom image will be used for the artist
     * @return
     */
    def addPerformerWithCustomImage(AddPerformerCommand command) {

        if (validatePerformer(command)) {
            performanceService.addPerformerWithCustomImage(command)
            flashHelper.info 'artist.addToLineup.success': command.artist.name
        }
        redirect controller: 'festival', action: 'show', id: command.festival.id
    }

    private boolean validatePerformer(AddPerformerCommand command) {

        // if the artist was retrieved from the Lucene index, properties that are not available therein
        // (mbid, image, etc.) will always be null, so make sure we don't save it
        if (command.artist?.id) {
            command.artist.discard()
        }

        boolean isValid = !command.hasErrors()

        if (!isValid) {
            String artistName = command.artist?.name
            log.error "Failed to add artist '$artistName' to festival $command.festival due to errors ${command.errors}"
            flashHelper.warn 'artist.addToLineup.fail': artistName
            render status: HttpServletResponse.SC_BAD_REQUEST
        }
        isValid
    }
}

class AddPerformerCommand {
    Priority priority
    Festival festival
    String image
    Date date
    Integer hour
    Integer minute
    Artist artist

    static constraints = {
        image nullable: true, url: true
        date nullable: true
        hour nullable: true, range: 0..23
        minute nullable: true, range: 0..59
    }
}