<g:if test="${changes}">
    <p>
        These artists have been ${removed ? 'removed from' : 'added to'} the
        <festival:show base="${baseUrl}" id="${festival.key.id}" name="${festival.key.name}"
                       fragment="lineupBookmark">lineup</festival:show>:
    </p>
    <ul>
        <g:each in="${changes}">
            <li>
                <artist:show base="${baseUrl}" id="${it.id}" name="${it.name}">${it.name}</artist:show>

                <g:if test="${it.date}">
                    will <g:if test="${removed}">not </g:if>perform on <g:formatDate date="${it.date}"/>
                </g:if>
            </li>
        </g:each>
    </ul>
</g:if>