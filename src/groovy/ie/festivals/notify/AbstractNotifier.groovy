package ie.festivals.notify

import groovy.sql.GroovyRowResult

abstract class AbstractNotifier {

    String email
    String recipientName

    abstract void sendNotification(EmailSender emailSender)
    abstract void addEvent(GroovyRowResult data)
}
