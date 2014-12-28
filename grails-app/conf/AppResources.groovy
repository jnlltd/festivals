modules = {

    tablesorter {
        dependsOn 'jquery'

        resource 'js/tablesorter/jquery.metadata.js'
        resource 'js/tablesorter/jquery.tablesorter.min.js'
        resource 'js/tablesorter/init.js'
        resource 'css/tablesorter/style.css'
    }

    highcharts {
        dependsOn 'jquery'
        resource 'js/highcharts.min.js'
    }

    wysihtml5 {
        dependsOn 'layout'

        resource 'css/wysihtml5/bootstrap-wysihtml5.css'
        resource 'css/wysihtml5/custom.css'

        resource 'js/wysihtml5/wysihtml5.js'
        resource 'js/wysihtml5/bootstrap-wysihtml5.js'
        resource 'js/wysihtml5/init.js'
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
}
