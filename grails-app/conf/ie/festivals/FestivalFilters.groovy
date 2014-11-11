package ie.festivals


class FestivalFilters {

    FestivalService festivalService

    def filters = {

        // Exclude for the controllers named below
        addApprovedFestivalCount(controller: 'simpleCaptcha|api|favoriteFestival', invert: true) {

            after = { Map model ->

                if (log.traceEnabled) {
                    log.trace "Filter invoked for URL: $request.forwardURI"
                }

                // store the data as a request attribute rather than in the model, because the model is not available
                // when a URL is mapped directly to a view in UrlMappings.groovy
                request['approvedFestivalCount'] = festivalService.approvedFestivalCount
            }
        }
    }
}
