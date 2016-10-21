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
            <td>${dataSource.name!}</td>
	    <td>
	        <#if dataSource.lastUpdate??>
	            ${dataSource.lastUpdate?datetime}
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
	        <#if dataSource.status.statusOk>
		    <strong class="statusOK">OK</strong></td>
		<#else>
                    <strong class="statusError">ERROR</strong></td>
		</#if>
            </td>
	    <td>
                <img class="edit-individual" src="${urls.images}/individual/editIcon.gif" alt="${i18n().edit_entry}" /></a>
                <img class="delete-individual" src="${urls.images}/individual/deleteIcon.gif" alt="${i18n().delete_entry}" /></a>
	    </td>

	</tr>
    </#list>
    </tbody>
</table>


