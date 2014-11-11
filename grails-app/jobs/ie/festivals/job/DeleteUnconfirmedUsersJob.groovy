package ie.festivals.job

import ie.festivals.User
import ie.festivals.UserRegistrationService
import ie.festivals.enums.ConfirmationCodeType
import org.springframework.transaction.TransactionStatus

class DeleteUnconfirmedUsersJob {

    static triggers = {
        // execute at 2AM every monday
        cron cronExpression: '0 0 2 ? * MON'
    }

    private static final CONFIRMATION_DAYS = 7
    def concurrent = false

    UserRegistrationService userRegistrationService

    def execute() {
        log.info "$DeleteUnconfirmedUsersJob.simpleName started at ${new Date()}"
        def deletedUserCount = 0

        List<ConfirmationCodeType> allCodeTypes = ConfirmationCodeType.values().toList()
        Date registrationCutOff = new Date() - CONFIRMATION_DAYS

        User.findAllByAccountLockedAndDateCreatedLessThan(true, registrationCutOff).each { User user ->

            log.info "Deleting user '$user.username' that registered on $user.dateCreated"

            User.withTransaction { TransactionStatus status ->

                try {
                    userRegistrationService.deleteConfirmationCodes(user.username, allCodeTypes)
                    user.delete()
                    deletedUserCount++
                } catch (ex) {
                    status.setRollbackOnly()
                    log.error "Failed to delete unconfirmed user '$user.username'", ex
                }
            }
        }

        log.info "$DeleteUnconfirmedUsersJob.simpleName finished at ${new Date()}. A total of $deletedUserCount user(s) were removed"
    }
}
