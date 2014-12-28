<head>
    <asset:stylesheet href="wysihtml5/custom.css"/>
</head>

<body>

<div class="container main">
    <h1 class="hi-fi">Update Festival</h1>
    <div class="bright-alert spacer double-top-spacer">Please complete all fields marked *</div>

    <g:form method="post" action="update" class="panel-form">

        <g:hiddenField name="id" value="${festivalInstance.id}"/>
        <g:hiddenField name="version" value="${festivalInstance.version}"/>

        <g:render template="form"/>
        <button type="submit" class="btn"><i class="icon-ok"></i> Update</button>
    </g:form>
</div>

<asset:javascript src="wysihtml5/init.js"/>

</body>
