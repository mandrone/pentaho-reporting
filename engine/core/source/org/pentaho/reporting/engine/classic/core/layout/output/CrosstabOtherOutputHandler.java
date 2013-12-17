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

package org.pentaho.reporting.engine.classic.core.layout.output;

import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.event.ReportEvent;
import org.pentaho.reporting.engine.classic.core.layout.Renderer;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class CrosstabOtherOutputHandler extends RelationalGroupOutputHandler
{
  public CrosstabOtherOutputHandler()
  {
  }

  public void groupBodyFinished(final DefaultOutputFunction outputFunction,
                                final ReportEvent event) throws ReportProcessingException
  {
    CrosstabLayoutUtil.handleFinishPending(outputFunction, event);

    final Renderer renderer = outputFunction.getRenderer();
    outputFunction.updateFooterArea(event);
    renderer.endGroupBody();
  }

  public void groupFinished(final DefaultOutputFunction outputFunction,
                            final ReportEvent event) throws ReportProcessingException
  {
    super.groupFinished(outputFunction, event);
  }

  public void itemsStarted(final DefaultOutputFunction outputFunction,
                           final ReportEvent event) throws ReportProcessingException
  {
    throw new ReportProcessingException("A crosstab-row cannot contain a detail band. Never.");
  }

  public void itemsAdvanced(final DefaultOutputFunction outputFunction,
                            final ReportEvent event) throws ReportProcessingException
  {
    throw new ReportProcessingException("A crosstab-row cannot contain a detail band. Never.");
  }

  public void itemsFinished(final DefaultOutputFunction outputFunction,
                            final ReportEvent event) throws ReportProcessingException
  {
    throw new ReportProcessingException("A crosstab-row cannot contain a detail band. Never.");
  }
}