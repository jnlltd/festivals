<head>
    <title><content:title>${artistInstance.name}</content:title></title>
    <asset:stylesheet href="artist.css"/>
</head>

<body>

<!-- Main Content-->
<div class="container main" itemscope itemtype="http://schema.org/Person">

    <!-- Artist Title-->
    <div class="title clearfix">
        <h1 class="pull-left"><span itemprop="name">${artistInstance.name.encodeAsHTML()}</span></h1>

        <div class="pull-right title-btn-wrapper">
            <div class="btn-group pull-left" data-toggle="buttons-checkbox">
                <g:render template="/common/artistSubscribeButton" model="[subscribed: subscribed]"/>
            </div>

            <div class="social-links pull-right">
                <cache:render template="/common/addThis"/>
            </div>
        </div>
    </div>

    <div class="row-fluid">

        <!-- Performances -->
        <div class="span8 spacer">

            <div class="clearfix">
                <h2 class="pull-left">Performances</h2>
            </div>
            <g:if test="${performances}">

                <table class="table table-striped last show1-2 tablesorter {sortlist: [[1, 1]]}">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th class="{sorter: 'formattedDate'}">Start</th>
                            <th class="{sorter: 'formattedDate'} hide-narrow">End</th>
                            <th class="hide-narrow">Country</th>
                        </tr>
                    </thead>
                    <tbody>

                    %{--By default we hide all finished festivals except for the most recent 3--}%
                    <g:set var="finishedFestivals" value="${0}"/>
                    <g:set var="maxFinishedFestivals" value="${3}"/>

                    <g:each in="${performances}" var="festival" status="i">

                        <g:set var="festivalCssClass"
                               value="${festival.finished ? ++finishedFestivals > 3 ? 'over hideable hide' : 'over' : ''}"/>

                        <tr class="${festivalCssClass}">
                            <td class="festivalName">
                                <festival:show festival="${festival}"/>
                            </td>
                            <td><g:formatDate date="${festival.start}"/></td>
                            <td class="hide-narrow"><g:formatDate date="${festival.end}"/></td>
                            <td class="hide-narrow">${festival.countryName}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>

                <g:if test="${finishedFestivals > maxFinishedFestivals}">
                    <div class="top-spacer">
                        <button title="Only the ${maxFinishedFestivals} most recent past performances are shown by default. Click here to see them all."
                                class="btn performances-toggle btn-small" type="button" data-toggle="button">
                            <i class="icon-plus-sign"></i> Show All Past Performances
                        </button>

                        <button title="Click here to hide all past performances except the most recent ${maxFinishedFestivals}."
                                class="btn performances-toggle btn-small hide" type="button" data-toggle="button">
                            <i class="icon-minus-sign"></i> Recent Performances Only
                        </button>

                    </div>

                    <asset:script>
                        $('.performances-toggle').click(function () {
                            $('.hideable, .performances-toggle').toggleClass('hide');
                        });
                    </asset:script>
                </g:if>
            </g:if>
            <g:else>
                <div class="spacer alert">No festival performances were found for ${artistInstance.name.encodeAsHTML()}</div>
            </g:else>
        </div>

        <!-- Artist Image-->
        <div class="span4 spacer center">
            <artist:img class="img-rounded" alt="${artistInstance.name.encodeAsHTML()}" url="${artistInstance.image}"
                        itemprop="image"/>
        </div>
    </div>

    <sec:ifAllGranted roles='ROLE_ADMIN'>
        <div class="row-fluid">

            <div class="span12 panel">
                <h3 style="margin-top: 0">Change Artist Image</h3>

                <p id="googleImageSearch">
                    <a target="_blank"
                       href="https://www.google.com/search?q=${artistInstance.name.encodeAsURL()}&hl=en&safe=on&tbm=isch">Click here</a>
                    to use Google image search to find a new image.
                </p>
                <g:form controller="artist" action="updateImage">
                    <g:textField name="image" placeholder="New Image URL" class="input-xlarge"/>
                    <button class="btn btn-inline" type="submit">
                        <i class="icon-picture"></i> Update Image
                    </button>
                    <g:hiddenField name="artistId" value="${artistInstance.id}"/>
                </g:form>
            </div>
        </div>
    </sec:ifAllGranted>

    <g:set var="bio" value="${artistInstance.bioFull ?: artistInstance.bioSummary}"/>

    <g:if test="${bio}">
        <div class="row-fluid">

            <!-- Bio -->
            <div class="span12 spacer" id="bio">
                <div class="title clearfix">
                    <h2 class="pull-left">Biography</h2>
                </div>

                <div class="split-column">${bio}</div>
            </div>
        </div>
    </g:if>

    <div class="row-fluid">

        <g:if test="${tracks}">
            <!-- Tracks -->
            <div class="span4 clearfix spacer">
                <div class="title clearfix">
                    <h2 class="pull-left">Top Tracks</h2>
                </div>
                <ul class="unstyled last panel">

                    <g:each in="${tracks}">
                        <li class="spacer">
                            <a href="${it.url}" target="_blank">
                                <i class="icon-music"></i> <strong>${it.name.encodeAsHTML()}</strong>
                                <artist:track duration="${it.duration}"/>
                            </a>
                        </li>
                    </g:each>
                </ul>
            </div>
        </g:if>

        <g:if test="${albums}">

            <!-- Albums -->
            <div class="span8">
                <div class="title clearfix">
                    <h2 class="pull-left">Top Albums</h2>
                </div>

                <div class="row-fluid">
                    <ul class="thumbnails last">

                        <g:each in="${albums}">
                            <li class="span3">
                                <div class="thumbnail">
                                    <a href="${it.url}" target="_blank">
                                        <img src="${it.largeImageUrl}" alt="${it.name.encodeAsHTML()}"/>
                                    </a>

                                    <div class="caption">
                                        <h4>
                                            <a href="${it.url}" target="_blank">${it.name.encodeAsHTML()}</a>
                                        </h4>
                                    </div>
                                </div>
                            </li>
                        </g:each>
                    </ul>
                </div>
            </div>
        </g:if>
    </div>

    <g:if test="${artistInstance.videoEmbedCode}">

    %{--make the muzu player video responsive: https://github.com/davatron5000/FitVids.js--}%
        <asset:script>
            $(function () {
                $("#video-container").fitVids({customSelector: "iframe[src^='//player.muzu.tv']"});
            });
        </asset:script>

        <div class="row-fluid" id="video-container">
            <div class="span12 spacer">
                <div class="title clearfix">
                    <h2 class="pull-left">${artistInstance.name.encodeAsHTML()} Videos</h2>
                </div>
                ${artistInstance.videoEmbedCode}
            </div>
        </div>
    </g:if>

    <g:if test="${artistInstance.lastFm}">
        <div class="row-fluid">
            <div class="span12" id="credit"><small>Artist data provided by</small>
                <a target="_blank" href="http://www.last.fm">
                    <asset:image src="lastfm.png" alt="Last.fm" title="Last.fm"/>
                </a>
            </div>
        </div>
    </g:if>
</div>

<asset:javascript src="jquery.fitvids.js"/>

</body>