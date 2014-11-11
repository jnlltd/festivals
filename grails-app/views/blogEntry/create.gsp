<head>
    <meta http-equiv="Content-type" content="text/html; charset=utf-8">
    <meta name="description" content="${g.message(code: 'blog.meta.description')}"/>

    <title><content:title>Write Blog Post</content:title></title>
    <r:require module="wysihtml5"/>

    <style type="text/css">
    a.cancel {
        color: #C14D40;
        margin-left: 10px;
        border-bottom: 1px dotted #C14D40;
    }
    </style>

</head>

<body>

<div class="container main">
    <h1 class="hi-fi">
        <g:if test="${entry.id}">Edit Blog Post</g:if>
        <g:else>Create Blog Post</g:else>
    </h1>

    <g:form class="createEntryForm panel-form" id="createEntryForm" name="createEntryForm"
            url="[controller: 'blog', action: 'publish']">

        <g:if test="${entry.id}">
            <g:hiddenField name="id" value="${entry.id}"/>
        </g:if>

        <f:field bean="entry" property="title" label="Title">
            <g:textField name="entry.title" value="${value}" class="block" maxlength="191"/>
        </f:field>

        <f:field bean="entry" property="body" label="Body">
            <g:textArea name="entry.body" value="${value}" rows="15" cols="1" class="rich block"/>
        </f:field>

    %{--The fields plugin doesn't work with dynamic properties like tags, so we need to render all the markup
    for this field ourselves: https://github.com/robfletcher/grails-fields/issues/112--}%

        <g:render template="/_fields/default/field" model="[
                invalid: entry.errors.hasFieldErrors('tags'),
                property: 'tags',
                required: false,
                label: 'Tags',
                widget: g.textField(name: 'tags', value: entry.tags.join(','), class: 'input-xxlarge',
                        placeholder: 'Separate tags with commas e.g. tag1, tag2'),
                errors: entry.errors.getFieldErrors('tags')]"/>

        <g:submitButton class="btn" name="publish" value="Publish"/>

    %{--Return to home page if creating a new post, return to the post if editing--}%
        <g:if test="${entry.id}">
            <g:link class="cancel" controller="blog" action="showEntry"
                    params="[title: entry.title, author: entry.author]">Cancel</g:link>
        </g:if>
        <g:else>
            <g:link class="cancel" controller="blog" action="list">Cancel</g:link>
        </g:else>
    </g:form>
</div>
</body>