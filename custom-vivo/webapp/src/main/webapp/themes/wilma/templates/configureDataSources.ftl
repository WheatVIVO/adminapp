${stylesheets.add('<link rel="stylesheet" href="${urls.theme}/css/wheatvivo.css" />')}

<h2>Data Sources
<img class="add-individual" src="${urls.images}/individual/addIcon.gif" alt="${i18n().add}" /></a>
</h2>

<table class="adminTable">
    <thead>
        <tr>
	    <td>Priority</td>
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
	        <a href="${urls.base}/adminFunctionality?feature=editDataSource&uri=${dataSource.URI?url}">
		    <#if dataSource.name??>
		        ${dataSource.name!}
		    <#else>
		        ${dataSource.URI}
	            </#if>
		</a>
	    </td>
	    <td>
	        <#if dataSource.lastUpdate??>
	            ${dataSource.lastUpdate?datetime}
		<#else>
		    ---
	        </#if>
            </td>
	    <td>
	        <#if dataSource.nextUpdate??>
	            ${dataSource.nextUpdate?datetime}
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
	        <a href="${urls.base}/individual?uri=${dataSource.URI?url}"><img class="edit-individual" src="${urls.images}/individual/editIcon.gif" alt="${i18n().edit_entry}" /></a>
                <img class="delete-individual" src="${urls.images}/individual/deleteIcon.gif" alt="${i18n().delete_entry}" /></a>
	    </td>

	</tr>
    </#list>
    </tbody>
</table>


