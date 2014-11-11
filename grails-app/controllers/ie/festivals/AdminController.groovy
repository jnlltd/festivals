package ie.festivals

import grails.plugins.springsecurity.Secured
import grails.util.GrailsNameUtils
import org.apache.commons.lang.RandomStringUtils
import org.grails.blog.BlogEntry
import org.grails.comments.CommentLink
import org.grails.taggable.Tag
import org.grails.taggable.TagLink

import javax.servlet.http.HttpServletResponse

@Secured(['ROLE_ADMIN'])
class AdminController {

    def executeJob(String className, String infoMsgKey) {
        Class jobClass = Class.forName(className)
        jobClass.triggerNow([:])
        flashHelper.info infoMsgKey
        redirect action: 'index'
    }

    def generateApiKey() {
        User user = User.get(params.id)

        if (user && !user.apiKey) {
            user.apiKey = RandomStringUtils.randomAlphanumeric(32)
            flashHelper.info 'admin.apiKey.success': [user.apiKey, user.username]
        } else {
            flashHelper.warn 'admin.apiKey.fail': params.id
        }
        redirect controller: 'user', action: 'list'
    }

    def index() {

        def countUnapprovedFestivals = Festival.countByApproved(false)
        def unapprovedReviews = Review.countByApproved(false)
        [festivalCount: countUnapprovedFestivals, reviewCount: unapprovedReviews]
    }

    def listUnapprovedReviews() {
        def approved = Review.findAllByApproved(true)
        def unapproved = Review.findAllByApproved(false)
        [approved: approved, unapproved: unapproved]
    }

    def moderateReview() {
        def reviewId = params.long('id')
        Review review = Review.get(reviewId)

        if (!review) {
            flashHelper.warn 'default.not.found.message': ['Review', reviewId]
            redirect(view: 'index')

        } else {
            [review: review]
        }
    }

    def listUnapproved() {
        [festivalInstanceList: Festival.findAllNotApproved()]
    }

    def deleteTag() {
        Tag tag = Tag.get(params.id)

        if (tag) {
            Tag.withTransaction {
                
                TagLink.findAllByTagAndType(tag, 'blogEntry').each { 
                    it.delete() 
                }
                tag.delete()
            }
        } else {
            flashHelper.warn 'default.not.found.message': ['Tag', params.id]
        }
        render status: HttpServletResponse.SC_OK
    }

    def moderateBlog() {

        def blogEntries = getEntitiesWithComments(BlogEntry)
        def tags = Tag.listOrderByName()

        [entries: blogEntries, tags: tags]
    }

    private List<Long> getEntitiesWithComments(Class commentableDomainClass) {

        String domainClassType = GrailsNameUtils.getPropertyName(commentableDomainClass)

        def domainClassIds = CommentLink.createCriteria().listDistinct {
            eq('type', domainClassType)
            projections {
                property('commentRef')
            }
            cache true
        }

        if (domainClassIds) {
            commentableDomainClass.withCriteria {
                'in'('id', domainClassIds)
            }
        } else {
            Collections.emptyList()
        }
    }
}
