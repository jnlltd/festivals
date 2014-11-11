<%@ page import="ie.festivals.enums.FestivalType" %>
<ul class="tags">
    <g:each in="${FestivalType.values()}" var="type">
        <li class="label">
            <festival:typeSearch type="${type}">${type}</festival:typeSearch>
        </li>
    </g:each>
</ul>