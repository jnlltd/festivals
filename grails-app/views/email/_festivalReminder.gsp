<g:render template="/email/header"/>

<div style="margin: 20px 0;">Hi ${name},</div>

<div style="margin-bottom: 20px;">
    You requested a reminder about the ${festivalName} festival.

    <g:if test="${daysAhead == 0}">
        <strong> This festival starts today. </strong>
    </g:if>
    <g:elseif test="${daysAhead == 1}">
        <strong> This festival starts tomorrow. </strong>
    </g:elseif>
    <g:else>
        <strong> This festival starts in ${daysAhead} days on <g:formatDate date="${start}"/>. </strong>
    </g:else>

    For more information about this festival, please visit our
    <festival:show absolute="true" id="${festivalId}" name="${festivalName}">${festivalName} festival page</festival:show>.
</div>

<g:render template="/email/signature"/>