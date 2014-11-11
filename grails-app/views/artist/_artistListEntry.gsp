<%@ page import="ie.festivals.enums.Priority; ie.festivals.enums.ArtistAction" %>

<g:set var="performDate" value="${performance?.date ? css.getDateClass(date: performance.date) : ''}"/>
<g:set var="rowId" value="${artist.id ? "artist-${artist.id}-${performDate}" : artist.mbid ? "artist-${artist.mbid}" : ''}"/>

<div id="${rowId}" class="artistEntry thumbnail ${performDate} ${css.getArtistClass(artist: artist)}" 
        ${performer ? 'itemprop="performer"' : ''} itemscope itemtype="http://schema.org/Person">

    <browser:isNotMobile>
        <div class="artist-image-container">
            <artist:show id="${artist.id}" name="${artist.name}">
                <artist:img class="imageFrame shadow" url="${artist.thumbnail}" thumb="true" dimensions="${artist.thumbnailDimensions}" itemprop="image"/>
            </artist:show>
        </div>
    </browser:isNotMobile>

    <h3>
        <artist:show id="${artist.id}" name="${artist.name}" itemprop="url">
            <span itemprop="name">${artist.name}</span>
        </artist:show>
    </h3>

    <div>
        <g:if test="${artistAction == ArtistAction.LINEUP_DELETE}">

            <g:if test="${performance.date}">
                <div class="performDate" title="Artist performance date">
                    <g:set var="perfTimeFormat" value="${performance.hasPerformanceTime ? 'E d MMM, HH:mm' : 'E d MMM'}"/>
                    <g:formatDate date="${performance.date}" format="${perfTimeFormat}"/>
                </div>
            </g:if>

            <sec:ifAllGranted roles="ROLE_ADMIN">
                <div class="btn-group">
                    <g:remoteLink
                            class="btn btn-danger"
                            onSuccess="SF.removeArtist('#bottomArtistList #${rowId}', '${performance.priority.id}')"
                            controller="performance" action="delete"
                            id="${performance.id}">Remove
                    </g:remoteLink>
                </div>
            </sec:ifAllGranted>

            <div class="btn-group" data-toggle="buttons-checkbox">

                <g:render template="/common/artistSubscribeButton" model="[
                        artistInstance: artist,
                        subscribed: artist.id in subscribedArtistIds,
                        hideHelpButton: performance.priority == Priority.MIDLINE,
                        artistDomId: rowId]"/>
            </div>
        </g:if>

        <g:elseif test="${artistAction == ArtistAction.UNSUBSCRIBE}">
            <g:remoteLink
                    class="btn btn-danger"
                    onSuccess="SF.removeArtist('#${rowId}')"
                    controller="artistSubscription" action="delete"
                    id="${artist.id}">Unsubscribe
            </g:remoteLink>
        </g:elseif>

        <g:elseif test="${artistAction == ArtistAction.SUBSCRIBE}">
            %{--
                fieldValue tag must be used with nullable properties to prevent null from being converted to 'null' String
                Don't put the id in the params map because this will convert 2431 to 2,431 which will obviously
                fail we try to bind it back to the property
            --}%
            <g:remoteLink
                    class="btn"
                    before="SF.fadeOut('#${rowId}')"
                    onSuccess="SF.subscribe('#${rowId}', data)"
                    controller="artistSubscription" action="add"
                    id="${artist.id}"
                    params="[name: artist.name,
                            mbid: fieldValue(bean: artist, field: 'mbid'),
                            thumbnail: fieldValue(bean: artist, field: 'thumbnail'),
                            image: fieldValue(bean: artist, field: 'image')]">Subscribe
            </g:remoteLink>
        </g:elseif>

        <g:elseif test="${artistAction == ArtistAction.DELETE}">

            <sec:ifAllGranted roles="ROLE_ADMIN">
                <g:remoteLink
                        class="btn btn-danger"
                        onSuccess="SF.remove('#${rowId}')"
                        controller="artist" action="delete"
                        id="${artist.id}">Delete
                </g:remoteLink>
            </sec:ifAllGranted>

            <g:render template="/common/artistSubscribeButton" model="[
                    artistInstance: artist,
                    subscribed: artist.id in subscribedArtistIds,
                    hideHelpButton: true,
                    artistDomId: rowId]"/>
        </g:elseif>
    </div>
</div>
