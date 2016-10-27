package org.wheatinitiative.vivo.mockup.datasource;

import java.util.List;

public interface DataSourceManager {

    public abstract List<DataSource> listDataSources();
 
    public abstract DataSource getDataSource(String URI);
    
    public abstract DataSource getDataSourceByGraphURI(String graphURI);
    
}
