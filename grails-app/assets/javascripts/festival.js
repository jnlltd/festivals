$(document).ready(function () {

    // similar festivals carousel
    $('#similarCarousel').carousel({ interval: 4000 });

    // Why subscribe to artist popover
    $(".why-subscribe-artist").popover({
        animation: true,
        placement:'bottom',
        trigger:'click',
        title: 'Why subscribe to this artist?',
        content: "When you subscribe to an artist, you will receive an email alert when the artist is added to any festival's lineup. Only registered users may subscribe to an artist."
    });

    // Kill the popover and the active button on window resize
    $(window).bind('resize', function () {
        if ($('.popover').is(":visible")) {
            $('.why-subscribe-artist').popover('hide');
            $(".why-subscribe-artist").removeClass('active');
        }
    });

    // Radio buttons to toggle between entire lineup and headline artists only
    $.fn.slideFadeToggle = function (speed, easing, callback) {
        return this.animate({opacity:'toggle', height:'toggle'}, speed, easing, callback);
    };

    $(".lineup-toggle input").click(function () {
        $("#midline").slideFadeToggle(400);
    });

    // festival filter checkboxes
    $('.festival-date-filter').click(function() {
        var cssDateClass = '.' + $(this).attr('name');
        $(cssDateClass).toggleClass('fadeout');
    });

    // carousel that shows reviews
    $('#reviewsCarousel').carousel({
        interval:null
    });

    // reviewsCarousel height animation: on .nextReview or .prevReview button click, set the carousel-inner class to animate
    // to the height of the next item or the first item if there is no next item.
    // On carousel slide, set the current review number to the index position of the .active .item
    var setCurrentReviewIndex = function() {
        $("#reviewsCarousel .reviewIndex").html($("#reviewsCarousel .active").index("#reviewsCarousel .item") + 1);
    };

    $("#reviewsCarousel .btn.nextReview").click(function () {
        var reviewHeight = $("#reviewsCarousel .item.active").next(".item").height();
        if (!reviewHeight) {
            reviewHeight = $("#reviewsCarousel .item").first(".item").height();
        }
        $("#reviewsCarousel .carousel-inner").animate({"height":reviewHeight + "px"}, 400);
        $('#reviewsCarousel').bind('slid', setCurrentReviewIndex);
    });

    $("#reviewsCarousel .btn.prevReview").click(function () {
        var reviewHeight = $("#reviewsCarousel .item.active").prev(".item").height();
        if (!reviewHeight) {
            reviewHeight = $("#reviewsCarousel .item").last(".item").height();
        }
        $("#reviewsCarousel .carousel-inner").animate({"height":reviewHeight + "px"}, 400);
        $('#reviewsCarousel').bind('slid', setCurrentReviewIndex);
    });

    // handler for booking.com form
    $('#booking').submit(function() {
        var bookingDotComUrl = $(this).attr('action');

        var params = $(this).serializeArray();
        var bookingParams = [];

        // we need to convert:
        //
        // &checkin=2013-05-26
        // &checkout=2013-05-27
        //
        // to:
        //
        // &checkin_monthday=26
        // &checkin_year_month=2013-5
        // &checkout_monthday=27
        // &checkout_year_month=2013-5
        for (var i = 0; i < params.length; i++) {

            var param = params[i];
            var paramName = param.name;

            if (paramName == 'checkin' || paramName == 'checkout') {

                var date = SF.parseDate(param.value);
                var day = {name: paramName + '_monthday', value: date.getDate()};
                bookingParams.push(day);

                var monthAndYear = date.getFullYear() + '-' + (date.getMonth() + 1);
                var monthYearParam = {name: paramName + '_year_month', value: monthAndYear};
                bookingParams.push(monthYearParam);

            } else {
                bookingParams.push(param);
            }
        }
        bookingDotComUrl += '?' + $.param(bookingParams);

        window.open(bookingDotComUrl);
        return false;
    });

});
