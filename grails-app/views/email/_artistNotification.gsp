<g:render template="/email/header"/>

<div style="margin: 20px 0;">Hi ${name},</div>

<div>
    The following festival lineup changes have recently been made
    <ul>
        <g:each in="${added}">
            <g:render template="/email/artistNotificationEntry" model="[baseUrl: baseUrl, performance: it]"/>
        </g:each>

        <g:each in="${deleted}">
            <g:render template="/email/artistNotificationEntry" model="[baseUrl: baseUrl, performance: it, removed: true]"/>
        </g:each>
    </ul>
</div>

<g:render template="/email/signature"/>

<div style="margin-top: 20px; font-size: 10px;">
    If you no longer wish to be notified about this artist, remove them from your artist alerts by
    <g:link controller="artistSubscription" action="list" base="${baseUrl}">clicking here</g:link>
    or copying the following link into your browser's address bar:
    <g:createLink controller="artistSubscription" action="list" base="${baseUrl}"/>
</div>