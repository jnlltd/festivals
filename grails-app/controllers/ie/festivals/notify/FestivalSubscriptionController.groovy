package ie.festivals.notify

import grails.plugins.springsecurity.Secured
import ie.festivals.AbstractController
import ie.festivals.Festival
import ie.festivals.FestivalService
import ie.festivals.User
import javax.servlet.http.HttpServletResponse

class FestivalSubscriptionController extends AbstractController {

    static allowedMethods = [create: "POST", delete: "POST"]
    def springSecurityService
    NotificationService notificationService
    FestivalService festivalService

    def list() {

        if (springSecurityService.isLoggedIn()) {
            def subscribedFestivalIds = notificationService.getSubscribedFestivals()*.id

            // Can't subscribe to a festival that can't have a lineup
            def subscribableFestivals = festivalService.findAll(futureOnly: true, hasLineup: true)
            [festivalInstanceList: subscribableFestivals, subscriptionIds: subscribedFestivalIds]
        }
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def create() {
        def festival = getSafelyById(Festival)
        User currentUser = springSecurityService.currentUser
        def subscription = FestivalSubscription.findOrSaveWhere(festival: festival, user: currentUser)

        render status: subscription ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def delete() {
        def festival = getSafelyById(Festival)
        User currentUser = springSecurityService.currentUser

        Integer deletedRows = FestivalSubscription.executeUpdate(
                "delete FestivalSubscription fs where fs.user = ? and fs.festival.id = ?", [currentUser, festival?.id])

        if (deletedRows != 1) {
            log.error "No subscription found for user ID ${currentUser?.id} and festival ID ${festival?.id}"
        }
        render status: HttpServletResponse.SC_OK
    }
}
