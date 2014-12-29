<%@ page import="ie.festivals.Festival" %>
<head>
    <style type="text/css">

    .color-key {
        display: none !important;
    }

    #table-container {
        margin-top: 7px;
    }
    </style>

    <asset:stylesheet href="tablesorter/style.css"/>
</head>

<body>

<div class="container main">

    <h1 class="hi-fi">${heading ?: "$command.location.displayName Festival List"}</h1>

    <p>Use the filter to change the festivals shown in the table.</p>

    <div class="row-fluid">
        <div class="span9" id="table-container">
            <g:render template="festivalTable" model="[colSort: '[[2, 1]]', tableCssClass: 'full-width']"/>
        </div>

        <div class="span3">
            <g:render template="festivalFilter"
                      model="[filterUrl: [controller: 'festival', action: 'list'], command: command]"/>
        </div>
    </div>
</div>

<asset:javascript src="tablesorter/init.js"/>

</body>
