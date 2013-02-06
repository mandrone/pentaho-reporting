package org.pentaho.reporting.ui.datasources.jdbc.ui;

import java.util.ArrayList;
import javax.swing.ListModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.pentaho.reporting.ui.datasources.jdbc.connection.JdbcConnectionDefinition;
import org.pentaho.reporting.ui.datasources.jdbc.connection.JndiConnectionDefinition;

public class ConnectionsTreeModel implements TreeModel
{
  public static class MetaNode
  {
    public static final Object ROOT = new MetaNode();
    public static final Object SHARED = new MetaNode();
    public static final Object PRIVATE = new MetaNode();
  }

  private class ChangeHandler implements ListDataListener
  {
    private ChangeHandler()
    {
    }

    public void intervalAdded(final ListDataEvent e)
    {
      refresh();
    }

    public void intervalRemoved(final ListDataEvent e)
    {
      refresh();
    }

    public void contentsChanged(final ListDataEvent e)
    {
//      refresh();
    }
  }

  private final Object rootNode = MetaNode.ROOT;
  private final Object sharedNode = MetaNode.SHARED;
  private final Object privateNode = MetaNode.PRIVATE;

  private EventListenerList listenerList;
  private ArrayList<JdbcConnectionDefinition> sharedConnections;
  private ArrayList<JdbcConnectionDefinition> privateConnections;
  private final ListModel model;

  public ConnectionsTreeModel(final ListModel model)
  {
    if (model == null)
    {
      throw new NullPointerException();
    }
    listenerList = new EventListenerList();
    sharedConnections = new ArrayList<JdbcConnectionDefinition>();
    privateConnections = new ArrayList<JdbcConnectionDefinition>();
    this.model = model;
    this.model.addListDataListener(new ChangeHandler());
    refresh();
  }

  public void refresh()
  {
    sharedConnections.clear();
    privateConnections.clear();

    for (int i = 0; i < model.getSize(); i += 1)
    {
      final Object elementAt = model.getElementAt(i);
      if (elementAt instanceof JndiConnectionDefinition)
      {
        final JndiConnectionDefinition c = (JndiConnectionDefinition) elementAt;
        if (c.isShared())
        {
          sharedConnections.add(c);
          continue;
        }
      }
      privateConnections.add((JdbcConnectionDefinition) elementAt);
    }
    fireTreeDataChanged();
  }

  public void fireTreeDataChanged()
  {
    fireTreeDataChanged(new TreePath(getRoot()));
  }

  public void fireTreeDataChanged(final TreePath treePath)
  {
    final TreeModelListener[] treeModelListeners = getListeners();
    final TreeModelEvent treeEvent = new TreeModelEvent(this, treePath);
    for (int i = 0; i < treeModelListeners.length; i++)
    {
      final TreeModelListener listener = treeModelListeners[i];
      listener.treeStructureChanged(treeEvent);
    }
  }

  protected TreeModelListener[] getListeners()
  {
    return listenerList.getListeners(TreeModelListener.class);
  }

  public void addTreeModelListener(final TreeModelListener l)
  {
    listenerList.add(TreeModelListener.class, l);
  }

  public void removeTreeModelListener(final TreeModelListener l)
  {
    listenerList.remove(TreeModelListener.class, l);
  }

  public Object getRoot()
  {
    return this.rootNode;
  }

  public int getChildCount(final Object parent)
  {
    if (this.rootNode.equals(parent))
    {
      return 2;
    }
    if (this.sharedNode.equals(parent))
    {
      return sharedConnections.size();
    }
    if (this.privateNode.equals(parent))
    {
      return privateConnections.size();
    }
    return 0;
  }

  public Object getChild(final Object parent, final int index)
  {
    if (this.rootNode.equals(parent))
    {
      switch (index)
      {
        case 0:
          return this.privateNode;
        case 1:
          return this.sharedNode;
        default:
          throw new IndexOutOfBoundsException();
      }
    }

    if (this.privateNode.equals(parent))
    {
      return privateConnections.get(index);
    }
    if (this.sharedNode.equals(parent))
    {
      return sharedConnections.get(index);
    }
    return null;
  }

  public boolean isLeaf(final Object node)
  {
    if (node instanceof MetaNode)
    {
      return false;
    }
    return true;
  }

  public void valueForPathChanged(final TreePath path, final Object newValue)
  {
    fireTreeDataChanged(path);
  }

  public int getIndexOfChild(final Object parent, final Object child)
  {
    if (this.rootNode.equals(parent))
    {
      if (this.privateNode.equals(child))
      {
        return 0;
      }
      if (this.sharedNode.equals(child))
      {
        return 1;
      }
      return -1;
    }

    if (this.privateNode.equals(parent))
    {
      return privateConnections.indexOf(child);
    }
    if (this.sharedNode.equals(parent))
    {
      return sharedConnections.indexOf(child);
    }
    return -1;
  }

  public TreePath getPath(final JdbcConnectionDefinition def)
  {
    if (def instanceof JndiConnectionDefinition)
    {
      final JndiConnectionDefinition jndi = (JndiConnectionDefinition) def;
      if (jndi.isShared())
      {
        return new TreePath(new Object[]{this.rootNode, this.sharedNode, def});
      }
    }
    return new TreePath(new Object[]{this.rootNode, this.privateNode, def});
  }
}
