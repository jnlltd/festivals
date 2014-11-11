<%@ taglib prefix="janrain" uri="http://janrain4j.googlecode.com/tags" %>
<head>
    <meta name='layout' content='responsive'/>
    <style type="text/css">
        #remember_me {
            margin-bottom: 5px;
        }
    </style>

</head>

<body>

<div class="container main">
    <div class="row-fluid">
        <div class="span4 spacer">
            <h2 class="hi-fi">Festivals.ie Login</h2>

            <form action="${postUrl}" method="post" class="no-control-group panel-form">

                <label for='username'>Email</label>
                <input type='text' name='j_username' id='username' value="${params.username}" class="input-large"/>

                <label for='password'>Password</label>
                <input type='password' name='j_password' id='password' class="input-large"/>

                <label for='remember_me'>
                    <input type='checkbox' name='${rememberMeParameter}'
                           id='remember_me' ${hasCookie ? "checked='checked'" : ''}/>
                    Remember Me
                </label>

                <button type="submit" class="btn top-spacer">Login</button>
            </form>

            <ul>
                <li>Forgot password?
                    <a href="${createLink(controller: 'register', action: 'beginPasswordReset')}">Recover it here</a>.
                </li>
                <li>Need an account? <g:link controller="register" action="newUser">Sign up here</g:link>.</li>
            </ul>
        </div>

        <div class="span8 spacer">
            <h2 class="hi-fi">Social Login</h2>

            <p>
                If your Festivals.ie account was created using a social service, you must use this same service (below) to login.
                Otherwise, please use the form on the left to login instead.
            </p>
            <janrain:signInEmbedded/>
        </div>
    </div>
</div>

</body>