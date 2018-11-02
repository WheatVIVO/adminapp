# Wheat VIVO Admin Application and Complete Data Browser
VIVO extension for monitoring the complete data integration process.  From this application it will be possible to:
- Initiate any of the processing steps, or schedule them to run automatically at specific times
- View the progress of currently-running processing steps
- View the results of completed processing steps
- Configure parameters for retrieval and filtering of remote data
- Configure conversion
- View raw RDF data resulting from retrieval, filtering and conversion
- Create or edit reference tables for alignment of values
- Edit merge criteria
- Edit preference order of sources for selection of stable URIs and/or selection from among conflicting data
- Manually specify stable URIs or stable URI creation rules
- Run interactive test merges for viewing in the staging Wheat VIVO
- Publish data from the staging Wheat VIVO to the public Wheat VIVO
## Installation
WheatVIVO adminapp is a customization of VIVO 1.9.3.  Refer to the VIVO 1.9.x installation instructions here: <https://wiki.duraspace.org/display/VIVODOC19x/VIVO+1.9.x+Documentation> .  Additions to or changes of the standard installation instructions are noted below.
### Functional Overview
The WheatVIVO adminapp is designed to be used in conjuction with the WheatVIVO portal.  The two may be installed together or on separate servers as desired.  Only the WheatVIVO portal application is designed to be viewed publicly.
### Software Recommendations
Lack of RAM will significantly slow data loads and lead to apparent interface sluggishness as disk swap space is used especially during search index updates.  Allow a bare minimum of 4G of ram, with a practical minimum of 8G and a recommended value of 16G.  VIVO's suggested mimimum storage space of 100G continues to hold.
### Software Requirements
- WheatVIVO datasources <https://github.com/WheatVIVO/datasources> must be installed.  Clone the project from Github, change to the datasources directory, and run `mvn install`.
- MySQL is not required.  WheatVIVO adminapp is configured to use TDB, which will yield enormous speed improvements.
### Installing VIVO
After cloning this project, change to the custom-vivo directory and skip to the section "Preparing the Installation Settings".  Then run `mvn install -s settings.xml` to effect the installation.  Rejoin the VIVO documentation at the section "Completing the Installation."  When editing the runtime.properties file, ignore any database-related settings but observe the added properties specific to the WheatVIVO adminapp.  The most critical of these are the properties for specifying ORCID API credentials: these must be set in order to harvest ORCID data.
