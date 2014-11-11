<%@ page import="ie.festivals.enums.ArtistAction; ie.festivals.notify.ArtistSubscription" %>
<head>

    <title><content:title>Artist Alert Subscription</content:title></title>

    <sec:ifLoggedIn>
        <r:require module="isotope"/>

        <r:script>
            $(function () {

                SF.container = $('div.artistsList');

                SF.container.isotope({
                    itemSelector: '.artistEntry',
                    layoutMode: 'fitRows'
                });
            });
        </r:script>
    </sec:ifLoggedIn>

    <style type="text/css">
    div.artistsList {
        margin-top: 10px;
        margin-bottom: 10px;
        padding-left: 10px;
    }

    .artistEntry {
        margin-right: 20px;
        width: 145px;
        padding: 10px 0;
    }

    .artistEntry h3 {
        margin: 10px;
    }
    </style>

</head>

<body>
<div class="container main">
    <h1 class="hi-fi">Artist Alerts</h1>

    <ul class="spacer spaced">
        <li>When you subscribe to an artist, you will receive an email
        alert when the artist is added to any festival's lineup.</li>

        <li>If instead you would like to be notified when a particular festival's lineup changes, check out our
        <g:link controller="festival" action="subscriptions">festival alerts</g:link>.</li>
    </ul>

    <sec:ifNotLoggedIn>
        <p class="alert spacer">
            Artist Alerts are only available to <g:link controller="register" action="newUser">registered users</g:link>.
            To subscribe to an artist, <g:link controller="login">login</g:link> then return to this page.
        </p>
    </sec:ifNotLoggedIn>

    <sec:ifLoggedIn>
        <h2 class="hi-fi">Add Artist Alerts</h2>

        <p>Search for artists to subscribe to by entering one or more whole words in their name.</p>
        <g:render template="/artist/searchForm" model="[searchController: 'artistSubscription', searchAction: 'subscriptionSearch']"/>

        <div id="bottomArtistList">
            <h2 class="hi-fi">Your Current Alerts</h2>

            <p id="artistsNotEmpty" style="${artistInstanceList ? '' : 'display: none'}">
                You have subscribed to receive alerts from the following artists. Click on the artists below to see their bio page.
            </p>

            <p id="artistsEmpty" style="${artistInstanceList ? 'display: none' : ''}">
                You have not subscribed to any artist alerts.
            </p>

            <div class="artistsList">

                <g:each in="${artistInstanceList}">

                    <g:render template="/artist/artistListEntry"
                              model="${[artist: it, artistAction: ArtistAction.UNSUBSCRIBE]}"/>
                </g:each>
            </div>
        </div>
    </sec:ifLoggedIn>
</div>
</body>
