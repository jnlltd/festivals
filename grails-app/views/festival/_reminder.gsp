<g:if test="${reminder}">
    We will send you a reminder about this festival on
    <span style="white-space: nowrap; font-weight: bold;"><g:formatDate date="${festival.start - reminder.daysAhead}"/></span>.
    If you no longer wish to receive this reminder,
    <g:remoteLink action="cancelReminder" update="reminder" id="${festival.id}">click here to cancel it</g:remoteLink>.
</g:if>
<g:else>
    Email me a reminder about this festival
    <g:formRemote name="reminder" url="[controller: 'festival', action: 'requestReminder']" update="reminder" class="last">

        <g:select name="daysAhead"
                  class="top-spacer block"
                  from="${reminderRange}"
                  value="${Math.min(reminderRange.to, 3)}"
                  optionValue="${{it == 0 ? 'The day it starts' : it == 1 ? 'The day before it starts' : "$it days before it starts"}}"/>

        <g:hiddenField name="festivalId" value="${festival.id}"/>
        <button type="submit" class="btn btn-small top-spacer"><i class="icon-exclamation-sign"></i> Request Reminder</button>
    </g:formRemote>
</g:else>
