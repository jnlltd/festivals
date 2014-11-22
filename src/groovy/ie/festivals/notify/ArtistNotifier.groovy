package ie.festivals.notify

import groovy.sql.GroovyRowResult
import ie.festivals.i18n.GroovyMessageSourceResolvable

class ArtistNotifier extends AbstractNotifier {

    private ArtistNotification artistNotification = new ArtistNotification()

    @Override
    void sendNotification(EmailSender emailSender) {

        Set<PerformanceNotification> netAddedPerformances = artistNotification?.netAddedPerformances
        Set<PerformanceNotification> netDeletedPerformances = artistNotification?.netDeletedPerformances

        if (netAddedPerformances || netDeletedPerformances) {
            def mailModel = [name: recipientName, added: netAddedPerformances, deleted: netDeletedPerformances]
            def subject = new GroovyMessageSourceResolvable('email.subject.artist')

            // mail sending job will be started after all notification email have been queued
            emailSender.send(email, subject, '/email/artistNotification', mailModel)
        }
    }

    @Override
    void addEvent(GroovyRowResult data) {
        def event = data.eventName
        PerformanceNotification notificationEntry = new PerformanceNotification(
                artist: new LinkData(id: data.artistId, name: data.artistName),
                festival: new LinkData(id: data.festivalId, name: data.festivalName),
                date: data.date
        )

        if (event == 'INSERT') {
            log.debug "Artist $notificationEntry.artist added to festival $notificationEntry.festival"
            artistNotification.addedPerformances << notificationEntry

        } else if (event == 'UPDATE') {
            log.debug "Artist $notificationEntry.artist removed from festival $notificationEntry.festival"
            artistNotification.deletedPerformances << notificationEntry
        }
    }
}
