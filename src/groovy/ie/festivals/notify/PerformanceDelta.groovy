package ie.festivals.notify

import groovy.transform.EqualsAndHashCode

/**
 * Data required to notify a user about a single change (addition or subtraction of an artist) to a
 * festival's lineup
 */
@EqualsAndHashCode(callSuper = true)
class PerformanceDelta extends LinkData {
    Date date
}
