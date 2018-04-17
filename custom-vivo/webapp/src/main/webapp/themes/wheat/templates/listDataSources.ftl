${stylesheets.add('<link rel="stylesheet" href="${urls.theme}/css/wheatvivo.css" />')}
${stylesheets.add('<meta http-equiv="refresh" content="5">')}

<h2>Data Sources
<a href="${urls.base}/editForm?controller=Entity&VClassURI=http%3A%2F%2Fvivo.wheatinitiative.org%2Fontology%2Fadminapp%2FDataSource"><img class="add-individual" src="${urls.images}/individual/addIcon.gif" alt="${i18n().add}" /></a>
</h2>

<table class="adminTable">
    <thead>
        <tr>
	    <td>Priority</td>
	    <td></td>
            <td>Source</td>
	    <td>Last updated</td>
	    <td>Next update</td>
	    <td>Status message</td>
	    <td>% done</td>
	    <td>Total records</td>
	    <td>Processed records</td>
	    <td>Error records</td>
	    <td>OK?</td>
	    <td>Status</td>
	</tr>
    </thead>
    <tbody>
    <#list dataSources as dataSource>
        <tr>
	    <td>${dataSource?index}</td>
            <td>
	        <form method="post" action="${urls.base}/invokeService">
		    <input type="hidden" name="uri" value="${dataSource.configuration.URI}"/>
		    <#if type?has_content>
		        <input type="hidden" name="type" value="${type}"/>
		    </#if>
                    <#if dataSource.status.running>
                        <input type="submit" name="stop"  class="submit" value="Stop"/>
                    <#else>
                        <input type="submit" name="start" class="submit" value="Start"/>
                    </#if>
		</form>
	    </td>
            <td>
	        <a href="${urls.base}/individual?uri=${dataSource.configuration.URI?url}">
		    <#if dataSource.configuration.name??>
		        ${dataSource.configuration.name!}
		    <#else>
		        ${dataSource.configuration.URI}
	            </#if>
		</a>
	    </td>
	    <td>
	        <#if dataSource.status.lastUpdate??>
	            ${dataSource.status.lastUpdate?datetime}
		<#else>
		    ---
	        </#if>
            </td>
	    <td>
	        <#if dataSource.configuration.nextUpdate??>
	            ${dataSource.configuration.nextUpdate?datetime}
		<#else>
		    ---
	        </#if>
            </td>
	    <td> 
	        <#if dataSource.status.stopRequested>
                    stop requested
		<#elseif dataSource.status.message?has_content>
                    ${dataSource.status.message}
		</#if>
            </td>
	    <td>${dataSource.status.completionPercentage}</td>
	    <td>${dataSource.status.totalRecords}</td>
	    <td>${dataSource.status.processedRecords}</td>
	    <td>${dataSource.status.errorRecords}</td>
	    <td>
	        <#if dataSource.status.statusOk>
		    <strong class="statusOK">OK</strong></td>
		<#else>
                    <strong class="statusError">ERROR</strong></td>
		</#if>
            </td>

	    <td>
	        <#if dataSource.status.running>
		    <strong class="statusRunning">RUNNING</strong></td>
		<#else>
                    <strong class="statusIdle">IDLE</strong></td>
		</#if>
            </td>
	</tr>
    </#list>
    </tbody>
</table>


