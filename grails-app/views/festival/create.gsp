<head>
    <asset:stylesheet href="wysihtml5/custom.css"/>
</head>

<body>

<div class="container main">
    <h1 class="hi-fi">Add Festival</h1>
    <div class="bright-alert spacer double-top-spacer">To add a festival to the site, please complete all fields marked *</div>

    <g:form action="save" class="panel-form">
        <g:render template="form"/>
        <button type="submit" class="btn"><i class="icon-ok"></i> Submit</button>
    </g:form>
</div>

<asset:javascript src="wysihtml5/init.js"/>

</body>