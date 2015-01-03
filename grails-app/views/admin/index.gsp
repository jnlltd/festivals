<%@ page import="ie.festivals.job.*" %>
<head>
</head>

<body>
<div class="container main">

    <ul class="spaced">
        <li><g:link controller='user' action='registrationTimeline'>Registered Users Timeline</g:link></li>
        <li><g:link controller='user' action='list'>List Users</g:link></li>

        <li>
            <g:link action='executeJob' params="[className: NotificationJob.name, infoMsgKey: 'admin.notifications']">Send Pending Notifications</g:link>
            Send artist/festival alerts and festival reminders
        </li>

        <li>
            <g:link action='executeJob' params="[className: UpdateMusicianDetailsJob.name, infoMsgKey: 'admin.artistUpdating']">
                Update Artists
            </g:link>
        </li>

        <li>
            <g:link action='executeJob' params="[className: PurgeObsoleteDataJob.name, infoMsgKey: 'admin.unconfirmedUsers']">Purge Obsolete Data</g:link>
            Delete unconfirmed users and unapproved festivals that have finished
        </li>

        <li>
            <g:link action="listUnapproved">Show Unapproved Festivals (${festivalCount})</g:link>
            Unapproved festivals are not displayed on the site while unapproved
        </li>

        <li>
            <g:link action="listUnapprovedReviews">Moderate Reviews (${reviewCount})</g:link>
            Reviews are not displayed on the site while unapproved
        </li>

        <li><g:link action="create" controller="competition">Add Competition</g:link></li>
        <li><g:link action="moderateBlog">Moderate Blog</g:link></li>

        <li>
            <g:link action="executeJob" params="[className: ImportSkiddleFeedJob.name, infoMsgKey: 'admin.skiddle']">
                Import Skiddle Festivals
            </g:link>
        </li>

        <li>
            <g:link action="executeJob" params="[className: ImportEventbriteFestivalsJob.name, infoMsgKey: 'admin.eventbrite']">
                Import Eventbrite Festivals
            </g:link>
        </li>
    </ul>
</div>
</body>
