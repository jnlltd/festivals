<h2 style="margin-bottom: 15px;">Add ${artistName} to ${festival.name}</h2>

<g:form controller="performance" action="addPerformerWithCustomImage" class= "add-performer"
        name="addPerformerForm">

    <g:hiddenField name="festival.id" value="${festival.id}"/>
    <g:hiddenField name="artist.name" value="${artistName}"/>
    <g:hiddenField name="artist.lastFm" value="${false}"/>

    <h3>Image URL</h3>

    <p>
        Enter the URL of the image you wish to use for this artist.
        <a target="_blank" href="https://www.google.com/search?q=${params.artistName.encodeAsURL()}&hl=en&safe=on&tbm=isch">Click here</a>
        to use Google image search to find a suitable image. Leave this field blank if you cannot find an image for this artist.
    </p>

    <g:textField name="image" placeholder="Artist Image URL" style="width: 90%; margin-bottom: 0;"/>

    <g:render template="/artist/priorityAndDateFields" model="[festival: festival]"/>
</g:form>