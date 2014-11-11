<div class="spacer">
    <plugin:isAvailable name="searchable">
        <g:form class="form-inline" url="[controller: 'blog', action: 'search']">
            <g:textField name="q" class="span8" placeholder="Find Blog Posts"/>

            <button type="submit" class="btn"><i class="icon-search"></i> Blog</button>
        </g:form>
    </plugin:isAvailable>

    <plugin:isAvailable name="feeds">
        <div class="blogButton">
            <g:link controller="blog" action="feed" params="[format: 'rss']">
                <i class="icon-rss"></i> Subscribe to Blog (RSS)
            </g:link>
        </div>
    </plugin:isAvailable>

    <div class="blogButton">
        <g:link controller="blog">
            <i class="icon-home"></i> Blog Home
        </g:link>
    </div>


    <div class="well">
        <h3 class="well-title">Posts by Date</h3>
        <ul>
            <blog:recentEntryLinks>
                <li>
                    <g:link controller="blog" action="showEntry"
                            params="[title: it.title, author: it.author]">${it.title}</g:link>
                    <span class="blogControlPanelDetail"><g:formatDate date="${it.publishedOn}"/></span>
                </li>
            </blog:recentEntryLinks>
        </ul>
    </div>

    <g:if test="${tagNames}">
        <div class="well" id="tag-well">
            <h3 class="well-title">Posts by Tag</h3>

            <ul class="tags">
                <g:each var="tag" in="${tagNames}">

                    <li class="label">
                        <g:link controller="blog" action="byTag" params="[tag: tag]">${tag}</g:link>
                    </li>
                </g:each>
            </ul>
        </div>
    </g:if>

    <div class="well">
        <h3 class="well-title">Festival Links</h3>
        <ul class="festivalLinks">
            <li>
                <a href="http://ayearoffestivalsinireland.com" target="_blank"
                   rel="nofollow">A Year of Festivals in Ireland</a>
                <span class="blogControlPanelDetail">a quest to attend 3 festivals in Ireland, every week, for a year!</span>
            </li>

            <li>
                <a href="http://musicfestivals.ie" target="_blank" rel="nofollow">Music Festivals.ie</a>
                <span class="blogControlPanelDetail">Ireland's leading agency for booking musicians & festival entertainers</span>
            </li>

            <li>
                <a href="http://GotIreland.com"target="_blank" rel="nofollow">Got Ireland</a>
                <span class="blogControlPanelDetail">Irish Travel and Culture Blog</span>
            </li>

            <li>
                <a href="http://musicscene.ie/" target="_blank" rel="nofollow">MusicScene.ie</a>
                <span class="blogControlPanelDetail">for all the latest from the Irish music scene</span>
            </li>
        </ul>
    </div>
</div>
