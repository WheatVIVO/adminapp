${stylesheets.add('<link rel="stylesheet" href="${urls.theme}/css/wheatvivo.css" />')}

<h2>${dataSource.name!dataSource.URI}</h2>

<div class="editingForm">

<form method="post" action="${urls.base}/adminFunctionality">
	    <label for="name">Name</label><input type="text" id="name" name="name" value="${dataSource.name!}"/>
	    <label for="priority">Priority</label><input type="text" id="priority" name="priority" value="${dataSource.priority!}"/>
	    <label for="serviceURL">Service URL</label><input type="text" id="serviceURL" name="serviceURL" value="${dataSource.serviceURL!}"/>
	    <#if dataSource.updateFrequency??>
                <p>Update frequency: ${dataSource.updateFrequency.label}</p>
	    </#if>

	    <#if dataSource.status??>
                <#if dataSource.status.running><p>Currently running</p><#else><p>Not currently running</p></#if>
		<p>Completion percentage: ${dataSource.status.completionPercentage}</p>
		<p>Total records: ${dataSource.status.totalRecords}</p>
		<p>Processed records: ${dataSource.status.processedRecords}</p>
		<p>Records with errors: ${dataSource.status.errorRecords}</p>
	    </#if>
	    
	    <#if dataSource.nextUpdate??>
                <#assign nextUpdate = dataSource.nextUpdate?datetime>
            <#else>
	        <#assign nextUpdate = "">
	    </#if>
	    <label for="nextUpdate">Next update</label><input type="text" id="nextUpdate" name="nextUpdate" value="${nextUpdate}"/>
	    <input type="hidden" name="uri" value="${dataSource.URI}"/>
	    <input type="hidden" name="feature" value="editDataSource"/>
	    <br/> <!-- This is what other VIVO forms do... -->
	    <!--
	    <input type="submit" name="submit" class="submit" value="Save"/>
	    <input type="submit" name="cancel" class="delete" value="Cancel"/>
	    -->

	    <label for="deploymentURL">Deployment URL</label><input type="text" id="deploymentURL" name="deploymentURL" value="${deploymentURL!}"/>
	    <label for="sparqlEndpointURL">SPARQL Endpoint URL</label><input type="text" id="sparqlEndpointURL" name="sparqlEndpointURL" value="${sparqlEndpointURL!}"/>
	    <label for="endpointUsername">SPARQL Endpoint Username</label><input type="text" id="endpointUsername" name="endpointUsername" value="${endpointUsername!}"/>
	    <label for="endpointPassword">SPARQL Endpoint Password</label><input type="text" id="endpointPassword" name="endpointPassword" value="${endpointPassword!}"/>
	    <label for="resultsGraphURI">Results Graph URI</label><input type="text" id="resultsGraphURI" name="resultsGraphURI" value="${resultsGraphURI!}"/>
            <br />
            <#if dataSource.status.running>
	       <input type="submit" name="stop" class="submit" value="Stop"/>
	    <#else>   
	       <input type="submit" name="start" class="submit" value="Start"/>
	    </#if>
</form>

