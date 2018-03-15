<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

    <div id="leaderboard-wrapper" class="dn db-l pv5-l ph4-l cf">
        <div class="container clearfix">
          <div class="region region-leaderboard"><div class="region-inner clearfix"><div id="block-search-form" class="block block-search no-title"  role="search"><div class="block-inner clearfix">

  <div class="block-content content"><form class="search-form" role="search" action="${urls.search}" method="post" id="search-block-form" accept-charset="UTF-8"><div><div class="container-inline">
      <h2 class="element-invisible">Search form</h2>
    <div class="form-type-textfield form-item-search-block-form">
  <label class="element-invisible" for="edit-search-block-form--2">Search this site </label>
 <input title="search WheatVIVO" class="custom-search-box form-text" placeholder="" type="search" id="edit-search-block-form--2" name="querytext" value="${querytext!}" maxlength="512" />
</div>
<div class="form-actions form-wrapper" id="edit-actions"><input type="image" id="edit-submit" name="Search" value="search" src="${urls.theme}/images/search-button.png" class="form-submit" /></div>
<input type="hidden" name="form_id" value="search_block_form" />
</div>
</div></form></div>
  </div></div>

<div id="block-block-33" class="block block-block no-title" ><div class="block-inner clearfix">

<div class="block-content content">
    <ul>
        <li><a href="https://www.youtube.com/channel/UCxHIrfpWx-44r4NQ6EItmEA" class="youtube">Youtube</a></li>
        <li><a href="https://twitter.com/WheatInitiative" class="twitter">Twitter</a></li>
        <li><a href="https://slideshare.net/WheatInitiative" class="slideshare">Slideshare</a></li>
        <li><a href="https://plus.google.com/+WheatinitiativeOrg" class="google-plus">G+</a></li>
    </ul>
</div>

</div></div>

<div id="block-block-23" class="block block-block no-title" ><div class="block-inner clearfix">
     <nav role="navigation">
        <ul id="header-nav" role="list">
            <#include "languageSelector.ftl">
            <li role="listitem"><a href="${urls.index}" title="${i18n().identity_index}">${i18n().identity_index}</a></li>
            <#if user.loggedIn>
                <#-- COMMENTING OUT THE EDIT PAGE LINK FOR RELEASE 1.5. WE NEED TO IMPLEMENT THIS IN A MORE
                     USER FRIENDLY WAY. PERHAPS INCLUDE A LINK ON THE PAGES THEMSELVES AND DISPLAY IF THE
                     USER IS A SITE ADMIN. tlw72

                     <#if (page??) && (page?is_hash || page?is_hash_ex) && (page.URLToEditPage??) >
                        <li role="listitem"><a href="${page.URLToEditPage}" title="${i18n().identity_edit}">${i18n().identity_edit}</a></li>
                     </#if>
                -->
                <#if user.hasSiteAdminAccess>
                    <li role="listitem"><a href="${urls.siteAdmin}" title="${i18n().identity_admin}">${i18n().identity_admin}</a></li>
                </#if>
                    <li>
                        <ul class="dropdown">
                            <li id="user-menu"><a href="#" title="${i18n().identity_user}">${user.loginName}</a>
                                <ul class="sub_menu">
                                     <#if user.hasProfile>
                                         <li role="listitem"><a href="${user.profileUrl}" title="${i18n().identity_myprofile}">${i18n().identity_myprofile}</a></li>
                                     </#if>
                                     <#if urls.myAccount??>
                                         <li role="listitem"><a href="${urls.myAccount}" title="${i18n().identity_myaccount}">${i18n().identity_myaccount}</a></li>
                                     </#if>
                                     <li role="listitem"><a href="${urls.logout}" title="${i18n().menu_logout}">${i18n().menu_logout}</a></li>
                                </ul>
                            </li>
                         </ul>
                     </li>



                ${scripts.add('<script type="text/javascript" src="${urls.base}/js/userMenu/userMenuUtils.js"></script>')}

            <#else>
                <li role="listitem"><a class="log-out" title="${i18n().menu_loginfull}" href="${urls.login}">${i18n().menu_login}</a></li>
            </#if>
        </ul>

    </nav>
  </div></div></div></div>        </div>
      </div>

<header id="branding" class="mobile-menu-toggle-holder flex justify-between items-center overflow-hidden h3 pa2
                              pa0-l absolute-l top-1-l left-2-l w6-l h4-l mb4 mb0-m"
role="banner">

    <h1 class="vivo-logo overflow-hidden">
      <span class="siteName">${siteName}</span>
    </h1>
    <span class="dib f3 ph2 th-gray f2-l ml2-l green">${siteName}</span>


    <label for="mobile-menu-toggler" class="mobile-menu-toggle-btn dib ph2 dn-m dn-l">
      <i class="fas fa-bars fa-2x"></i>
    </label>


    <#-- Since we are using a graphic text for the tagline, we won't render ${siteTagline}
    <#if siteTagline?has_content>
        <em>${siteTagline}</em>
    </#if>-->
