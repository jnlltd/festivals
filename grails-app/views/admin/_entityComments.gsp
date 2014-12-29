<h4>${title}</h4>

<ul class="commentList">
    <g:each in="${commentable.comments}" var="comment">
        <li id="comment-${comment.id}">
            <g:remoteLink
                    title="Click to permanently delete this comment"
                    controller="commentable"
                    action="delete"
                    onSuccess="SF.remove('#comment-${comment.id}')"
                    id="${comment.id}"><asset:image src="icons/delete.png"/></g:remoteLink>

            ${comment.body.encodeAsHTML()}
        </li>
    </g:each>
</ul>