package ie.festivals.command

import grails.validation.Validateable

@Validateable
abstract class AbstractPaginationCommand {
    Integer max
    Integer offset = 0

    static constraints = {
        max nullable: true, min: 0
        offset nullable: true, min: 0
    }
}
