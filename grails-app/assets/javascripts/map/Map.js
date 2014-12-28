//= encoding UTF-8

/**
 * Add a map to the page
 * @param elementId ID of the element where the map should be displayed
 * @param zoomLevel
 * @param center
 * @param baseImageDir
 * @constructor
 */
SF.Map = function(elementId, zoomLevel, center, baseImageDir) {

    var markerImageCache = {};

    var focalPoint = new google.maps.LatLng(center.latitude, center.longitude);

    var getMarkerImage = function(imageFile) {

        var markerImagePath = baseImageDir + '/map/' + imageFile;
        var cachedMarkerImage = markerImageCache[markerImagePath];

        if (!cachedMarkerImage) {
            var newMarkerImage = new google.maps.MarkerImage(markerImagePath);
            markerImageCache[markerImagePath] = newMarkerImage;
            return newMarkerImage;
        } else {
            return cachedMarkerImage;
        }
    };

    var map = new google.maps.Map(document.getElementById(elementId), {
        streetViewControl: false,
        zoom: zoomLevel,
        center: focalPoint,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        mapTypeControl: false,
        scrollwheel: false
    });

    var shadow = getMarkerImage('shadow.png');

    var addField = function(value) {
        return "<span class='mapField'>" + value + "</span>";
    };

    var createInfoBubble = function(festivalData) {
        var bubbleContent = "<a class='festivalName' href='" + festivalData.url + "'>" + festivalData.name + "</a><br/>";
        var startDate = festivalData.start;
        var endDate = festivalData.end;

        if (startDate == endDate) {
            bubbleContent += addField(startDate);

        } else {
            bubbleContent += addField(startDate + " - " + endDate);
        }

        // InfoBubble example page http://google-maps-utility-library-v3.googlecode.com/svn/trunk/infobubble/examples/example.html
        return new InfoBubble({
            map: map,
            content: bubbleContent,
            shadowStyle: 1,
            padding: 10,
            borderRadius: 8,
            borderWidth: 1,
            borderColor: '#2c2c2c',
            disableAutoPan: true,
            hideCloseButton: false,
            arrowSize: 0,
            arrowPosition: 50,
            arrowStyle: 0
        });
    };

    /**
     * Create one or more map markers
     * @param festivalsData Data about where the markers should be placed, the icon that should be used, etc.
     */
    this.addMarkers = function(festivalsData) {

        // store the markers in an array then add them all to the map at once using MarkerClusterer. This should
        // perform better than adding each marker to the map individually
        var markers = [];

        for (var i = 0; i < festivalsData.length; i++) {

            var festivalData = festivalsData[i];
            var markerFile = festivalData.markerImage;

            var marker = new google.maps.Marker({
                position: new google.maps.LatLng(festivalData.latitude, festivalData.longitude),
                title: festivalData.name,
                shadow: shadow,
                animation: google.maps.Animation.DROP,
                icon: getMarkerImage(markerFile)
            });

            markers.push(marker);

            // open the InfoBubble when the user clicks on the marker
            var showPopup = function(marker, festivalData) {

                return function() {
                    var infoBubble = createInfoBubble(festivalData);
                    if (!infoBubble.isOpen()) {
                        infoBubble.open(map, marker);
                    }
                };
            };
            google.maps.event.addListener(marker, 'click', showPopup(marker, festivalData));
        }

        // the smaller the gridSize, the fewer the number of clusters that will be formed
        var markerClustererOptions = {gridSize: 30};

        // Add the markers to the map: http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerclusterer/docs/examples.html
        new MarkerClusterer(map, markers, markerClustererOptions);
    };
};