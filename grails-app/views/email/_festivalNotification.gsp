<g:render template="/email/header"/>

<div style="margin: 20px 0;">Hi ${name}!</div>
<div>
    You requested to be notified about changes to some festival lineups. The
    following changes have been made:
</div>

<g:each in="${changes}" var="festival">
    <p style="margin-top: 10px; color: #7F7F7F; font-weight: bold; font-size: 14px;">

        <festival:show base="${baseUrl}" id="${festival.key.id}" name="${festival.key.name}">
            <strong>${festival.key.name}</strong>
        </festival:show>
    </p>

    <g:set var="festivalLineupChange" value="${festival.value}"/>
    <g:render template="/email/festivalChanges" model="[changes: festivalLineupChange.netAddedArtists,
            baseUrl: baseUrl, festival: festival]"/>

    <g:render template="/email/festivalChanges" model="[changes: festivalLineupChange.netDeletedArtists,
            baseUrl: baseUrl, festival: festival, removed: true]"/>
</g:each>

<g:render template="/email/signature"/>

<div style="margin-top: 15px; font-size: 10px;">
    If you no longer wish to be notified about changes to this festival's lineup, remove it from your festival alerts by
    <g:link controller="festival" action="subscriptions" base="${baseUrl}">clicking here</g:link>
    or copying the following link into your browser's address bar:
    <g:createLink controller="festival" action="subscriptions" base="${baseUrl}"/>
</div>