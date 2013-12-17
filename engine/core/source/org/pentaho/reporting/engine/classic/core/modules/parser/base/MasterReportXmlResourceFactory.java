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
 * Copyright (c) 2001 - 2009 Object Refinery Ltd, Pentaho Corporation and Contributors..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.modules.parser.base;

import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceData;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.xmlns.parser.AbstractXmlResourceFactory;
import org.pentaho.reporting.libraries.xmlns.parser.MultiplexRootElementHandler;
import org.pentaho.reporting.libraries.xmlns.parser.RootXmlReadHandler;

/**
 * Creation-Date: 08.04.2006, 14:27:36
 *
 * @author Thomas Morgner
 */
public class MasterReportXmlResourceFactory extends AbstractXmlResourceFactory
{
  public MasterReportXmlResourceFactory()
  {
  }

  protected Configuration getConfiguration()
  {
    return ClassicEngineBoot.getInstance().getGlobalConfig();
  }

  public Class getFactoryType()
  {
    return MasterReport.class;
  }

  protected Object finishResult(final Object res,
                                final ResourceManager manager,
                                final ResourceData data,
                                final ResourceKey context)
      throws ResourceCreationException, ResourceLoadingException
  {
    final MasterReport report = (MasterReport) res;
    if (report == null)
    {
      throw new ResourceCreationException("Report has not been parsed.");
    }

    if (context != null)
    {
      report.setContentBase(context);
    }
    else
    {
      report.setContentBase(data.getKey());
    }
    report.setDefinitionSource(data.getKey());
    report.setResourceManager(manager);
    report.updateLegacyConfiguration();
    return report;

  }

  protected Resource createResource(final ResourceKey targetKey,
                                    final RootXmlReadHandler handler,
                                    final Object createdProduct,
                                    final Class createdType)
  {
    if (ReportParserUtil.INCLUDE_PARSING_VALUE.equals(handler.getHelperObject(ReportParserUtil.INCLUDE_PARSING_KEY)))
    {
      return new ReportResource
          (targetKey, handler.getDependencyCollector(), createdProduct, createdType, false);
    }
    return new ReportResource
        (targetKey, handler.getDependencyCollector(), createdProduct, createdType, true);

  }


}
