package ie.festivals.enums

import org.apache.commons.lang.WordUtils

public enum Priority {
    HEADLINE, MIDLINE

    @Override
    String toString() {
        WordUtils.capitalizeFully(name()) + 'r'
    }

    String getId() {
        name().toLowerCase()
    }
}