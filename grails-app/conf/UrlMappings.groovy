class UrlMappings {

	static mappings = {
        "/robots.txt"(controller: 'home', action: 'robots')

        "/sitemap.xml"(controller: 'home', action: 'sitemap')

        "/writeForUs"(controller: 'home', action: 'writeForUs')

		"/$controller/$action?/$id?/$name?"{
			constraints {
				// apply constraints here
			}
		}

        "/"(controller: 'home')

        "500"(controller: 'error')

        // 405 => HTTP method not allowed
        "405"(controller: 'error', action: 'handle405')

        // we can't map straight to the view because we need to run the filter
        "404"(controller: 'error', action: 'handle404')

        def staticPages = ['privacy', 'terms', 'about']

        staticPages.each {page ->
            "/$page"(view: "/home/$page")
        }

        "/apiDocs"(view: "/api/apiDocs")

        name showFestival: "/show/$id/$type?/$name?" {
            controller = "festival"
            action = "show"
        }

        name oauth: "/oauth/${action}/${provider}"(controller: 'oauth')
	}
}
