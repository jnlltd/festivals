package ie.festivals

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ie.festivals.dto.UserFestivalCount
import ie.festivals.enums.ConfirmationCodeType
import ie.festivals.i18n.GroovyMessageSourceResolvable
import ie.festivals.notify.EmailSender
import ie.festivals.security.ConfirmationCode
import org.apache.commons.lang.RandomStringUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import grails.transaction.Transactional
import org.pac4j.core.profile.CommonProfile

import static ie.festivals.enums.ConfirmationCodeType.PASSWORD_RESET
import static ie.festivals.enums.ConfirmationCodeType.REGISTRATION

@Transactional(rollbackFor = Throwable)
class UserRegistrationService extends AbstractJdbcService {

    EmailSender emailSender
    LinkGenerator grailsLinkGenerator
    GrailsApplication grailsApplication

    private static final int RANDOM_PASSWORD_LENGTH = 8

    List<User> findAllByRole(String roleName) {
        // we have to use HQL here in order to avoid N+1 queries: http://stackoverflow.com/questions/16530290/grails-n1-query
        List<UserRole> usersByRole = UserRole.executeQuery('''
                from UserRole ur
                left join fetch ur.user
                left join fetch ur.role
                where ur.role.authority = ?''', [roleName])

        usersByRole.user
    }

    List<GroovyRowResult> getUsersByFestivalsCreated() {
        List<GroovyRowResult> results = []

        def query = """\
                SELECT      u.username email,
                            u.name name,
                            u.id id,
                            u.date_created dateCreated,
                            u.api_key apiKey,
                            r.authority role,
                            count(f.created_by_id) festivalCount
                FROM        user u
                INNER JOIN  user_role ur ON u.id = ur.user_id
                INNER JOIN  role r ON ur.role_id = r.id
                LEFT JOIN   festival f ON u.id = f.created_by_id
                WHERE       u.account_locked = false
                GROUP BY    email, name, id, dateCreated, role
                ORDER BY    festivalCount DESC"""

        doJdbcWork {Sql sql ->
            sql.eachRow(query) {
                results << new UserFestivalCount(
                        email: it.email,
                        name: it.name,
                        id: it.id,
                        dateCreated: it.dateCreated,
                        role: it.role,
                        festivalCount: it.festivalCount,
                        apiKey: it.apiKey)
            }
        }

        results
    }

    /**
     * Save a user and send a confirmation email
     * @param user a validated user
     */
    void saveUnconfirmedUser(User user) {
        // account should be locked if unconfirmed
        user.accountLocked = true
        user = user.save(validate: false)

        def registrationCode = new ConfirmationCode(username: user.username, type: REGISTRATION).save(failOnError: true)
        String url = generateLink('verifyRegistration', [token: registrationCode.token])

        def mailModel = [name: user.name, url: url]
        def recipientEmail = user.username
        def subject = new GroovyMessageSourceResolvable('email.subject.register')

        // If the mail sending fails, don't save the user data. Otherwise the user won't receive the
        // confirmation email and won't be able to try registering with this email address again
        emailSender.send(recipientEmail, subject, '/email/register', mailModel, true)
        log.info "Registration email sent to $recipientEmail"
    }

    private String generateLink(String actionName, Map params) {
        grailsLinkGenerator.link(controller: 'register', action: actionName, params: params, absolute: true)
    }

    /**
     * Confirm a user's account
     * @param token registration token
     * @return the User if confirmation was successful or the registration code if confirmation failed
     */
    def confirmUser(String token) {

        ConfirmationCode registrationCode = ConfirmationCode.findByTokenAndType(token, REGISTRATION)

        if (registrationCode && !registrationCode.expired) {
            User user = User.findByUsername(registrationCode.username)

            if (user) {
                user.accountLocked = false
                user.save(flush: true, failOnError: true)

                // use a criteria instead of a dynamic finder or result won't be cached: http://jira.grails.org/browse/GRAILS-10119
                Role userRole = Role.createCriteria().get {
                    eq 'authority', 'ROLE_USER'
                    cache true
                }
                UserRole.create user, userRole
                deleteConfirmationCodes(registrationCode.username, [REGISTRATION], registrationCode)
                return user
            }
        }
        registrationCode
    }

    /**
     * Send an email that will allow a user to reset their password
     * @param username validated username/email address
     */
    void sendPasswordResetEmail(String username) {
        User user = User.findByUsername(username)
        def confirmationCode = new ConfirmationCode(username: user.username, type: PASSWORD_RESET).save(failOnError: true)

        String url = generateLink('resetPassword', [token: confirmationCode.token])

        def mailModel = [name: user.name, url: url]
        def subject = new GroovyMessageSourceResolvable('email.subject.passwordReset')
        emailSender.send(user.username, subject, '/email/passwordReset', mailModel, true)
    }

    void resetPassword(ConfirmationCode confirmationCode, String newPassword) {
        def user = User.findByUsername(confirmationCode.username)
        user.password = newPassword
        user.save(failOnError: true)
        deleteConfirmationCodes(confirmationCode.username, [PASSWORD_RESET])
    }

    /**
     * Delete confirmation codes
     * @param username owner of tokens
     * @param types type of tokens
     * @param expire this code will be marked as expired rather than deleted
     */
    void deleteConfirmationCodes(String username, Collection<ConfirmationCodeType> types, ConfirmationCode expire = null) {

        Long expireTokenId = expire?.id

        def deletableCodes = ConfirmationCode.withCriteria {

            eq('username', username)
            'in'('type', types)

            if (expireTokenId) {
                ne('id', expireTokenId)
            }
        }

        deletableCodes.each { it.delete() }
        log.debug "Deleted ${deletableCodes.size()} confirmation codes for user '$username'"

        expire?.expired = true
    }

    /**
     * Register or login a user using a social service (Twitter, Facebook, etc.)
     * @param socialUserDetails information about the user provided to us by the social network
     * @param provider name of the OAuth2 provider
     * @return a confirmed user
     */
    User socialSignIn(CommonProfile socialUserDetails, String provider) {

        // We determine if a social user is already registered by querying the DB for
        // 1. A user from this provider with the same CommonProfile.id
        // 2. A user from this provider with the same CommonProfile.username. This is a legacy (and unreliable)
        // way of uniquely identifying users within a particular provider
        // 3. A user with the same email. This should be our last resort because some OAuth providers (e.g. Twitter & Yahoo)
        // don't include an email in the response
        User user = User.findBySocialIdAndSocialLoginProvider(socialUserDetails.id, provider) ?:
                User.findBySocialIdAndSocialLoginProvider(socialUserDetails.username, provider) ?:
                User.findByUsername(socialUserDetails.email)

        if (user) {
            log.info "Callback from $provider found existing user with ID $user.id, social ID $socialUserDetails.id, " +
                    "social username $socialUserDetails.username, social email $socialUserDetails.email"
            // update legacy social users to use the new ID (see above)
            user.socialId = socialUserDetails.id ?: user.socialId
            user.save(failOnError: true)

        } else {
            // save a new user
            user = new User(
                    name: socialUserDetails.displayName,
                    username: socialUserDetails.email,
                    socialLoginProvider: provider,
                    socialId: socialUserDetails.id ?: socialUserDetails.username)

            // don't try to save the user if they registered with Yahoo or Twitter because their username/email will be null
            if (socialUserDetails.email) {
                assignRandomPassword(user).save(failOnError: true)

                log.info "Saved a new user in callback from $provider with social ID $socialUserDetails.id, social username " +
                        "$socialUserDetails.username, social email $socialUserDetails.email"

                String userRoleName = grailsApplication.config.festival.userRoleName
                Role userRole = Role.findByAuthority(userRoleName)
                UserRole.create user, userRole
            }
        }
        user
    }

    User assignRandomPassword(User user) {
        // We could email this password to them and they can use this thereafter to login directly
        user.password = user.passwordConfirm = RandomStringUtils.random(RANDOM_PASSWORD_LENGTH, true, true)
        user
    }

    User delete(Long userId) {
        User user = User.get(userId)

        if (user) {

            // deletion of users should not cascade to festivals or reviews so we need to remove the association ourselves
            Festival.findAllByCreatedBy(user).each {
                it.createdBy = null
            }

            Review.findAllByAuthor(user).each {
                it.author = null
            }

            UserRole.removeAll(user)
            user.delete()
            user
        }
    }
}
