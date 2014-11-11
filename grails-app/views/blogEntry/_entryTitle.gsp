<div class="title clearfix">
    <h2 class="pull-left lo-fi">
        <g:link controller="blog" action="showEntry" params="[title: entry.title, author: entry.author]">${entry.title}</g:link>
    </h2>

    <sec:ifAllGranted roles='ROLE_ADMIN'>
        <div class="pull-right" id="admin-buttons">
            <g:link class="btn" controller="blog" action="createEntry">New</g:link>
            <g:link class="btn" controller="blog" action="editEntry" id="${entry.id}">Update</g:link>
            <g:link class="btn btn-danger" controller="blog" action="deleteEntry" id="${entry.id}"
                    onclick="return confirm('Are you sure you want to permanently delete this blog entry?');">Delete</g:link>
        </div>
    </sec:ifAllGranted>
</div>

<g:if test="${showShareButtons}">
    <div class="spacer">
        <cache:render template="/common/addThis"/>
    </div>
</g:if>

<div class="muted">
    <div class="blog-glyphs">
        <span>
            <g:link controller="blog" action="${entry.author}"><i class="icon-user"></i> ${entry.author}</g:link>
        </span>
        <span>
            <i class="icon-calendar"></i> <g:formatDate date="${entry.dateCreated}" format="MMM dd, yyyy"/>
        </span>
        <span>
            <i class="icon-comment"></i> <content:pluralize count="${entry.totalComments}" singular="comment"/>
        </span>
    </div>

    <g:if test="${entry.tags}">
        <div class="tags">
            <g:each status="i" var="tag" in="${entry.tags}">
                <g:link controller="blog" action="tagged" id="${tag}"><span class="label">${tag}</span></g:link>
            </g:each>
        </div>
    </g:if>
</div>
