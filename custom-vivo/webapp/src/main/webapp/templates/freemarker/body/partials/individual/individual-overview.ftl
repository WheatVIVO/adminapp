<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Overview on individual profile page -->

<#assign overview = propertyGroups.pullProperty("${core}overview")!>
<#if overview?has_content> <#-- true when the property is in the list, even if not populated (when editing) -->
    <@p.addLinkWithLabel overview editable />
    <#list overview.statements as statement>
        <div class="individual-overview mb4">
                ${statement.value}
            <@p.editingLinks "${overview.name}" "" statement editable />
        </div>
    </#list>
</#if>
