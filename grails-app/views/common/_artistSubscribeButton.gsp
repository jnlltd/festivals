<g:if test="${subscribed}">
    <button class="btn subscribe-artist btn-danger">
        <i class="icon-remove"></i> Unsubscribe
    </button>
</g:if>
<g:else>
    <button class="btn subscribe-artist" ${sec.ifNotLoggedIn(null, 'disabled="disabled"')}>
        <i class="icon-bell"></i> Subscribe
    </button>
</g:else>


<g:if test="${!hideHelpButton}">
    <button class="btn last why-subscribe-artist"><i class="icon-question-sign"></i></button>

    <asset:script>
    $(function () {
        // Why subscribe to artist popover
        $(".why-subscribe-artist").popover({
            animation: true,
            placement: 'bottom',
            trigger: 'click',
            title: 'Why subscribe to this artist?',
            content: "When you subscribe to an artist, you will receive an email alert when the artist is added to any festival's lineup. Only registered users may subscribe to an artist."
        });
    });
    </asset:script>
</g:if>

<sec:ifLoggedIn>
    <asset:script>
    $(function () {

        function subscribe(artistDomId) {
            return function() {
                $.ajax({
                    url: '${createLink(controller: 'artistSubscription', action: 'add')}',
                    type: 'POST',
                    data: {id: ${artistInstance.id}},
                    success: function() {

                        var msg = "Subscribed! We'll send you an email when ${artistInstance.name.encodeAsHTML()} is added to a festival's lineup.";
                        $().notificationMsg({message: msg, cssClass: 'MessageBarCommon MessageBarOk'});

                        var buttonSelector = getButtonSelector(artistDomId);
                        $(buttonSelector).addClass('btn-danger')
                                .html('<i class="icon-remove"></i> Unsubscribe')
                                .button('toggle')
                                .one('click', unsubscribe(artistDomId));
                    }
                });
            };
        }

        function getButtonSelector(artistDomId) {
            return artistDomId ? '#' + artistDomId + ' .subscribe-artist' : '.subscribe-artist';
        }

        function unsubscribe(artistDomId) {
            return function() {
                $.ajax({
                    url: '${createLink(controller: 'artistSubscription', action: 'delete')}',
                    type: 'POST',
                    data: {id: ${artistInstance.id}},
                    success: function() {

                        var msg = "Unsubscribed! You will no longer be notified when ${artistInstance.name.encodeAsHTML()} is added to a festival's lineup.";
                        $().notificationMsg({message: msg, cssClass: 'MessageBarCommon MessageBarOk'});

                        var buttonSelector = getButtonSelector(artistDomId);
                        $(buttonSelector).removeClass('btn-danger')
                                .html('<i class="icon-bell"></i> Subscribe')
                                .button('toggle')
                                .one('click', subscribe(artistDomId));
                    }
                });
            };
        }

        // set button handler initially to either subscribe or unsubscribe
        var artistDomId = "${artistDomId}";
        $(getButtonSelector(artistDomId)).one('click', ${subscribed} ? unsubscribe(artistDomId) : subscribe(artistDomId));
    });
    </asset:script>
</sec:ifLoggedIn>