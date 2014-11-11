<head>
    <style type="text/css">
        #captcha-label {
            margin: 3px 0;
        }
    </style>

</head>

<body>
<div class="container main">
    <h1 class="hi-fi">Contact Us</h1>

    <p>Use the form below if you have any questions about the site or suggestions for how we can improve it. You can also contact us through our
        <a target="_blank" href="http://www.facebook.com/pages/Festivals.ie">Facebook</a> and
        <a target="_blank" href="https://twitter.com/FestivalsIrish">Twitter</a> pages.
    </p>

    <div class="double-spacer bright-alert">Please complete all fields marked *</div>

    <g:form controller="home" action="sendFeedback" class="panel-form">

        <sec:ifNotLoggedIn>
            <f:field bean="contact" property="name" label="Name">
                <g:textField name="${property}" value="${value}" class="input-xlarge"/>
                <div class="info-inline">Enter your name</div>
            </f:field>

            <f:field bean="contact" property="email" label="Email">
                <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
                <div class="info-inline">Enter your email address, so we can send you a reply</div>
            </f:field>
        </sec:ifNotLoggedIn>

        <f:field bean="contact" property="subject" label="Subject">
            <g:textField name="${property}" value="${value}" class="block" maxlength="191"/>
            <div class="info-inline">Enter the topic of your message</div>
        </f:field>

        <f:field bean="contact" property="message" label="Message">
            <g:textArea name="${property}" value="${value}" class="block" rows="5"/>
        </f:field>

        <sec:ifNotLoggedIn>
            <div class="control-group">
                <img src="${createLink(controller: 'simpleCaptcha', action: 'captcha')}" alt="CAPTCHA"/>
                <label for="captcha" id="captcha-label">Type the letters above in the box below *</label>
                <g:textField name="captcha" class="input-medium"/>
            </div>

        </sec:ifNotLoggedIn>

        <button type="submit" class="btn">Send</button>
    </g:form>
</div>
</body>