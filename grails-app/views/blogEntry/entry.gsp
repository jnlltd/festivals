<head>
    <meta name="description" content="${g.message(code: 'blog.meta.description')}"/>

    <title><content:title>${entry.title}</content:title></title>
    <r:require module="blog"/>

    <style type="text/css">
        .title h2 {
            font-size: 34px;
            padding: 5px 0;
        }
    </style>

</head>

<body>
<div class="container main">
    <div class="row-fluid">
        <div class="span8 spacer">

            <div id="blogMain">
                <g:render template="/blogEntry/entry" model="[entry: entry]"/>
            </div>
        </div>

        <div class="span4">
            <g:render template="/blogEntry/controlPanel"/>
        </div>
    </div>
</div>
</body>