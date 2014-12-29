grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
//grails.project.war.file = "target/${appName}-${appVersion}.war"


grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        grailsPlugins()
        grailsHome()
        mavenLocal()
        grailsCentral()
        mavenCentral()
        mavenRepo "http://repository.springsource.com/maven/bundles/external"
        mavenRepo "http://repo.grails.org/grails/core"

        // required by the browser-detection plugin if version > 0.4.3
        mavenRepo 'https://raw.githubusercontent.com/HaraldWalker/user-agent-utils/mvn-repo/'
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        runtime 'mysql:mysql-connector-java:5.1.34'

        compile 'org.jsoup:jsoup:1.8.1',
                'org.imgscalr:imgscalr-lib:4.2',
                'javax.media.jai:com.springsource.javax.media.jai.core:1.1.3',
                'org.codehaus.groovy.modules.http-builder:http-builder:0.7.1',
                'com.neovisionaries:nv-i18n:1.13'

        test    'org.gmock:gmock:0.8.3',
                'org.hamcrest:hamcrest-all:1.3'
    }

    plugins {
        build   ':tomcat:7.0.55'
        runtime ':hibernate:3.6.10.18'

        compile ":airbrake:0.9.4",
                ":asset-pipeline:2.0.20",
                ":audit-logging:1.0.3",
                ":browser-detection:2.1.0",
                ":cache:1.1.8",
                ":commentable:0.8.1",
                ":export:1.6",
                ":fields:1.4",
                ":geocode:0.3",
                ":janrain:1.1.0",
                ":less-asset-pipeline:2.0.8",
                ":searchable:0.6.9",
                ":simple-captcha:1.0.0",
                ":spring-security-core:1.2.7.4",
                ":taggable:1.1.0",
                ":webxml:1.4.1"

        runtime ":cache-ehcache:1.0.0",
                ":cache-headers:1.1.7",
                ":console:1.5.2",
                ":feeds:1.6",
                ":flash-helper:0.9.9",
                ":jdbc-pool:7.0.47",
                ":jquery:1.11.1",
                ":mail:1.0.7",
                ":quartz:1.0.2",
                ":simple-blog:0.3.5"

        test    ":build-test-data:2.2.2"
    }
}

//grails.plugin.location.'simple-blog' = "../grails-simple-blog"
