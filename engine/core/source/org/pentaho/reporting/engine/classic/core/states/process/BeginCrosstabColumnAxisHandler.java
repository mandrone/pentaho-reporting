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

package org.pentaho.reporting.engine.classic.core.states.process;

import org.pentaho.reporting.engine.classic.core.CrosstabColumnGroupBody;
import org.pentaho.reporting.engine.classic.core.Group;
import org.pentaho.reporting.engine.classic.core.GroupBody;
import org.pentaho.reporting.engine.classic.core.GroupDataBody;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.RootLevelBand;
import org.pentaho.reporting.engine.classic.core.event.ReportEvent;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class BeginCrosstabColumnAxisHandler implements AdvanceHandler
{
  public static final AdvanceHandler HANDLER = new BeginCrosstabColumnAxisHandler();

  private BeginCrosstabColumnAxisHandler()
  {
  }

  public ProcessState advance(final ProcessState state) throws ReportProcessingException
  {
    final ProcessState next = state.deriveForAdvance();
    next.enterGroup();
    next.fireReportEvent();
    next.enterPresentationGroup();
    final Group group = next.getReport().getGroup(next.getCurrentGroupIndex());
    return InlineSubreportProcessor.processInline(next, group.getHeader());
  }

  public ProcessState commit(final ProcessState next) throws ReportProcessingException
  {
    final Group group = next.getReport().getGroup(next.getCurrentGroupIndex());

    final GroupBody body = group.getBody();
    if (body instanceof CrosstabColumnGroupBody)
    {
      next.setAdvanceHandler(BeginCrosstabColumnAxisHandler.HANDLER);
    }
    else if (body instanceof GroupDataBody)
    {
      next.setAdvanceHandler(BeginCrosstabFactHandler.HANDLER);
    }
    else
    {
      throw new IllegalStateException("This report is totally messed up!");
    }

    final RootLevelBand rootLevelBand = group.getHeader();
    return InlineSubreportProcessor.process(next, rootLevelBand);

  }

  public boolean isFinish()
  {
    return false;
  }

  public int getEventCode()
  {
    return ReportEvent.CROSSTABBING_COL | ReportEvent.GROUP_STARTED;
  }

  public boolean isRestoreHandler()
  {
    return false;
  }
}