<head>

</head>

<body>
<div class="container main">

    <h1 class="hi-fi">Moderate Festival Reviews</h1>

    <h2>Unpublished Reviews</h2>

    <p>
        The reviews of the festivals below have not been approved and may require modification. Use
        the links below to edit or delete these review.
    </p>

    <ul class="spaced spacer">
        <g:each in="${unapproved}">
            <li>
                <g:link action="moderateReview" id="${it.id}">Review of ${it.festival.encodeAsHTML()}</g:link>
                by ${it.author.username.encodeAsHTML()}
            </li>
        </g:each>
    </ul>

    <h2>Published Reviews</h2>

    <p>
        The reviews of the festivals below are currently published. Use the links below to edit or
        delete these review.
    </p>

    <ul class="spaced">
        <g:each in="${approved}">
            <li>
                <g:link action="moderateReview" id="${it.id}">Review of ${it.festival.encodeAsHTML()}</g:link>
                by ${it.author.username.encodeAsHTML()}
            </li>
        </g:each>
    </ul>
</div>
</body>