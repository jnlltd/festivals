package ie.festivals


class MailLog {

    String subject
    String recipient
    Date dateCreated

    static constraints = {
        subject nullable: true
        recipient blank: false
    }

    static mapping = {
        version false
    }
}
