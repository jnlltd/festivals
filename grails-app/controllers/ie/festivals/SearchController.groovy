package ie.festivals

import grails.converters.JSON
import grails.plugin.searchable.internal.lucene.LuceneUtils

class SearchController {

    SearchService searchService

    /**
     * A list of artists and/or festivals matching a query string
     * @param query
     * @return search suggestions for the <a href="http://www.devbridge.com/projects/autocomplete/jquery/">autocomplete</a>
     * in the main search box
     */
    def suggest(String query) {

        def searchResults = searchService.domainSearch(query, params.searchableClasses)

        def resultsMap = [
                query: query,
                suggestions: []
        ]

        if (searchResults) {
            // sort lexicographically
            searchResults.results.sort { Named suggestion1, Named suggestion2 ->
                suggestion1.name.compareToIgnoreCase(suggestion2.name)
            }

            searchResults.results.each { suggestion ->

                if (suggestion instanceof Artist) {
                    def urlParams = [id: suggestion.id, name: suggestion.name.encodeAsSeoName()]

                    def result = [
                            value: suggestion.name,
                            data : [url: g.createLink(controller: 'artist', action: 'show', params: urlParams)]
                    ]
                    resultsMap.suggestions << result

                } else if (suggestion instanceof Festival) {

                    // the skiddle festivals might have the year include appended to the name and we can't change
                    // the name or event merging will fail
                    def festivalNameAndYear = festival.concatenateNameAndYear(festival: suggestion)

                    resultsMap.suggestions << [
                            value: festivalNameAndYear,
                            data : [url: festival.showFestivalUrl(festival: suggestion), finished: suggestion.isFinished()]
                    ]

                } else {
                    log.error "Unrecognised search result: $suggestion"
                }
            }
        }
        render contentType: 'application/json', text: resultsMap as JSON
    }

    /**
     * Searches for matches within a single domain class
     * @param searchCommand
     * @return
     */
    def domainClassSearch(SearchCommand searchCommand) {

        // use findAll() in case searchCommand.domainClass returns null
        params.searchableClasses = [searchCommand.domainClass].findAll()
        String actionName = searchCommand.suggest ? 'suggest' : 'search'
        forward action: actionName, params: params
    }

    /**
     * Searches domain classes indexed by the searchable plugin
     * @param query the query parameter used by searches performed within the application
     * @param q the query parameter used by searches performed from a
     * <a href="https://developers.google.com/webmasters/richsnippets/sitelinkssearch">Google sitelinks search box</a>
     * @return
     */
    def search(String query, String q) {

        // The API supports a single method call to get the results and a suggested alternative query. However,
        // this doesn't work if the query string contains wildcards (which ours might). So we have to make these
        // calls separately
        query = query ?: q
        def results = searchService.domainSearch(query, params.searchableClasses)

        if (!results) {
            Collections.emptyMap()

        } else if (results.results) {
            def artistResults = []
            def festivalResults = []

            results.results.each {
                if (it instanceof Artist) {
                    artistResults << it

                } else if (it instanceof Festival) {
                    festivalResults << it

                } else {
                    log.error "Unrecognised search result: $it"
                }
            }
            [searchResults: results, artistResults: artistResults, festivalResults: festivalResults]

        } else {
            String suggestedQuery = null

            try {
                // A query such as "When is ladies day?" causes an exception, cleaning it prevents this #880
                String cleanedQuery = LuceneUtils.cleanQuery(query)
                suggestedQuery = searchService.suggestQuery(cleanedQuery, [allowSame: false])

                // don't suggest a one-letter alternative
                suggestedQuery = suggestedQuery?.size() > 1 ? suggestedQuery : null

                // #428 sometimes the suggested query will return something like "+electric +picnic". If, so remove the '+' chars
                if (suggestedQuery?.startsWith('+')) {

                    def tokens = suggestedQuery.tokenize().collect { String token ->
                        token[0] == '+' ? token[1..-1] : token
                    }
                    suggestedQuery = tokens.join(' ')
                }
            } catch (ex) {
                // #766 If the query string is something like '&&' or '&', suggestQuery() throws this exception.
                // Normalizing or cleaning the query string beforehand doesn't help
                log.warn "Error finding suggestion for query: $query", ex
            }
            [suggestedQuery: suggestedQuery]
        }
    }

    def festivalTypeSearch(String query) {

        def model = searchService.festivalTypeSearch(query)
        render view: 'search', model: model
    }
}

class SearchCommand {

    /**
     * If true a list of suggestions should be returned rather than the search results
     */
    boolean suggest = false
    Class domainClass
}