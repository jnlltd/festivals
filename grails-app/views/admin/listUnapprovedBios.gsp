<head>

</head>
<body>
<div class="container main">
    <h1 class="hi-fi">Unapproved Artist Biographies</h1>

    <p>The biography of the artists below have not been approved and may require modification.</p>

    <ul>
        <g:each in="${artistList}">
            <li><g:link action="editBio" id="${it.id}">${it.name}</g:link></li>
        </g:each>
    </ul>
</div>
</body>