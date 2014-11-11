package ie.festivals.command

import grails.validation.Validateable
import ie.festivals.enums.FestivalType
import ie.festivals.map.MapFocalPoint
import org.grails.databinding.BindingFormat

@Validateable
class FestivalFilterCommand extends AbstractPaginationCommand {

    MapFocalPoint location = MapFocalPoint.EUROPE

    @BindingFormat('FestivalType')
    Collection<FestivalType> types
    boolean futureOnly = true
    boolean freeOnly = false
}
