<%@ page import="ie.festivals.UserRole" %>

<head>
    <style type="text/css">
    .role, .delete{
        width: 5%;
    }

    .date, .count {
        width: 10%;
    }

    td.count, th.count {
        text-align: center;
    }

    td {
        overflow: hidden;
    }
    </style>

</head>

<body>

<div class="container main">
    <h1 class="hi-fi">User List</h1>
    <p>Total number of users: ${allUsers.size()}</p>
    <table class="tablesorter table full-width table-striped">
        <thead>
        <tr>
            <th class="role">Role</th>
            <th class="name">Name</th>
            <th class="email">Email Address</th>
            <th class="count">No. Festivals</th>
            <th class="date {sorter: 'formattedDate'}">Reg. Date</th>
            <th class="api">API Key</th>
            <th class="delete"></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${allUsers}">

            <g:set var="isUser" value="${it.role != 'ROLE_ADMIN'}"/>

            <tr>
                <td class="role">${isUser ? 'User' : 'Admin'}</td>
                <td class="name">${it.name.encodeAsHTML()}</td>
                <td class="email">${it.email.encodeAsHTML()}</td>
                <td class="count">${it.festivalCount}</td>
                <td class="date">${it.dateCreated?.format('d MMM yyyy')}</td>
                <td class="api">
                    <g:if test="${it.apiKey}">${it.apiKey}</g:if>
                    <g:else>
                        <g:link controller="admin" action="generateApiKey" params="[id: it.id]">
                            Generate
                        </g:link>
                    </g:else>
                </td>
                <td class="delete">
                    <g:if test="${isUser}">
                        <g:link action="delete"
                                onclick="return confirm('${g.message(code: 'user.delete.confirm')}');"
                                id="${it.id}"
                                class="danger">Delete</g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
</body>
