<head>
    <meta name='layout' content='responsive'/>
</head>

<body>
<div class="container main">
    <h1 class="hi-fi">Change Password</h1>
    <div class="double-spacer bright-alert">Enter your new password below, twice</div>

    <g:form action="resetPassword" class="panel-form">
        <g:hiddenField name='token' value='${command.token}'/>

        <f:field bean="command" property="password" label="New Password">
            <g:passwordField name="${property}" class="input-xlarge"/>
        </f:field>

        <f:field bean="command" property="password2" label="Confirm New Password" required="true">
            <g:passwordField name="${property}" class="input-xlarge"/>
        </f:field>

        <button type="submit" class="btn"><i class="icon-ok"></i> Submit</button>
    </g:form>
</div>
</body>

