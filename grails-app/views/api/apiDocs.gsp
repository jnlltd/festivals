<%@ page import="ie.festivals.map.MapFocalPoint; ie.festivals.enums.FestivalType" %>
<head>

    <asset:script>
    $(document).ready(function(){
        $("#toc").tableOfContents(
                $(".container.main"),   // parent element of headings
                {
                    startLevel: 2,      // H2 and up
                    depth:      3,      // H2 through H4,
                    topLinks:   false   // Add "Top" links to each header
                }
        );
    });
    </asset:script>

    <asset:stylesheet href="api.css"/>
</head>
<body>

<cache:block>

<div class="container main">

    <ol id="toc"></ol>

    <div id="left-col">
        <h1 class="hi-fi">Developer API</h1>

        <p class="spacer block-text">
            If you wish to use our data in your website or application, you may request access to our API. The API allows you
            to retrieve festival data by location, time, festival type, etc. Additional features may be added on request.
            Usage of the API is free-of-charge, but we request that users include links back to festivals.ie wherever the data is used.
        </p>

        <h2 class="hi-fi">Registration</h2>

        <p class="spacer block-text">
            To request access to our API, please complete the following steps:
        </p>

        <ol>
            <li>If you don't already have a festivals.ie account, create one.</li>
            <li>Login to your festivals.ie account and complete
            <g:link controller="home" action="contact" params="[subject: 'API Registration']">this form</g:link>
            including the following information:

                <ul>
                    <li>The URL of your website (if applicable).</li>
                    <li>A description of how you intend to use data provided by our API.</li>
                </ul>
            </li>
        </ol>

        <p>
            If your request is approved you will be provided with an API key which must be included with
            all API requests.
        </p>
    </div>
    <h2 class="hi-fi">Introduction</h2>

    <p>
        Our <a target="_blank" href="http://en.wikipedia.org/wiki/Representational_State_Transfer">RESTful</a> API
        returns data in JSON format. The API is read-only, so all methods should be invoked using HTTP GET and all
        parameters should be URL-encoded.
    </p>

    <p>
        Use common sense when deciding how many API calls to make, and cache the responses. Our data changes
        relatively slowly (a couple of festivals are added/updated per day on average), so repeatedly making the same API
        calls within a short space of time is a waste of your computing resources and ours. Your account may be suspended
        if your application is continuously making an excessive number of API calls.
    </p>

    <h2 class="hi-fi">Error Handling</h2>

    <p>
        When a successful API request is sent, the response will return the relevant data in JSON format with a HTTP 200
        status code. When an unsuccessful API request is sent, a HTTP status code between 400 and 599 will be returned
        and the JSON data will include a message that indicates the cause of the error. For example,
        if a request is received with invalid user and key parameters, the response will have a HTTP status of 403
        (forbidden) and the JSON will be:
    </p>

<pre>
{"errorMessage":"Unauthorised - API request did not include valid 'user' and 'key' parameters"}
</pre>

    <p>
        Therefore, callers of the API can detect an error either by checking whether the HTTP status is
        200, or by checking for the presence of an <code>errorMessage</code> property in the JSON response.
    </p>

    <h2 class="hi-fi" id="pagination">Controlling Response Size</h2>

    <p>
        Some of the API methods that return a list of festivals support pagination via the <code>max</code> and
        <code>offset</code> parameters. The <code>max</code> parameter limits the number of results that are returned
        and can be used in combination with <code>offset</code> to retrieve a page of results. For example,
        for a page size of 20, the first page of results can be retrieved via <code>offset=0&max=20</code>. The second
        page of results would be retrieved via <code>offset=20&max=20</code>, and so on.
    </p>
    <p>
        Although neither of these parameters are required, it is recommended that a <code>max</code> value should be
        provided (where possible) in order to avoid returning more results than the caller can handle.
        A <code>max</code> value can be provided without an <code>offset</code>, but if an <code>offset</code> is provided,
        a <code>max</code> value is required.
    </p>

    <h2 class="hi-fi">API Methods</h2>

    <h3 id="festival-search">Festival Search</h3>

    <p>
        Search for festivals using the following criteria:
    </p>

    <dl>
        <dt>location</dt>
        <dd>Use this parameter to restrict the search to certain locations. If the value of this parameter
        is either IRELAND or UK then only festivals from this location will be matched. If this parameter is omitted
        festivals from any location may be included in the results.
        </dd>

        <dt>futureOnly</dt>
        <dd>
            Use this parameter to prevent the results from including festivals that have already occurred.
            If this parameter is omitted, it is assumed that only future festivals should be included in the results.
        </dd>

        <dt>freeOnly</dt>
        <dd>
            Use this parameter to restrict the search to festivals that do not charge an entrance fee. If this parameter
            is omitted, the results may include both free festivals and festivals that charge an admission fee.
        </dd>

        <dt>start</dt>
        <dd>
            Use this parameter to restrict the search to a certain date range. The start date must be provided in the
            format <code>yyyy-mm-dd</code> e.g. <code>2012-12-01</code>. If a start date is provided an end
            date must also be provided and only festivals that start or end between these dates will be included in the
            results. The <code>futureOnly</code> parameter is ignored when a date range is specified, i.e. it is assumed that
            <code>futureOnly=false</code>.
        </dd>

        <dt>end</dt>
        <dd>
            Use this parameter to restrict the search to a certain date range. The end date must be provided in the
            format <code>yyyy-mm-dd</code> e.g. <code>2012-12-01</code>. If an end date is provided a start
            date must also be provided and only festivals that start or end between these dates will be included in the
            results. The <code>futureOnly</code> parameter is ignored when a date range is specified, i.e. it is assumed that
            <code>futureOnly=false</code>.
        </dd>

        <dt>max</dt>
        <dd>
            See <a href="#pagination">controlling response size</a>
        </dd>

        <dt>offset</dt>
        <dd>
            See <a href="#pagination">controlling response size</a>
        </dd>

        <dt>types</dt>
        <dd>Use this parameter to specify the type(s) of festival to include in the results. If this parameter is omitted,
        festivals of all types may be included. This parameter may have one (or more) of the following values:

            <g:render template="/api/festivalTypes"/>
        </dd>
    </dl>

    <h4>Example Requests</h4>

    <div class="alert badge-warning spacer">
        To test the URLs below you must replace the <code>user</code> and <code>key</code> parameter values
        with your festivals.ie email address and API key.
    </div>

    <g:set var="comedyAndMusic" value="${[FestivalType.MUSIC, FestivalType.COMEDY].collect { it.name().toLowerCase() }}"/>

    <ul class="spaced">
        <li>
            Find all Irish music and comedy festivals. Include festivals that have already occurred.<br/>

            <g:link controller="api" action="festivalSearch"
                    params="[types: comedyAndMusic,
                            location: MapFocalPoint.IRELAND,
                            futureOnly: false,
                            user: 'REPLACE_ME',
                            key: 'REPLACE_ME']">

                <g:createLink controller="api" action="festivalSearch" absolute="true"
                              params="[types: comedyAndMusic,
                                      location: MapFocalPoint.IRELAND,
                                      futureOnly: false,
                                      user: 'REPLACE_ME',
                                      key: 'REPLACE_ME']"/>
            </g:link>
        </li>

        <li>
            Find all free festivals (anywhere) that have not yet occurred.<br/>

            <g:link controller="api" action="festivalSearch"
                    params="[freeOnly: true,
                            user: 'REPLACE_ME',
                            key: 'REPLACE_ME']">

                <g:createLink controller="api" action="festivalSearch" absolute="true"
                        params="[freeOnly: true,
                                user: 'REPLACE_ME',
                                key: 'REPLACE_ME']"/>
            </g:link>
        </li>
    </ul>

    <h4>Example Response</h4>
    <pre>
[
    {
        "id": 229,
        "name": "Mayo International Choral Festival",
        "start": "2014-05-23",
        "end": "2014-05-26",
        "type": "Music",
        "url": "http:\/\/www.festivals.ie\/show\/229\/music\/mayo-international-choral-festival",
        "website": "http:\/\/www.mayochoral.com\/",
        "address": "Mayo, Co. Mayo, Ireland",
        "latitude": 54.0153,
        "longitude": -9.42894
    },
    {
        "id": 363,
        "name": "Mayo North Welcome Home Festival",
        "start": "2014-08-01",
        "end": "2014-08-09",
        "type": "Other",
        "url": "http:\/\/www.festivals.ie\/show\/363\/other\/mayo-north-welcome-home-festival",
        "website": "http:\/\/www.northmayo.ie\/",
        "address": "Mayo, Ireland",
        "latitude": 53.9346,
        "longitude": -9.35165
    }
]
</pre>
    <p>
        This example response shows 2 matching festivals. The fields returned for each festival are explained below:
    </p>
    <dl class="table-display response-fields" id="festival-search-response">
        <dt>id</dt>
        <dd>A unique numerical identifier for the festival</dd>

        <dt>name</dt>
        <dd>The name of the festival</dd>

        <dt>start</dt>
        <dd>The start date of the festival in the format <code>yyyy-mm-dd</code></dd>

        <dt>end</dt>
        <dd>The end date of the festival in the format <code>yyyy-mm-dd</code></dd>

        <dt>type</dt>
        <dd>The type of festival (music, comedy, etc.)</dd>

        <dt>url</dt>
        <dd>The URL of this festival's page on festival.ie</dd>

        <dt>website</dt>
        <dd>The URL of the festival's official website</dd>

        <dt>address</dt>
        <dd>The address of this festival's location</dd>

        <dt>latitude</dt>
        <dd>The latitudinal coordinate of this festival's location</dd>

        <dt>longitude</dt>
        <dd>The longitudinal coordinate of this festival's location</dd>
    </dl>

    <h3 class="separator" id="geospatial-search">Geospatial Festival Search</h3>

    <p>
        Find festivals near a geographic location.
    </p>

    <dl>
        <dt>latitude</dt>
        <dd>
            This parameter is required and must be a number between -90 and 90.
            It specifies the latitudinal coordinate of the location to search from.
        </dd>

        <dt>longitude</dt>
        <dd>
            This parameter is required and must be a number between -180 and 180.
            It specifies the longitudinal coordinate of the location to search from.
        </dd>

        <dt>radius</dt>
        <dd>
            This parameter is required and must be a whole number between 1 and 1000. It specifies the maximum distance in kilometers
            between the festival's location and the location identified by the latitude and longitude parameters.
        </dd>

        <dt>max</dt>
        <dd>
            See <a href="#pagination">controlling response size</a>
        </dd>

        <dt>offset</dt>
        <dd>
            See <a href="#pagination">controlling response size</a>
        </dd>

        <dt>types</dt>
        <dd>Use this parameter to specify the type(s) of festival to include in the results. If this parameter is omitted,
        festivals of all types may be included. This parameter may have one (or more) of the following values:

            <g:render template="/api/festivalTypes"/>
        </dd>
    </dl>

    <h4>Example Request</h4>

    <div class="alert badge-warning spacer">
        To test the URL below you must replace the <code>user</code> and <code>key</code> parameter values
        with your festivals.ie email address and API key.
    </div>

    <ul class="spaced">
        <li>
            Find all music and comedy festivals within 30km of the location with coordinates 53.01, -7.14.<br/>

            <g:link controller="api" action="festivalGeoSearch"
                    params="[latitude: 53.01,
                            longitude: -7.14,
                            radius: 30,
                            types: comedyAndMusic,
                            user: 'REPLACE_ME',
                            key: 'REPLACE_ME']">

                <g:createLink controller="api" action="festivalGeoSearch" absolute="true"
                              params="[latitude: 53.01,
                                      longitude: -7.14,
                                      radius: 30,
                                      types: comedyAndMusic,
                                      user: 'REPLACE_ME',
                                      key: 'REPLACE_ME']"/>
            </g:link>
        </li>
    </ul>

    <h4>Example Response</h4>
    <pre>
[
    {
        "id": 457,
        "name": "Electric Picnic",
        "start": "2014-08-30",
        "end": "2014-09-01",
        "type": "Headline",
        "url": "http:\/\/www.festivals.ie\/show\/457\/headline\/electric-picnic",
        "website": "http:\/\/electricpicnic.ie\/",
        "address": "Stradbally, Co. Laois, Ireland",
        "latitude": 53.0163,
        "longitude": -7.14741,
        "distance": 0.85819626
    },
    {
        "id": 380,
        "name": "The Irish Derby Festival",
        "start": "2014-06-28",
        "end": "2014-06-30",
        "type": "Sport",
        "url": "http:\/\/www.festivals.ie\/show\/380\/sport\/the-irish-derby-festival",
        "website": "http:\/\/www.curragh.ie",
        "address": "The Curragh Racecourse, The Curragh, Co. Kildare, Ireland",
        "latitude": 53.1618,
        "longitude": -6.80978,
        "distance": 27.772072
    }
]</pre>
    <p class="spacer">
        The response above shows that 2 festivals will occur within 30km of the specified location. Festivals
        that have already occurred will not be included in the response. The <code>distance</code> field indicates
        the distance in kilometers between the festival and the specified location. The festivals are listed in the response in order of
        increasing distance. For an explanation of the other fields, please refer to the
        <a href="#festival-search-response">previous section</a>.
    </p>

    <h3 class="separator">Festivals by Artist</h3>
    <p>Find all festivals that an artist will perform at. Past festival performances are <em>not</em> included in the results.</p>

    <h4>Example Request</h4>

    <div class="alert badge-warning spacer">
        To test the URL below you must replace the <code>user</code> and <code>key</code> parameter values
        with your festivals.ie email address and API key.
    </div>

    <ul class="spaced">
        <li>
            Get all festivals where artist with ID 123 will be performing<br/>

            <g:link controller="api" action="festivalsByArtist"
                    params="[id: 123,
                            user: 'REPLACE_ME',
                            key: 'REPLACE_ME']">

                <g:createLink controller="api" action="festivalsByArtist" absolute="true"
                              params="[id: 123,
                                      user: 'REPLACE_ME',
                                      key: 'REPLACE_ME']"/>
            </g:link>
        </li>
    </ul>

    <h4>Example Response</h4>
<pre>
[
    {
        "id": 457,
        "name": "Electric Picnic",
        "start": "2014-08-30",
        "end": "2014-09-01",
        "type": "Headline",
        "url": "http:\/\/www.festivals.ie\/show\/457\/headline\/electric-picnic",
        "website": "http:\/\/electricpicnic.ie\/",
        "address": "Stradbally, Co. Laois, Ireland",
        "latitude": 53.0163,
        "longitude": -7.14741,
        "distance": 0.85819626
    },
    {
        "id": 380,
        "name": "The Irish Derby Festival",
        "start": "2014-06-28",
        "end": "2014-06-30",
        "type": "Sport",
        "url": "http:\/\/www.festivals.ie\/show\/380\/sport\/the-irish-derby-festival",
        "website": "http:\/\/www.curragh.ie",
        "address": "The Curragh Racecourse, The Curragh, Co. Kildare, Ireland",
        "latitude": 53.1618,
        "longitude": -6.80978,
        "distance": 27.772072
    }
]</pre>
    <p class="spacer">
        The example above lists all future festivals that the artist with ID 123 will perform at. All of the fields in
        the response are described in the <a href="#festival-search-response">festival search method's response</a>.
    </p>

    <h3 class="separator">Festival Details</h3>
    <p>Provides detailed information about an individual festival.</p>

    <h4>Example Request</h4>

    <div class="alert badge-warning spacer">
        To test the URL below you must replace the <code>user</code> and <code>key</code> parameter values
        with your festivals.ie email address and API key.
    </div>

    <ul class="spaced">
        <li>
            Get detailed information about the festival with ID 610<br/>

            <g:link controller="api" action="festivalDetail"
                    params="[id: 610,
                            user: 'REPLACE_ME',
                            key: 'REPLACE_ME']">

                <g:createLink controller="api" action="festivalDetail" absolute="true"
                              params="[id: 610,
                                      user: 'REPLACE_ME',
                                      key: 'REPLACE_ME']"/>
            </g:link>
        </li>
    </ul>

    <h4>Example Response</h4>
<pre>
{
    "id": 610,
    "name": "Harmonic at Iveagh Gardens",
    "start": "2014-07-12",
    "end": "2014-07-20",
    "type": "Music",
    "url": "http:\/\/www.festivals.ie\/show\/610\/music\/harmonic-at-iveagh-gardens",
    "website": null,
    "address": "The Iveagh Gardens, Harcourt Street, Dublin 2, Ireland",
    "latitude": 53.3322,
    "longitude": -6.26216,
    "lineupAllowed": true,
    "synopsis": "&lt;div class='synopsis'&gt;Harmonic presents this 8 day mini festival in the scenic Iveagh Gardens in the heart of Dublin city, just off Harcourt Street, home to many well know late night bars and clubs. The lineup is The Tallest Man on Earth, Grizzly Bear and Beach House.&lt;/div&gt;",
    "lineup": [
        {
            "id": 123,
            "name": "Grizzly Bear",
            "headline": true,
            "performanceDate": "2014-07-18",
            "performanceTime": "18:30",
            "image": "http:\/\/festivals.ie\/artist\/getLocalImage?path=2590%2Fimage.png",
            "thumbnail": "http:\/\/festivals.ie\/artist\/getLocalImage?path=2590%2Fthumb.png"
        },
        {
            "id": 423,
            "name": "The Tallest Man on Earth",
            "headline": true,
            "image": "http:\/\/festivals.ie\/artist\/getLocalImage?path=2592%2Fimage.png",
            "thumbnail": "http:\/\/festivals.ie\/artist\/getLocalImage?path=2592%2Fthumb.png"
        },
        {
            "id": 412,
            "name": "Beach House",
            "headline": false,
            "image": "http:\/\/festivals.ie\/artist\/getLocalImage?path=2593%2Fimage.png",
            "thumbnail": "http:\/\/festivals.ie\/artist\/getLocalImage?path=2593%2Fthumb.png"
        }
    ]
}</pre>

    <p>
        The example above shows detailed information about the festival with ID 610. Most of the fields in the response are
        described in the the <a href="#festival-search-response">festival search method's response</a>. The remainder are
        described below.
    </p>

    <dl class="table-display response-fields">
        <dt>synopsis</dt>
        <dd>A HTML fragment that describes the festival. A synopsis is not available for all festivals, so this field may be null.</dd>

        <dt>lineup</dt>
        <dd>Lists the artists that will be appearing at a festival. This list may be empty, because some festivals don't have
        performers (e.g. film festivals), or the lineup of a festival is not yet known.</dd>

        <dt>lineupAllowed</dt>
        <dd>A boolean value that indicates whether this festival may have a lineup. If a lineup is not allowed, the
            <code>lineup</code> list will always be empty. If a lineup is allowed, the <code>lineup</code> list may currently
            be empty because (for example) it hasn't been announced yet.
        </dd>

        <dt>lineup.id</dt>
        <dd>Artist's unique identifier</dd>

        <dt>lineup.name</dt>
        <dd>Artist's name</dd>

        <dt>lineup.headline</dt>
        <dd>Indicates whether an artist is one of the festival's principal performers.</dd>

        <dt>lineup.performanceDate</dt>
        <dd>The date of an artist's appearance at this festival in the format <code>yyyy-mm-dd</code>. This field may be null.</dd>

        <dt>lineup.performanceTime</dt>
        <dd>The date of an artist's appearance at this festival in the format <code>hh:mm</code> (24-hour format). This field may be null.</dd>

        <dt>lineup.image</dt>
        <dd>Artist image file URL. This field may be null.</dd>

        <dt>lineup.thumbnail</dt>
        <dd>Thumbnail artist image file URL. This field may be null.</dd>
    </dl>

</div>
</cache:block>

<asset:javascript src="toc/jquery.tableofcontents.min.js"/>

</body>
