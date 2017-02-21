${stylesheets.add('<link rel="stylesheet" href="${urls.theme}/css/wheatvivo.css" />')}

<h2>Data Sources
<img class="add-individual" src="${urls.images}/individual/addIcon.gif" alt="${i18n().add}" /></a>
</h2>

<table class="adminTable">
    <thead>
        <tr>
	    <td>Priority</td>
	    <td></td>
            <td>Source</td>
	    <td>Last updated</td>
	    <td>Next update</td>
	    <td>Status</td>
	    <td><!-- edit controls --></td>
	</tr>
    </thead>
    <tbody>
    <#list dataSources as dataSource>
        <tr>
	    <td>${dataSource?index}</td>
            <td>
	        <form method="post" action="${urls.base}/invokeService">
		    <input type="hidden" name="uri" value="${dataSource.configuration.URI}"/>
		    <#if type?hasContent>
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
	        <#if dataSource.status.running>
		    <strong class="statusOK">RUNNING</strong></td>
		<#else>
                    <strong class="statusError">IDLE</strong></td>
		</#if>
            </td>
	    <td>
	        <a href="${urls.base}/individual?uri=${dataSource.configuration.URI?url}"><img class="edit-individual" src="${urls.images}/individual/editIcon.gif" alt="${i18n().edit_entry}" /></a>
                <img class="delete-individual" src="${urls.images}/individual/deleteIcon.gif" alt="${i18n().delete_entry}" /></a>
	    </td>

	</tr>
    </#list>
    </tbody>
</table>


