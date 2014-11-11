package ie.festivals.tag

/**
 * A fork of the CountryTagLib with the same name provided by Grails. I forked this so that I
 * could customise the list of countries, c.f. #85
 */
class EuropeTagLib {
    static namespace = "country"

    static final ISO3166_3 = [
            "alb": "Albania",
            "and": "Andorra",
            "aze": "Azerbaijan",
            "aut": "Austria",
            "arm": "Armenia",
            "bel": "Belgium",
            "bih": "Bosnia and Herzegovina",
            "bgr": "Bulgaria",
            "blr": "Belarus",
            "hrv": "Croatia",
            "cyp": "Cyprus",
            "cze": "Czech Republic",
            "dnk": "Denmark",
            "est": "Estonia",
            "fro": "Faroe Islands",
            "fin": "Finland",
            "fra": "France",
            "geo": "Georgia",
            "deu": "Germany",
            "gib": "Gibraltar",
            "grc": "Greece",
            "vat": "Vatican City State",
            "hun": "Hungary",
            "isl": "Iceland",
            "irl": "Ireland",
            "ita": "Italy",
            "kaz": "Kazakhstan",
            "lva": "Latvia",
            "lie": "Liechtenstein",
            "ltu": "Lithuania",
            "lux": "Luxembourg",
            "mlt": "Malta",
            "mco": "Monaco",
            "mda": "Republic of Moldova",
            "nld": "Netherlands",
            "nor": "Norway",
            "pol": "Poland",
            "prt": "Portugal",
            "rou": "Romania",
            "rus": "Russian Federation",
            "smr": "San Marino",
            "svk": "Slovakia",
            "svn": "Slovenia",
            "esp": "Spain",
            "swe": "Sweden",
            "che": "Switzerland",
            "tur": "Turkey",
            "ukr": "Ukraine",
            "mkd": "The Former Yugoslav Republic of Macedonia",
            "gbr": "United Kingdom",
            "imn": "Isle of Man",
            "srb": "Serbia",
            "mne": "Montenegro"
    ]

    // This needs to change, to sort on demand using the BROWSER's locale
    static final COUNTRY_CODES_BY_NAME_ORDER =
        ISO3166_3.entrySet().sort { a, b -> a.value.compareTo(b.value) }.collect() { it.key }
    static final COUNTRY_CODES_BY_NAME = new TreeMap()

    static {
        ISO3166_3.each { k, v ->
            COUNTRY_CODES_BY_NAME[v] = k
        }
    }

    /**
     * Display a country selection combo box.
     *
     * @emptyTag
     *
     * @attr from list of country codes or none for full list. Order is honoured
     * @attr valueMessagePrefix code prefix to use, if you want names of countries to come from message bundle
     * @attr value currently selected country code - ISO3166_3 (3 character, lowercase) form
     * @attr default currently selected country code - if value is null
     * @attr name the select name
     * @attr id the DOM element id - uses the name attribute if not specified
     * @attr keys A list of values to be used for the value attribute of each "option" element.
     * @attr optionKey By default value attribute of each <option> element will be the result of a "toString()" call on each element. Setting this allows the value to be a bean property of each element in the list.
     * @attr optionValue By default the body of each &lt;option&gt; element will be the result of a "toString()" call on each element in the "from" attribute list. Setting this allows the value to be a bean property of each element in the list.
     * @attr multiple boolean value indicating whether the select a multi-select (automatically true if the value is a collection, defaults to false - single-select)
     * @attr noSelection A single-entry map detailing the key and value to use for the "no selection made" choice in the select box. If there is no current selection this will be shown as it is first in the list, and if submitted with this selected, the key that you provide will be submitted. Typically this will be blank - but you can also use 'null' in the case that you're passing the ID of an object
     * @attr disabled boolean value indicating whether the select is disabled or enabled (defaults to false - enabled)
     */
    Closure countrySelect = { attrs ->
        if (!attrs.from) {
            attrs.from = COUNTRY_CODES_BY_NAME_ORDER
        }

        if (!attrs['valueMessagePrefix']) attrs.optionValue = { ISO3166_3[it] }

        if (!attrs.value) {
            attrs.value = attrs.remove('default')
        }
        out << select(attrs)
    }

    /**
     * Take a country code and output the country name, from the internal data.<br/>
     * Note: to use message bundle to resolve name, use g:message tag
     *
     * @emptyTag
     *
     * @attr code REQUIRED the ISO3166_3 country code
     */
    Closure country = { attrs ->
        if (!attrs.code) {
            throwTagError "[country] requires [code] attribute to specify the country code"
        }
        out << ISO3166_3[attrs.code]
    }
}
