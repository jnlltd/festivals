package ie.festivals.command

import grails.validation.Validateable
import ie.festivals.enums.FestivalType
import org.grails.databinding.BindingFormat

@Validateable
class FestivalGeoSearchCommand extends AbstractPaginationCommand {
    Float latitude
    Float longitude
    Integer radius

    @BindingFormat('FestivalType')
    Collection<FestivalType> types = FestivalType.values()

    /**
     * Prevent a falsey value from being bound to this
     * @param festivalTypes
     */
    void setTypes(Collection<FestivalType> festivalTypes) {
        this.types = festivalTypes ?: FestivalType.values()
    }

    static constraints = {
        latitude range: -90..90
        longitude range: -180..180
        radius range: 1..1000
        types minSize: 1
    }
}
