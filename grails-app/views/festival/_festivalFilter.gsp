<%@ page import="ie.festivals.enums.FestivalType; ie.festivals.map.MapFocalPoint" %>

<div class="well spacer" style="padding: 5px 5px 10px 5px;">
    <h3 class="well-title" style="margin: 0; text-align: center;">Filter Festivals</h3>
    <g:form url="${filterUrl}" method="get" id="festival-filter">
        <label class="top-spacer">Festival Type</label>
        <div class="multiple-checkbox" style="margin-bottom: 10px;">

            <g:each in="${FestivalType.values()}" var="type" status="i">
                <g:set var="checkboxId" value="typeCheckbox${i}"/>

                <label class="checkbox ${type.id} festivalType" for="${checkboxId}">

                    %{--use <input> instead of <g:checkBox> to make the URLs prettier #1043--}%
                    <input type="checkbox" id="${checkboxId}" name="types" value="${type.name()}"
                            ${type in command.types ? "checked='checked'" : ''}/>
                    <span class="color-key">■</span>
                    ${type}
                </label>
            </g:each>
        </div>

        <label>Festival Location</label>
        <g:select name="location" class="block"
                  from="${MapFocalPoint.values()}"
                  optionKey="${{it.name()}}"
                  optionValue="${{it.displayName}}"
                  value="${command?.location?.name()}"/>

        <label class="checkbox top-spacer hoverable" for="futureOnly">
            <g:checkBox title="Exclude festivals that have already taken place"
                        name="futureOnly" value="${command.futureOnly}"/> Future Festivals Only
        </label>

        <label class="checkbox top-spacer hoverable" for="freeOnly" title="${g.message(code: 'filter.free.help')}">
            <g:checkBox name="freeOnly" value="${command.freeOnly}"/> Free Festivals Only
        </label>

        <div class="center">
            <button type="submit" class="btn"><i class="icon-refresh"></i> Update</button>
        </div>
    </g:form>
</div>