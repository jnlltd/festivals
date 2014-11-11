<g:formRemote name="search" url="[controller: searchController, action: searchAction]" update="searchResults" class="form-inline">
    <label for="artistName">Artist Name</label>
    <g:textField name="artistName" value="${artistInstance?.name}" class="input-xlarge" maxlength="191"/>
    <g:hiddenField name="festivalId" value="${festivalId}"/>

    <button class="btn" type="submit">
        <i class="icon-search"></i> Search
    </button>
</g:formRemote>

<div id="searchResults" class="row-fluid"></div>