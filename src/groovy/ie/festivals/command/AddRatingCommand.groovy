package ie.festivals.command

import grails.validation.Validateable
import ie.festivals.enums.RatingType

@Validateable
class AddRatingCommand {
    Integer festivalId
    RatingType type
    Integer score
}
