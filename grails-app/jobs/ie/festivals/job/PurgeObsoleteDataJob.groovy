package ie.festivals.job

import ie.festivals.Festival
import ie.festivals.User
import ie.festivals.UserRegistrationService
import ie.festivals.enums.ConfirmationCodeType
import org.springframework.transaction.TransactionStatus

class PurgeObsoleteDataJob {

    static triggers = {
        // execute at 2AM every monday
        cron cronExpression: '0 0 2 ? * MON'
    }

    private static final CONFIRMATION_DAYS = 7
    def concurrent = false

    UserRegistrationService userRegistrationService

    def execute() {
        log.info "$PurgeObsoleteDataJob.simpleName started at ${new Date()}"

        def deletedUserCount = deleteUnconfirmedUsers()
        log.info "$deletedUserCount unconfirmed user(s) were removed"

        def deletedFestivalCount = deleteUnapprovedExpiredFestivals()
        log.info "$deletedFestivalCount festival(s) were removed"

        log.info "$PurgeObsoleteDataJob.simpleName finished at ${new Date()}"
    }

    /**
     * Delete unapproved festivals that are over
     * @return the number of deleted festivals
     */
    private Integer deleteUnapprovedExpiredFestivals() {
        Date today = new Date().clearTime()

        List<Festival> obsoleteFestivals = Festival.findAllByEndLessThanAndApproved(today, false)
        obsoleteFestivals.each { it.delete() }
        obsoleteFestivals.size()
    }

    /**
     * Delete users that have failed to confirm their account within a certain period after registration
     * @return the number of deleted users
     */
    private Integer deleteUnconfirmedUsers() {
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

        deletedUserCount
    }
}
