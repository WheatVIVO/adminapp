<#-- $This file is distributed under the terms of the license in /doc/license.txt$  -->

<@widget name="login" include="assets" />

<#--
        With release 1.6, the home page no longer uses the "browse by" class group/classes display.
        If you prefer to use the "browse by" display, replace the import statement below with the
        following include statement:

            <#include "browse-classgroups.ftl">

        Also ensure that the homePage.geoFocusMaps flag in the runtime.properties file is commented
        out.
-->
<#import "lib-home-page.ftl" as lh>

<!DOCTYPE html>
<html lang="en">
    <head>
        <#include "head.ftl">
        <#if geoFocusMapsEnabled >
            <#include "geoFocusMapScripts.ftl">
        </#if>
        <script type="text/javascript" src="${urls.base}/js/homePageUtils.js?version=x"></script>
    </head>

    <body class="${bodyClasses!}" onload="${bodyOnload!}">
    <#-- supplies the faculty count to the js function that generates a random row number for the search query -->
        <@lh.facultyMemberCount  vClassGroups! />
        <#include "identity.ftl">

        <#include "menu.ftl">


        <h2 class="f3 f2-ns mb4 tc">${i18n().intro_title}</h2>

        <#if geoFocusMapsEnabled >
            <!-- Map display of researchers' areas of geographic focus. Must be enabled in runtime.properties -->
            <@lh.geographicFocusHtml />
        </#if>

        <section id="intro" role="region" class="fl-l w-30-l">

            <p class="mb3">${i18n().intro_para1}</p>
            <p>${i18n().intro_para2}</p>

            <section id="search-home" role="region" class="mt4 center cf">
                <h3 class="f3 mb3">${i18n().intro_searchvivo} <span class="search-filter-selected mb3">filteredSearch</span></h3>

                <form id="search-homepage" action="${urls.search}" name="search-home" role="search" method="post" >
                    <div id="search-home-field cf">
                        <input type="text" name="querytext" class="search-homepage fl dib pa2 w-70" value="" autocapitalize="off" />
                        <input type="submit" value="${i18n().search_button}"
                          class="search dib fl pa2 w-30 input-reset white bg-black bb bb-0 b--black br2 br--right" />
                        <input type="hidden" name="classgroup"  value="" autocapitalize="off" />
                    </div>

                    <a class="filter-search filter-default" href="#" title="${i18n().intro_filtersearch}"></a>

                    <ul id="filter-search-nav">
                        <li><a class="active" href="">${i18n().all_capitalized}</a></li>
                        <@lh.allClassGroupNames vClassGroups! />
                    </ul>
                </form>

            </section> <!-- #search-home -->

        </section> <!-- #intro -->


        <div class="mt4 cb flex flex-wrap justify-around-ns">

          <!-- List of research classes: e.g., articles, books, collections, conference papers -->
          <@lh.researchClasses />

          <!-- List of four randomly selected faculty members -->
          <@lh.facultyMbrHtml />

          <!-- List of randomly selected academic departments -->
          <@lh.academicDeptsHtml />

          <!-- Statistical information relating to property groups and their classes; displayed horizontally, not vertically-->
          <@lh.allClassGroups vClassGroups! />

        </div>

        <#include "footer.ftl">
        <#-- builds a json object that is used by js to render the academic departments section -->
        <@lh.listAcademicDepartments />
    <script>
        var i18nStrings = {
            researcherString: '${i18n().researcher}',
            researchersString: '${i18n().researchers}',
            currentlyNoResearchers: '${i18n().currently_no_researchers}',
            countriesAndRegions: '${i18n().countries_and_regions}',
            countriesString: '${i18n().countries}',
            regionsString: '${i18n().regions}',
            statesString: '${i18n().map_states_string}',
            stateString: '${i18n().map_state_string}',
            statewideLocations: '${i18n().statewide_locations}',
            researchersInString: '${i18n().researchers_in}',
            inString: '${i18n().in}',
            noFacultyFound: '${i18n().no_faculty_found}',
            placeholderImage: '${i18n().placeholder_image}',
            viewAllFaculty: '${i18n().view_all_faculty}',
            viewAllString: '${i18n().view_all}',
            viewAllDepartments: '${i18n().view_all_departments}',
            noDepartmentsFound: '${i18n().no_departments_found}'
        };
        // set the 'limmit search' text and alignment
        if  ( $('input.search-homepage').css('text-align') == "right" ) {
             $('input.search-homepage').attr("value","${i18n().limit_search} \u2192");
        }
    </script>
    </body>
</html>
