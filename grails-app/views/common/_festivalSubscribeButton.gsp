<g:if test="${!festival.finished && festival.hasLineup}">

    <g:if test="${subscribed}">
        <button class="btn subscribe-festival btn-danger">
            <i class="icon-remove"></i> Unsubscribe
        </button>
    </g:if>
    <g:else>
        <button class="btn subscribe-festival" ${sec.ifNotLoggedIn(null, 'disabled="disabled"')}>
            <i class="icon-bell"></i> Subscribe
        </button>
    </g:else>

    <button class="btn last why-subscribe-festival"><i class="icon-question-sign"></i></button>

    <r:script>
        $(function () {
            // Why subscribe to festival popover
            $(".why-subscribe-festival").popover({
                animation: true,
                placement: 'bottom',
                trigger: 'click',
                title: 'Why subscribe to this festival?',
                content: "When you subscribe to a festival, you will receive an email alert when the festival's lineup changes. Only registered users may subscribe to a festival."
            });

            <sec:ifLoggedIn>
                // the event handlers below are bound using the 'one' function because otherwise we would attach multiple
                // handlers to each button

                function subscribe() {
                    $.ajax({
                        url: '${createLink(controller: 'festivalSubscription', action: 'create')}',
                        type: 'POST',
                        data: {id: ${festival.id}},
                        success: function() {

                            var msg = "Subscribed! We'll send you an email when changes are made to this festival's lineup.";
                            $().notificationMsg({message: msg, cssClass: 'MessageBarCommon MessageBarOk'});
                            $('.subscribe-festival').addClass('btn-danger')
                                    .html('<i class="icon-remove"></i> Unsubscribe')
                                    .button('toggle')
                                    .one('click', unsubscribe);
                        }
                    });
                }

                function unsubscribe() {
                    $.ajax({
                        url: '${createLink(controller: 'festivalSubscription', action: 'delete')}',
                        type: 'POST',
                        data: {id: ${festival.id}},
                        success: function() {

                            var msg = "Unsubscribed! You will no longer be informed when changes are made to this festival's lineup.";
                            $().notificationMsg({message: msg, cssClass: 'MessageBarCommon MessageBarOk'});
                            $('.subscribe-festival').removeClass('btn-danger')
                                    .html('<i class="icon-bell"></i> Subscribe')
                                    .button('toggle')
                                    .one('click', subscribe);
                        }
                    });
                }

                // Subscribe to festival button toggle (fade in/out alerts, set button text, toggle the button)
                $(".subscribe-festival").one('click',
                    ${subscribed ? 'unsubscribe' : 'subscribe'}
                );
            </sec:ifLoggedIn>
        });
    </r:script>
</g:if>