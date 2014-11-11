<g:set value="EEE, MMM d yyyy" var="ticketDateFormat"/>

<h4 class='ticket-heading'>Tickets for ${date.format(ticketDateFormat)}</h4>

<g:if test="${tickets}">
    <ul class='ticket-info'>
        <g:each in="${tickets}">
            <li class='skiddleTicket'>
                <span class='ticketDescription'>${it.description}</span>
                <span class='ticketCost'>${it.cost}</span>
            </li>
        </g:each>
    </ul>
</g:if>

<g:render template="/festival/buyTicketsButton"/>