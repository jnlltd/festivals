<head>
    <meta name='layout' content='responsive'/>
</head>

<body>
<div class="container main">

    <h1 class="hi-fi">Confirm Email Address</h1>

    <p>To complete the registration process, please enter your email address below. We will send an email
    to this address containing instructions for activating your account.</p>

    <g:form action="socialEmailRegistration" controller="register">
        <g:hiddenField name="name" value="${user.name}"/>
        <g:hiddenField name="socialLoginProvider" value="${user.socialLoginProvider}"/>
        <g:hiddenField name="socialId" value="${user.socialId}"/>

        <f:field bean="user" property="username" label="Email Address">
            <g:textField name="${property}" value="${value}" class="input-xlarge" maxlength="191"/>
        </f:field>
        <button type="submit" class="btn"><i class="icon-ok"></i> Submit</button>
    </g:form>
</div>
</body>
