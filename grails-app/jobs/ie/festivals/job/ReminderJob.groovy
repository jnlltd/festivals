package ie.festivals.job

import ie.festivals.notify.NotificationService

class ReminderJob {

    static triggers = {

        // Trigger patterns explained: http://www.quartz-scheduler.org/documentation/quartz-1.x/tutorials/crontrigger
        cron cronExpression: '0 0 4 * * ?'
    }

    NotificationService notificationService
    def concurrent = false

    def execute() {
        log.info "$ReminderJob.simpleName started at ${new Date()}"
        notificationService.sendReminders()
        log.info "$ReminderJob.simpleName completed at ${new Date()}"
    }
}
