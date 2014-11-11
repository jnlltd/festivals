<%@ taglib prefix="janrain" uri="http://janrain4j.googlecode.com/tags" %>

<g:set var="competition" value="${entry.competition}"/>

<head>

    <style type="text/css">
    input[type=radio] {
        margin-top: 0;
        margin-bottom: 2px;
    }

    @media (max-width: 450px) {
        #janrain {
            display: none;
        }
    }

    .link-btn {
        height: 22px;
    }

    .img-polaroid {
        max-width: 98%;
        height: auto;
    }
    </style>

    <title>
        <content:title>${competition.title.encodeAsHTML()}</content:title>
    </title>

</head>

<body>
<div class="container main">
    <h1 class="hi-fi">${competition.title.encodeAsHTML()}</h1>

    <g:if test="${competition.image}">
        <div class="double-spacer center">
            <img class="img-polaroid" src="data:image/png;base64,${competition.image.encodeBase64()}"/>
        </div>
    </g:if>

    <div class="row-fluid">
        <div class="span12 block-text">
            <p>${competition.description}</p>
        </div>
    </div>

    <div class="double-spacer bright-alert">
        This competition ${competition.over ? 'closed' : 'will close'} on <g:formatDate date="${competition.end}"/>.
        <sec:ifAllGranted roles='ROLE_ADMIN'>${entryCount} entries have been submitted.</sec:ifAllGranted>
    </div>

    <g:form action="enter" class="panel-form">

        <h3>${competition.question}</h3>

        <g:each in="${competition.answers}" var="answer">
            <div class="spacer">
                <g:radio name="answer" value="${answer.id}" checked="${answer.id == entry.answer}"/>
                ${answer.answer}
            </div>
        </g:each>

        <f:field bean="entry" property="phoneNumber" label="Phone Number">
            <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="25"/>
            <div class="info-inline">Phone number is optional and will <em>only</em> be used to contact competition winner</div>
        </f:field>

        <div>
            <label for='terms' class="spacer">
                <g:checkBox name="terms" value="${entry.terms}"/>
                I accept the <g:link target="_blank" uri="/terms#competition">Terms &amp; Conditions</g:link>
            </label>
        </div>

        <g:hiddenField name="competition.id" value="${competition.id}"/>

        <g:if test="${competition.over}">
            <div class="double-spacer">
                <g:if test="${winners}">
                    This competition was won by:
                    <ul>
                        <g:each in="${winners}">
                            <li class="top-spacer">
                                <a href="mailto:${it.user.username}">${it.user.name}</a>
                                <g:if test="${it.phoneNumber}">
                                    <i class="icon-phone"></i>${it.phoneNumber}
                                </g:if>
                            </li>
                        </g:each>
                    </ul>
                </g:if>
                <g:else>
                    The winner of this competition has not been chosen yet. Please use the button below to choose a winner.
                </g:else>
            </div>
        </g:if>

        <sec:ifLoggedIn>
            <div>
                <sec:ifAllGranted roles='ROLE_ADMIN'>
                    <g:link class="btn link-btn" action="edit" id="${competition.id}">Edit</g:link>
                    <g:link class="btn link-btn" action="exportCorrectEntrants" id="${competition.id}">Export Correct Entries</g:link>

                    <g:if test="${competition.over}">

                        <g:if test="${winners}">
                            <g:link class="btn link-btn" action="chooseWinner"
                                    onclick="return confirm('Are you sure you want to add another competition winner?')"
                                    id="${competition.id}">
                                Choose Winner
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link class="btn link-btn" action="chooseWinner" id="${competition.id}">
                                Choose Winner
                            </g:link>
                        </g:else>
                    </g:if>
                </sec:ifAllGranted>

                <g:if test="${!competition.over}">
                    <button type="submit" class="btn"><i class="icon-ok"></i> Enter</button>
                </g:if>
            </div>
        </sec:ifLoggedIn>

        <sec:ifNotLoggedIn>
            <div class="alert spacer">
                Only <g:link controller="register" action="newUser">registered users</g:link> can submit competition entries.
                You must <g:link controller="login">login</g:link> before you can enter this competition.
                If you already have an account with Facebook, Twitter, etc. you can use it to
                <janrain:signInLink>login/register socially</janrain:signInLink> with Festivals.ie.
            </div>

            <div id="janrain">
                <janrain:signInEmbedded/>
            </div>

        </sec:ifNotLoggedIn>
    </g:form>

</div>
</body>
