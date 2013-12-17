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
 * Copyright (c) 2007 - 2009 Pentaho Corporation, .  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.testcases.base.functionality.demohandler;

import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.PreviewDialog;
import org.pentaho.reporting.engine.classic.demo.util.AbstractXmlDemoHandler;
import org.pentaho.reporting.engine.classic.demo.util.ReportDefinitionException;

/**
 * @deprecated
 */
public class TextAlignmentFailureTestHandler extends AbstractXmlDemoHandler
{
  public TextAlignmentFailureTestHandler()
  {
  }

  public String getDemoName()
  {
    return "Testcase: A report where the group header looks weird";
  }

  public MasterReport createReport() throws ReportDefinitionException
  {
    return parseReport();
  }

  public URL getDemoDescriptionSource()
  {
    return null;
  }

  public JComponent getPresentationComponent()
  {
    return new JPanel();
  }

  public URL getReportDefinitionSource()
  {
    return getClass().getResource("text-alignment-failure.xml");
  }

  public static void main(String[] args)
      throws ReportDefinitionException
  {
    ClassicEngineBoot.getInstance().start();
    TextAlignmentFailureTestHandler handler = new TextAlignmentFailureTestHandler();
    PreviewDialog dialog = new PreviewDialog(handler.createReport());
    dialog.setSize(600, 700);
    dialog.setModal(true);
    dialog.setVisible(true);
    System.exit(0);
  }
}
