/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.ui.datasources.jdbc.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Locale;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.pentaho.reporting.engine.classic.core.designtime.DesignTimeContext;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;
import org.pentaho.reporting.libraries.base.util.ResourceBundleSupport;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.designtime.swing.BorderlessButton;
import org.pentaho.reporting.libraries.designtime.swing.LibSwingUtil;
import org.pentaho.reporting.ui.datasources.jdbc.JdbcDataSourceModule;
import org.pentaho.reporting.ui.datasources.jdbc.connection.JdbcConnectionDefinition;
import org.pentaho.reporting.ui.datasources.jdbc.connection.JndiConnectionDefinition;
import org.pentaho.ui.xul.XulException;

public abstract class ConnectionPanel extends JPanel
{
  private class DataSourceDefinitionListSelectionListener implements TreeSelectionListener
  {
    private DataSourceDefinitionListSelectionListener()
    {
    }

    public void valueChanged(final TreeSelectionEvent e)
    {
      final TreePath selectionPath = dataSourceList.getSelectionPath();
      if (selectionPath == null)
      {
        return;
      }

      final Object lastPathComponent = selectionPath.getLastPathComponent();
      if (lastPathComponent instanceof JdbcConnectionDefinition)
      {
        getDialogModel().getConnections().setSelectedItem(lastPathComponent);
      }
      else
      {
        getDialogModel().getConnections().setSelectedItem(null);
      }

    }
  }

  private static class DataSourceDefinitionListCellRenderer extends DefaultTreeCellRenderer
  {
    public Component getTreeCellRendererComponent(final JTree tree,
                                                  final Object value,
                                                  final boolean sel,
                                                  final boolean expanded,
                                                  final boolean leaf,
                                                  final int row,
                                                  final boolean hasFocus)
    {
      final JLabel listCellRendererComponent = (JLabel)
          super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
      if (value instanceof JdbcConnectionDefinition)
      {
        final JdbcConnectionDefinition def = (JdbcConnectionDefinition) value;
        final String jndiName = def.getName();
        if (!"".equals(jndiName))
        {
          listCellRendererComponent.setText(jndiName);
        }
        else
        {
          listCellRendererComponent.setText(" ");
        }
      }
      else if (ConnectionsTreeModel.MetaNode.ROOT.equals(value))
      {
        listCellRendererComponent.setText("ROOT");
      }
      else if (ConnectionsTreeModel.MetaNode.PRIVATE.equals(value))
      {
        listCellRendererComponent.setText("Stored Connections");
      }
      else if (ConnectionsTreeModel.MetaNode.SHARED.equals(value))
      {
        listCellRendererComponent.setText("Shared Connections");
      }
      return listCellRendererComponent;
    }
  }

  private class SelectionConnectionUpdateHandler implements PropertyChangeListener
  {
    private SelectionConnectionUpdateHandler()
    {
    }

    public void propertyChange(final PropertyChangeEvent aEvent)
    {
      final DataSourceDialogModel model = (DataSourceDialogModel) aEvent.getSource();
      final DefaultComboBoxModel connections = model.getConnections();
      final JdbcConnectionDefinition connection = (JdbcConnectionDefinition) connections.getSelectedItem();
      if (connection != null)
      {
        setSelectedValue(connection);
      }
      else
      {
        clearSelection();
      }
    }
  }

  private class EditDataSourceAction extends AbstractAction implements PropertyChangeListener
  {
    private EditDataSourceAction()
    {
      final URL location =
          ConnectionPanel.class.getResource("/org/pentaho/reporting/ui/datasources/jdbc/resources/Edit.png");
      if (location != null)
      {
        putValue(Action.SMALL_ICON, new ImageIcon(location));
      }
      else
      {
        putValue(Action.NAME, bundleSupport.getString("ConnectionPanel.Edit.Name"));
      }
      putValue(Action.SHORT_DESCRIPTION, bundleSupport.getString("ConnectionPanel.Edit.Description"));
      setEnabled(getDialogModel().isConnectionSelected());
    }

    public void propertyChange(final PropertyChangeEvent evt)
    {
      setEnabled(getDialogModel().isConnectionSelected());
    }

    public void actionPerformed(final ActionEvent e)
    {
      final JdbcConnectionDefinition existingConnection = getSelectedValue();

      final DesignTimeContext designTimeContext = getDesignTimeContext();
      try
      {
        final Window parentWindow = LibSwingUtil.getWindowAncestor(ConnectionPanel.this);
        final XulDatabaseDialog connectionDialog = new XulDatabaseDialog(parentWindow, designTimeContext);
        final JdbcConnectionDefinition connectionDefinition = connectionDialog.oerformEdit(existingConnection, false);

        // See if the edit completed...
        if (connectionDefinition != null)
        {
          // If the name changed, delete it before the update is performed
          if (existingConnection.getName().equals(connectionDefinition.getName()) == false)
          {
            getDialogModel().getConnectionDefinitionManager().removeSource(existingConnection.getName());
          }
          final DataSourceDialogModel dialogModel = getDialogModel();
          // Add / update the JNDI source
          getDialogModel().getConnectionDefinitionManager().updateSourceList(connectionDefinition);

          dialogModel.editConnection(existingConnection, connectionDefinition);
          setSelectedValue(connectionDefinition);
        }
      }
      catch (XulException e1)
      {
        designTimeContext.error(e1);
      }
    }
  }

  private class RemoveDataSourceAction extends AbstractAction implements PropertyChangeListener
  {
    /**
     * Defines an <code>Action</code> object with a default description string and default icon.
     */
    private RemoveDataSourceAction()
    {
      setEnabled(getDialogModel().isConnectionSelected());
      final URL resource = ConnectionPanel.class.getResource("/org/pentaho/reporting/ui/datasources/jdbc/resources/Remove.png");
      if (resource != null)
      {
        putValue(Action.SMALL_ICON, new ImageIcon(resource));
      }
      else
      {
        putValue(Action.NAME, bundleSupport.getString("ConnectionPanel.Remove.Name"));
      }
      putValue(Action.SHORT_DESCRIPTION, bundleSupport.getString("ConnectionPanel.Remove.Description"));
    }

    public void propertyChange(final PropertyChangeEvent evt)
    {
      setEnabled(getDialogModel().isConnectionSelected());
    }

    public void actionPerformed(final ActionEvent e)
    {
      final JdbcConnectionDefinition source = getSelectedValue();
      if (source != null)
      {
        getDialogModel().getConnectionDefinitionManager().removeSource(source.getName());
        getDialogModel().removeConnection(source);
      }
    }
  }

  private class AddDataSourceAction extends AbstractAction
  {
    private AddDataSourceAction()
    {
      final URL location = ConnectionPanel.class.getResource(
          "/org/pentaho/reporting/ui/datasources/jdbc/resources/Add.png");
      if (location != null)
      {
        putValue(Action.SMALL_ICON, new ImageIcon(location));
      }
      else
      {
        putValue(Action.NAME, bundleSupport.getString("ConnectionPanel.Add.Name"));
      }
      putValue(Action.SHORT_DESCRIPTION, bundleSupport.getString("ConnectionPanel.Add.Description"));
    }

    public void actionPerformed(final ActionEvent e)
    {
      final DesignTimeContext designTimeContext = getDesignTimeContext();
      try
      {
        final Window parentWindow = LibSwingUtil.getWindowAncestor(ConnectionPanel.this);
        final XulDatabaseDialog connectionDialog = new XulDatabaseDialog(parentWindow, designTimeContext);
        final JdbcConnectionDefinition connectionDefinition = connectionDialog.oerformEdit(null, false);

        if (connectionDefinition != null &&
            !StringUtils.isEmpty(connectionDefinition.getName()))
        {
          // A new JNDI source was created
          if (getDialogModel().getConnectionDefinitionManager().updateSourceList(connectionDefinition) == false)
          {
            getDialogModel().addConnection(connectionDefinition);
            setSelectedValue(connectionDefinition);
          }
        }
      }
      catch (XulException e1)
      {
        designTimeContext.error(e1);
      }
    }
  }

  private DataSourceDialogModel dialogModel;
  private DesignTimeContext designTimeContext;
  private ResourceBundleSupport bundleSupport;
  private boolean securityConfigurationAvailable;
  private ConnectionsTreeModel connectionsTreeModel;
  private JTree dataSourceList;

  public ConnectionPanel(final DataSourceDialogModel aDialogModel,
                         final DesignTimeContext designTimeContext)
  {
    this.securityConfigurationAvailable = true;
    this.dialogModel = aDialogModel;
    this.designTimeContext = designTimeContext;
    this.bundleSupport = new ResourceBundleSupport(Locale.getDefault(), JdbcDataSourceModule.MESSAGES,
        ObjectUtilities.getClassLoader(JdbcDataSourceModule.class));
  }

  protected void initPanel()
  {
    setLayout(new BorderLayout());

    connectionsTreeModel = new ConnectionsTreeModel(dialogModel.getConnections());

    final DefaultTreeSelectionModel selectionModel = new DefaultTreeSelectionModel();
    selectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    dataSourceList = new JTree(connectionsTreeModel);
    dataSourceList.setCellRenderer(new DataSourceDefinitionListCellRenderer());
    dataSourceList.setSelectionModel(selectionModel);
    dataSourceList.addTreeSelectionListener(new DataSourceDefinitionListSelectionListener());
    dataSourceList.setVisibleRowCount(10);
    dataSourceList.setRootVisible(false);

    final SelectionConnectionUpdateHandler theSelectedConnectionAction = new SelectionConnectionUpdateHandler();
    dialogModel.addPropertyChangeListener(theSelectedConnectionAction);

    final EditDataSourceAction editDataSourceAction = new EditDataSourceAction();
    dialogModel.addPropertyChangeListener(editDataSourceAction);

    final RemoveDataSourceAction removeDataSourceAction = new RemoveDataSourceAction();
    dialogModel.addPropertyChangeListener(removeDataSourceAction);

    final JPanel connectionButtonPanel = new JPanel();
    connectionButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    if (isSecurityConfigurationAvailable())
    {
      connectionButtonPanel.add(new JButton(createEditSecurityAction()));
      connectionButtonPanel.add(Box.createHorizontalStrut(40));
    }
    connectionButtonPanel.add(new BorderlessButton(editDataSourceAction));
    connectionButtonPanel.add(new BorderlessButton(new AddDataSourceAction()));
    connectionButtonPanel.add(new BorderlessButton(removeDataSourceAction));

    final JPanel connectionButtonPanelWrapper = new JPanel(new BorderLayout());
    connectionButtonPanelWrapper.add(new JLabel(bundleSupport.getString("ConnectionPanel.Connections")), BorderLayout.CENTER);
    connectionButtonPanelWrapper.add(connectionButtonPanel, BorderLayout.EAST);

    add(BorderLayout.NORTH, connectionButtonPanelWrapper);
    add(BorderLayout.CENTER, new JScrollPane(dataSourceList));
  }

  protected abstract Action createEditSecurityAction();

  public boolean isSecurityConfigurationAvailable()
  {
    return securityConfigurationAvailable;
  }

  public void setSecurityConfigurationAvailable(final boolean securityConfigurationAvailable)
  {
    this.securityConfigurationAvailable = securityConfigurationAvailable;
  }

  public DataSourceDialogModel getDialogModel()
  {
    return dialogModel;
  }

  public DesignTimeContext getDesignTimeContext()
  {
    return designTimeContext;
  }

  protected ResourceBundleSupport getBundleSupport()
  {
    return bundleSupport;
  }

  public JdbcConnectionDefinition getSelectedValue()
  {
    final TreePath selectionPath = dataSourceList.getSelectionPath();
    if (selectionPath == null)
    {
      return null;
    }
    final Object lastPathComponent = selectionPath.getLastPathComponent();
    if (lastPathComponent instanceof JndiConnectionDefinition)
    {
      return (JdbcConnectionDefinition) lastPathComponent;
    }
    return null;

  }

  public void setSelectedValue(final JdbcConnectionDefinition selectedValue)
  {
    if (selectedValue == null)
    {
      dataSourceList.clearSelection();
      return;
    }

    final TreePath path = connectionsTreeModel.getPath(selectedValue);
    dataSourceList.setSelectionPath(path);
  }

  public void clearSelection()
  {
    dataSourceList.clearSelection();
  }
}
