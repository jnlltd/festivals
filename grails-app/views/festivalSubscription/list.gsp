<%@ page import="ie.festivals.Festival" %>

<head>
    <title><content:title>Festival Alert Subscriptions</content:title></title>

    <style type="text/css">

    .festivalUnsubscribe a, .festivalUnsubscribe i {
        color: #40C17E;
    }

    .festivalSubscribe a, .festivalSubscribe i {
        color: #B94A48;
    }

    .no-wrap {
        white-space: nowrap;
    }

    @media (max-width: 767px) {
        .typeCol {
            display: none;
        }
    }

    @media (max-width: 480px) {
        .startCol {
            display: none;
        }

        .no-wrap {
            white-space: normal;
        }
    }
    </style>

    <r:require module="tablesorter"/>
</head>

<body>
<div class="container main">
    <h1 class="hi-fi">Festival Alerts</h1>

    <ul class="spacer spaced">
        <li>When you subscribe to a festival, you will receive an email alert when the festival's lineup changes.</li>

        <li>If instead you would like to be notified when a particular artist is added to <em>any</em> festival's lineup, check out our
        <g:link controller="artistSubscription" action="list">artist alerts</g:link>.</li>
    </ul>

    <sec:ifNotLoggedIn>
        <p class="alert spacer">
            Festival Alerts are only available to <g:link controller="register" action="newUser">registered users</g:link>.
            To subscribe to a festival, <g:link controller="login">login</g:link> then return to this page.
        </p>
    </sec:ifNotLoggedIn>

    <sec:ifLoggedIn>
        <p>
            The table below shows which festivals you have subscribed to receive alerts for. To change your status
            from unsubscribed to subscribed (or vice versa), click the link in the <strong>Status</strong> column.
        </p>
        <g:render template="/festival/festivalTable" model="[tableCssClass: 'full-width', colSort: '[[7, 1], [2, 0]]']"/>
    </sec:ifLoggedIn>
</div>
</body>
