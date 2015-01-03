<g:if test="${changes}">
    <p>
        These artists have been ${removed ? 'removed from' : 'added to'} the
        <festival:show absolute="true"
                       id="${festival.key.id}"
                       name="${festival.key.name}"
                       fragment="bottomArtistList">lineup</festival:show>:
    </p>
    <ul>
        <g:each in="${changes}">
            <li>
                <artist:show absolute="true" id="${it.id}" name="${it.name}">${it.name}</artist:show>

                <g:if test="${it.date}">
                    will <g:if test="${removed}">not </g:if>perform on <g:formatDate date="${it.date}"/>
                </g:if>
            </li>
        </g:each>
    </ul>
</g:if>