<%@ taglib prefix="janrain" uri="http://janrain4j.googlecode.com/tags" %>
<head>
    <meta name='layout' content='responsive'/>
</head>

<body>

<div class="container main">
    <div class="row-fluid">
        <div class="span5 spacer">

            <h2 class="hi-fi">Festivals.ie Registration</h2>

            <p class="spacer">If you already have an account, <g:link controller="login">login</g:link> instead.</p>

            <g:form action="register" class="panel-form">

                <f:field bean="user" property="username" label="Email Address">
                    <g:textField name="${property}" value="${value}" class="input-large" maxlength="191"/>
                    <div class="info-inline">A confirmation email will be sent to this address</div>
                </f:field>

                <f:field bean="user" property="name" label="Name">
                    <g:textField name="${property}" value="${value}" class="input-large" maxlength="191"/>
                    <div class="info-inline">Choose a name that you will be known by on the website</div>
                </f:field>

                <f:field bean="user" property="password" label="Password">
                    <g:passwordField name="${property}" class="input-large"/>
                    <div class="info-inline">Enter the password that you will use to logon to the site</div>
                </f:field>

                <f:field bean="user" property="passwordConfirm" label="Confirm Password">
                    <g:passwordField name="${property}" class="input-large"/>
                    <div class="info-inline">Type the password again</div>
                </f:field>

                <div class="control-group">
                    <img src="${createLink(controller: 'simpleCaptcha', action: 'captcha')}" alt="CAPTCHA"/>
                    <label for="captcha">Type the letters above in the box below</label>
                    <input type="text" class="input-medium" id="captcha" name="captcha"/>
                </div>

                <label for='terms' class="spacer">
                    <input type='checkbox' name='terms' id='terms' ${'terms' in params ? "checked='checked'" : ''}/>
                    I accept the <g:link target="_blank" uri="/terms">Terms &amp; Conditions</g:link>
                </label>

                <button type="submit" class="btn">Submit</button>
            </g:form>
        </div>

        <div class="span7 spacer">

            <h2 class="hi-fi">Register Socially</h2>

            <p>
                If you already have an account with any of the services below, you can use it to register with Festivals.ie.
            </p>
            <janrain:signInEmbedded/>

            <h3 class="hi-fi">Why Register?</h3>
            <g:render template="/login/whyRegister"/>
        </div>
    </div>
</div>
</body>