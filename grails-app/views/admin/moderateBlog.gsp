<head>

    <style type="text/css">
    .commentList {
        margin-left: 20px;
    }

    ul {
        list-style: none;
    }

    img:hover {
        background-color: transparent !important;
    }

    h2.banner {
        margin-top: 10px;
    }

    ul.commentList li {
        margin: 10px 0;
    }
    </style>

</head>

<body>
<div class="container main">
    <h1 class="hi-fi">Blog Moderation</h1>

    <p>Use this page to <strong>permanently delete comments and tags</strong> from the blog.</p>

    <h2 class="title double-top-spacer">Blog Comments</h2>
    <g:if test="${entries}">
        <g:each in="${entries}">
            <g:render template="entityComments" model="[commentable: it, title: it.title]"/>
        </g:each>
    </g:if>
    <g:else>
        <p>No blog comments found</p>
    </g:else>

    <h2 class="title double-top-spacer">Blog Tags</h2>
    <ul class="commentList">
        <g:each in="${tags}" var="tag">
            <li id="tag-${tag.id}">
                <g:remoteLink
                        title="Click to permanently delete this tag"
                        action="deleteTag"
                        onSuccess="SF.remove('#tag-${tag.id}')"
                        id="${tag.id}"><r:img uri="/images/icons/delete.png"/></g:remoteLink>

                <span class="label">${tag.name.encodeAsHTML()}</span>
            </li>
        </g:each>
    </ul>
</div>
</body>
