<%@ page import="ie.festivals.competition.Competition" %>
<head>

    <asset:stylesheet href="wysihtml5/custom.css"/>

    <style type="text/css">
        input[type=radio] {
            margin-top: 0;
            margin-bottom: 2px;
        }
    </style>
</head>

<body>
<div class="container main">
    <h1 class="hi-fi">Update Competition</h1>

    <div class="double-spacer bright-alert">Please complete all fields marked *</div>

    <g:uploadForm action="update" class="panel-form">

        <g:hiddenField name="id" value="${competition.id}"/>
        <g:hiddenField name="version" value="${competition.version}"/>

        <g:render template="form"/>
            <div>Enter the possible answers below and select the correct one</div>

            <g:each status="i" var="answer" in="${competition.answers}">
                <div class="spacer top-spacer">
                    <g:textField name="answers[${i}].answer" value="${answer.answer}" maxlength="191"/>
                    <g:radio name="correctAnswer" value="${i}" checked="${answer.correct}"/>
                    Correct
                </div>
            </g:each>

            <button type="submit" class="btn">Update</button>
    </g:uploadForm>
</div>

<asset:javascript src="wysihtml5/init.js"/>

</body>