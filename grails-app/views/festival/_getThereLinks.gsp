%{--Generates the getthere.ie links that appear below the Public Transport & Car Sharing form.--}%
%{--See #409 for an explanation of how this works.--}%
<div>
    <span class="event-page-exists" style="display:none;">
        A summary of

        <getthere:summary name="${festival.name}">
            <span class="event-page-public-transport">public transport</span>
            <span class="event-page-both">and</span><span class="event-page-carsharing">car sharing</span> options
        </getthere:summary>

        is available from <a href="http://www.getthere.ie">getthere.ie</a>. You can also try
        <getthere:offer name="${festival.name}">offering</getthere:offer> or
        <getthere:request name="${festival.name}">requesting</getthere:request> a car share for this festival.
    </span>

    <span class="not-event-page-exists">You can also try
        <getthere:offer name="${festival.name}">offering</getthere:offer> or
        <getthere:request name="${festival.name}">requesting</getthere:request> a car share for this festival on
        <a href="http://www.getthere.ie">getthere.ie</a>
    </span>
</div>

<g:javascript base="http://getthere.ie" src="/${festival.name.encodeAsGetThere()}/?event_exists_js" type="js" />
