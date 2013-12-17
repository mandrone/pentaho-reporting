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
 * Copyright (c) 2008 - 2009 Object Refinery Ltd, Pentaho Corporation and Contributors.  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.extensions.datasources.olap4j;

import org.pentaho.reporting.libraries.base.boot.AbstractModule;
import org.pentaho.reporting.libraries.base.boot.ModuleInitializeException;
import org.pentaho.reporting.libraries.base.boot.SubSystem;
import org.pentaho.reporting.engine.classic.core.metadata.ElementMetaDataParser;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class Olap4JDataFactoryModule extends AbstractModule
{
  public static final String NAMESPACE = "http://jfreereport.sourceforge.net/namespaces/datasources/olap4j";
  public static final String META_DOMAIN =
      "http://reporting.pentaho.org/namespaces/engine/meta-attributes/olap4j";
  public static final String TAG_DEF_PREFIX =
      "org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.tag-def.";
  public static final String CONNECTION_WRITER_PREFIX =
      "org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.writer.handler.sql-connection-provider.";


  public Olap4JDataFactoryModule() throws ModuleInitializeException
  {
    loadModuleInfo();
  }

  /**
   * Initializes the module. Use this method to perform all initial setup operations.
   * This method is called only once in a modules lifetime. If the initializing cannot
   * be completed, throw a ModuleInitializeException to indicate the error,. The module
   * will not be available to the system.
   *
   * @param subSystem the subSystem.
   * @throws ModuleInitializeException if an error ocurred while initializing the module.
   */
  public void initialize(final SubSystem subSystem) throws ModuleInitializeException
  {
    ElementMetaDataParser.initializeOptionalDataFactoryMetaData
        ("org/pentaho/reporting/engine/classic/extensions/datasources/olap4j/meta-datafactory.xml");
  }

}
