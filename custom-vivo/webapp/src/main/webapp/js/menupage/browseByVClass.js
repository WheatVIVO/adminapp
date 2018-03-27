/* $This file is distributed under the terms of the license in LICENSE$ */

var browseByVClass = {

    onLoad: function() {
        // page is not loaded first time ever (has artificial /categName/alpha-page added to url)
        if (this.hasPaginationSuffix()) {

          this.mergeFromTemplate();
          this.initObjects();
          this.bindEventListeners();

          // page is loaded due to back/fwd buttons
          if (this.hasBrowsedClasses(window.history)) {

            this.getIndividuals( window.history.state.uri, window.history.state.alpha,
              window.history.state.page, window.history.state.scroll, 'pageLoad');

          } else { // page is loaded due to clicking on a shared link
            // get activeCategory from url
            var splittedPath = this.splitPath();
            var activeCategory = splittedPath[splittedPath.length-2];

            // find it in browse-list and take the url
            var activeLi = Array.from(this.browseVClasses.children())
                                .find(x => x.id == activeCategory)
            var uri = activeLi.querySelector('a').dataset.uri

            var lastInPathComponents = splittedPath[splittedPath.length-1].split('-')
            var alpha = lastInPathComponents[0]
            var page = lastInPathComponents[1]

            this.getIndividuals( uri, alpha,
              page, false, 'pageLoad');
          }



        } else { // page loaded first time ever
          this.mergeFromTemplate();
          this.initObjects();
          this.bindEventListeners();
          this.defaultVClass();
        }

    },

    hasBrowsedClasses: function(historyOrEvent) {
      return (typeof historyOrEvent.state === 'object') &&
            (historyOrEvent.state !== null) &&
            (typeof historyOrEvent.state.uri == 'string')
    },

    splitPath: function() {
      return window.location.pathname.split('/')
    },

    hasPaginationSuffix: function() {
      var splittedPath = this.splitPath();
      var lastInPath = splittedPath[splittedPath.length-1];
      var lastInPathMatch = lastInPath.match(/([a-z]{1}|all)\-{1}[1-9]{1,3}/);

      if (lastInPathMatch && lastInPathMatch[0] == lastInPath) return true;

      return false;
    },

    // Add variables from menupage template
    mergeFromTemplate: function() {
        $.extend(this, menupageData);
        $.extend(this, i18nStrings);
    },

    // Create references to frequently used elements for convenience
    initObjects: function() {
        this.vgraphVClasses = $('#vgraph-classes');
        this.vgraphVClassLinks = $('#vgraph-classes li a');
        this.browseVClasses = $('#browse-classes');
        this.browseVClassLinks = $('#browse-classes li a');
        this.alphaIndex = $('#alpha-browse-individuals');
        this.alphaIndexLinks = $('#alpha-browse-individuals li a');
        this.individualsInVClass = $('#individuals-in-class ul');
        this.individualsContainer = $('#individuals-in-class');
    },

    // Event listeners. Called on page load
    bindEventListeners: function() {
        // Listeners for vClass switching
        this.vgraphVClassLinks.click(function() {
            var uri = $(this).attr('data-uri');
            browseByVClass.getIndividuals(uri);
        });

        this.browseVClassLinks.click(function() {
            var uri = $(this).attr('data-uri');
            browseByVClass.getIndividuals(uri);
            return false;
        });

        // Listener for alpha switching
        this.alphaIndexLinks.click(function() {
            var uri = $('#browse-classes li a.selected').attr('data-uri');
            var alpha = $(this).attr('data-alpha');
            browseByVClass.getIndividuals(uri, alpha);
            return false;
        });

        // Call the pagination listener
        this.paginationListener();
    },

    // Listener for page switching -- separate from the rest because it needs to be callable
    paginationListener: function() {
        $('.pagination li a').click(function() {
            var uri = $('#browse-classes li a.selected').attr('data-uri');
            var alpha = $('#alpha-browse-individuals li a.selected').attr('data-alpha');
            var page = $(this).attr('data-page');
            browseByVClass.getIndividuals(uri, alpha, page);
            return false;
        });
    },

    // Load individuals for default class as specified by menupage template
    defaultVClass: function() {
        if ( this.defaultBrowseVClassURI != "false" ) {
            if ( location.hash ) {
                // remove the trailing white space
                location.hash = location.hash.replace(/\s+/g, '');
                this.getIndividuals(location.hash.substring(1,location.hash.length), "all", 1, false, 'pageLoad');
            }
            else {
                this.getIndividuals(this.defaultBrowseVClassUri, "all", 1, false, 'pageLoad');
            }
        }
    },

    // Where all the magic happens -- gonna fetch me some individuals
    getIndividuals: function(vclassUri, alpha, page, scroll, fromEvent) {
        var url = this.dataServiceUrl + encodeURIComponent(vclassUri);
        if ( alpha && alpha != "all") {
            url += '&alpha=' + alpha;
        }
        if ( page ) {
            url += '&page=' + page;
        } else {
            page = 1;
        }
        if ( typeof scroll === "undefined" ) {
            scroll = true;
        }

        // Scroll to #menupage-intro page unless told otherwise
        if ( scroll != false ) {
            // only scroll back up if we're past the top of the #browse-by section
            var scrollPosition = browseByVClass.getPageScroll();
            var browseByOffset = $('#browse-by').offset();
            if ( scrollPosition[1] > browseByOffset.top) {
                $.scrollTo('#menupage-intro', 500);
            }
        }

        $.getJSON(url, function(results) {
            var individualList = "";

            // Catch exceptions when empty individuals result set is returned
            // This is very likely to happen now since we don't have individual counts for each letter and always allow the result set to be filtered by any letter
            if ( results.individuals.length == 0 ) {
                browseByVClass.emptyResultSet(results.vclass, alpha)
            } else {
                var vclassName = results.vclass.name;
                $.each(results.individuals, function(i, item) {
                    var individual = results.individuals[i];
                    individualList += individual.shortViewHtml;
                })

                // Remove existing content
                browseByVClass.wipeSlate();

                // And then add the new content
                browseByVClass.individualsInVClass.append(individualList);

                if (!alpha) alpha = 'all';

                var pathInHistoryState;
                var newCategory = results.vclass.name.trim().split(' ')
                    newCategory[0] = newCategory[0].toLowerCase();
                    newCategory = newCategory.join('');

                if (browseByVClass.hasPaginationSuffix()) {
                  // remove ending:  /category/suffix
                  var part = window.location.pathname.split('/').slice(0, -2).join('/')

                  // add currentCategory/newSuffix and save it to variable
                  pathInHistoryState = part + '/' + newCategory + '/' + alpha + '-' + page;
                } else {
                  pathInHistoryState = window.location.pathname + '/' + newCategory + '/' + alpha + '-' + page;
                }




                switch(fromEvent) {
                  case 'pageLoad':
                    window.history.replaceState({
                      uri: vclassUri,
                      alpha: alpha,
                      page: page,
                      scroll: scroll
                    }, null, pathInHistoryState);
                    break;
                  case 'popstate':
                    break;
                  default:
                    window.history.pushState({
                      uri: vclassUri,
                      alpha: alpha,
                      page: page,
                      scroll: scroll
                    }, null, pathInHistoryState);
                }

                // Check to see if we're dealing with pagination
                if ( results.pages.length ) {
                    var pages = results.pages;
                    browseByVClass.pagination(pages, page);
                }
            }

            // Set selected class, alpha and page
            // Do this whether or not there are any results
            $('h3.selected-class').text(results.vclass.name);
            browseByVClass.selectedVClass(results.vclass.URI);
            browseByVClass.selectedAlpha(alpha);
        });
    },

    // getPageScroll() by quirksmode.org
    getPageScroll: function() {
        var xScroll, yScroll;
        if (self.pageYOffset) {
          yScroll = self.pageYOffset;
          xScroll = self.pageXOffset;
        } else if (document.documentElement && document.documentElement.scrollTop) {
          yScroll = document.documentElement.scrollTop;
          xScroll = document.documentElement.scrollLeft;
        } else if (document.body) {// all other Explorers
          yScroll = document.body.scrollTop;
          xScroll = document.body.scrollLeft;
        }
        return new Array(xScroll,yScroll)
    },

    // Print out the pagination nav if called
    pagination: function(pages, page) {
        var pagination = '<div class="pagination menupage">';
        pagination += '<h3>' + browseByVClass.pageString + '</h3>';
        pagination += '<ul>';
        $.each(pages, function(i, item) {
            var anchorOpen = '<a class="round" href="#" title="' + browseByVClass.viewPageString + ' '
            + pages[i].text + ' '
            + browseByVClass.ofTheResults + ' " data-page="'+ pages[i].index +'">';
            var anchorClose = '</a>';

            pagination += '<li class="round';
            // Test for active page
            if ( pages[i].text == page) {
                pagination += ' selected';
                anchorOpen = "";
                anchorClose = "";
            }
            pagination += '" role="listitem">';
            pagination += anchorOpen;
            pagination += pages[i].text;
            pagination += anchorClose;
            pagination += '</li>';
        })
        pagination += '</ul>';

        // Add the pagination above and below the list of individuals and call the listener
        browseByVClass.individualsContainer.prepend(pagination);
        browseByVClass.individualsContainer.append(pagination);
        browseByVClass.paginationListener();
    },

    // Toggle the active class so it's clear which is selected
    selectedVClass: function(vclassUri) {
        // Remove active class on all vClasses
        $('#browse-classes li a.selected').removeClass('selected');

        // Add active class for requested vClass
        $('#browse-classes li a[data-uri="'+ vclassUri +'"]').addClass('selected');
    },

    // Toggle the active letter so it's clear which is selected
    selectedAlpha: function(alpha) {
        // if no alpha argument sent, assume all
        if ( !alpha ) {
            alpha = "all";
        }
        // Remove active class on all letters
        $('#alpha-browse-individuals li a.selected').removeClass('selected');

        // Add active class for requested alpha
        $('#alpha-browse-individuals li a[data-alpha="'+ alpha +'"]').addClass('selected');

        return alpha;
    },

    // Wipe the currently displayed individuals, no-content message, and existing pagination
    wipeSlate: function() {
        browseByVClass.individualsInVClass.empty();
        $('p.no-individuals').remove();
        $('.pagination').remove();
    },

    // When no individuals are returned for the AJAX request, print a reasonable message for the user
    emptyResultSet: function(vclass, alpha) {
        var nothingToSeeHere;

        this.wipeSlate();
        var alpha = this.selectedAlpha(alpha);

        if ( alpha != "all" ) {
            nothingToSeeHere = '<p class="no-individuals">' + browseByVClass.thereAreNo + ' ' + vclass.name + ' ' + browseByVClass.indNamesStartWith + ' <em>'+ alpha.toUpperCase() +'</em>.</p> <p class="no-individuals">' + browseByVClass.tryAnotherLetter + '</p>';
        } else {
            nothingToSeeHere = '<p class="no-individuals">' + browseByVClass.thereAreNo + ' ' + vclass.name + ' ' + browseByVClass.indsInSystem + '</p> <p class="no-individuals">' + browseByVClass.selectAnotherClass + '</p>';
        }

        browseByVClass.individualsContainer.prepend(nothingToSeeHere);
    }

};

$(document).ready(function() {
    browseByVClass.onLoad();
    window.addEventListener('popstate', function(e) {

      if (browseByVClass.hasBrowsedClasses(e)) {
        browseByVClass.getIndividuals(e.state.uri, e.state.alpha, e.state.page, e.state.scroll, 'popstate');
      }
    })
});
