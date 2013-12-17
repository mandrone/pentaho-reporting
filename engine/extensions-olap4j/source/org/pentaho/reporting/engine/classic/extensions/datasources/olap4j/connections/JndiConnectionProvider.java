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

package org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.connections;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.olap4j.OlapConnection;
import org.olap4j.OlapWrapper;

/**
 * Todo: Document me!
 * <p/>
 * Date: 04.05.2009
 * Time: 12:38:28
 *
 * @author Thomas Morgner.
 */
public class JndiConnectionProvider implements OlapConnectionProvider
{
  private static InitialContext initialContext;

  protected static synchronized InitialContext getInitialContext() throws NamingException
  {
    if (initialContext == null)
    {
      initialContext = new InitialContext();
    }
    return initialContext;
  }

  private static final Log logger = LogFactory.getLog(JndiConnectionProvider.class);
  private String connectionPath;
  private String username;
  private String password;

  public JndiConnectionProvider()
  {
  }

  public String getConnectionPath()
  {
    return connectionPath;
  }

  public void setConnectionPath(final String connectionPath)
  {
    this.connectionPath = connectionPath;
  }

  public String getUsername()
  {
    return username;
  }

  public void setUsername(final String username)
  {
    this.username = username;
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(final String password)
  {
    this.password = password;
  }

  public OlapConnection createConnection(final String user, final String password) throws SQLException
  {
    try
    {
      final Context ctx = getInitialContext();
      final DataSource ds = findDataSource(ctx, connectionPath);

      final String realUser;
      final String realPassword;
      if (username != null)
      {
        realUser = username;
      }
      else
      {
        realUser = user;
      }
      if (this.password != null)
      {
        realPassword = this.password;
      }
      else
      {
        realPassword = password;
      }

      if (realUser == null)
      {
        final Connection connection = ds.getConnection();
        if (connection == null)
        {
          throw new SQLException("JNDI DataSource is invalid; it returned null without throwing a meaningful error.");
        }
        if (connection instanceof OlapConnection)
        {
          return (OlapConnection) connection;
        }
        if (connection instanceof OlapWrapper)
        {
          final OlapWrapper wrapper = (OlapWrapper) connection;
          return wrapper.unwrap(OlapConnection.class);
        }
        throw new SQLException("Unable to unwrap the connection: " + connectionPath); //$NON-NLS-1$
      }

      final Connection connection = ds.getConnection(realUser, realPassword);
      if (connection == null)
      {
        throw new SQLException("JNDI DataSource is invalid; it returned null without throwing a meaningful error.");
      }
      if (connection instanceof OlapConnection)
      {
        return (OlapConnection) connection;
      }
      if (connection instanceof OlapWrapper)
      {
        final OlapWrapper wrapper = (OlapWrapper) connection;
        return wrapper.unwrap(OlapConnection.class);
      }
      throw new SQLException("Unable to unwrap the connection: " + connectionPath); //$NON-NLS-1$
    }
    catch (NamingException ne)
    {
      JndiConnectionProvider.logger.warn("Failed to access the JDNI-System", ne);
      throw new SQLException("Failed to access the JNDI system");
    }
  }

  public Object getConnectionHash()
  {
    final ArrayList<Object> list = new ArrayList<Object>();
    list.add(getClass().getName());
    list.add(connectionPath);
    list.add(username);
    return list;
  }

  private DataSource findDataSource(final Context initialContext, final String connectionPath) throws SQLException
  {
    try
    {
      final Object o = initialContext.lookup(connectionPath);
      if (o instanceof DataSource)
      {
        return (DataSource) o;
      }
    }
    catch (NamingException e)
    {
      // ignored ..
    }

    final Configuration config = ClassicEngineBoot.getInstance().getGlobalConfig();
    final Iterator keys = config.findPropertyKeys
        ("org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.jndi-prefix.");
    while (keys.hasNext())
    {
      final String key = (String) keys.next();
      final String prefix = config.getConfigProperty(key);
      try
      {
        final Object o = initialContext.lookup(prefix + connectionPath);
        if (o instanceof DataSource)
        {
          return (DataSource) o;
        }
      }
      catch (NamingException e)
      {
        // ignored ..
      }
    }

    throw new SQLException("Failed to access the JNDI system");
  }

  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final JndiConnectionProvider that = (JndiConnectionProvider) o;

    if (connectionPath != null ? !connectionPath.equals(that.connectionPath) : that.connectionPath != null)
    {
      return false;
    }
    if (password != null ? !password.equals(that.password) : that.password != null)
    {
      return false;
    }
    if (username != null ? !username.equals(that.username) : that.username != null)
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    int result;
    result = (connectionPath != null ? connectionPath.hashCode() : 0);
    result = 31 * result + (username != null ? username.hashCode() : 0);
    result = 31 * result + (password != null ? password.hashCode() : 0);
    return result;
  }
}
