package ie.festivals.databinding

import groovy.util.logging.Slf4j
import ie.festivals.enums.FestivalType
import org.grails.databinding.converters.FormattedValueConverter

/**
 * Converts a String such as <code>HEADLINE,MUSIC,SPORT</code> to the corresponding collection of enum constants
 */
@Slf4j
class EnumCollectionConverter implements FormattedValueConverter {

    /**
     * To make other enum classes convertible, just add them to this list
     */
    private static final List<Class<Enum>> CONVERTIBLE_ENUMS = [FestivalType]

    @Override
    Object convert(Object value, String format) {

        Class<Enum> targetEnumClass = CONVERTIBLE_ENUMS.find { it.simpleName == format }

        if (targetEnumClass) {

            // Sometimes the enum params are bound as a String[] (e.g. festival list page) and on other occasions they're
            // bound as a List<String>
            List<String> enumNames = value?.class?.isArray() ? value as List :  value?.toString()?.tokenize(',')
            Set<Enum> results = []

            enumNames.each { String enumName ->
                enumName = enumName?.trim()?.toUpperCase()
                if (enumName) {

                    // Because of FestivalType.RENAMED_CONSTANTS we can't check if the name is in the list of valid
                    // enum names without putting FestivalType specific code in here. Catching ths exception seems
                    // like the lesser of these 2 evils
                    try {
                        results << targetEnumClass.valueOf(enumName)

                    } catch (IllegalArgumentException ex) {
                        log.warn "Unrecognised constant name $enumName for enum $targetEnumClass"
                    }
                }
            }
            return results

        }
        value
    }

    @Override
    Class<?> getTargetType() {

        // this converts to a Collection<T extends Enum<T>> but the return value of the method can't be any more specific
        // than Collection due to type erasure
        Collection
    }
}
