<g:if test="${isFavorite}">
    <button class="btn favorite-festival btn-danger">
        <i class="icon-remove"></i> Unfavourite
    </button>
</g:if>
<g:else>
    <button class="btn favorite-festival" ${sec.ifNotLoggedIn(null, 'disabled="disabled"')}>
        <i class="icon-star"></i> Favourite
    </button>
</g:else>

<button class="btn last why-favorite-festival"><i class="icon-question-sign"></i></button>

<r:script>
    $(function () {
        // Why favourite festival popover
        $(".why-favorite-festival").popover({
            animation: true,
            placement: 'bottom',
            trigger: 'click',
            title: 'Why favourite this festival?',
            content: "When you favourite a festival it will be added to a list of your favourite festivals that is displayed on the homepage. Only registered users can save their favourite festivals."
        });

        <sec:ifLoggedIn>
            // the event handlers below are bound using the 'one' function because otherwise we would attach multiple
            // handlers to each button

            function addFavorite() {
                $.ajax({
                    url: '${createLink(controller: 'favoriteFestival', action: 'create')}',
                    type: 'POST',
                    data: {id: ${festival.id}},
                    success: function() {

                        var msg = "This festival has been added to your list of favourite festivals shown on the homepage.";
                        $().notificationMsg({message: msg, cssClass: 'MessageBarCommon MessageBarOk'});
                        $('.favorite-festival').addClass('btn-danger')
                                .html('<i class="icon-remove"></i> Unfavourite')
                                .button('toggle')
                                .one('click', removeFavorite);
                    }
                });
            }

            function removeFavorite() {
                $.ajax({
                    url: '${createLink(controller: 'favoriteFestival', action: 'delete')}',
                    type: 'POST',
                    data: {id: ${festival.id}},
                    success: function() {

                        var msg = "This festival has been removed from your list of favourite festivals shown on the homepage";
                        $().notificationMsg({message: msg, cssClass: 'MessageBarCommon MessageBarOk'});
                        $('.favorite-festival').removeClass('btn-danger')
                                .html('<i class="icon-star"></i> Favourite')
                                .button('toggle')
                                .one('click', addFavorite);
                    }
                });
            }

            // Favorite festival button toggle (fade in/out alerts, set button text, toggle the button)
            $(".favorite-festival").one('click',
                ${isFavorite ? 'removeFavorite' : 'addFavorite'}
            );
        </sec:ifLoggedIn>
    });
</r:script>