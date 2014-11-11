<g:render template="/email/header"/>

<div style="margin: 20px 0;">Hi ${name},</div>
<p>
    Thanks for registering with Festivals.ie. To complete the registration process, please <a href="${url}">click here</a>
    or copy the following link into your browser's address bar:
</p>
<p style="margin-bottom: 10px;">${url}</p>

<p style="margin-bottom: 10px;">
    This account confirmation link will expire after a week. If you do not click it within this time your unconfirmed
    account will be deleted.
</p>
<g:render template="/email/signature"/>