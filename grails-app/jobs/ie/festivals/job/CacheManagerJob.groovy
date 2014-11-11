package ie.festivals.job

import groovy.time.TimeCategory
import ie.festivals.SearchService
import org.springframework.cache.CacheManager

class CacheManagerJob {

    static triggers = {

        // Fire at 00:01 every day
        cron cronExpression: '0 1 0 ? * *'
    }

    CacheManager grailsCacheManager
    SearchService searchService

    def concurrent = false

    def execute() {
        log.info "$CacheManagerJob.simpleName started at ${new Date()}"

        // clean out the festivalGroup cache. A festival may change from a future festival
        // to a past festival once midnight is crossed, so cached groups of festivals are no longer valid
        grailsCacheManager.getCache('festivalGroup')?.clear()

        // remove old festivals from the search index, which could be considered a type of cache
        use(TimeCategory) {
            Date endDate = new Date() - 6.months
            searchService.unindexFestivals(endDate)
        }

        log.info "$CacheManagerJob.simpleName finished at ${new Date()}"
    }
}
