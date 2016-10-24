${stylesheets.add('<link rel="stylesheet" href="${urls.theme}/css/wheatvivo.css" />')}

<h2>${dataSource.name!dataSource.URI}</h2>

<div class="editingForm">

<form method="post" action="${urls.base}/adminFunctionality">
	    <label for="name">Name</label><input type="text" id="name" name="name" value="${dataSource.name!}"/>
	    <label for="priority">Priority</label><input type="text" id="priority" name="priority" value="${dataSource.priority!}"/>
	    <#if dataSource.nextUpdate??>
                <#assign nextUpdate = dataSource.nextUpdate?datetime>
            <#else>
	        <#assign nextUpdate = "">
	    </#if>
	    <label for="nextUpdate">Next update</label><input type="text" id="nextUpdate" name="nextUpdate" value="${nextUpdate}"/>
	    <input type="hidden" name="uri" value="${dataSource.URI}"/>
	    <input type="hidden" name="feature" value="editDataSource"/>
	    <br/> <!-- This is what other VIVO forms do... -->
	    <input type="submit" name="submit" class="submit" value="Save"/>
	    <input type="submit" name="cancel" class="delete" value="Cancel"/>
</form>

