var SF = {};

SF.subscribe = function(artistInSearchResults, newArtist) {
    SF._moveArtist(artistInSearchResults, $(newArtist), SF.container);
};

/**
 * Parse a string to a date
 * @param dateString a date in yyyy-MM-dd format
 */
SF.parseDate = function(dateString) {
    var dateParts = dateString.match(/(\d+)/g);
    return new Date(dateParts[0], dateParts[1] - 1, dateParts[2]); // months are 0-based
};

/**
 * Uses isotope to layout content that contains images.
 * @param containerSelector jQuery selector that identifies the container. The visibility of the container should
 * initially be set to hidden to prevent a FOUC
 * @param itemSelector identifies the items within the container to be arranged (in rows)
 * @param sortField
 * @return the isotope container
 */
SF.layoutImageContent = function(containerSelector, itemSelector, sortField) {

    var container = $(containerSelector);

    // Use the imagesLoaded plugin or browser-dependent layout problems will occur #220
    var isotopeOptions = {
        itemSelector: itemSelector,
        onLayout: function() {
            this.css('visibility', 'visible');
        }
    };

    if (sortField) {

        var sorters = {
            perfDate: function (element) {

                // parse out the performance date from the css classes
                var classList = element.attr('class').split(/\s+/);
                var dateClassPrefix = 'date-';
                var date;

                $.each(classList, function(index, cssClassName) {

                    if (cssClassName.substring(0, dateClassPrefix.length) === dateClassPrefix) {

                        // Should be a date in format 'yyyy-MM-dd'
                        var dateString = cssClassName.substring(dateClassPrefix.length);
                        date = SF.parseDate(dateString).getTime();

                        // break the $.each() loop
                        return false;
                    }
                });
                return date;
            }
        };

        if (sorters.hasOwnProperty(sortField)) {
            isotopeOptions.sortBy = sortField;
            isotopeOptions.getSortData = sorters;
        }
    }

    container.imagesLoaded(function() {
        container.isotope(isotopeOptions);
    });

    return container;
};

SF.remove = function(selector) {
    $(selector).fadeOut(SF.effectsSpeed, function() {
        $(this).remove();
    });
};

/**
 * Remove an artist from an isotope container
 * @param selector identifies the item to be removed
 * @param priorityContainer identifies the container to remove it from. If not provided, SF.container is assumed
 */
SF.removeArtist = function(selector, priorityContainer) {

    var container = priorityContainer ? SF.container[priorityContainer] : SF.container;

    var isLastArtist = $('#bottomArtistList div.artistEntry').size() == 1;
    container.isotope('remove', $(selector));

    if (isLastArtist) {
        $('#artistsNotEmpty').hide();
        var artistsEmpty = $('#artistsEmpty');
        artistsEmpty.show();

        // if there is a lineup URL only show that, otherwise hide the whole section
        if (artistsEmpty.length) {
            $(this).show();
        } else {
            $('#bottomArtistList').hide();
        }
    }
};

/**
 * Adds an artist to a festival's lineup
 * @param artistFormContainerId jQuery selector that identifies the parent of the add artist form
 * @param newArtist markup that displays the added performer
 */
SF.addArtistToLineup = function(artistFormContainerId, newArtist) {

    var priority = $(artistFormContainerId + ' input:radio[name=priority]:checked').val();

    // isotope container names are indexed by Priority.id
    priority = priority.toLowerCase();
    var container = SF.container[priority];
    $(artistFormContainerId).modal('hide');
    $('#bottomArtistList').show();

    var artistInSearchResults = $(artistFormContainerId).parents('.artist-result');

    if (!newArtist) {
        var notificationMessage = "This artist is already scheduled to perform at this festival. An artist can only be added to a festival's lineup multiple times if a date is specified for each appearance and each appearance date is different";

        $().notificationMsg({message: notificationMessage, cssClass: 'MessageBarCommon MessageBarWarning'});
        artistInSearchResults.remove();

    } else {
        newArtist = $(newArtist);
        var keepInSearchResults = newArtist.find('div.performDate').length;
        SF._moveArtist(artistInSearchResults, newArtist, container, keepInSearchResults);
    }
};

SF.festivalSubscriptionChanged = function(festivalId, festivalName, subscribed) {
    $('tr#' + festivalId + ' .festivalSubscribe').add('tr#' + festivalId + ' .festivalUnsubscribe').toggleClass('hide');

    // Update the data-subscribed custom attribute that indicates whether the user is subscribed
    var subscriptionCell = $('tr#' + festivalId + ' td.subCol');
    subscriptionCell.attr('data-subscribed', subscribed);

    // Tell the tablesorter plugin that the content of the table has changed: http://tablesorter.com/docs/example-ajax.html
    $("table.tablesorter").trigger("update");

    SF.festivalSubscriptionNotification(subscribed, festivalName);
};

SF.festivalSubscriptionNotification = function(subscribed, festivalName) {
    var notificationMessage = subscribed ? "Thanks for subscribing to " + festivalName +
        ". We'll send you an email when changes are made to this festival's lineup." :
        "Unsubscription successful. You will no longer be informed when changes are made to this festival's lineup";

    $().notificationMsg({message: notificationMessage, cssClass: 'MessageBarCommon MessageBarOk'});
};

/**
 * Move an artist into an isotope container
 * @param sourceSelector jQuery selector that identifies the source location
 * @param artist Content to be moved into the container
 * @param targetContainer Reference to the target container
 * @param copyToTarget Optional, if truthy, artist will be copied instead of moved
 */
SF._moveArtist = function(sourceSelector, artist, targetContainer, copyToTarget) {

    targetContainer.isotope('insert', artist);

    if (!copyToTarget) {
        $(sourceSelector).remove();
    }
    $('#artistsEmpty').hide();
    $('#artistsNotEmpty').show();
};

SF.effectsSpeed = 300;

SF.fadeOut = function(selector) {
    $(selector).fadeOut(SF.effectsSpeed);
};

$(document).ready(function () {

    // clear the comment form after it's been submitted http://stackoverflow.com/questions/9588683/
    $('#commentBody').ajaxSuccess(function() {
        $(this).val(null);
    });

    var showSpinner = function() {
        $("#spinner").fadeIn('fast');
    };

    // Global handlers for AJAX events
    $(document)
        .on("ajaxSend", showSpinner)
        .on("ajaxStop", function() {
            $("#spinner").fadeOut('fast');
        })
        .on("ajaxError", function(event, jqxhr, settings, exception) {
            $("#spinner").hide();
            var response = jqxhr.responseText;

            if (response) {
                // hide any currently displayed modal dialogs and show the error message in a modal dialog
                $('.modal').modal('hide');
                $(response).modal('show');
            }
        });

    // Disable the spinner for AJAX requests submitted by the autocompleter
    $('input.search-query')
        .focus(function() {
            $(document).off('ajaxSend');
        })
        .blur(function() {
            $(document).on("ajaxSend", showSpinner)
        });


    // Add placeholder support to legacy browsers https://github.com/danielstocks/jQuery-Placeholder
    $('input[placeholder], textarea[placeholder]').placeholder();

    // Tooltips
    $('.tip').tooltip();

    // Bootstrap-datepicker.js
    $('.datepicker').datepicker();

    // Modernizer svg detection & fallback
    if (!Modernizr.svg) {
        $("html").css("background", "#D9E3F3");
        var images = $('img[data-png-fallback]');
        images.each(function (i) {
            $(this).attr('src', $(this).data('png-fallback'));
        });
    }

    $('#login-modal a.rpxnow').click(function () {
        $('#login-modal').modal('hide')
    });

    $('#register-modal a.rpxnow').click(function () {
        $('#register-modal').modal('hide')
    });

    $('#to-top').click(function() {
        $('body, html').animate({scrollTop: 0}, 800);
    });

    $("#search-toggle").click(function() {
        $("#searchform").toggle("fast");
    });
});