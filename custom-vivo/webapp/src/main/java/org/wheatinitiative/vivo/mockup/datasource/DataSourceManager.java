package org.wheatinitiative.vivo.mockup.datasource;

import java.util.List;

import org.wheatinitiative.vivo.datasource.DataSourceDescription;

public interface DataSourceManager {

    public abstract List<DataSourceDescription> listDataSources();
 
    public abstract DataSourceDescription getDataSource(String URI);
    
    public abstract DataSourceDescription getDataSourceByGraphURI(String graphURI);
    
}
