%{-- tablesorter plugin uses sortlist to determine initial sort order http://tablesorter.com/docs/example-meta-sort-list.html --}%

<g:set var="df" value="${grailsApplication.config.festival.dateFormat}"/>
<g:set var="sortSpec" value="${colSort ? "{sortlist: $colSort}" : ''}"/>

<table ${tableId ? "id='$tableId'" : ""} class="table table-striped festival-list spacer ${tableCssClass ?: ''} tablesorter ${sortSpec}">
    <thead>
    <tr>
        <g:if test="${tableTitle}">
            <th class="multiCol" colspan="2">${tableTitle}</th>
        </g:if>
        <g:else>
            <th class="nameCol">Name</th>
            <th class="typeCol">Type</th>

            <g:if test="${showRemoveFavorite}" class="favoriteCol">
                <th class="removeFavoriteCol"></th>
            </g:if>

            <th class="startCol {sorter: 'formattedDate'}">Start</th>
            <th class="endCol {sorter: 'formattedDate'}">End</th>
            <th class="addressCol">Location</th>
            <th class="countryCol">Country</th>
            <th class="freeCol">Free</th>

            <g:if test="${subscriptionIds != null}">
                <th class="subCol">Status</th>
            </g:if>

            <g:if test="${showDelete}">
                <th></th>
            </g:if>
        </g:else>
    </tr>
    </thead>
    <tbody>
    <g:each in="${festivalInstanceList}" var="festivalInstance">

        <g:set var="festivalCssClass" value="${festivalInstance.finished ? 'over' : ''}"/>

        %{--the same festival could appear in several tables on a single page, so we may need a prefix to
        ensure the table row IDs are unique--}%
        <g:set var="rowId" value='${rowIdPrefix ? "${rowIdPrefix}-${festivalInstance.id}" : festivalInstance.id}'/>

        <tr ${noRowIds ? '' : "id='$rowId'"} itemscope itemtype="http://schema.org/Festival" class="${festivalCssClass}">

            <td class="nameCol">
                <festival:show festival="${festivalInstance}" useSchemaDotOrgProps="true"/>
            </td>

            <td class="typeCol">${festivalInstance.type}</td>

            <g:if test="${showRemoveFavorite}">
                <td class="removeFavoriteCol">
                    <g:remoteLink
                            class="danger"
                            onSuccess="SF.remove('#${rowId}')"
                            controller="favoriteFestival"
                            action="delete"
                            title="Remove this festival from your favourites"
                            id="${festivalInstance.id}"><strong>Remove</strong>
                    </g:remoteLink>
                </td>
            </g:if>

            <td class="startCol">
                <meta itemprop="startDate" content="${festivalInstance.start.format(df)}">
                <g:formatDate date="${festivalInstance.start}"/>
            </td>
            
            <td class="endCol">
                <meta itemprop="endDate" content="${festivalInstance.end.format(df)}">
                <g:formatDate date="${festivalInstance.end}"/>
            </td>

            <td class="addressCol" itemprop="location">${festivalInstance.city.encodeAsHTML()}</td>
            <td class="countryCol">${festivalInstance.countryName.encodeAsHTML()}</td>

            <td class="freeCol">${festivalInstance.freeEntry ? 'Yes' : 'No'}</td>

            <g:if test="${subscriptionIds != null}">

                <g:set var="subscribed" value="${festivalInstance.id in subscriptionIds}"/>

                %{-- use a custom data attribute to store the subscription status http://ejohn.org/blog/html-5-data-attributes --}%
                <td class="subCol" data-subscribed="${subscribed}">

                    <span class="festivalSubscribe ${subscribed ? 'hide' : ''}">

                        <g:remoteLink
                                onSuccess="SF.festivalSubscriptionChanged(${festivalInstance.id}, '${festivalInstance.name}', true)"
                                controller="festivalSubscription"
                                action="create"
                                class="hoverable"
                                title="click to subscribe"
                                id="${festivalInstance.id}"><i class="icon-remove hoverable"></i> <strong>Not Subscribed</strong>
                        </g:remoteLink>
                    </span>

                    <span class="festivalUnsubscribe ${subscribed ? '' : 'hide'}">

                        <g:remoteLink
                                onSuccess="SF.festivalSubscriptionChanged(${festivalInstance.id}, '${festivalInstance.name}', false)"
                                controller="festivalSubscription"
                                action="delete"
                                class="no-wrap hoverable"
                                title="click to unsubscribe"
                                id="${festivalInstance.id}"><i class="icon-ok hoverable"></i> <strong>Subscribed</strong>
                        </g:remoteLink>
                    </span>
                </td>
            </g:if>

            <g:if test="${showDelete}">
                <td>
                    <g:remoteLink
                            class="danger"
                            onSuccess="SF.remove('#${rowId}')"
                            controller="festival"
                            action="delete"
                            id="${festivalInstance.id}"><strong>Delete</strong>
                    </g:remoteLink>
                </td>
            </g:if>
        </tr>
    </g:each>

    %{--an empty table causes an error with the tablesorter plugin, so append an empty row if we've no data--}%
    <g:if test="${!festivalInstanceList}">
        <tr>
            <td class="nameCol"></td>
            <td class="typeCol"></td>
            <g:if test="${showRemoveFavorite}">
                <td class="removeFavoriteCol"></td>
            </g:if>
            <td class="startCol"></td>
            <td class="endCol"></td>
            <td class="addressCol"></td>
            <td class="countryCol"></td>
            <g:if test="${subscriptionIds != null}">
                <td class="subCol"></td>
            </g:if>
            <g:if test="${showDelete}">
                <td></td>
            </g:if>
        </tr>
    </g:if>
    </tbody>
</table>