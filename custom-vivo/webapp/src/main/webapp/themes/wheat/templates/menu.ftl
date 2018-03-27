<#-- $This file is distributed under the terms of the license in /doc/license.txt$ -->

</header>

<#include "developer.ftl">

<nav id="top-nav" role="navigation" class="bg-main-l mb3 mb5-ns">
    <input type="checkbox" id="mobile-menu-toggler" class='dn'/>
    <ul id="main-nav" role="list" class="pa3 bg-main dn nt4 mt0-ns db-ns pa0-ns nowrap-m overflow-auto-m dt-l center-l">
        <#list menu.items as item>
            <li role="listitem" class="dib-ns bb b--main-lighter bn-ns fl-l">
              <a href="${item.url}"
                  class="db pv2 pl2 no-underline white-80 fw7 pa3-ns dib-ns hover-bg-main-darker <#if item.active>bg-main-darker</#if>"
                  title="${item.linkText} ${i18n().menu_item}"
                  >
                  ${item.linkText}
              </a>
            </li>
        </#list>
        <li role="listitem" class="pt2 dn-ns">
          <a href="${urls.login}" class="db white-80 no-underline fw7 pv2"><span class="fw5 white-80">Are you an admin?</span> Login!</a>
        </li>
    </ul>

</nav>

<div id="wrapper-content" class="ph3" role="main">
    <#if flash?has_content>
        <#if flash?starts_with(i18n().menu_welcomestart) >
            <section  id="welcome-msg-container" role="container">
                <section  id="welcome-message" role="alert">${flash}</section>
            </section>
        <#else>
            <section id="flash-message" role="alert">
                ${flash}
            </section>
        </#if>
    </#if>

    <!--[if lte IE 8]>
    <noscript>
        <p class="ie-alert">This site uses HTML elements that are not recognized by Internet Explorer 8 and below in the absence of JavaScript. As a result, the site will not be rendered appropriately. To correct this, please either enable JavaScript, upgrade to Internet Explorer 9, or use another browser. Here are the <a href="http://www.enable-javascript.com"  title="java script instructions">instructions for enabling JavaScript in your web browser</a>.</p>
    </noscript>
    <![endif]-->
