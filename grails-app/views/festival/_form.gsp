<%@ page import="ie.festivals.enums.FestivalSource; ie.festivals.Festival; ie.festivals.enums.FestivalType" %>

<asset:script>
    // show/hide the early bird field depending on whether or not this festival is free
    $(function() {
        if (!${festivalInstance.freeEntry}) {
            $('#earlyBird').show();
        }

        $('#freeEntry').click(function() {
             $(this).is(':checked') ? $('#earlyBird').hide() : $('#earlyBird').show();
        });
    });
</asset:script>

<g:set var="dateFormat" value="${grailsApplication.config.festival.dateFormat}"/>
<g:hiddenField name="previousOccurrence.id" value="${festivalInstance.previousOccurrence?.id}"/>

<f:with bean="festivalInstance">

    <f:field property="name" label="Name">
        <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
        <div class="info-inline">Enter the name of the festival</div>
    </f:field>

    <f:field property="type" label="Type" required="true">
        <g:select name="type" class="input-large"
                  from="${FestivalType.values()}"
                  keys="${FestivalType.values()*.name()}"
                  value="${value?.name()}"
                  noSelection="${['': 'Please Select...']}"/>

        <div class="info-inline">Main type of entertainment or subject matter of this festival</div>
    </f:field>

    <f:field property="website" label="Website">
        <g:textField name="${property}" value="${value}" class="input-xlarge" />
        <div class="info-inline">Enter the full URL of the festival's official website, e.g. http://www.example.org</div>
    </f:field>

    <f:field property="lineupUrl" label="Lineup Webpage">
        <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
        <div class="info-inline">Enter the URL of a webpage that lists the performers appearing at this festival (if applicable)</div>
    </f:field>

    <f:field property="start" label="Start Date">
        <g:textField name="${property}"
                     value="${g.formatDate(date: value, format: dateFormat)}"
                     class="input-medium datepicker"
                     data-date-format="${dateFormat.toLowerCase()}"
                     readonly="readonly"/>
    </f:field>

    <f:field property="end" label="End Date">
        <g:textField name="${property}"
                     value="${g.formatDate(date: value, format: dateFormat)}"
                     class="input-medium datepicker"
                     data-date-format="${dateFormat.toLowerCase()}"
                     readonly="readonly"/>
        <div class="info-inline">For one-day festivals, the start and end dates should be the same</div>
    </f:field>

    <fieldset>
        <legend>Address</legend>

        <f:field property="addressLine1" label="Address Line 1">
            <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
        </f:field>

        <f:field property="addressLine2" label="Address Line 2">
            <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
        </f:field>

        <f:field property="city" label="Town / City" required="true">
            <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
            <div class="info-inline">The name of the town/city/village where the festival will take place</div>
        </f:field>

        <f:field property="region" label="State / Province / County / Region">
            <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
        </f:field>

        <f:field property="postCode" label="ZIP / Postal Code">
            <g:textField name="${property}" value="${value}" class="input-medium" maxlength="191"/>
        </f:field>

        <f:field property="countryCode" label="Country">
            <country:countrySelect name="countryCode" default="irl" value="${value}" class="input-medium"/>
        </f:field>
    </fieldset>

    <fieldset>
        <legend>Pricing</legend>

        <f:field property="freeEntry">
            <g:checkBox name="${property}" value="${value}"/> Free Entry
            <div class="info-inline">Tick this box if the festival does not charge an admission fee</div>
        </f:field>

        <f:field property="earlyBirdExpiry" label="Early Bird Price Expiry">
            <g:textField name="${property}"
                         value="${g.formatDate(date: value, format: dateFormat)}"
                         class="input-medium datepicker"
                         data-date-format="${dateFormat.toLowerCase()}"/>
            <div class="info-inline">If this festival offers an early bird price (i.e. a discount for purchasing tickets early), enter the date this offer expires</div>
        </f:field>

    </fieldset>

    <sec:ifAllGranted roles="ROLE_ADMIN">

        <f:field property="latitude" label="Latitude">
            <g:textField name="${property}" value="${value}" class="input-medium"/>
            <div class="info-inline">If this field is left blank, we'll try to figure out the correct value for latitude from the address</div>
        </f:field>

        <f:field property="longitude" label="Longitude">
            <g:textField name="${property}" value="${value}" class="input-medium"/>
            <div class="info-inline">If this field is left blank, we'll try to figure out the correct value for longitude from the address</div>
        </f:field>

        <f:field property="videoUrl" label="YouTube Embed URL">
            <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
            <div class="info-inline">The URL contained within the YouTube embed code, e.g. http://www.youtube.com/embed/bTWckZef770. This is not the same URL you use to load the video in a browser</div>
        </f:field>

        <f:field property="hasLineup">
            <g:checkBox name="${property}" value="${value}"/> Has Lineup
            <div class="info-inline">If the festival organisers publish (in advance) a list of the performers that will be appearing, this box should be checked</div>
        </f:field>
    </sec:ifAllGranted>

    <f:field property="twitterUsername" label="Twitter Username">
        <g:textField name="${property}" value="${value}" class="input-medium" maxlength="191"/>
        <div class="info-inline">If this festival has an official Twitter account, enter the username thereof, e.g. @myfestival</div>
    </f:field>

    <f:field property="ticketInfo" label="Ticket Information">
        <g:if test="${value && festivalInstance.source != FestivalSource.HUMAN}">
            <g:hiddenField name="${property}" value="${value}"/>
            <div class="alert">
                Ticket information cannot be changed for ${festivalInstance.source} festivals.
            </div>
        </g:if>
        <g:else>
            <g:textArea name="${property}" value="${value}" rows="6" cols="1" class="rich block"/>
            <div class="info-inline">
                If tickets are still available, indicate where they may be purchased and how much they cost. If tickets are sold out, please say so.
            </div>
        </g:else>
    </f:field>

    <sec:ifAllGranted roles="ROLE_ADMIN">

        %{-- admins can add a Skiddle ticket URL to festivals not imported from Skiddle #774 --}%
        <g:if test="${!festivalInstance.source == FestivalSource.SKIDDLE}">
            <f:field property="skiddleUrl" label="Skiddle Event URL">
                <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
                <div class="info-inline">To add the Skiddle ticket button to this festival's ticketing info, enter the event URL from the Skiddle XML feed.</div>
            </f:field>
        </g:if>
    </sec:ifAllGranted>

    <f:field property="synopsis" label="Synopsis">
        <g:textArea name="${property}" value="${value}" rows="8" cols="1" class="rich block"/>
        <div class="info-inline">
            A description of this festival. Please include information about the festival's performers, events, entertainment, attractions, etc.
        </div>
    </f:field>

    <sec:ifAllGranted roles="ROLE_ADMIN">
        <f:field property="approved">
            <g:checkBox name="${property}" value="${value}"/> Approved
        </f:field>
    </sec:ifAllGranted>
</f:with>