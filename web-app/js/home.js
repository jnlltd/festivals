$(function () {

    // main carousel
    $('#mainCarousel').carousel({
        interval: 7000
    });

    // pause carousel on click
    $('.carousel-control').bind('click', function () {
        $('#mainCarousel').carousel({interval: false}).carousel('pause');
    });
});