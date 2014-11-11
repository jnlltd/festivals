package ie.festivals

import grails.plugins.springsecurity.Secured

class ReviewController extends AbstractController {

    def springSecurityService

    @Secured(['ROLE_USER', 'ROLE_ADMIN'])
    def submit(Review review) {

        review.author = springSecurityService.currentUser
        review.approved = isAdmin()

        if (review.save()) {
            flashHelper.info review.approved ? 'review.success.admin' : 'review.success'

        } else {
            flashHelper.warn 'review.invalid'
            flash.invalidReview = review
        }
        redirect controller: 'festival', action: 'show', id: review.festival.id
    }

    @Secured(['ROLE_ADMIN'])
    def update() {
        Review review = getSafelyById(Review)
        def reviewCompleteAction = [controller:  'admin', action: 'listUnapprovedReviews']
        def failModelAndView = [view: '/admin/moderateReview', model: [review: review]]

        if (!review) {
            flashHelper.warn 'default.not.found.message': ['Review', params.id]
            redirect reviewCompleteAction

        } else if (isStale(review)) {
            render failModelAndView

        } else {
            review.properties = params

            if (!review.save()) {
                flashHelper.warn 'default.invalid': 'Review'
                render failModelAndView
                return
            }
            redirect reviewCompleteAction
        }
    }

    @Secured(['ROLE_ADMIN'])
    def delete() {

        def review = getSafelyById(Review)

        if (review) {
            review.delete()
            flashHelper.info 'review.delete.success'

        } else {
            flashHelper.warn 'default.not.found.message': ['review', params.id]
        }
        redirect controller: 'admin', action: 'listUnapprovedReviews'
    }
}
