<g:if test="${artistInstanceList.size()}">
    <div class="artistsList">
        <g:each in="${artistInstanceList}">
            <g:render template="/artist/artistListEntry" model="${[artist: it]}"/>
        </g:each>
    </div>
</g:if>
<g:else>
    <p class="bright-alert">
        Your search has not returned any results. If the artist's name was spelled wrongly, please correct the name and search again.
    </p>
</g:else>