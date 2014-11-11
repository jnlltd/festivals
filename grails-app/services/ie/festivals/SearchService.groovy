package ie.festivals

import grails.plugin.searchable.SearchableService
import grails.plugin.searchable.internal.lucene.LuceneUtils


class SearchService {

    static transactional = false

    SearchableService searchableService
    FestivalService festivalService

    private String normaliseQuery(query) {
        LuceneUtils.escapeQuery(query?.trim())
    }

    /**
     * Remove from the search index all festivals that finished before the specified date
     * @param endDate
     */
    void unindexFestivals(Date endDate) {
        def finishedFestivalIds = Festival.withCriteria {
            lt 'end', endDate

            projections {
                property 'id'
            }
        }

        Festival.unindex(finishedFestivalIds.toArray())
        log.info "Removed ${finishedFestivalIds.size()} from search index"
    }

    def suggestQuery(Object[] args) {
        // I tried annotating searchableService with @Delegate, instead of writing a delegation method, but it caused a NPE
        searchableService.suggestQuery(args)
    }

    /**
     * Search for matching domain object
     * @param query the search query
     * @param searchableClasses the types of domain objects to include in the results
     * @return
     */
    def domainSearch(String query, List<Class> searchableClasses) {

        query = normaliseQuery(query)

        if (!query) {
            return [:]
        }

        query += '*'

        // Need to ensure that unapproved festivals are never shown http://stackoverflow.com/a/8934053/2648
        query = "($query AND approved:true) OR ($query -approved:false -approved:true)".toString()

        // Don't include blog posts in the search results,
        def searchOptions = [classes: searchableClasses ?: [Festival, Artist]]

        // In order to retrieve all matching results, first of all count how many there are, then execute the search
        // with max set to this value. I tried setting max to a very large constant, but this causes an OutOfMemoryError
        def matchingItems = searchableService.countHits(query, searchOptions)
        searchOptions.max = matchingItems
        searchableService.search(query, searchOptions)
    }

    Map festivalTypeSearch(String query) {

        query = normaliseQuery(query)

        if (!query) {
            [searchResults: [:]]

        } else {
            // We have to specify a maximum number of results, because otherwise the plugin default of 10 will be
            // used. Don't use a huge number, e.g. Integer.MAX_VALUE because it causes an OutOfMemoryError
            def maxResults = festivalService.approvedFestivalCount

            def results = Festival.search("$query approved:true", [defaultProperty: "type", max: maxResults])
            [searchResults: results, festivalResults: results.results]
        }
    }
}
