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

package org.pentaho.reporting.engine.classic.core.designtime;

import java.awt.Window;

import org.pentaho.reporting.engine.classic.core.AbstractReportDefinition;
import org.pentaho.reporting.engine.classic.core.wizard.DataSchemaModel;
import org.pentaho.reporting.engine.classic.core.wizard.DefaultDataSchemaModel;
import org.pentaho.reporting.libraries.designtime.swing.settings.DefaultLocaleSettings;
import org.pentaho.reporting.libraries.designtime.swing.settings.LocaleSettings;

/**
 * Todo: Document me!
 * <p/>
 * Date: 19.07.2009
 * Time: 18:17:59
 *
 * @author Thomas Morgner.
 */
public class DefaultDesignTimeContext implements DesignTimeContext
{
  private AbstractReportDefinition report;
  private Window parentWindow;
  private LocaleSettings localeSettings;

  public DefaultDesignTimeContext(final AbstractReportDefinition report)
  {
    if (report == null)
    {
      throw new NullPointerException();
    }
    this.report = report;
    this.localeSettings = new DefaultLocaleSettings();
  }

  /**
   * The currently active report (or subreport).
   *
   * @return the active report.
   */
  public AbstractReportDefinition getReport()
  {
    return report;
  }

  public void setParentWindow(final Window parentWindow)
  {
    this.parentWindow = parentWindow;
  }

  /**
   * The parent window in the GUI for showing modal dialogs.
   *
   * @return the window or null, if there is no parent.
   */
  public Window getParentWindow()
  {
    return parentWindow;
  }

  public DataSchemaModel getDataSchemaModel()
  {
    return new DesignTimeDataSchemaModel(report);
  }

  public void error(final Exception e)
  {
    e.printStackTrace();
  }

  public void userError(final Exception e)
  {
    e.printStackTrace();
  }

  public LocaleSettings getLocaleSettings()
  {
    return localeSettings;
  }
}
