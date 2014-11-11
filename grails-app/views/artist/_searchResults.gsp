<g:if test="${artistInstanceList}">
    <ul class="thumbnails last">
        <g:each in="${artistInstanceList}" var="artist" status="i">

            <g:set var="modalId" value="add-performer-${i}"/>

            <li class="span2 artist-result">
                <div class="thumbnail date2">
                    <artist:show id="${artist.id}" name="${artist.name}">
                        <artist:img alt="${artist.name}" url="${artist.thumbnail}" thumb="true"/>
                    </artist:show>

                    <div class="caption">
                        <h4 class="artist-name">
                            <artist:show id="${artist.id}" name="${artist.name}">${artist.name.encodeAsHTML()}</artist:show>
                        </h4>

                        <button class="btn" onclick='$("#${modalId}").modal("show")'>
                            <i class="icon-ok"></i> Add
                        </button>

                        <div id="${modalId}" class="modal hide fade" tabindex="-1" role="dialog" aria-hidden="true">

                            <g:render template="/artist/addPerformer" model="[artist: artist, festival: festival, modalId: modalId]"/>
                        </div>
                    </div>
                </div>
            </li>

        </g:each>
    </ul>
</g:if>
<g:else>
    <p class="alert">
        Your search has not returned any results. If the artist's name was spelled wrongly, please correct the name and search again.
    </p>
</g:else>