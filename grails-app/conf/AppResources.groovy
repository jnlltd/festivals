modules = {

    tablesorter {
        dependsOn 'jquery'

        resource 'js/tablesorter/jquery.metadata.js'
        resource 'js/tablesorter/jquery.tablesorter.min.js'
        resource 'js/tablesorter/init.js'
        resource 'css/tablesorter/style.css'
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
}
