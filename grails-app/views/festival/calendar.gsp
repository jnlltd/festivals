<%@ page import="ie.festivals.map.MapFocalPoint; ie.festivals.enums.FestivalType" %>

<head>

    <r:require module="fullcalendar"/>

    <r:script>
        $(document).ready(function () {

            var getSelectedFestivalTypes = function () {
                var checkValues = [];

                $('input[name=types]:checked').each(function () {
                    checkValues.push($(this).val());
                });
                return checkValues.join(',');
            };

            var toGrailsDate = function (date) {
                var day = date.getDate();
                var month = date.getMonth() + 1; //months are zero based
                var year = date.getFullYear();

                return year + '-' + month + '-' + day;
            };

            var fullCalendarOptions = {
                theme: true,
                events: function (start, end, callback) {
                    $.ajax({
                        url: 'getCalendarEvents',
                        dataType: 'json',
                        data: {
                            start: toGrailsDate(start),
                            end: toGrailsDate(end),
                            types: getSelectedFestivalTypes(),
                            location: $('#location').val(),
                            freeOnly: $('#freeOnly').is(':checked')
                        },
                        success: function (eventsAsJSON) {
                            callback(eventsAsJSON);
                        }
                    });
                },

                // Make the first day of each week a monday
                firstDay: 1,
                weekMode: 'variable',
                header: {
                    left: 'prev,next today',
                    center: 'title',
                    right: null
                }
            };

            $('#calendar').fullCalendar(fullCalendarOptions);

            // fetch the events again when an festival filter input changes
            var refreshEvents = function () {
                $('#calendar').fullCalendar('refetchEvents');
            };

            $(':checkbox').click(refreshEvents);
            $('select#location').change(refreshEvents);

            // Add some titles to the buttons
            $('.fc-button-prev').attr('title', 'Previous month');
            $('.fc-button-next').attr('title', 'Next month');
            $('.fc-button-today').attr('title', 'Current month');
        });
    </r:script>

    <style type="text/css">
        /* transitions mess up the spacing between events in Chrome, so disable them #677 */
        a.fc-event {
            -webkit-transition: none !important;
            -moz-transition: none !important;
            -o-transition: none !important;
            transition: none !important;
        }
    </style>

</head>

<body>
<div class="container main">
    <h1 class="hi-fi">${location.displayName} Festival Calendar</h1>

    <p class="spacer">
        Use the Festival Filter to specify the type and/or location of festivals that are shown.
        Click the arrow buttons below to move to the next/previous month. To see more information about one of the festivals displayed,
        click on the festival name.
    </p>

    <div class="row-fluid">
        <div class="span9 spacer">
            <div id="calendar"></div>
        </div>

        <div class="span3">
            <div class="well" style="padding: 7px;">
                <h3 class="well-title" style="margin: 0;">Filter Festivals</h3>
                <label class="top-spacer">Festival Type</label>

                <div class="multiple-checkbox" style="margin-bottom: 10px;">
                    <g:each in="${FestivalType.values()}" var="type" status="i">

                        <label class="checkbox ${type.id} festivalType" for="typeCheckbox${i}">
                            <g:checkBox name="types" value="${type.name()}" checked="true" id="typeCheckbox${i}"/>
                            <span class="color-key">■</span>
                            ${type}
                        </label>
                    </g:each>
                </div>

                <label>Festival Location</label>
                <g:select name="location" class="block"
                          from="${MapFocalPoint.values()}"
                          optionKey="${{ it.name() }}"
                          optionValue="${{ it.displayName }}"
                          value="${location.name()}"/>

                <label class="hoverable checkbox top-spacer last" for="freeOnly"
                       title="${g.message(code: 'filter.free.help')}">
                    <g:checkBox name="freeOnly"/> Free Festivals Only
                </label>
            </div>
        </div>
    </div>
</div>
</body>