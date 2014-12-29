<%@ page import="ie.festivals.enums.ArtistAction; ie.festivals.Artist" %>
<head>
    <style type="text/css">
        .initial, .step, .currentStep, .prevLink, .nextLink {
            padding: 0 5px;
            font-size: 180%;
            font-weight: bold;
        }

        #initials, #pages {
            line-height: 220%;
            text-align: center;
        }

        .initial:hover, .selected, .step:hover, .currentStep, .prevLink:hover, .nextLink:hover {
            background-color: #E5E5E5;
            border-radius: 4px 4px 4px 4px;
            color: #4e87e1;
        }

        a.delete {
            color: #C14D40;
        }

        .artistEntry {
            margin: 10px;
            margin-left: 0;
            width: 145px;
            padding: 10px 0;
        }        

        .artistEntry h3 {
            margin: 10px;
        }

        .btn.subscribe-artist {
            margin-top: 10px;
        }

        #artistsList {
            margin: 0 auto;
        }
    </style>

    <asset:stylesheet href="isotope/isotope.css"/>

    <asset:script>
        $(function() {
            SF.layoutImageContent('#artistsList', 'div.artistEntry');
        });
    </asset:script>
</head>

<body>
<div class="container main">
    
    <p class="spacer">
        Click on the first letter of the artist's name, or use the <g:link action="list">Other</g:link> link to view
        artists with names that begin with a digit, accented character, symbol, etc. You can also use the search box
        above to find an artist by name.
    </p>

    <div id="initials">
        <g:each in="${'A'..'Z'}">
            <g:link action="list" params="[name: it]">
                <span class="initial ${params.name == it ? 'selected' : ''}">${it}</span>
            </g:link>
        </g:each>

        <g:link action="list">
            <span class="initial ${params.name == null ? 'selected' : ''}">Other</span>
        </g:link>
    </div>

    <div id="bottomArtistList">
        <h1 class="hi-fi double-top-spacer">
            <g:if test="${params.name}">
                Artists Named "${params.name}…"
            </g:if>
            <g:else>
                Other Artists
            </g:else>
        </h1>

        <div id="pages" class="double-spacer">
            <g:paginate action="list" total="${total}" params="[name: params.name]" prev="&lt;" next="&gt;"/>
        </div>

        <g:if test="${artistInstanceList}">
            <p>
                Click on an artist below to see their bio page.
                <g:if test="${total > params.max}">
                    Use the pagination controls above to see another page of artists in this category.
                </g:if>
            </p>
        </g:if>
        <g:else>
            <div class="alert badge-warning spacer">No matching artists found</div>
        </g:else>

        <div id="artistsList">
            <g:each in="${artistInstanceList}">
                <g:render template="/artist/artistListEntry" model="${[
                        artist: it,
                        subscribedArtistIds: subscribedArtistIds,
                        artistAction: ArtistAction.DELETE]}"/>
            </g:each>
        </div>
    </div>
</div>

<asset:javascript src="isotope/isotope.centered.js"/>

</body>
