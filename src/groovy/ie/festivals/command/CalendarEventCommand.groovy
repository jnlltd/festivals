package ie.festivals.command

import grails.validation.Validateable
import ie.festivals.enums.FestivalType
import ie.festivals.map.MapFocalPoint
import org.grails.databinding.BindingFormat

@Validateable
class CalendarEventCommand {

    @BindingFormat('FestivalType')
    Collection<FestivalType> types
    Date start
    Date end
    MapFocalPoint location
    boolean freeOnly = false

    static constraints = {

        // These constraints must be explicitly stated, I have no idea why
        // http://stackoverflow.com/questions/22771686/grails-default-nullable-constraints#22771686
        start nullable: false
        end nullbale: false
    }
}
