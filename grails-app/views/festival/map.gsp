<%@ page import="ie.festivals.enums.FestivalType; ie.festivals.map.MapFocalPoint" %>

<head>
    <asset:script>
        $(function() {
            var mapData = ${mapData};

            var map = new SF.Map('map', mapData.zoom, mapData.center, mapData.baseImageDir);
            map.addMarkers(mapData.festivals);
        });
    </asset:script>

    <style type="text/css">
    #map {
        height: 650px;
    }

    #map img {
        max-width: none;
    }

    .span9 {
        padding-top: 7px;
    }

    a.festivalName {
        font-size: 12px;
    }

    .bright-alert {
        margin-bottom: 10px;
    }
    </style>

    %{-- Using an API key is recommended (but not mandatory): https://developers.google.com/maps/faq#keysystem --}%
    <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?sensor=false&key=${grailsApplication.config.festival.googleApiClientKey}"></script>
    <script type="text/javascript" src="http://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerclusterer/src/markerclusterer_compiled.js"></script>
    <script type="text/javascript" src="http://google-maps-utility-library-v3.googlecode.com/svn/trunk/infobubble/src/infobubble-compiled.js"></script>
</head>

<body>
<div class="container main">
    <h1 class="hi-fi">${command.location.displayName} Festival Map</h1>

    <ul class="spaced">
        <li>Use the Festival Filter to choose which festivals are shown on the map.</li>
        <li>To see more information about a festivals, click on the map marker.</li>
        <li>If there's not enough room on the map to show a marker for each festival, the markers will be clustered
        and a numbered icon will be displayed instead. The number indicates how many festivals are in the cluster.
        To see the individual festivals in a cluster either click on the numbered icon, or zoom in closer.</li>
    </ul>

    <g:if test="${festivalCount && command.location.countryCode}">
        <div class="bright-alert top-spacer">
            ${festivalCount == 1 ? "1 festival matches" : "$festivalCount festivals match"} the types selected in the
            Festival Filter. If you can't see them all, zoom out to reveal the rest.
        </div>
    </g:if>

    <div class="row-fluid">
        <div class="span9 spacer">
            <div id="map"></div>
        </div>

        <div class="span3">
            <g:render template="festivalFilter"
                      model="[filterUrl: [controller: 'festival', action: 'map'], command: command]"/>
        </div>
    </div>
</div>

<asset:javascript src="map/Map.js"/>

</body>
