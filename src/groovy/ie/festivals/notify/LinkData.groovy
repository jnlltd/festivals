package ie.festivals.notify

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class LinkData {
    Long id
    String name

    @Override
    String toString() {
        name
    }
}
