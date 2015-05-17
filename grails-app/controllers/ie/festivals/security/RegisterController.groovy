package ie.festivals.security

import grails.plugin.simplecaptcha.SimpleCaptchaService
import grails.plugins.springsecurity.SpringSecurityService
import ie.festivals.User
import ie.festivals.UserRegistrationService
import org.codehaus.groovy.grails.commons.GrailsApplication

import static ie.festivals.enums.ConfirmationCodeType.PASSWORD_RESET

class RegisterController {

    static defaultAction = 'newUser'
    static allowedMethods = [register: 'POST']

    SimpleCaptchaService simpleCaptchaService
    SpringSecurityService springSecurityService
    GrailsApplication grailsApplication
    UserRegistrationService userRegistrationService

    def newUser() {
        render view: 'register', model: [user: new User(), captchaValid: true]
    }

    def register() {
        // Previously, the user was declared as an action argument, but for reasons unclear, attempts were being made
        // to access this action with GET /register/register/newUser. This would cause a binding failure as it
        // would attempt to bind 'newUser' to user.id. When we move the binding inside the action, this can't
        // happen because the allowedMethods declaration prevents accessing this URL via HTTP GET. My conclusion is that
        // the binding happens before allowedMethods is checked which seems like a Grails bug.
        User user = new User(params)

        boolean captchaValid = simpleCaptchaService.validateCaptcha(params.captcha)
        boolean termsAccepted = 'terms' in params
        boolean userValid = user.validate()

        // User validation could have failed because there's an unconfirmed user with the same username. If so, delete
        // them and revalidate. If we don't do this, there's no way for a user to re-register if the confirmation email
        // goes missing after the first registration attempt #357
        User unconfirmedUser = User.findByAccountLockedAndUsername(true, user.username)

        if (!userValid && unconfirmedUser) {
            unconfirmedUser.delete(flush: true)
            userValid = user.validate()
        }

        if (!userValid || !captchaValid || !termsAccepted) {

            flashHelper.warn !userValid ? 'register.error' : !captchaValid ? 'register.captchaError' : 'register.termsError'
            return [user: user, captchaValid: captchaValid]
        }

        saveAndSendConfirmationEmail(user)
    }

    private saveAndSendConfirmationEmail(User user) {

        try {
            userRegistrationService.saveUnconfirmedUser(user)
            flashHelper.info 'register.confirm': [user.name, user.username]

        } catch (ex) {
            log.error "failed to register user: $user", ex
            flashHelper.warn 'register.saveAndSendError'
        }

        redirect uri: '/'
    }

    def verifyRegistration(String token) {

        def result = userRegistrationService.confirmUser(token)

        if (result instanceof User) {
            springSecurityService.reauthenticate result.username
            flashHelper.info 'register.confirm.success'

        } else if (!result) {
            log.warn "Registration verification attempted with missing token"
            flashHelper.warn 'register.confirm.fail'

        } else {
            log.warn "Duplicate attempt to activate account with token '$token'"
            flashHelper.warn 'register.confirm.expired'
        }

        redirect uri: '/'
    }

    /**
     * Show the form that (when submitted) will trigger the sending of the password reset email
     */
    def beginPasswordReset() {
        [forgotPassword: new ForgotPasswordCommand()]
    }

    /**
     * Send an email that will allow the user to reset their password
     * @param command
     * @return
     */
    def forgotPassword(ForgotPasswordCommand command) {

        if (!command.validate()) {
            render(view: 'beginPasswordReset', model: [forgotPassword: command])
            return
        }

        String username = command.username
        userRegistrationService.sendPasswordResetEmail(username)

        log.info "Password confirmation email sent to $username"
        flashHelper.info 'forgotPassword.email': username
        redirect uri: '/'
    }

    /**
     * This action has two roles:
     * <ul>
     *   <li>Handles the link contained in the password reset email, i.e. shows the form that allows the user to
     *   choose a new password</li>
     *   <li>Handles the submission of this form</li>
     * </ul>
     */
    def resetPassword(ResetPasswordCommand command) {

        String token = command.token
        ConfirmationCode confirmationCode = ConfirmationCode.findByTokenAndType(token, PASSWORD_RESET)

        if (!confirmationCode) {
            flashHelper.warn 'resetPassword.badCode'
            redirect uri: '/'
            return
        }

        // if the user has clicked on the reset password email link, show them the form that allows them choose a
        // new password
        if (request.get) {
            return [command: new ResetPasswordCommand(token: token)]
        }

        command.username = confirmationCode.username

        if (!command.validate()) {
            flashHelper.warn 'resetPassword.change.error'
            return [command: command]
        }

        userRegistrationService.resetPassword(confirmationCode, command.password)
        springSecurityService.reauthenticate confirmationCode.username
        flashHelper.info 'resetPassword.success'
        redirect uri: '/'
    }

    /**
     * Some social networks (e.g. Twitter) don't provide email addresses, so they must complete a form that
     * provides it. This action handles the submission of this form
     * @param user
     */
    def socialEmailRegistration(User user) {

        userRegistrationService.assignRandomPassword(user)

        if (user.validate()) {
            saveAndSendConfirmationEmail(user)
        } else {
            render view: 'confirmEmail', model: [user: user]
            flashHelper.warn 'default.invalid': 'Email address'
        }
    }
}

class ResetPasswordCommand {
    String username
    String password
    String password2
    String token

    static constraints = {
        password blank: false, validator: { val, self ->

            if (val != self.password2) {
                'user.password.validator.error'
            }
        }
    }
}

class ForgotPasswordCommand {
    String username

    static constraints = {
        username blank: false, email: true, validator: { email ->
            User.countByUsername(email) == 1
        }
    }
}
