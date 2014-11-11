<g:render template="/email/header"/>

<div style="margin: 20px 0;">Hi ${name},</div>

<p>
    You requested that your password be reset. To complete the process, please <a href="${url}">click here</a>
    or copy the following link into your browser's address bar:
</p>

<p style="margin: 15px 0;">${url}</p>
<g:render template="/email/signature"/>