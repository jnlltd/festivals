<g:render template="/artist/searchResults"/>

<g:if test="${params.artistName?.trim()}">
    <div class="spacer">
        If you didn't find the artist you're looking for and you're sure the their name is spelled correctly,
        you may add the artist by

        <a href="javascript:void(0)" onclick='$("#add-artist-without-image").modal("show")'>choosing an image</a> for them.

        <div id="add-artist-without-image" class="modal hide fade" tabindex="-1" role="dialog" aria-hidden="true">
            <g:render template="/artist/addPerformerFromImageSearch" model="[festival: festival, artistName: params.artistName]"/>
        </div>
    </div>

</g:if>