import grails.util.Environment
import ie.festivals.Role
import ie.festivals.User
import ie.festivals.UserRole
import org.pac4j.oauth.client.FacebookClient
import org.pac4j.oauth.client.Google2Client
import org.pac4j.oauth.client.TwitterClient
import org.pac4j.oauth.client.YahooClient
import org.springframework.security.core.context.SecurityContextHolder

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [html: ['text/html', 'application/xhtml+xml'],
        xml: ['text/xml', 'application/xml'],
        text: 'text/plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        json: ['application/json', 'text/json'],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data',
        excel: 'application/vnd.ms-excel'
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true

grails.sitemesh.default.layout = 'responsive'


// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password', 'passwordConfirm']

// when a stacktrace is logged, include the request params in all environments
grails.exceptionresolver.logRequestParameters = true

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

// the imports themselves don't need to be compiled, only when they are included in style.less
grails.assets.excludes = ["imports/*.less"]

// since Grails 2.4.0, this is required for the Janrain JSP taglib
// http://stackoverflow.com/questions/23796109/using-jsp-taglibs-in-grails-2-4-0
grails.gsp.tldScanPattern='classpath*:/META-INF/*.tld,/WEB-INF/tld/*.tld'

environments {

    development {
        festival.sendEmail = false
        festival.feedbackEmail = 'festivals-feedback-dev@mailinator.com'

        grails.logging.jul.usebridge = true
        festival.images.artistDir = System.properties['user.home'] + '/workspace/examples/festivals-backup/artists/'

        //grails.serverURL = "http://localhost:8080/festivals"
        grails.serverURL = "http://127.0.0.1/festivals"
    }

    test {
        festival.sendEmail = false
        festival.images.artistDir = System.properties['java.io.tmpdir'] + '/artists/'
        festival.eventbrite.maxResultsPerCountry = 5

        grails.serverURL = "http://festivals-test.com"

        def dummyValue = 'preventsExceptionsWhenRunningTests'

        janrain.apiKey = dummyValue
        janrain.applicationID = dummyValue
        systemWidePasswordSalt = dummyValue
    }

    production {
        festival.sendEmail = true
        grails.logging.jul.usebridge = false

        festival.images.artistDir = System.properties['user.home'] + '/festivals-files/artists/'

        grails.serverURL = "http://www.festivals.ie"
    }
}

festival {
    recentFestivalsCount = 9
    utf8mb4MaxLength = 191

    // http://www.skiddle.com/affiliates/links.php
    skiddle {
        feedUrl = 'http://www.skiddle.com/affiliates/xml/festivals.xml'
    }

    eventbrite {
        feedUrl = 'https://www.eventbrite.com/xml/event_search'
        maxResultsPerCountry = 100
    }

    feedbackEmail = 'info@festivals.ie'
    userRoleName = 'ROLE_USER'
    adminRoleName = 'ROLE_ADMIN'

    // The format dates used when dates are sent to the server and the calendar JQuery plugin.
    // The date display format is in messages.properties
    dateFormat = 'yyyy-MM-dd'

    lastFM {
        artistSearchLimit = 5
        topTracksLimit = 10
        topAlbumsLimit = 6
    }

    images {
        artistImageType = 'png'
        thumbnailWidth = 126
    }
}

grails.gorm.default.constraints = {

    // use minSize and maxSize instead of size, because size cannot be used with nullable
    phoneNumber(nullable: true, minSize: 5, maxSize: 25, matches: "[0-9 \\-()+]+")

    // apply a max size of 191 chars to String columns to support utf8mb4
    // http://mathiasbynens.be/notes/mysql-utf8mb4
    '*'(maxSize: festival.utf8mb4MaxLength)

    // This shared constraint provides a way to override the default above for long text properties in application
    // domain classes. For plugin domain classes, we do it in Bootstrap.groovy
    unlimitedSize(maxSize: Integer.MAX_VALUE)
    enumLength(length: festival.utf8mb4MaxLength)
}

grails.databinding.dateFormats = [festival.dateFormat]


log4j = {
    appenders {
        def logPattern = '%d{dd-MM-yyyy HH:mm:ss,SSS} %5p %c{2} - %m%n'
        console name: 'consoleAppender', layout: pattern(conversionPattern: logPattern)

        file name: "fileAppender", file: "festivals.log"

        environments {
            production {
                // Change the location of the built-in unfiltered stacktrace logger's file output
                // Must be a location tomcat7 user can write to: http://joshuakehn.com/2012/2/9/Grails-in-Production.html
                rollingFile name: "stacktrace", file: "/var/log/tomcat7/stacktrace.log"

                // Also change location of FileAppender output (see above)
                file name: "fileAppender", file: "/var/log/tomcat7/festivals.log"
            }
        }
    }

    root {
        // define the root logger's level and appenders, these will be inherited by all other loggers
        error 'consoleAppender', 'fileAppender'
    }

    def festivalNamespaces = [
            'ie.festivals',
            'grails.app.conf.ie.festivals',
            'grails.app.filters.ie.festivals',
            'grails.app.taglib.ie.festivals',
            'grails.app.services.ie.festivals',
            'grails.app.controllers.ie.festivals',
            'grails.app.domain.ie.festivals',
            'grails.app.jobs.ie.festivals'
    ]

    // statements from the app should be logged at DEBUG level in dev/test, and at INFO level in prod
    festivalNamespaces.each { debug it }

    environments {
        production {
            festivalNamespaces.each { info it }
        }
    }
}

// Enable the console plugin in all envs. The security rules below prevent non-admins from accessing it
grails.plugin.console.enabled = true

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = User.name
grails.plugins.springsecurity.userLookup.authorityJoinClassName = UserRole.name
grails.plugins.springsecurity.authority.className = Role.name

grails.plugins.springsecurity.controllerAnnotations.staticRules = [

        // https://github.com/sheehan/grails-console/blob/master/README.md
        "/console/**": ['ROLE_ADMIN'],
        "/plugins/console*/**": ['ROLE_ADMIN'],

        '/blog/createEntry/**': ['ROLE_ADMIN'],
        '/blog/editEntry/**': ['ROLE_ADMIN'],
        '/blog/publish/**': ['ROLE_ADMIN'],
        '/searchable/**': ['ROLE_ADMIN'],
        '/commentable/**': ['ROLE_ADMIN', 'ROLE_USER'],
        '/auditlogevent/**': ['ROLE_ADMIN'],
        '/fontimage/**': ['ROLE_ADMIN']
]

grails {

    mail {
        host = "smtp.gmail.com"
        port = 465
        username = "festivals@festivals.ie"

        props = ["mail.transport.protocol":"smtps",
                 "mail.smtps.host":"smtp.gmail.com",
                 "mail.smtps.port":"465",
                 "mail.smtps.auth":"true"]
    }
}

flashHelper.keys = ['info', 'warn']
flashHelper.separator = ' '

simpleCaptcha {
    fontSize = 18
    length = 6
    bottomPadding = 8
    lineSpacing = 10
}

// http://stackoverflow.com/questions/3541142/grails-user-evaluator-for-commentable-with-spring-security-plugin
def evaluator = {

    def principal = SecurityContextHolder.context.authentication.principal

    if (principal.hasProperty('id')) {

        def currentUserId = principal.id
        if (currentUserId) {
            User.get(currentUserId)
        }
    }
}

grails.blog.author.evaluator = evaluator
grails.commentable.poster.evaluator = evaluator

grails.cache.config = {

    // ehcache settings that will be inherited by each cache (below)
    // see the "Cache configuration" section of this page for an explanation of these params: http://ehcache.org/ehcache.xml
    defaults {
        maxElementsInMemory 1000
        eternal false
        overflowToDisk false
        maxElementsOnDisk 0

        timeToIdleSeconds 60 * 60 // item will be removed if not requested at least this often
        timeToLiveSeconds 60 * 90 // item will be always be removed after this period
    }

    cache {
        // this cache is cleared at 00:01 daily by CacheManagerJob because a festival may change from
        // a future festival to a past festival once midnight is crossed,
        name 'festivalGroup'
    }

    cache {
        name 'image'

        // item will stay in this cache as long as it is requested every timeToIdleSeconds
        timeToIdleSeconds 60 * 60 * 24
        timeToLiveSeconds 0
    }

    cache {
        // it seems there's already a cache named 'default' that is created by EhCache itself
        name 'festival-default'
    }
}

grails.cache.clearAtStartup	= true

// Setup the login providers here: https://dashboard.janrain.com/
// Login as domurtag@yahoo.co.uk
janrain.applicationDomain = "https://festivals-ie.rpxnow.com/"
janrain.tokenUrl = "${grails.serverURL}/register/socialLoginHandler"

grails.plugins.airbrake.async = true
grails.plugins.airbrake.includeEventsWithoutExceptions = true
grails.plugins.airbrake.enabled = Environment.current == Environment.PRODUCTION
grails.plugins.airbrake.paramsFilteredKeys = ['password', 'passwordConfirm']


// Sensitive configuration (e.g. passwords) that should not be in version control should be stored in these local files
//
// grails-app/conf/secret.properties
// grails-app/conf/secret-DEVELOPMENT.properties
// grails-app/conf/secret-PRODUCTION.properties
//
// Environment-specific configuration will override configuration in secret.properties
//
grails.config.locations = [
        "classpath:secret.properties",
        "classpath:secret-${Environment.current}.properties"
]

oauth {
    google {
        client = Google2Client
        scope = Google2Client.Google2Scope.EMAIL_AND_PROFILE
    }

    facebook {
        client = FacebookClient
        scope = 'email'
        fields = 'id,name,first_name,middle_name,last_name,email,picture.width(200).height(200).type(square)'
    }

    twitter.client = TwitterClient
    yahoo.client = YahooClient
}