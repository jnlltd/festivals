//= encoding UTF-8
//= require jquery.metadata.js
//= require jquery.tablesorter.min.js

$(function() {

    // Define a parser that allows date columns to be sorted by converting them to ms since 1/1/1970
    $.tablesorter.addParser({
        id: 'formattedDate',
        is: function(s) {
            // parser is not auto detected
            return false;
        },
        format: function(text) {
            // The time in milliseconds
            return new Date(text).getTime();
        },

        type: 'numeric'
    });


    $("table.tablesorter").tablesorter({
        // preserve zebra striping after sorting
        widgets: ['zebra'],
        textExtraction: function(node) {

            node = $(node);

            // the subscription column is sorted based on a custom data attribute
            if (node.hasClass('subCol')) {
                return node.attr('data-subscribed');
            } else {
                // pass only the visible text to the sorting function
                return $.trim(node.filter(":visible").text());
            }
        }
    });
});
