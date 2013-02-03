package org.pentaho.reporting.engine.classic.extensions.modules.connections;

import javax.sql.DataSource;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.modules.misc.connections.DBDatasourceServiceException;
import org.pentaho.reporting.engine.classic.core.modules.misc.connections.DataSourceService;
import org.pentaho.reporting.engine.classic.core.modules.misc.connections.DatasourceMgmtService;
import org.pentaho.reporting.engine.classic.core.modules.misc.connections.DatasourceMgmtServiceException;

public class PooledDataSourceService implements DataSourceService
{
  private DataSourceCache cacheManager;

  public PooledDataSourceService()
  {
    final DataSourceCacheManager manager =
        ClassicEngineBoot.getInstance().getObjectFactory().get(DataSourceCacheManager.class);
    cacheManager = manager.getDataSourceCache();
  }

  protected DataSource retrieve(final String datasource) throws DBDatasourceServiceException
  {
    try
    {
      final DatasourceMgmtService datasourceMgmtSvc =
          ClassicEngineBoot.getInstance().getObjectFactory().get(DatasourceMgmtService.class);
      final IDatabaseConnection databaseConnection = datasourceMgmtSvc.getDatasourceByName(datasource);
      if (datasource != null)
      {
        return PooledDatasourceHelper.setupPooledDataSource(databaseConnection);
      }
      else
      {
        throw new DBDatasourceServiceException
            (Messages.getInstance().getString("PooledDataSourceService.ERROR_0002_UNABLE_TO_GET_DATASOURCE")); //$NON-NLS-1$
      }
    }
    catch (DatasourceMgmtServiceException daoe)
    {
      throw new DBDatasourceServiceException
          (Messages.getInstance().getString("PooledDataSourceService.ERROR_0002_UNABLE_TO_GET_DATASOURCE"), daoe); //$NON-NLS-1$
    }
  }

  /**
   * This method clears the JNDI DS cache.  The need exists because after a JNDI
   * connection edit the old DS must be removed from the cache.
   */
  public void clearCache()
  {
    cacheManager.clear();
  }

  /**
   * This method clears the JNDI DS cache.  The need exists because after a JNDI
   * connection edit the old DS must be removed from the cache.
   */
  public void clearDataSource(final String dsName)
  {
    cacheManager.remove(dsName);
  }

  /**
   * Since JNDI is supported different ways in different app servers, it's
   * nearly impossible to have a ubiquitous way to look up a datasource. This
   * method is intended to hide all the lookups that may be required to find a
   * jndi name.
   *
   * @param dsName The Datasource name
   * @return DataSource if there is one bound in JNDI
   */
  public DataSource getDataSource(final String dsName)
      throws DBDatasourceServiceException
  {
    if (cacheManager != null)
    {
      final DataSource foundDs = cacheManager.get(dsName);
      if (foundDs != null)
      {
        return foundDs;
      }
      else
      {
        return retrieve(dsName);
      }
    }
    return null;
  }

  /**
   * Since JNDI is supported different ways in different app servers, it's
   * nearly impossible to have a ubiquitous way to look up a datasource. This
   * method is intended to hide all the lookups that may be required to find a
   * jndi name, and return the actual bound name.
   *
   * @param dsName The Datasource name (like SampleData)
   * @return The bound DS name if it is bound in JNDI (like "jdbc/SampleData")
   * @throws DBDatasourceServiceException
   */
  public String getDSBoundName(final String dsName) throws DBDatasourceServiceException
  {
    return dsName;
  }
}
