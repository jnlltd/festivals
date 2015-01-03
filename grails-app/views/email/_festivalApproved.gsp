<g:render template="/email/header"/>

<div style="margin: 20px 0;">Hi ${name},</div>

<div style="margin-bottom: 20px;">
    Thanks for adding ${festivalName} to our site. <festival:show absolute="true" id="${festivalId}" name="${festivalName}">Click here</festival:show>
    to view this festival on festivals.ie.
</div>

<g:render template="/email/signature"/>