package ie.festivals

import grails.plugin.cache.CacheEvict
import grails.plugins.springsecurity.Secured
import grails.plugins.springsecurity.SpringSecurityService
import ie.festivals.notify.ArtistSubscription
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.imageio.ImageIO
import javax.servlet.http.HttpServletResponse
import java.awt.image.BufferedImage

class ArtistController extends AbstractController {

    static allowedMethods = [delete: "POST"]

    FestivalService festivalService
    ArtistService artistService
    ImageService imageService

    SpringSecurityService springSecurityService
    GrailsApplication grailsApplication

    @Lazy
    private String artistImageType = grailsApplication.config.festival.images.artistImageType

    def list() {

        def firstInitial = params.name
        def artists = Artist.withCriteria {

            if (firstInitial) {
                ilike("name", "$firstInitial%")
            } else {
                rlike("name", /^[^a-zA-Z].*/)
            }

            order("name", "asc")
        }

        def subscribedArtistIds = artistService.currentSubscriptions()*.id
        [artistInstanceList: artists, subscribedArtistIds: subscribedArtistIds]
    }

    def show() {
        Artist artist = getSafelyById(Artist)

        if (!artist) {
            flashHelper.warn 'default.not.found.message': ['Artist', params.id]
            redirect(controller: "home")
            return
        }

        // Optimisation: avoid running query if user not logged in
        User user = springSecurityService.currentUser
        boolean subscribed = user && ArtistSubscription.countByUserAndArtist(user, artist) == 1

        List<Festival> performances = festivalService.getPerformances(artist)

        def tracks = selectRandomSample(artist.topTracks, 5)
        def albums = selectRandomSample(artist.topAlbums, 4)
        [artistInstance: artist, subscribed: subscribed, performances: performances, tracks: tracks, albums: albums]
    }

    private <T> Collection<T> selectRandomSample(Collection<T> population, Integer maxSampleSize) {
        if (!population) {
            Collections.emptyList()

        } else if (population.size() <= maxSampleSize) {
            population

        } else {
            population = new ArrayList(population)
            Collections.shuffle(population)
            population[0..<maxSampleSize]
        }
    }

    @Secured(['ROLE_ADMIN'])
    def delete() {
        // Don't try to optimise this by replacing it with
        // Artist.executeUpdate("delete Artist a where a.id = ?", [artistId])
        // Because deleting it this way doesn't cascade to Track, Album, etc.
        //
        // Strictly speaking we should also evict this artists image, but I don't know how to evict only a single image
        // and it will eventually expire due to a lack of use
        Artist artist = getSafelyById(Artist)

        if (artist) {
            artist.delete()
        } else {
            log.error "No artist found with ID : $params.id"
        }
        render status: HttpServletResponse.SC_OK
    }

    // TODO Once this issue has been resolved, only evict the image of the artist in question
    // http://jira.grails.org/browse/GPCACHE-14
    @CacheEvict(value = 'image', allEntries = true)
    @Secured(['ROLE_ADMIN'])
    def updateImage(UpdateImageCommand command) {

        if (!command.validate()) {
            storeErrorMessagesInFlash(command)

        } else {
            Artist artist = Artist.get(command.artistId)
            String imageUrl = command.image

            try {
                artistService.saveArtistImagesLocally(artist, imageUrl)
            } catch (ex) {
                log.warn "Unable to change artist image to $imageUrl", ex
                flashHelper.warn 'updateImageCommand.image.url.invalid': [null, null, command.image]
            }
        }
        redirect action: 'show', id: command.artistId
    }

    /**
     * Get an artist's image that is stored on the local filesystem
     * @return
     */
    def getLocalImage(String path) {

        if (path) {
            log.debug "Retrieving local image with relative path $path"
            BufferedImage image = imageService.readLocalImage(path)
            ImageIO.write(image, artistImageType, response.outputStream)
        } else {
            // somebody (I don't think it's me), is calling this without a path param
            // https://festivals.airbrake.io/projects/83807/groups/66691126
            render status: HttpServletResponse.SC_BAD_REQUEST, text: message(code: 'localImage.path.error')
        }
    }
}

class UpdateImageCommand {
    Long artistId
    String image

    static constraints = {
        image url: true
    }
}