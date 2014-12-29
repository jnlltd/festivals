<head>
    <style type="text/css">
    #captcha-label {
        margin: 3px 0;
    }
    </style>

</head>

<body>
<div class="container main">

    <h1 class="hi-fi">We Need Your Help</h1>

    <div class="spacer">
        <em>Share this Page</em> <cache:render template="/common/addThis"/>
    </div>

    <div class="row-fluid">
        <div class="span8">
            <p>We’re looking for people who love festivals to share their festival stories here in the shape of reviews and articles.</p>

            <h4 class="double-top-spacer">Why Write for Festivals.ie?</h4>

            <ul>
                <li>Will be good craic, no obligations, deadlines, or any of that stuff</li>
                <li>Regular contributors will be eligible for free festival tickets</li>
                <li>Write about something you love doing and help the next generation of festival goers!</li>
                <li>An opportunity for aspiring writers and journalists to get published and credited, this could be a stepping stone to paid work</li>
                <li>A chance to speak your mind and make your opinion count when it comes to the Irish festival scene</li>
            </ul>
        </div>
        <div class="span4 hidden-phone">
            <asset:image src="banners/love-festivals-299-250.gif"/>
        </div>
    </div>

    <h4>We're looking for the following writers to cover Irish festivals:</h4>

    <ul>
        <li>Music Festivals – 6 people</li>
        <li>Arts Festivals – 2 people</li>
        <li>Sport Festivals – 2 people</li>
        <li>Food &amp; Drink Festivals – 2 people</li>
        <li>Comedy Festivals – 2 people</li>
        <li>Film Festivals – 2 people</li>
        <li>All Others – 2 people</li>
        <li>Facebook Contributors – 2 people</li>
    </ul>

    <p class="top-spacer">
        If you have a friend who enjoys the same festivals as you, then consider teaming up, write
        a few reviews and previews and we'll agree a festival to send you along to during the 2014 festival season.
    </p>

    <p class="top-spacer">
        <span style="font-weight: bold;">Please submit the form below to apply for one of these roles</span>.
        We will respond to all emails to explain the next steps. Thanks!
    </p>

    <div class="spacer">
        <em>Share this Page</em> <cache:render template="/common/addThis"/>
    </div>

    <div class="bright-alert spacer">Please complete all fields marked *</div>

    <g:set var="roles"
           value="${['Music', 'Comedy', 'Arts', 'Food & Drink', 'Sports', 'Film', 'Other', 'Facebook Contributor']}"/>

    <g:form controller="home" action="sendWriterApplication" class="panel-form">

        <sec:ifNotLoggedIn>
            <f:field bean="application" property="name" label="Name">
                <g:textField name="${property}" value="${value}" class="input-xlarge"/>
                <div class="info-inline">Enter your name</div>
            </f:field>

            <f:field bean="application" property="email" label="Email">
                <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
                <div class="info-inline">Enter your email address, so we can send you a reply</div>
            </f:field>
        </sec:ifNotLoggedIn>

        <f:field bean="application" property="subject" label="Role" required="true">
            <g:select name="${property}" from="${roles}" value="${value}"/>
            <div class="info-inline">Select the role that most interests you</div>
        </f:field>

        <f:field bean="application" property="message" label="Message">
            <g:textArea name="${property}" value="${value}" class="block" rows="7"/>
        </f:field>

        <sec:ifNotLoggedIn>
            <div class="control-group">
                <img src="${createLink(controller: 'simpleCaptcha', action: 'captcha')}" alt="CAPTCHA"/>
                <label id="captcha-label" for="captcha">Type the letters above in the box below *</label>
                <g:textField name="captcha" class="input-medium"/>
            </div>
        </sec:ifNotLoggedIn>

        <button type="submit" class="btn">Apply</button>
    </g:form>
</div>
</body>