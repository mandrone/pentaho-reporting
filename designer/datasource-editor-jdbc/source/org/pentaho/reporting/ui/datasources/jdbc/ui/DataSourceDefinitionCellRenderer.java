package org.pentaho.reporting.ui.datasources.jdbc.ui;

import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.pentaho.reporting.ui.datasources.jdbc.Messages;
import org.pentaho.reporting.ui.datasources.jdbc.connection.JdbcConnectionDefinition;

public class DataSourceDefinitionCellRenderer extends DefaultTreeCellRenderer
{
  public DataSourceDefinitionCellRenderer()
  {
  }

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
      listCellRendererComponent.setText("ROOT"); // NON-NLS
    }
    else if (ConnectionsTreeModel.MetaNode.PRIVATE.equals(value))
    {
      listCellRendererComponent.setText(Messages.getString("ConnectionPanel.StoredConnections"));
    }
    else if (ConnectionsTreeModel.MetaNode.SHARED.equals(value))
    {
      listCellRendererComponent.setText(Messages.getString("ConnectionPanel.SharedConnections"));
    }
    return listCellRendererComponent;
  }
}
