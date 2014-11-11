<div class="control-group ${invalid ? 'error' : ''}">
    <label class="control-label" for="${property}">${required ? "$label *" : label}</label>
    <div class="controls">
        <%= widget %>

        <g:render template="/_fields/errors"/>
    </div>
</div>