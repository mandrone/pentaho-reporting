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
 * Copyright (c) 2005-2011 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.extensions.datasources.cda;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.table.TableModel;

import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.ParameterMapping;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.ResourceBundleFactory;
import org.pentaho.reporting.engine.classic.core.util.TypedTableModel;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.base.util.CSVQuoter;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.formula.util.URLEncoder;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Abstract Class that will be extended by each implementation of CDA invocation LOCAL or HTTP
 *
 * @author dduque
 */
public abstract class CdaQueryBackend implements Cloneable
{
  public static final String METHOD_LIST_PARAMETERS = "listParameters";
  public static final String DATA_ACCESS_ID = "dataAccessId";
  public static final String METHOD_DO_QUERY = "doQuery";
  public static final String PARAM_NAME = "name";
  public static final String PARAM_TYPE = "type";
  public static final String PARAM_DEFAULT_VALUE = "defaultValue";
  public static final String PARAM_PATTERN = "pattern";
  public static final String TYPE_DATE = "Date";
  public static final String TYPE_INTEGER = "Integer";
  public static final String TYPE_NUMERIC = "Numeric";
  public static final String TYPE_STRING = "String";
  public static final String TYPE_ARRAY_SUFFIX = "Array";

  private String username;
  private String password;

  private String solution;
  private String path;
  private String file;

  private transient Configuration configuration;
  private transient ResourceBundleFactory resourceBundleFactory;
  private transient String baseUrl;

  public CdaQueryBackend()
  {
  }

  public void initialize(final Configuration configuration,
                         final ResourceManager resourceManager,
                         final ResourceKey contextKey,
                         final ResourceBundleFactory resourceBundleFactory)
  {

    this.configuration = configuration;
    this.resourceBundleFactory = resourceBundleFactory;
  }

  protected String parameterToString(final String name,
                                     final String type,
                                     final String pattern,
                                     final Object raw) throws ReportDataFactoryException
  {
    if (raw == null)
    {
      return "";
    }
    if (TYPE_DATE.equals(type))
    {
      if (raw instanceof Date == false && raw instanceof Number == false)
      {
        throw new ReportDataFactoryException("For parameter " + name + " Expected date, but got " + raw.getClass());
      }
      final SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, resourceBundleFactory.getLocale());
      dateFormat.setTimeZone(resourceBundleFactory.getTimeZone());
      return dateFormat.format(raw);
    }
    if (TYPE_INTEGER.equals(type) || TYPE_NUMERIC.equals(type))
    {
      if (raw instanceof Number == false)
      {
        throw new ReportDataFactoryException("For parameter " + name + " Expected number, but got " + raw.getClass());
      }
      return String.valueOf(raw);
    }
    if (TYPE_STRING.equals(type))
    {
      return String.valueOf(raw);
    }
    if (type.endsWith(TYPE_ARRAY_SUFFIX))
    {
      if (raw.getClass().isArray() == false)
      {
        if (raw instanceof String)
        {
          return raw.toString();
        }
        else
        {
          throw new ReportDataFactoryException("For parameter " + name + " Expected array, but got " + raw.getClass());
        }
      }

      final CSVQuoter quoter = new CSVQuoter(';');
      final String arrayType = type.substring(0, type.length() - 5);
      final StringBuilder b = new StringBuilder();
      final int length = Array.getLength(raw);
      for (int i = 0; i < length; i++)
      {
        final Object o = Array.get(raw, i);
        if (i > 0)
        {
          b.append(";");
        }
        final String str = parameterToString(name + "[" + i + "]", arrayType, pattern, o);
        b.append(quoter.doQuoting(str));
      }
      return b.toString();
    }
    throw new ReportDataFactoryException("Unknown type " + type + " for parameter " + name);
  }

  private String getURLEncoding()
  {
    return configuration.getConfigProperty("org.pentaho.reporting.engine.classic.core.URLEncoding");
  }

  protected String encodeParameter(final String value)
  {
    if (StringUtils.isEmpty(value))
    {
      return "";
    }
    try
    {
      return URLEncoder.encode(value, getURLEncoding());
    }
    catch (UnsupportedEncodingException e)
    {
      throw new IllegalStateException(e);
    }
  }

  protected String createURL(final String method,
                           final Map<String, String> extraParameter)
  {
    final String baseURL = getBaseUrl();

    final StringBuilder url = new StringBuilder();
    url.append(baseURL);
    url.append("/content/cda/");
    url.append(method);
    url.append("?");
    url.append("outputType=xml");
    url.append("&solution=");
    url.append(encodeParameter(getSolution()));
    url.append("&path=");
    url.append(encodeParameter(getPath()));
    url.append("&file=");
    url.append(encodeParameter(getFile()));
    for (final Map.Entry<String, String> entry : extraParameter.entrySet())
    {
      final String key = encodeParameter(entry.getKey());
      if (StringUtils.isEmpty(key))
      {
        continue;
      }
      url.append("&");
      url.append(key);
      url.append("=");
      url.append(encodeParameter(entry.getValue()));
    }
    return url.toString();
  }

  /**
   * @deprecated Replaced by fetchParameter(DataRow , CdaQueryEntry )
   */
  protected TypedTableModel fetchParameter(final DataRow dataRow, final String queryId)
      throws ReportDataFactoryException
  {
    final HashMap<String, String> extras = new HashMap<String, String>();
    extras.put(DATA_ACCESS_ID, queryId);
    return fetchData(dataRow, METHOD_LIST_PARAMETERS, extras);
  }

  protected TypedTableModel fetchParameter(final DataRow dataRow, final CdaQueryEntry realQuery)
      throws ReportDataFactoryException
  {
    final HashMap<String, String> extras = new HashMap<String, String>();
    extras.put(DATA_ACCESS_ID, realQuery.getId());
    return fetchData(dataRow, METHOD_LIST_PARAMETERS, extras);
  }

  /**
   * @deprecated Replaced by TableModel queryData(CdaQueryEntry ,  DataRow )
   */
  public synchronized TableModel queryData(final String realQuery, final DataRow parameters)
      throws ReportDataFactoryException
  {
    if (realQuery == null)
    {
      throw new NullPointerException("Query is null."); //$NON-NLS-1$
    }

    final TypedTableModel parameterModel = fetchParameter(parameters, realQuery);
    // name = 0
    // type = 1
    // defaultValue = 2
    // pattern = 3
    final HashMap<String, String> extraParams = new HashMap<String, String>();
    extraParams.put(DATA_ACCESS_ID, realQuery);

    final int nameIdx = parameterModel.findColumn(PARAM_NAME);
    final int typeIdx = parameterModel.findColumn(PARAM_TYPE);
    final int defaultValueIdx = parameterModel.findColumn(PARAM_DEFAULT_VALUE);
    final int patternIdx = parameterModel.findColumn(PARAM_PATTERN);

    for (int p = 0; p < parameterModel.getRowCount(); p++)
    {
      final String name = (String) parameterModel.getValueAt(p, nameIdx);
      final String type = (String) parameterModel.getValueAt(p, typeIdx);
      final String pattern = (String) parameterModel.getValueAt(p, patternIdx);

      // if parameter is null, use default value from cda
      final Object rawValue = parameters.get(name);
      final String param;
      if (rawValue == null)
      {
        param = (String) parameterModel.getValueAt(p, defaultValueIdx);
      }
      else
      {
        param = parameterToString(name, type, pattern, rawValue);
      }
      //set parameter like CDA Style "param<parameterName>"
      extraParams.put("param" + name, param);
    }

    return fetchData(parameters, METHOD_DO_QUERY, extraParams);
  }

  public synchronized TableModel queryData(final CdaQueryEntry realQuery,
                                           final DataRow parameters)
      throws ReportDataFactoryException
  {
    if (realQuery == null)
    {
      throw new NullPointerException("Query is null."); //$NON-NLS-1$
    }

    final TypedTableModel parameterModel = fetchParameter(parameters, realQuery);
    // name = 0
    // type = 1
    // defaultValue = 2
    // pattern = 3
    final HashMap<String, String> extraParams = new HashMap<String, String>();
    extraParams.put(DATA_ACCESS_ID, realQuery.getId());

    final int nameIdx = parameterModel.findColumn(PARAM_NAME);
    final int typeIdx = parameterModel.findColumn(PARAM_TYPE);
    final int defaultValueIdx = parameterModel.findColumn(PARAM_DEFAULT_VALUE);
    final int patternIdx = parameterModel.findColumn(PARAM_PATTERN);

    for (int p = 0; p < parameterModel.getRowCount(); p++)
    {
      final String name = (String) parameterModel.getValueAt(p, nameIdx);
      final String type = (String) parameterModel.getValueAt(p, typeIdx);
      final String pattern = (String) parameterModel.getValueAt(p, patternIdx);

      final String aliasName = findParameterAlias(realQuery, name);
      final Object rawValue = parameters.get(aliasName);
      final String param;
      if (rawValue == null)
      {
        param = (String) parameterModel.getValueAt(p, defaultValueIdx);
      }
      else
      {
        param = parameterToString(name, type, pattern, rawValue);
      }

      extraParams.put("param" + name, param);
    }

    return fetchData(parameters, METHOD_DO_QUERY, extraParams);
  }

  private String findParameterAlias(final CdaQueryEntry query, final String name)
  {
    final ParameterMapping[] parameterMapping = query.getParameters();
    for (int i = 0; i < parameterMapping.length; i++)
    {
      final ParameterMapping mapping = parameterMapping[i];
      if (name.equals(mapping.getAlias()))
      {
        return mapping.getName();
      }
    }

    return name;
  }

  /**
   * Fetch the data, has to be implemented in each sub class
   *
   * @param dataRow
   * @param method
   * @param extraParameter
   * @return
   * @throws ReportDataFactoryException
   */
  public abstract TypedTableModel fetchData(final DataRow dataRow,
                                            final String method,
                                            final Map<String, String> extraParameter) throws ReportDataFactoryException;


  public Object clone()
  {
    try
    {
      return super.clone();
    }
    catch (CloneNotSupportedException cne)
    {
      throw new IllegalStateException(cne);
    }
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

  public String getSolution()
  {
    return solution;
  }

  public void setSolution(final String solution)
  {
    this.solution = solution;
  }

  public String getPath()
  {
    return path;
  }

  public void setPath(final String path)
  {
    this.path = path;
  }

  public String getFile()
  {
    return file;
  }

  public void setFile(final String file)
  {
    this.file = file;
  }

  public String getBaseUrl()
  {
    return baseUrl;
  }

  public void setBaseUrl(final String baseUrl)
  {
    this.baseUrl = baseUrl;
  }

  public void cancelRunningQuery()
  {
  }

}
