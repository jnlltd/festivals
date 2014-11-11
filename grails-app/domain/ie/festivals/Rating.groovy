package ie.festivals

import grails.util.Holders
import ie.festivals.enums.RatingType

class Rating {

    RatingType type
    Integer score
    static belongsTo = [festival: Festival, user: User]

    static constraints = {
        // compound unique index
        festival unique: ['user', 'type']
    }

    static mapping = {
        type length: Holders.config.festival.utf8mb4MaxLength
    }
}
