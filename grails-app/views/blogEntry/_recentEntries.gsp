<div id="recentEntriesWrapper" class="recentEntries">

	<g:each var="entry" in="${entries}">
			<div class="recentEntry">
				<g:render template="/blogEntry/entryTitle" model="[entry:entry]"/>
			</div>
	</g:each>
</div>