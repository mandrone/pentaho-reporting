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
 * Copyright (c) 2001 - 2009 Object Refinery Ltd, Pentaho Corporation and Contributors.  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.extensions.modules.sparklines;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.metadata.ElementMetaDataParser;
import org.pentaho.reporting.libraries.base.boot.AbstractModule;
import org.pentaho.reporting.libraries.base.boot.ModuleInitializeException;
import org.pentaho.reporting.libraries.base.boot.SubSystem;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;

/**
 * This module adds support for Sparkline graphs. Sparkline graphs are tiny word sized graphs allowing a rather fast
 * reading of data evolution over the time (usual case).<br/> Such graphs can be seen as tiny linecharts or barcharts.
 * <p/>
 * For additional reading check out <a href="http://www.edwardtufte.com/bboard/q-and-a-fetch-msg?msg_id=0001OR">
 * Sparklines: theory and practice</a> thread.
 * <p/>
 * This module adds BarSparkline and LineSparkline graph element types to the reporting engine.
 *
 * @author Thomas Morgner
 */
public class SparklineModule extends AbstractModule
{
  // Extensions share the same namespace for the XML-Element itself and the Attributes for the sake of simplicity.
  public static final String NAMESPACE = SparklineAttributeNames.NAMESPACE;

  public SparklineModule() throws ModuleInitializeException
  {
    loadModuleInfo();
  }

  /**
   * Initializes the module. Use this method to perform all initial setup operations. This method is called only once in
   * a modules lifetime. If the initializing cannot be completed, throw a ModuleInitializeException to indicate the
   * error,. The module will not be available to the system.
   *
   * @param subSystem the subSystem.
   * @throws ModuleInitializeException if an error ocurred while initializing the module.
   */
  public void initialize(final SubSystem subSystem) throws ModuleInitializeException
  {
    try
    {
      final ClassLoader loader = ObjectUtilities.getClassLoader(getClass());
      Class.forName("org.pentaho.reporting.libraries.libsparklines.BarGraphDrawable", false, loader);
    }
    catch (Exception e)
    {
      throw new ModuleInitializeException("Unable to load the Sparkline library class.");
    }

    ElementMetaDataParser.initializeOptionalElementMetaData
        ("org/pentaho/reporting/engine/classic/extensions/modules/sparklines/meta-elements.xml");
    ElementMetaDataParser.initializeOptionalExpressionsMetaData
        ("org/pentaho/reporting/engine/classic/extensions/modules/sparklines/meta-expressions.xml");

  }
}
