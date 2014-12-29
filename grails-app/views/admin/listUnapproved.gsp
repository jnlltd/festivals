<head>
    <style type="text/css">
        .startCol, .endCol {
            min-width: 90px;
        }

        .freeCol {
            min-width: 40px;
        }
    </style>

    <asset:stylesheet href="tablesorter/style.css"/>
</head>

<body>
<div class="container main">
    <h1 class="hi-fi">Unapproved Festivals</h1>

    <p>The festivals below have not been approved and may require modification.</p>
    <g:render template="/festival/festivalTable"
              model="[showDelete: true, festivalInstanceList: festivalInstanceList]"/>
</div>

<asset:javascript src="tablesorter/init.js"/>

</body>
