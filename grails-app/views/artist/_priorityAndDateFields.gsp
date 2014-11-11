<%@ page import="ie.festivals.enums.Priority" %>
<g:set var="serverDateFormat" value="${grailsApplication.config.festival.dateFormat}"/>

<h3>Priority</h3>

<g:each in="${Priority.values()}" var="priority">
    <g:radio id="" name="priority" value="${priority.name()}" checked="${priority == Priority.HEADLINE}"/>
        <span>${priority}</span>
</g:each>

<g:if test="${festival.multiDayDuration}">
    <h3>Performance Date</h3>

    <div>
        Select the date on which the artist will be performing. If you don't know when the artist will
        be appearing, leave this selection blank.
    </div>

    <g:each in="${festival.start..festival.end}" var="festivalDate">
        <div>
            <g:radio name="date" value="${g.formatDate(date: festivalDate, format: serverDateFormat)}"/> ${g.formatDate(date: festivalDate)}
        </div>
    </g:each>


    <h3>Performance Time</h3>
    <div>
        The time when the artist's performance will begin. Performance time will be ignored unless a
        performance date is also specified.
    </div>
    <div>
        <g:select name="hour" from="${0..23}" noSelection="['': 'Hour']" style="width: 80px;"/>

        <g:select name="minute" from="${(0..55).step(5)}" noSelection="['': 'Minute']" style="width: 100px;"
                  optionValue="${{it < 10 ? it + '0' : it}}"/>
    </div>
</g:if>

<div class="double-top-spacer">
    <input class="btn" type="submit" value="Add Artist"/>
</div>
