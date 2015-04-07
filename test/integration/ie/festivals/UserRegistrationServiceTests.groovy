package ie.festivals

import ie.festivals.enums.ConfirmationCodeType
import ie.festivals.security.ConfirmationCode
import org.hibernate.SessionFactory


class UserRegistrationServiceTests extends GroovyTestCase {

    UserRegistrationService userRegistrationService
    SessionFactory sessionFactory

    void testDeleteUserThatCreatedFestival() {
        User user = User.build()
        Festival festival = Festival.build(createdBy: user)

        userRegistrationService.delete(user.id)
        sessionFactory.currentSession.flush()

        // the delete should not cascade to the festival
        assertNotNull Festival.get(festival.id)
    }

    void testDeleteUserThatCreatedReview() {
        User user = User.build()
        Review review = Review.build(author: user)

        userRegistrationService.delete(user.id)
        sessionFactory.currentSession.flush()

        // the delete should not cascade to the review
        assertNotNull Review.get(review.id)
    }

    void testDeleteAllConfirmationCodes() {
        def user1 = 'user1'
        def user2 = 'user2'

        3.times {
            ConfirmationCode.build(username: user1)
        }
        ConfirmationCode.build(username: user2)
        assertEquals 4, ConfirmationCode.count()

        userRegistrationService.deleteConfirmationCodes(user1, ConfirmationCodeType.values().toList())
        sessionFactory.currentSession.flush()

        assertEquals 1, ConfirmationCode.count()
        assertEquals user2, ConfirmationCode.first().username
    }

    void testDeleteConfirmationCodes() {

        def user = 'user'
        3.times {
            ConfirmationCode.build(username: user, type: ConfirmationCodeType.PASSWORD_RESET)
        }
        ConfirmationCode.build(username: user, type: ConfirmationCodeType.REGISTRATION)

        assertEquals 4, ConfirmationCode.count()
        userRegistrationService.deleteConfirmationCodes(user, [ConfirmationCodeType.PASSWORD_RESET])
        sessionFactory.currentSession.flush()

        assertEquals 0, ConfirmationCode.countByUsernameAndType(user, ConfirmationCodeType.PASSWORD_RESET)
        assertEquals 1, ConfirmationCode.countByUsernameAndType(user, ConfirmationCodeType.REGISTRATION)
    }

    void testDeleteAndExpireConfirmationCodes() {

        def user = 'user'
        3.times {
            ConfirmationCode.build(username: user, type: ConfirmationCodeType.PASSWORD_RESET)
        }

        def expire = ConfirmationCode.first()
        userRegistrationService.deleteConfirmationCodes(user, ConfirmationCodeType.values().toList(), expire)
        sessionFactory.currentSession.flush()

        assertEquals 1, ConfirmationCode.count()
        ConfirmationCode expiredCode = ConfirmationCode.first()
        assertEquals expire.id, expiredCode.id
        assertTrue expiredCode.expired
    }
}
