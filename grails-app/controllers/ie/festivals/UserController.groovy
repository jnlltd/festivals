package ie.festivals

import grails.converters.JSON
import grails.plugins.springsecurity.Secured

@Secured(['ROLE_ADMIN'])
class UserController {

    UserRegistrationService userRegistrationService

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        [allUsers: userRegistrationService.usersByFestivalsCreated]
    }

    def registrationTimeline() {
        Integer countUsersWithoutRegDate = User.countByAccountLockedAndDateCreatedIsNull(false)
        List<User> usersWithRegDate = User.findAllByAccountLockedAndDateCreatedIsNotNull(false,
                [sort: "dateCreated", order: "asc"])

        if (usersWithRegDate) {

            // group together users that registered on the same day
            Map<Date, List<User>> usersGroupedByRegDate = usersWithRegDate.groupBy {User user ->
                user.discard()
                user.dateCreated.clearTime()
            }

            // example of JSON structure expected by Highcharts
            // http://jsfiddle.net/gh/get/jquery/1.9.1/highslide-software/highcharts.com/tree/master/samples/highcharts/demo/spline-irregular-time/
            List<List> chartDataSeries = []

            if (countUsersWithoutRegDate) {
                // users may have registered before User.dateCreated was added, so we'll show them all as having registered on
                // the day before the user with the earliest registration date
                Date dayBeforeFirstRegistrationDate = usersWithRegDate[0].dateCreated - 1
                chartDataSeries << [dayBeforeFirstRegistrationDate.time, countUsersWithoutRegDate]
            }

            Integer totalUsers = countUsersWithoutRegDate
            usersGroupedByRegDate.each {Date regDate, List<User> users ->
                chartDataSeries << [regDate.time, totalUsers += users.size()]
            }

            [dataSeries: chartDataSeries as JSON, total: totalUsers, yAxisMin: countUsersWithoutRegDate]
        } else {
            flashHelper.warn 'admin.userTimeline.fail'
            redirect controller: 'admin'
        }
    }

    def delete(Long id) {
        User user = userRegistrationService.delete(id)

        if (user) {
            flashHelper.info 'user.deleted': user.username
        } else {
            flashHelper.warn 'default.not.found.message': ['User', id]
        }
        redirect action: 'list'
    }
}
