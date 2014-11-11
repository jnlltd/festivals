<g:if test="${festival.videoEmbedCode}">
    <h2 class="banner bgImage video" style="margin-top: 10px;">${festival.name.encodeAsHTML()} Video</h2>
    <div id="video">${festival.videoEmbedCode}</div>
</g:if>

<h2 class="banner bgImage comments" style="margin-top: 10px;">Comments</h2>

<div id="comment" class="entryComments">
    <comments:render bean="${festival}"/>
</div>