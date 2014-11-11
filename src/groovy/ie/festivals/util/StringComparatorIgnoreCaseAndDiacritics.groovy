package ie.festivals.util

import java.text.Collator

/**
 * Compares two strings ignoring differences in case and diacritics, e.g. a is considered the same as A, á, à, etc.
 */
@Singleton
class StringComparatorIgnoreCaseAndDiacritics implements Comparator<String> {

    @Delegate
    private final Collator ukCollator = {
        // can't do this initialization in a constructor because the class is a @Singleton
        Collator collator = Collator.getInstance(Locale.UK)
        collator.strength = Collator.PRIMARY
        collator
    }()
}
