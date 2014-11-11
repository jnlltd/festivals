
<%@ page import="ie.festivals.enums.FestivalType; ie.festivals.map.MapFocalPoint" %>

<li>
    <festival:mapLink location="${location}">${label}</festival:mapLink>
    <ul>
        <g:each in="${types ?: FestivalType.values()}" var="type">
            <li>
                <festival:mapLink location="${location}" types="${type}">${type.toString()}</festival:mapLink>
            </li>
        </g:each>
    </ul>
</li>