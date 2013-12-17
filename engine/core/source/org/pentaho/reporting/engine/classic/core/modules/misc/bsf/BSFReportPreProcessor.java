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
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.modules.misc.bsf;

import org.pentaho.reporting.engine.classic.core.ReportPreProcessor;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.SubReport;
import org.pentaho.reporting.engine.classic.core.states.datarow.DefaultFlowController;
import org.apache.bsf.BSFManager;
import org.apache.bsf.BSFException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 30.06.2009
 * Time: 13:39:38
 *
 * @author Thomas Morgner.
 */
public class BSFReportPreProcessor implements ReportPreProcessor
{
  private String script;
  private String language;

  public BSFReportPreProcessor()
  {
  }

  public String getLanguage()
  {
    return language;
  }

  public void setLanguage(final String language)
  {
    this.language = language;
  }

  public String getScript()
  {
    return script;
  }

  public void setScript(final String script)
  {
    this.script = script;
  }

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }

  public MasterReport performPreProcessing(final MasterReport definition,
                                           final DefaultFlowController flowController) throws ReportProcessingException
  {
    if (script == null || language == null)
    {
      return definition;
    }

    try
    {
      final BSFManager interpreter = new BSFManager();
      interpreter.declareBean("definition", definition, MasterReport.class); //$NON-NLS-1$
      interpreter.declareBean("flowController", flowController, DefaultFlowController.class); //$NON-NLS-1$
      final Object o = interpreter.eval(getLanguage(), "expression", 1, 1, script);
      if (o instanceof MasterReport == false)
      {
        throw new ReportDataFactoryException("Not a MasterReport");
      }
      return (MasterReport) o; //$NON-NLS-1$

    }
    catch (BSFException e)
    {
      throw new ReportDataFactoryException("Failed to initialize the BSF-Framework", e);
    }
  }

  public SubReport performPreProcessing(final SubReport definition,
                                        final DefaultFlowController flowController) throws ReportProcessingException
  {
    if (script == null || language == null)
    {
      return definition;
    }

    try
    {
      final BSFManager interpreter = new BSFManager();
      interpreter.declareBean("definition", definition, SubReport.class); //$NON-NLS-1$
      interpreter.declareBean("flowController", flowController, DefaultFlowController.class); //$NON-NLS-1$
      final Object o = interpreter.eval(getLanguage(), "expression", 1, 1, script);
      if (o instanceof SubReport == false)
      {
        throw new ReportDataFactoryException("Not a MasterReport");
      }
      return (SubReport) o; //$NON-NLS-1$

    }
    catch (BSFException e)
    {
      throw new ReportDataFactoryException("Failed to initialize the BSF-Framework", e);
    }
  }
}
