<%@ page import="ie.festivals.job.*" %>
<head>

</head>

<body>
<div class="container main">

    <ul class="spaced">
        <li><g:link controller='user' action='registrationTimeline'>Registered Users Timeline</g:link></li>
        <li><g:link controller='user' action='list'>List Users</g:link></li>

        <li>
            <g:link action='executeJob' params="[className: NotificationJob.name, infoMsgKey: 'admin.notifications']">
                Send Pending Notifications
            </g:link>
        </li>

        <li>
            <g:link action='executeJob' params="[className: UpdateMusicianDetailsJob.name, infoMsgKey: 'admin.artistUpdating']">
                Update Artists
            </g:link>
        </li>

        <li>
            <g:link action='executeJob' params="[className: ReminderJob.name, infoMsgKey: 'admin.remindersSent']">
                Send Festival Reminders
            </g:link>
        </li>

        <li>
            <g:link action='unconfirmedUsers' params="[className: DeleteUnconfirmedUsersJob.name, infoMsgKey: 'admin.unconfirmedUsers']">
                Delete Unconfirmed Users
            </g:link>
        </li>

        <li><g:link action="listUnapproved">Show Unapproved Festivals (${festivalCount})</g:link>.
        Unapproved festivals <strong>are not</strong> displayed on the site while unapproved.</li>

        <li><g:link action="listUnapprovedReviews">Moderate Reviews</g:link>.
            <strong>${reviewCount} unapproved review(s)</strong> awaiting moderation. Unapproved reviews <strong>are not</strong>
            displayed on the site while unapproved.
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
