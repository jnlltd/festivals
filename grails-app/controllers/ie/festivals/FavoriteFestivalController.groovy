package ie.festivals

import grails.plugins.springsecurity.Secured
import ie.festivals.notify.FestivalSubscription

import javax.servlet.http.HttpServletResponse

class FavoriteFestivalController extends AbstractController {

    static allowedMethods = [create: "POST", delete: "POST"]
    def springSecurityService

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def create() {
        def festival = getSafelyById(Festival)
        User currentUser = springSecurityService.currentUser
        def success = FavoriteFestival.findOrSaveWhere(festival: festival, user: currentUser)

        render status: success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR
    }

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def delete() {
        def festival = getSafelyById(Festival)
        User currentUser = springSecurityService.currentUser

        Integer deletedRows = FestivalSubscription.executeUpdate(
                "delete FavoriteFestival f where f.user = ? and f.festival.id = ?", [currentUser, festival?.id])

        if (deletedRows != 1) {
            log.error "No favorite festival found for user ID ${currentUser?.id} and festival ID ${festival?.id}"
        }
        render status: HttpServletResponse.SC_OK
    }
}
