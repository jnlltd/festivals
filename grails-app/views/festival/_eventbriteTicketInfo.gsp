<table class="top-spacer" style="margin-bottom: 10px;">
    <thead>
        <tr>
            <th>Ticket Type</th>
            <th class="no-wrap">Status</th>
            <th>Price</th>
        </tr>
    </thead>

    <g:each in="${tickets}">
        <tr>
            <td>${it.name}</td>
            <td class="no-wrap">${it.status}</td>
            <td class="no-wrap">${it.free ? 'Free' : it.price}</td>
        </tr>
    </g:each>
</table>

<g:render template="/festival/buyTicketsButton"/>