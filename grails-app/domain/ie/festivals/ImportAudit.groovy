package ie.festivals

import grails.util.Holders
import ie.festivals.enums.FestivalSource

class ImportAudit {

    Long eventId
    FestivalSource source

    static constraints = {
        eventId unique: 'source'
    }

    static mapping = {
        source length: Holders.config.festival.utf8mb4MaxLength
    }
}
