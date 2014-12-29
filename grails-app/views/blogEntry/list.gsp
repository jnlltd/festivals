<head>

    <plugin:isAvailable name="feeds">
        <feed:meta kind="rss" version="2.0" controller="blog" action="feed" params="[format: 'rss']"/>
    </plugin:isAvailable>

    <title><content:title>Blog Home</content:title></title>
    <meta name="description" content="${g.message(code: 'blog.meta.description')}"/>

    <asset:stylesheet href="blog.css"/>
</head>

<body>

<div class="container main">
    <div class="row-fluid">
        <div class="span8">
            <div id="blogEntries" class="blogEntries">
                <g:set var="first" value="${entries ? entries[0] : null}"/>
                <div class="firstEntry">
                    <g:if test="${first}">
                        <g:render template="/blogEntry/entry" model="[entry: first]"/>
                    </g:if>
                </div>
                <g:set var="remaining" value="${entries - first}"/>
                <g:if test="${remaining}">
                    <h2 class="hi-fi" style="margin-top: 30px">Recent Posts</h2>
                    <g:render template="/blogEntry/recentEntries" model="[entries: remaining]"/>
                </g:if>
            </div>
        </div>

        <div class="span4">
            <g:render template="/blogEntry/controlPanel"/>
        </div>
    </div>
</div>
</body>