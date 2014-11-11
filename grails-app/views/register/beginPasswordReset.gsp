<head>
    <meta name='layout' content='responsive'/>
</head>

<body>
<div class="container main">

    <h1 class="hi-fi">Reset Password</h1>

    <p>To reset your password, enter your email address below. We will send you an email with instructions for completing the process.</p>

    <g:form action="forgotPassword" class="panel-form">

        <f:field bean="forgotPassword" property="username" label="Email Address">
            <g:textField name="${property}" value="${value}" class="input-xlarge"/>
        </f:field>

        <button type="submit" class="btn"><i class="icon-ok"></i> Submit</button>
    </g:form>
</div>
</body>

