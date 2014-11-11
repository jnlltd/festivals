package ie.festivals.enums

import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j
import org.apache.commons.lang.WordUtils

@Slf4j
enum FestivalType {

    HEADLINE,
    MUSIC,
    COMEDY,
    SPORT,
    ARTS,
    FILM,
    FOOD_AND_DRINK {
        @Override
        String toString() {
            "Food and Drink"
        }
    },
    OTHER

    static final Map<String, FestivalType> RENAMED_CONSTANTS = [
            BIG_MUSIC: HEADLINE,
            OTHER_MUSIC: MUSIC,
            ALTERNATIVE: OTHER
    ].asImmutable()

    static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {

        try {
            // sometimes the type name is passed in the URL in lower case, e.g. types=music
            name = name?.toUpperCase()
            Enum.valueOf(enumType, name)

        } catch (IllegalArgumentException ex) {
            // A long time ago the constants BIG_MUSIC, OTHER_MUSIC and ALTERNATIVE were renamed to HEADLINE, MUSIC and OTHER.
            // As of October 2013 the old names are still occasionally passed as parameters to the
            // FestivalController.map action I can't find any reference to these old names in the code or the DB.
            // Possibly they are coming from (out-of-date) external links or old cached pages. This is a workaround
            // for this issue
            if (name in RENAMED_CONSTANTS) {
                log.warn "Obsolete $enumType.name constant name '$name'", ex
                return RENAMED_CONSTANTS[name]
            }
            throw ex
        }
    }

    // http://stackoverflow.com/a/9128656/2648
    private static class Holder {
        static Set<String> allSearchTokens = []
        static String NAME_SEPARATOR = '_'
        static Map<String, FestivalType> typesById = [:]
    }

    FestivalType() {
        String searchToken = name().split(Holder.NAME_SEPARATOR)[0].toLowerCase()

        if (searchToken in Holder.allSearchTokens) {
            throw new FestivalTypeSearchTokenException("Duplicate search token '$searchToken', rename constant '${this.name()}'")
        }
        this.searchToken = searchToken
        Holder.allSearchTokens << searchToken
        Holder.typesById[this.id] = this
    }

    final String searchToken

    /**
     * Indicates whether a type of festival has a lineup by default, this may be overridden on the <tt>Festival</tt> instance
     */
    boolean hasLineup() {
        this in EnumSet.of(HEADLINE, MUSIC, COMEDY)
    }

    String getId() {
        name().replaceAll(Holder.NAME_SEPARATOR, '').toLowerCase()
    }

    static FestivalType getById(String id) {
        Holder.typesById[id]
    }

    String toString() {
        WordUtils.capitalizeFully(name().replaceAll(Holder.NAME_SEPARATOR, ' '))
    }
}

@InheritConstructors
class FestivalTypeSearchTokenException extends RuntimeException {

}