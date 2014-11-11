<h2>Add ${artist.name} to ${festival.name}</h2>

<g:formRemote
        class="add-performer"
        name="addPerformerForm"
        url="[controller: 'performance', action: 'add']"
        onSuccess="SF.addArtistToLineup('#${modalId}', data)">

    <g:hiddenField name="festival.id" value="${festival.id}"/>


    %{--we can remove the if tag when this bug is fixed http://jira.grails.org/browse/GRAILS-11218--}%
    <g:if test="${artist.id}">
        <g:hiddenField name="artist.id" value="${artist.id}"/>
    </g:if>

    <g:hiddenField name="artist.name" value="${artist.name}"/>
    <g:hiddenField name="artist.mbid" value="${artist.mbid}"/>
    <g:hiddenField name="artist.thumbnail" value="${artist.thumbnail}"/>
    <g:hiddenField name="artist.image" value="${artist.image}"/>

    <g:render template="/artist/priorityAndDateFields" model="[festival: festival]"/>
</g:formRemote>