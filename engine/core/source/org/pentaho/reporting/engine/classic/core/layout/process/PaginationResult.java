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

package org.pentaho.reporting.engine.classic.core.layout.process;

import org.pentaho.reporting.engine.classic.core.layout.model.PageBreakPositionList;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;

/**
 * Creation-Date: 02.05.2007, 14:55:58
 *
 * @author Thomas Morgner
 */
public final class PaginationResult
{
  private PageBreakPositionList allBreaks;
  private boolean overflow;
  private boolean nextPageContainsContent;
  private ReportStateKey lastVisibleState;

  public PaginationResult(final PageBreakPositionList allBreaks,
                          final boolean overflow,
                          final boolean nextPageContainsContent,
                          final ReportStateKey lastVisibleState)
  {
    this.nextPageContainsContent = nextPageContainsContent;
    this.allBreaks = allBreaks;
    this.overflow = overflow;
    this.lastVisibleState = lastVisibleState;
  }


  public boolean isNextPageContainsContent()
  {
    return nextPageContainsContent;
  }

  public ReportStateKey getLastVisibleState()
  {
    return lastVisibleState;
  }

  public PageBreakPositionList getAllBreaks()
  {
    return allBreaks;
  }

  public boolean isOverflow()
  {
    return overflow;
  }

  public long getLastPosition()
  {
    return allBreaks.getLastMasterBreak();
  }
}
