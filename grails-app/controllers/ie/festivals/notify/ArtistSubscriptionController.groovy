package ie.festivals.notify

import grails.plugins.springsecurity.Secured
import grails.plugins.springsecurity.SpringSecurityService
import ie.festivals.AbstractController
import ie.festivals.Artist
import ie.festivals.ArtistService
import ie.festivals.User
import ie.festivals.enums.ArtistAction

import static javax.servlet.http.HttpServletResponse.SC_OK

class ArtistSubscriptionController extends AbstractController {

    static allowedMethods = [delete: "POST"]

    SpringSecurityService springSecurityService
    ArtistService artistService

    // This method looks like it should be @Secured, but we want to allow unregistered users to see the page this
    // renders as a sort of teaser to encourage registrations
    def list() {
        // Don't let the browser cache this page: https://pl3.projectlocker.com/SummerFestivals/Weceem/trac/ticket/199
        cache false
        [artistInstanceList: artistService.currentSubscriptions()]
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def subscriptionSearch() {
        String query = params.artistName
        List<Artist> searchResults = artistService.artistSubscriptionSearch(query)
        render template: 'searchResults',
                model: [artistAction: ArtistAction.SUBSCRIBE, artistInstanceList: searchResults]
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def delete() {
        Long artistId = params.long('id')
        User user = springSecurityService.currentUser

        Integer deletedRows = ArtistSubscription.executeUpdate(
                "delete ArtistSubscription sub where sub.user = ? and sub.artist.id = ?", [user, artistId])

        if (deletedRows != 1) {
            log.error "No subscription found for user ID $user.id and artist ID $artistId"
        }
        render status: SC_OK
    }

    /**
     * Save a new artist and subscribe to them
     * @return
     */
    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def add(Artist artist) {

        if (artist.id) {
            // if the artist was retrieved from the Lucene index, properties that are not available therein
            // (mbid, image, etc.) will always be null, so make sure we don't save it
            artist.discard()

        } else {
            artist = artistService.saveNew(artist)
        }

        log.debug "Saving subscription for artist with ID: $artist.id"
        User user = springSecurityService.currentUser
        ArtistSubscription.findOrSaveWhere(user: user, artist: artist)
        render template: '/artist/artistListEntry', model: [artist: artist, artistAction: ArtistAction.UNSUBSCRIBE]
    }
}