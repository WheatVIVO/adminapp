

<#-- $This file is distributed under the terms of the license in LICENSE$ -->

<#-- Template for browsing individuals in class groups for menupages -->

<#import "lib-string.ftl" as str>
<noscript>
<p style="padding: 20px 20px 20px 20px;background-color:#f8ffb7">${i18n().browse_page_javascript_one} <a href="${urls.base}/browse" title="${i18n().index_page}">${i18n().index_page}</a> ${i18n().browse_page_javascript_two}</p>
</noscript>

<section id="noJavascriptContainer" class="hidden">

<section id="browse-by" role="region" class="cf">
    <nav role="navigation" class="main-nav mb4 sticky top-0 top-2-l w5-l fl-l">
        <ul id="browse-classes" class="bg-main nowrap overflow-y-auto-l">
            <#list vClassGroup?sort_by("displayRank") as vClass>
                <#------------------------------------------------------------
                Need to replace vClassCamel with full URL that allows function
                to degrade gracefully in absence of JavaScript. Something
                similar to what Brian had setup with widget-browse.ftl
                ------------------------------------------------------------->
                <#assign vClassCamel = str.camelCase(vClass.name) />
                <#-- Only display vClasses with individuals -->
                <#if (vClass.entityCount > 0)>
                    <li id="${vClassCamel}" class="dib db-l">
                      <a href="#${vClassCamel}"
                        title="${i18n().browse_all_in_class}"
                        data-uri="${vClass.URI}"
                        class="dib line-2half ph3 no-underline white-80 db-l bb-l b--main-lighter-l"
                      >${vClass.name}
                        <span class="count-classes">(${vClass.entityCount})</span>
                      </a>
                    </li>
                </#if>
            </#list>
        </ul>
    </nav>

<section id="search-home" role="region" class="mt4 center cf">
                <h3 class="f3 mb3">Search ${vClassGroupPublicName} <span class="search-filter-selected mb3">filteredSearch</span></h3>

                <form id="search-homepage" action="${urls.search}" name="search-home" role="search" method="post" >
                    <div id="search-home-field cf">
                        <input type="text" name="querytext" class="search-homepage fl dib pa2 w-70" value="" autocapitalize="off" />
                        <input type="submit" value="${i18n().search_button}"
                          class="search dib fl pa2 w-30 input-reset white bg-black bb bb-0 b--black br2 br--right" />
                        <input type="hidden" name="classgroup"  value="${vClassGroupURI}" autocapitalize="off" />
                    </div>
  
                </form>

</section> <!-- #search-home -->



    <div class="fr-l w-70-l" role="main">
      <nav id="alpha-browse-container" role="navigation" class="sticky top-0 bg-white nowrap overflow-x-scroll">
          <h3 class="selected-class"></h3>
          <#assign alphabet = ["A", "B", "C", "D", "E", "F", "G" "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"] />

          <ul id="alpha-browse-individuals" class="pt3 pb2 mb3">
              <li class="dib"><a href="#" class="selected dib pv1 ph2 no-underline th-gray" data-alpha="all" title="${i18n().select_all}">${i18n().all}</a></li>
              <#list alphabet as letter>
                  <li class="dib"><a class="dib pv1 ph2 no-underline th-gray" href="#" data-alpha="${letter?lower_case}" title="${i18n().browse_all_starts_with(letter)}">${letter}</a></li>
              </#list>
          </ul>
      </nav>

      <section id="individuals-in-class" role="region">

          <ul role="list" class="mv3">

              <#-- Will be populated dynamically via AJAX request -->
          </ul>
      </section>
    </div>
</section>

</section>
<script type="text/javascript">
    $('section#noJavascriptContainer').removeClass('hidden');
</script>
