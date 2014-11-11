package ie.festivals.notify

import grails.plugins.springsecurity.SpringSecurityService
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import ie.festivals.*
import ie.festivals.i18n.GroovyMessageSourceResolvable
import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent
import org.springframework.context.MessageSource
import grails.transaction.Transactional

@Transactional(rollbackFor = Throwable)
class NotificationService extends AbstractJdbcService {

    SpringSecurityService springSecurityService
    EmailSender emailSender
    MessageSource messageSource

    @Lazy
    private String dateFormat = messageSource.getMessage('default.date.format', null, null)

    private List<GroovyRowResult> getSubscriptions(NotificationType notificationType, Sql sql, lastAuditLogId) {

        String joinType = notificationType.name().toLowerCase()

        String query = """
                SELECT au.id, au.event_name eventName, p.date, a.name artistName, a.id artistId, u.username email, u.name, f.name festivalName, f.id festivalId
                FROM audit_log au
                INNER JOIN performance p ON au.persisted_object_id = p.id
                INNER JOIN artist a ON p.artist_id = a.id
                INNER JOIN ${joinType}_subscription sub ON sub.${joinType}_id = p.${joinType}_id
                INNER JOIN user u ON sub.user_id = u.id
                INNER JOIN festival f ON p.festival_id = f.id
                WHERE au.class_name = '$Performance.name'
                AND (au.event_name = 'INSERT' OR (au.event_name = 'UPDATE' AND au.property_name = 'deleted'))
                ${lastAuditLogId != null ? 'AND au.id <= ?' : ''}
                ORDER BY username"""

        if (log.debugEnabled) {
            log.debug "Notification query: $query"
        }
        sql.rows(query, [lastAuditLogId])
    }

    void sendReminders() {

        String query = '''
            SELECT f.name festivalName, f.id festivalId, f.start start, r.days_ahead daysAhead, u.name name, u.username email, u.id userId
            FROM festival f
            INNER JOIN reminder r ON f.id = r.festival_id
            INNER JOIN user u ON r.user_id = u.id
            WHERE DATEDIFF(f.start, ?) = r.days_ahead'''

        doJdbcWork { Sql sql ->
            sql.eachRow(query, [new Date()]) {

                // if an error occurs sending the Nth notification we don't want to rollback because we'd re-send
                // notifications 1..N-1, so don't let any exceptions escape
                try {
                    def mailModel = [
                            name: it.name,
                            festivalName: it.festivalName,
                            daysAhead: it.daysAhead,
                            start: it.start,
                            festivalId: it.festivalId]

                    def startDate = it.start.format(dateFormat)
                    def subjectArgs = [it.festivalName, startDate]
                    def subject = new GroovyMessageSourceResolvable('email.subject.festivalReminder', subjectArgs)

                    emailSender.send(it.email, subject, '/email/festivalReminder', mailModel)

                    def userId = it.userId
                    def festivalId = it.festivalId

                    Reminder.executeUpdate(
                            "delete $Reminder.name r where r.user.id = ? and r.festival.id = ?", [userId, festivalId])
                    log.info "Reminder about festival $it.festivalName sent to $it.email"

                } catch (ex) {
                    log.error "Error sending reminder about festival $it.festivalName sent to $it.email", ex
                }
            }
        }
    }

    void sendFestivalApprovedNotification(User festivalAuthor, Festival festival) {

        def mailModel = [
                name: festivalAuthor.name,
                festivalName: festival.name,
                festivalId: festival.id
        ]

        def subject = new GroovyMessageSourceResolvable('email.subject.festivalApproved', [festival.name])
        emailSender.send(festivalAuthor.username, subject, '/email/festivalApproved', mailModel)
    }

    @Transactional(readOnly = true)
    List<Festival> getSubscribedFestivals(User user = springSecurityService.currentUser) {
        Festival.withCriteria {
            subscriptions {
                eq('user', user)
            }
            order("name")
        }
    }

    void sendNotifications() {

        doJdbcWork { Sql sql ->
            GroovyRowResult maxIds = sql.firstRow(
                    'select max(id) as maxAuditId, max(persisted_object_id) as lastPerformanceId from audit_log')

            def lastAuditLogId = maxIds.maxAuditId
            def lastPerformanceId = maxIds.lastPerformanceId?.toLong()

            if (lastAuditLogId) {

                List<GroovyRowResult> queryResults = getSubscriptions(NotificationType.ARTIST, sql, lastAuditLogId)
                emailNotifications(queryResults, ArtistNotifier)

                queryResults = getSubscriptions(NotificationType.FESTIVAL, sql, lastAuditLogId)
                emailNotifications(queryResults, FestivalNotifier)

                // Delete pending notifications and performances marked as deleted
                Performance.executeUpdate("delete $Performance.name p where p.deleted = true and p.id <= ?", [lastPerformanceId])
                AuditLogEvent.executeUpdate("delete $AuditLogEvent.name a where a.id <= ?", [lastAuditLogId])
            }
        }
    }

    private emailNotifications(List<GroovyRowResult> queryResults, Class notifierClass) {

        log.debug "Notifications query returned ${queryResults.size()} rows"
        AbstractNotifier notifier
        def lastNotification = queryResults.size() - 1

        queryResults.eachWithIndex { GroovyRowResult row, index ->

            if (row.email != notifier?.email) {
                // Send notifications for previous user (if any) and create a notifier for current user
                sendNotificationEmail(notifier)
                notifier = notifierClass.newInstance([email: row.email, recipientName: row.name])
            }

            notifier.addEvent(row)

            if (index == lastNotification) {
                sendNotificationEmail(notifier)
            }
        }
        log.debug "Completed sending $notifierClass.simpleName emails"
    }

    private sendNotificationEmail(AbstractNotifier notifier) {
        if (notifier) {
            log.info "Sending notifications to: $notifier.email"
            notifier.sendNotification(emailSender)
        }
    }

    static enum NotificationType {
        ARTIST, FESTIVAL
    }
}

