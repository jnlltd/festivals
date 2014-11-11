import grails.util.Holders

modules = {

    layout {
        dependsOn 'jquery'

        // we need to give this bundle an explicit name so that we can pass the same name to to the bundle attribute
        // of the LESS resource(s)
        defaultBundle 'layout'

        resource 'js/bootstrap/bootstrap.js'
        resource 'js/bootstrap/bootstrap-datepicker.js'

        resource 'js/modernizer.min.js'

        resource 'js/main.js'
        resource url: 'js/googleAnalytics.js', disposition: 'head'

        resource 'js/autocomplete/jquery.autocomplete.min.js'
        resource 'css/autocomplete/styles.css'

        resource 'js/jquery.placeholder.min.js'
        resource url: 'less/style.less', attrs: [rel: 'stylesheet/less', type: 'css'], bundle: 'layout'

        resource 'css/default.css'
    }

    tablesorter {
        dependsOn 'jquery'

        resource 'js/tablesorter/jquery.metadata.js'
        resource 'js/tablesorter/jquery.tablesorter.min.js'
        resource 'js/tablesorter/init.js'
        resource 'css/tablesorter/style.css'
    }

    highcharts {
        dependsOn 'jquery'
        resource 'js/highcharts.js'
    }

    home {
        dependsOn 'jquery'
        resource 'js/home.js'
        resource 'css/home.css'
    }

    wysihtml5 {
        dependsOn 'layout'

        resource 'css/wysihtml5/bootstrap-wysihtml5.css'
        resource 'css/wysihtml5/custom.css'

        resource 'js/wysihtml5/wysihtml5.js'
        resource 'js/wysihtml5/bootstrap-wysihtml5.js'
        resource 'js/wysihtml5/init.js'
    }

    fullcalendar {
        dependsOn 'jquery'

        resource 'css/fullcalendar/fullcalendar.css'
        resource 'css/fullcalendar/blue-theme.css'
        resource 'css/fullcalendar/custom.css'

        resource 'js/fullcalendar/fullcalendar.min.js'
    }

    map {
        dependsOn 'layout'

        // We don't have to use an API key, but there are certain benefits to doing so
        // https://developers.google.com/maps/faq#keysystem
        //
        // To manage this key go to https://code.google.com/apis/console
        resource url: "http://maps.googleapis.com/maps/api/js?sensor=false&amp;key=${Holders.config.festival.googleApiClientKey}", attrs: [type: 'js'], exclude: 'minify'
        resource url: 'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerclusterer/src/markerclusterer_compiled.js', exclude: 'minify'
        resource url: 'http://google-maps-utility-library-v3.googlecode.com/svn/trunk/infobubble/src/infobubble-compiled.js', exclude: 'minify'
        resource 'js/map/Map.js'
    }

    festival {
        dependsOn 'jquery'

        resource 'js/raty/jquery.raty.min.js'
        resource 'js/festival.js'
        resource 'css/festival.css'
    }

    isotope {
        dependsOn 'jquery'

        resource 'js/isotope/jquery.isotope.min.js'
        resource 'js/isotope/isotope.centered.js'
        resource 'css/isotope/isotope.css'
    }

    blog {
        resource 'css/blog.css'
    }

    artist {
        dependsOn 'jquery'
        resource 'js/jquery.fitvids.js'
        resource 'css/artist.css'
    }

    api {
        dependsOn 'jquery'

        resource 'js/toc/jquery.tableofcontents.min.js'
        resource 'css/api.css'
    }
}
