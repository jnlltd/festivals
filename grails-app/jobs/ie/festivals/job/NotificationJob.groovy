package ie.festivals.job

import ie.festivals.notify.NotificationService

class NotificationJob {

    static triggers = {

        // Trigger patterns explained: http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
        // Fire at 5AM
        cron cronExpression: '0 0 5 * * ?'
    }

    NotificationService notificationService
    def concurrent = false

    def execute() {
        log.info "$NotificationJob.simpleName started at ${new Date()}"
        notificationService.sendNotifications()
        log.info "$NotificationJob.simpleName completed at ${new Date()}"
    }
}
