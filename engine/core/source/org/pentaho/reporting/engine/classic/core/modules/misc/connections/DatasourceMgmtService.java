package org.pentaho.reporting.engine.classic.core.modules.misc.connections;

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;

/**
 * A design-time interface.
 */
public interface DatasourceMgmtService
{
  /**
   * Creates a JDBC datasource in a given repository and return an id
   *
   * @param databaseConnection
   * @return id
   * @throws DuplicateDatasourceException
   * @throws DatasourceMgmtServiceException
   */
  public String createDatasource(IDatabaseConnection databaseConnection) throws DuplicateDatasourceException, DatasourceMgmtServiceException;

  /**
   * Permanently deletes a JDBC datasource from a repository by name
   *
   * @param name
   * @throws NonExistingDatasourceException
   * @throws DatasourceMgmtServiceException
   */
  public void deleteDatasourceByName(String name) throws NonExistingDatasourceException, DatasourceMgmtServiceException;

  /**
   * Permanently deletes a JDBC datasource from a repository by id
   *
   * @param id
   * @throws NonExistingDatasourceException
   * @throws DatasourceMgmtServiceException
   */
  public void deleteDatasourceById(String id) throws NonExistingDatasourceException, DatasourceMgmtServiceException;

  /**
   * Retrieves a JDBC datasource form the repository by name
   *
   * @param name
   * @return IDatabaseConnection
   * @throws DatasourceMgmtServiceException
   */
  public IDatabaseConnection getDatasourceByName(String name) throws DatasourceMgmtServiceException;

  /**
   * Retrieves a JDBC datasource form the repository by id
   *
   * @param id
   * @return IDatabaseConnection
   * @throws DatasourceMgmtServiceException
   */
  public IDatabaseConnection getDatasourceById(String id) throws DatasourceMgmtServiceException;

  /**
   * Retrieves all JDBC datasources from the repository
   *
   * @return databaseConnection List
   * @throws DatasourceMgmtServiceException
   */
  public List<IDatabaseConnection> getDatasources() throws DatasourceMgmtServiceException;

  /**
   * Retrieves all JDBC datasource ids from the repository
   *
   * @return list of ids
   * @throws DatasourceMgmtServiceException
   */
  public List<String> getDatasourceIds() throws DatasourceMgmtServiceException;

  /**
   * Updates a given JDBC datasource by name
   *
   * @param name
   * @param databaseConnection
   * @return id
   * @throws NonExistingDatasourceException
   * @throws DatasourceMgmtServiceException
   */
  public String updateDatasourceByName(String name,
                                       IDatabaseConnection databaseConnection) throws NonExistingDatasourceException, DatasourceMgmtServiceException;

  /**
   * Updates a given JDBC datasource by id
   *
   * @param id
   * @param databaseConnection
   * @return id
   * @throws NonExistingDatasourceException
   * @throws DatasourceMgmtServiceException
   */
  public String updateDatasourceById(String id,
                                     IDatabaseConnection databaseConnection) throws NonExistingDatasourceException, DatasourceMgmtServiceException;
}
