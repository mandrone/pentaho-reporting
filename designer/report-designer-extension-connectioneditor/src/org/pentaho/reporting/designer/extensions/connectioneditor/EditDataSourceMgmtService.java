package org.pentaho.reporting.designer.extensions.connectioneditor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.modules.misc.connections.DataSourceMgmtService;
import org.pentaho.reporting.engine.classic.core.modules.misc.connections.DatasourceMgmtServiceException;
import org.pentaho.reporting.engine.classic.core.modules.misc.connections.DuplicateDatasourceException;
import org.pentaho.reporting.engine.classic.core.modules.misc.connections.NonExistingDatasourceException;

public class EditDataSourceMgmtService implements DataSourceMgmtService
{
  private DataSourceMgmtService parent;
  private Set<String> deletedMembers;
  private HashMap<String, IDatabaseConnection> editedMembers;
  private HashMap<String, IDatabaseConnection> createdMembers;

  public EditDataSourceMgmtService()
  {
    parent = ClassicEngineBoot.getInstance().getObjectFactory().get(DataSourceMgmtService.class);

    deletedMembers = new HashSet<String>();
    editedMembers = new HashMap<String, IDatabaseConnection>();
    createdMembers = new HashMap<String, IDatabaseConnection>();
  }

  public String createDatasource(final IDatabaseConnection databaseConnection) throws DuplicateDatasourceException, DatasourceMgmtServiceException
  {
    return parent.createDatasource(databaseConnection);
  }

  public void deleteDatasourceByName(final String name) throws NonExistingDatasourceException, DatasourceMgmtServiceException
  {
    parent.deleteDatasourceByName(name);
  }

  public void deleteDatasourceById(final String id) throws NonExistingDatasourceException, DatasourceMgmtServiceException
  {
    parent.deleteDatasourceById(id);
  }

  public IDatabaseConnection getDatasourceByName(final String name) throws DatasourceMgmtServiceException
  {
    return parent.getDatasourceByName(name);
  }

  public IDatabaseConnection getDatasourceById(final String id) throws DatasourceMgmtServiceException
  {
    return parent.getDatasourceById(id);
  }

  public List<IDatabaseConnection> getDatasources() throws DatasourceMgmtServiceException
  {
    return parent.getDatasources();
  }

  public List<String> getDatasourceIds() throws DatasourceMgmtServiceException
  {
    return parent.getDatasourceIds();
  }

  public String updateDatasourceByName(final String name,
                                       final IDatabaseConnection databaseConnection) throws NonExistingDatasourceException, DatasourceMgmtServiceException
  {
    return parent.updateDatasourceByName(name, databaseConnection);
  }

  public String updateDatasourceById(final String id,
                                     final IDatabaseConnection databaseConnection) throws NonExistingDatasourceException, DatasourceMgmtServiceException
  {
    return parent.updateDatasourceById(id, databaseConnection);
  }

  public void commit ()
  {

  }
}
