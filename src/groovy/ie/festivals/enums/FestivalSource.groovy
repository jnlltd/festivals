package ie.festivals.enums

import org.apache.commons.lang.WordUtils

public enum FestivalSource {

    /**
     * Entered by a human
     */
    HUMAN,

    /**
     * Imported from the Skiddle feed
     */
    SKIDDLE,

    /**
     * Retrieved from Eventbrite's API
     */
    EVENTBRITE

    @Override
    String toString() {
        WordUtils.capitalizeFully(name())
    }
}