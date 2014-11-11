<%@ page import="ie.festivals.competition.Competition" %>
<g:set var="dateFormat" value="${grailsApplication.config.festival.dateFormat}"/>

<f:field bean="competition" property="title" label="Title">
    <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
    <div class="info-inline">Heading that appears on this competition's entry page</div>
</f:field>

<f:field bean="competition" property="code" label="Code">
    <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
    <div class="info-inline">A unique code for this competition e.g. electric-picnic</div>
</f:field>

<f:field bean="competition" property="image" label="Image">
    <input type="file" name="${property}" accept="image/*" style="padding-bottom: 5px;"/>
    <div class="info-inline">
        <ul style="margin-left: 15px; margin-bottom: 0;">
            <li>The image should be no more than 900px wide. If it is wider please reduce it's size
                <em>proportionally</em> before uploading it
            </li>
            <li>The maximum allowed file size is 0.5MB, but ideally you should choose something much smaller</li>
            <li>If the image is a PNG, please use <a href="https://tinypng.com" target="_blank">TinyPNG</a>
                to compress the file before uploading it
            </li>
        </ul>
    </div>
</f:field>

<f:field bean="competition" property="description" label="Description">
    <g:textArea name="${property}" value="${value}" rows="8" cols="1" class="rich block"/>
</f:field>

<f:field bean="competition" property="question" label="Question">
    <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
    <div class="info-inline">The question competition entrants must answer</div>
</f:field>


<f:field bean="competition" property="end" label="End Date">
    <g:textField name="${property}"
                 value="${g.formatDate(date: value, format: dateFormat)}"
                 class="input-medium datepicker"
                 data-date-format="${dateFormat.toLowerCase()}"
                 readonly="readonly"/>
</f:field>