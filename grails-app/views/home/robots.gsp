# disallow adsense bot, as we don't use adsense.
User-agent: Mediapartners-Google
Disallow: /

# Yahoo Pipes is for feeds not web pages.
User-agent: Yahoo Pipes 1.0
Disallow: /

# The record for all user agents that are allowed crawl the site
User-Agent: *
Disallow: /login/
Disallow: /logout/
Disallow: /admin/
Disallow: /error/
Disallow: /festival/subscriptions
Disallow: /artistSubscription
Disallow: /privacy
Disallow: /terms
Disallow: /static
Disallow: /api/

# Tell the crawler where to find our sitemap
Sitemap: ${g.createLink(absolute: true, action: 'sitemap')}