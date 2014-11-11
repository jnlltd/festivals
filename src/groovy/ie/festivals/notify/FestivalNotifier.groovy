package ie.festivals.notify

import groovy.sql.GroovyRowResult
import groovy.util.logging.Slf4j
import ie.festivals.i18n.GroovyMessageSourceResolvable

@Slf4j
class FestivalNotifier extends AbstractNotifier {

    private FestivalNotification festivalNotification = new FestivalNotification()

    @Override
    void sendNotification(EmailSender emailSender) {

        if (festivalNotification?.hasChanges()) {
            def mailModel = [name: recipientName, changes: festivalNotification.festivalChanges]
            def subject = new GroovyMessageSourceResolvable('email.subject.festival')

            // mail sending job will be started after all notification email have been queued
            emailSender.send(email, subject, '/email/festivalNotification', mailModel)
        }
    }

    @Override
    void addEvent(GroovyRowResult data) {
        def event = data.eventName
        String festivalName = data.festivalName
        String artistName = data.artistName

        LinkData festival = new LinkData(id: data.festivalId, name: festivalName)
        PerformanceDelta artist = new PerformanceDelta(id:  data.artistId, name: artistName, date: data.date)

        if (event == 'INSERT') {
            log.debug "Notifying $data.email about addition of $artistName to $festivalName lineup "
            festivalNotification.addArtist(festival, artist)

        } else if (event == 'UPDATE') {
            log.debug "Notifying $data.email about removal of $artistName to $festivalName lineup "
            festivalNotification.removeArtist(festival, artist)
        }

        if (log.debugEnabled) {
            festivalNotification.festivalChanges.each {key, value ->
                log.debug "Artists added to festival $key: $value.netAddedArtists"
                log.debug "Artists removed from festival $key: $value.netDeletedArtists"
            }
        }
    }
}