<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for property listing on individual profile page -->

<#import "lib-properties.ftl" as p>
<#assign subjectUri = individual.controlPanelUrl()?split("=") >
<#assign tabCount = 1 >
<#assign sectionCount = 1 >

<#assign tabClasses = "pa2 dib mr-3 pointer bg-main white br4 br--top" >
<#assign h2Classes = "f4 pa3 ba bb-0 b--th-gray-2" >
<#assign propertyGroupClasses = "mb3" >

<!-- ${propertyGroups.all?size} -->
<ul class="propertyTabsList nowrap overflow-auto">

<#list propertyGroups.all as groupTabs>
    <#if ( groupTabs.properties?size > 0 ) >
        <#assign groupName = groupTabs.getName(nameForOtherGroup)>
        <#if groupName?has_content>
		    <#--the function replaces spaces in the name with underscores, also called for the property group menu-->
    	    <#assign groupNameHtmlId = p.createPropertyGroupHtmlId(groupName) >
        <#else>
            <#assign groupName = "${i18n().properties_capitalized}">
    	    <#assign groupNameHtmlId = "${i18n().properties}" >
        </#if>
        <#if tabCount = 1 >
            <li class="selectedGroupTab clickable ${tabClasses} bg-main-hover" groupName="${groupNameHtmlId?replace("/","-")}">${groupName?capitalize}</li>
            <#assign tabCount = 2>
        <#else>
            <li class="nonSelectedGroupTab clickable ${tabClasses}" groupName="${groupNameHtmlId?replace("/","-")}">${groupName?capitalize}</li>
        </#if>
    </#if>
</#list>
<#if (propertyGroups.all?size > 1) >
    <li  class="nonSelectedGroupTab clickable ${tabClasses}" groupName="viewAll">${i18n().view_all_capitalized}</li>
</#if>
</ul>
<#list propertyGroups.all as group>
    <#if (group.properties?size > 0)>
        <#assign groupName = group.getName(nameForOtherGroup)>
        <#assign groupNameHtmlId = p.createPropertyGroupHtmlId(groupName) >
        <#assign verbose = (verbosePropertySwitch.currentValue)!false>
        <section id="${groupNameHtmlId?replace("/","-")}" class="property-group ${propertyGroupClasses}" role="region" style="<#if (sectionCount > 1) >display:none<#else>display:block</#if>">
        <nav id="scroller" class="scroll-up hidden" role="navigation">
            <a href="#branding" title="${i18n().scroll_to_menus}" >
                <img src="${urls.images}/individual/scroll-up.gif" alt="${i18n().scroll_to_menus}" />
            </a>
        </nav>

        <#-- Display the group heading -->
        <#if groupName?has_content>
		    <#--the function replaces spaces in the name with underscores, also called for the property group menu-->
    	    <#assign groupNameHtmlId = p.createPropertyGroupHtmlId(groupName) >
            <h2 id="${groupNameHtmlId?replace("/","-")}" pgroup="tabs" class="hidden ${h2Classes}">${groupName?capitalize}</h2>
        <#else>
            <h2 id="properties" pgroup="tabs" class="hidden ${h2Classes}">${i18n().properties_capitalized}</h2>
        </#if>
        <div id="${groupNameHtmlId?replace("/","-")}Group" >
            <#-- List the properties in the group   -->
            <#include "individual-properties.ftl">
        </div>
        </section> <!-- end property-group -->
        <#assign sectionCount = 2 >
    </#if>
</#list>
<script>
    var individualLocalName = "${individual.localName}";
</script>

${stylesheets.add('<link rel="stylesheet" href="${urls.base}/css/individual/individual-property-groups.css" />')}
${headScripts.add('<script type="text/javascript" src="${urls.base}/js/amplify/amplify.store.min.js"></script>')}
${scripts.add('<script type="text/javascript" src="${urls.base}/js/individual/propertyGroupControls.js"></script>')}
