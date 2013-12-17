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
 * Copyright (c) 2007 - 2009 Pentaho Corporation,  ..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.bugs;

import java.awt.print.PageFormat;
import java.net.URL;
import javax.swing.table.DefaultTableModel;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.SimplePageDefinition;
import org.pentaho.reporting.engine.classic.core.function.ProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.Renderer;
import org.pentaho.reporting.engine.classic.core.layout.model.BlockRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.CanvasRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.output.DefaultProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.process.IterateStructuralProcessStep;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugExpressionRuntime;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugRenderer;
import org.pentaho.reporting.engine.classic.core.util.geom.StrictGeomUtility;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Creation-Date: 10.08.2007, 13:45:14
 *
 * @author Thomas Morgner
 */
public class Pre419Test extends TestCase
{
  public Pre419Test()
  {
  }

  public Pre419Test(final String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }


  public void testPre419() throws Exception
  {
    final URL target = Pre419Test.class.getResource("Pre-419.xml");
    final ResourceManager rm = new ResourceManager();
    rm.registerDefaults();
    final Resource directly = rm.createDirectly(target, MasterReport.class);
    final MasterReport report = (MasterReport) directly.getResource();


    final ProcessingContext processingContext = new DefaultProcessingContext();
    final DebugExpressionRuntime runtime = new DebugExpressionRuntime(new DefaultTableModel(), 0, processingContext);

    final MasterReport basereport = new MasterReport();
    basereport.setPageDefinition(new SimplePageDefinition(new PageFormat()));

    final ReportStateKey stateKey = new ReportStateKey();
    final DebugRenderer debugLayoutSystem = new DebugRenderer();
    debugLayoutSystem.startReport(basereport, processingContext);
    debugLayoutSystem.startSection(Renderer.TYPE_NORMALFLOW);

    final Band band = report.getReportHeader();
    debugLayoutSystem.add(band, runtime, stateKey);

    debugLayoutSystem.endSection();
    debugLayoutSystem.endReport();

    assertEquals(Renderer.LayoutResult.LAYOUT_PAGEBREAK, debugLayoutSystem.validatePages());

    final LogicalPageBox logicalPageBox = debugLayoutSystem.getPageBox();
    // simple test, we assert that all paragraph-poolboxes are on either 485000 or 400000
    // and that only two lines exist for each
    new ValidateRunner().startValidation(logicalPageBox);

  }

  private static class ValidateRunner extends IterateStructuralProcessStep
  {
    public void startValidation(final LogicalPageBox logicalPageBox)
    {
      startProcessing(logicalPageBox);
    }

    protected boolean startCanvasBox(final CanvasRenderBox box)
    {
      if (box.getName().equals("test2"))
      {
        assertEquals("Y=10pt", StrictGeomUtility.toInternalValue(10), box.getY());
        assertEquals("Height=10pt", StrictGeomUtility.toInternalValue(10), box.getHeight());
      }
      if (box.getName().equals("test"))
      {
        assertEquals("Y=5pt", StrictGeomUtility.toInternalValue(5), box.getY());
        assertEquals("Height=20pt", StrictGeomUtility.toInternalValue(20), box.getHeight());
      }
      return super.startCanvasBox(box);
    }

    protected boolean startBlockBox(final BlockRenderBox box)
    {
      if ("reportheader".equals(box.getName()))
      {
        assertEquals("Y=0pt", 0, box.getY());
        assertEquals("Height=30pt", StrictGeomUtility.toInternalValue(30), box.getHeight());
      }
      return true;
    }
  }
}
