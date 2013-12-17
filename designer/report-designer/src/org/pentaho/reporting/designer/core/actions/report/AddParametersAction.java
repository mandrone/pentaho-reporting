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

package org.pentaho.reporting.designer.core.actions.report;

import java.awt.event.ActionEvent;
import javax.swing.Action;

import org.pentaho.reporting.designer.core.actions.AbstractReportContextAction;
import org.pentaho.reporting.designer.core.actions.ActionMessages;
import org.pentaho.reporting.designer.core.editor.ReportRenderContext;
import org.pentaho.reporting.designer.core.util.IconLoader;
import org.pentaho.reporting.designer.core.util.exceptions.UncaughtExceptionsModel;
import org.pentaho.reporting.engine.classic.core.AbstractReportDefinition;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.SubReport;

/**
 * Todo: Document Me
 *
 * @author Ezequiel Cuellar
 */
public final class AddParametersAction extends AbstractReportContextAction
{

  public AddParametersAction()
  {
    configureForMaster();
  }

  private void configureForSubreport()
  {
    putValue(Action.NAME, ActionMessages.getString("EditParametersAction.SubReport.Text"));
    putValue(Action.SHORT_DESCRIPTION, ActionMessages.getString("EditParametersAction.SubReport.Description"));
    putValue(Action.MNEMONIC_KEY, ActionMessages.getOptionalMnemonic("EditParametersAction.SubReport.Mnemonic"));
    putValue(Action.ACCELERATOR_KEY, ActionMessages.getOptionalKeyStroke("EditParametersAction.SubReport.Accelerator"));
    putValue(Action.SMALL_ICON, IconLoader.getInstance().getParameterIcon());
  }

  private void configureForMaster()
  {
    putValue(Action.NAME, ActionMessages.getString("AddParametersAction.Text"));
    putValue(Action.SHORT_DESCRIPTION, ActionMessages.getString("AddParametersAction.Description"));
    putValue(Action.MNEMONIC_KEY, ActionMessages.getOptionalMnemonic("AddParametersAction.Mnemonic"));
    putValue(Action.ACCELERATOR_KEY, ActionMessages.getOptionalKeyStroke("AddParametersAction.Accelerator"));
    putValue(Action.SMALL_ICON, IconLoader.getInstance().getParameterIcon());
  }

  protected void updateActiveContext(final ReportRenderContext oldContext, final ReportRenderContext newContext)
  {
    super.updateActiveContext(oldContext, newContext);
    if (newContext == null)
    {
      configureForMaster();
      return;
    }
    final AbstractReportDefinition definition = newContext.getReportDefinition();
    if (definition instanceof SubReport)
    {
      configureForSubreport();
    }
    else
    {
      configureForMaster();
    }
  }

  public void actionPerformed(final ActionEvent e)
  {
    final ReportRenderContext activeContext = getActiveContext();
    if (activeContext == null)
    {
      return;
    }
    if (activeContext.getReportDefinition() instanceof SubReport)
    {
      EditParametersAction.performEditSubReportParameters(getReportDesignerContext());
    }
    else
    {
      try
      {
        EditParametersAction.performEditMasterReportParameters(getReportDesignerContext(), null);
      }
      catch (ReportDataFactoryException e1)
      {
        UncaughtExceptionsModel.getInstance().addException(e1);
      }
    }
  }

}
