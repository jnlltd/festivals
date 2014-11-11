<%@ page import="ie.festivals.enums.FestivalType" %>
<head>
    <style type="text/css">

        .artist-name {
            margin-bottom: 0;
        }

        #artist-results-heading {
            margin-bottom: 0;
        }

        table + #artist-results-heading {
            margin-top: 30px;
        }

        .bright-alert {
            text-align: right;
        }

        #no-match a {
            border-bottom: none;
            color: yellow;
            font-weight: bold;
        }

        #no-match a:hover {
            color: #335994;
        }

        .artist-result:last-child {
            margin-bottom: 20px;
        }

        .img-polaroid:hover {
            background-color: #4E87E1;
        }

        ul.tags {
            margin-bottom: 20px;
            margin-left: 0;
        }

        .label {
            font-size: 14px;
            padding: 7px;
        }

        .lo-fi {
            margin-bottom: 0;
        }
    </style>

    <r:require module="tablesorter"/>
</head>

<body>

<div class="container main">
    <g:if test="${searchResults}">
        <div class="bright-alert spacer">
            <strong>${searchResults.total}</strong> ${searchResults.total == 1 ? 'result' : 'results'}
        found for <strong>${params.query?.encodeAsHTML()}</strong>
        </div>
    </g:if>

    <g:if test="${!searchResults}">
        <g:if test="${params.query?.trim()}">
            <div id="no-match"
                 class="bright-alert">Nothing matched your search for <strong>${params.query.encodeAsHTML()}</strong>.

            <g:if test="${suggestedQuery}">
                Did you mean
                <g:link controller="search" action="search"
                        params="[query: suggestedQuery]">${suggestedQuery}</g:link>?

            </g:if>
            </div>
        </g:if>
        <g:else>
            <div class="bright-alert">Please enter some text in the search box before searching again.</div>
        </g:else>

        <h2 class="hi-fi">Search Hints</h2>

        <p>
            Search for an artist by name, or a festival by name, address or category. Click one of the following links to find
            all festivals in a particular category:
        </p>
        <g:render template="/common/categoryLinks"/>

        <div class="row-fluid">
            <div class="span6">
                <h2 class="lo-fi">Artist Alert Search</h2>

                <p class="spacer">
                    If you're searching for an artist because you wish to be alerted whenever they are added to a festival's
                    lineup, try our <g:link controller="artistSubscription"
                                            action="list">Artist Alert Search</g:link> instead.
                    <sec:ifNotLoggedIn>If you are not already a member, you will need to
                        <g:link controller="register">register</g:link> to view this page.
                    </sec:ifNotLoggedIn>
                </p>
            </div>

            <div class="span6">
                <h2 class="lo-fi">Festival Search</h2>

                <p>
                    If you can't find the festival you're searching for, try our
                    <festival:listLink>Festival List</festival:listLink> instead. If you still can't find it, please
                    <g:link controller="festival" action="create">add the festival</g:link> yourself.
                </p>
            </div>
        </div>
    </g:if>

    <g:if test="${searchResults}">
        <g:if test="${festivalResults}">
            <h2 class="hi-fi"><content:pluralize count="${festivalResults.size()}" singular="Festival Result"/></h2>
            <g:render template="/festival/festivalTable"
                      model="[tableCssClass: 'full-width', festivalInstanceList: festivalResults, colSort: '[[2, 1]]']"/>
        </g:if>

        <g:if test="${artistResults}">
            <h2 class="hi-fi" id="artist-results-heading">
                <content:pluralize count="${artistResults.size()}" singular="Artist Result"/>
            </h2>

            <div class="results">
                <g:each in="${artistResults}">
                    <div class="clearfix artist-result">
                        <h3 class="artist-name">
                            <artist:show id="${it.id}" name="${it.name}">${it.name}</artist:show>
                        </h3>

                        <div class="pull-left" style="margin-right: 10px;">
                            <artist:show id="${it.id}" name="${it.name}">
                                <artist:img class="img-polaroid" alt="${it.name}" url="${it.thumbnail}" thumb="true"/>
                            </artist:show>
                        </div>

                        <div class="searchDetails">${it.bioSummary}</div>
                    </div>
                </g:each>
            </div>
        </g:if>
    </g:if>
</div>
</body>
