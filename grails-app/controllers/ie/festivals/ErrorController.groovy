package ie.festivals

class ErrorController {

    /**
     * Global exception handler configured in <tt>UrlMappings.groovy</tt>
     */
    def index() {

        if (request.xhr) {
            render template: 'ajaxError'
        } else {
            render view: 'error'
        }
    }

    def handle404() {
        // we can't map straight to the view because we need to run the filter
        render view: '404'
    }

    def handle405() {
        flashHelper.warn 'error.405': request.method
        log.warn "Invalid HTTP method '$request.method' for URL $request.forwardURI"
        redirect uri: '/'
    }
}
