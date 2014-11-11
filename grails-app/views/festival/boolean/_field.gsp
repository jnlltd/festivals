<div class="control-group ${invalid ? 'error' : ''}">

    <label class="control-label" for="${property}">
        <%=widget%>
    </label>

    <g:render template="/_fields/errors"/>
</div>