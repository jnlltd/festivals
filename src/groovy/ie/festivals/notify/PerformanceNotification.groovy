package ie.festivals.notify

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class PerformanceNotification {

    LinkData artist
    LinkData festival
    Date date
}
