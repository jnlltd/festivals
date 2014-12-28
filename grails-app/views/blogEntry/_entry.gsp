<asset:script>
    $(function() {
        // Use JavaScript to customise UI of commentable, because the GSPs thereof cannot be overriden
        // http://stackoverflow.com/questions/8926448/override-grails-plugin-template
        $('#Post').val('Add Comment').addClass('btn');

        <sec:ifNotLoggedIn>
            var loginPrompt = '<div class="alert spacer double-top-spacer">If you wish to post a comment you must <g:link controller="login">login</g:link> first.</div>';
            $('#addComment').html(loginPrompt);
        </sec:ifNotLoggedIn>
        <sec:ifLoggedIn>
            // Show the comment form
            $('#commentBody').addClass('span12');
            $('#addCommentContainer').show();

        </sec:ifLoggedIn>

        // clear the comment form after a comment is posted. This handler will actually be called after every
        // AJAX request, but the only AJAX action on this page is posting comments
        $(document).on("ajaxStop", function() {
            $('#commentBody').val('')
        });
    });
</asset:script>

<div id="entry${entry.id}" class="blogEntry">
	<g:render template="/blogEntry/entryTitle" model="[entry: entry, showShareButtons: true]"/>
	<div class="entryBody">
		${entry.body}
	</div>
</div>

<cache:render template="/common/addThis"/>

<div id="comment" class="entryComments">

    <g:if test="${entry.comments.size()}">
        <h3 class="title sub-title">Comments</h3>
    </g:if>

    <comments:render bean="${entry}" />
</div>
