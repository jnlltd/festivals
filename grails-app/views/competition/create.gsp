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
    <h1 class="hi-fi">Add Competition</h1>

    <div class="double-spacer bright-alert">Please complete all fields marked *</div>

    <g:uploadForm action="save" class="panel-form">

        <g:render template="form"/>

        <div>Enter the possible answers below and select the correct one</div>

        <g:each var="i" in="${(0..<Competition.MIN_ANSWERS)}">
            <div class="spacer top-spacer">
                <g:textField name="answers[${i}].answer"
                             value="${competition.answers ? competition.answers[i].answer : ''}"
                             maxlength="191"
                             class="input-xlarge"/>

                <g:radio name="correctAnswer" value="${i}"
                         checked="${competition.answers?.size() > i ? competition.answers[i].correct : false}"/>
                Correct
            </div>
        </g:each>

        <button type="submit" class="btn">Save</button>
    </g:uploadForm>
</div>

<asset:javascript src="wysihtml5/init.js"/>

</body>