<head>
    <r:require module="wysihtml5"/>

</head>

<body>
<div class="container main">

    <h1 class="hi-fi">Moderate Review</h1>

    <p>
        Tick the Publish box if the review is ready to be displayed on the site, or leave it unticked if editing
        is incomplete. Use the delete button to permanently remove the review.
    </p>

    <g:form class="review spacer panel-form" controller="review">
        <g:hiddenField name="id" value="${review.id}"/>
        <g:hiddenField name="version" value="${review.version}"/>

        <f:field bean="review" property="title"/>
        <f:field bean="review" property="body" label="Review">
            <g:textArea name="${property}" value="${value}" rows="12" cols="1" class="block rich"/>
        </f:field>

        <f:field bean="review" property="approved" label="Publish" value="${review.approved}"/>

        <g:actionSubmit class="btn" value="Update" action="update"/>
        <g:actionSubmit class="btn btn-danger" value="Delete" action="delete"/>
    </g:form>

</div>
</body>
