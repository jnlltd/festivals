<table class="top-spacer" style="margin-bottom: 10px;">
    <thead>
        <tr>
            <th>Ticket Type</th>
            <th class="no-wrap">Sales End</th>
            <th>Price</th>
        </tr>
    </thead>

    <g:each in="${tickets}">
        <tr>
            <td>${it.name}</td>
            <td class="no-wrap">
                <g:formatDate date="${it.end}"/>
            </td>
            <td class="no-wrap">
                <g:if test="${it.price}">
                    ${it.currency ? "$it.currency " : ''}
                    <g:formatNumber number="${it.price}" maxFractionDigits="2"/>
                </g:if>
            </td>
        </tr>
    </g:each>
</table>

<g:render template="/festival/buyTicketsButton"/>