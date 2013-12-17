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
 * Copyright (c) 2000 - 2009 Pentaho Corporation, Simba Management Limited and Contributors..  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.testcases.base.functionality;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.Group;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.RelationalGroup;
import org.pentaho.reporting.engine.classic.demo.ancient.demo.world.CountryReportXMLDemoHandler;
import org.pentaho.reporting.engine.classic.core.event.ReportEvent;
import org.pentaho.reporting.engine.classic.core.function.AbstractFunction;
import org.pentaho.reporting.engine.classic.core.function.TotalGroupCountFunction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @deprecated
 */
public class TotalGroupCountTest extends TestCase
{
  private static final Log logger = LogFactory.getLog(TotalGroupCountTest.class);
  private static class TotalGroupCountVerifyFunction
      extends AbstractFunction
  {
    /**
     * Creates an unnamed function. Make sure the name of the function is set using
     * {@link #setName} before the function is added to the report's function collection.
     */
    public TotalGroupCountVerifyFunction()
    {
      setName("verification");
    }

    /**
     * Receives notification that a group has finished.
     *
     * @param event  the event.
     */
    public void groupFinished(final ReportEvent event)
    {
      if (event.getLevel() >= 0)
      {
        return;
      }
      assertEvent(event);
    }

    /**
     * Receives notification that a group has started.
     *
     * @param event  the event.
     */
    public void groupStarted(final ReportEvent event)
    {
      if (event.getLevel() >= 0)
      {
        return;
      }
      assertEvent(event);
    }

    private void assertEvent(final ReportEvent event)
    {
      // the number of continents in the report1
      final Number n = (Number) event.getDataRow().get("continent-total-gc");
      assertEquals("continent-total-gc", 6, n.intValue());

      // the number of continents in the report1
      // we also have the default group, so it should return the same as above
      final Number n2 = (Number) event.getDataRow().get("total-gc");
      assertEquals("total-gc", 7, n2.intValue());
    }

    public Object getValue()
    {
      return null;
    }
  }

  public TotalGroupCountTest()
  {
  }

  public TotalGroupCountTest(final String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testGroupCount()
  {
    final CountryReportXMLDemoHandler demoHandler =
            new CountryReportXMLDemoHandler();
    try
    {
      final MasterReport report = demoHandler.createReport();
      final RelationalGroup g = report.getGroupByName("default");
      if (g != null)
      {
        report.removeGroup(g);
      }
      report.addExpression(new TotalGroupCountVerifyFunction());

      final TotalGroupCountFunction f = new TotalGroupCountFunction();
      f.setName("continent-total-gc");
      f.setGroup("Continent Group");
      f.setDependencyLevel(1);
      report.addExpression(f);

      final TotalGroupCountFunction f2 = new TotalGroupCountFunction();
      f2.setName("total-gc");
      f2.setDependencyLevel(1);
      report.addExpression(f2);

      assertTrue(FunctionalityTestLib.execGraphics2D(report));
    }
    catch (Exception e)
    {
      logger.debug("Failed to parse " + demoHandler.getReportDefinitionSource(), e);
      fail();
    }


  }


  public void testGroupCount2()
  {
    final CountryReportXMLDemoHandler demoHandler =
            new CountryReportXMLDemoHandler();
    try
    {
      final MasterReport report = demoHandler.createReport();
      final RelationalGroup g = report.getGroupByName("default");
      if (g != null)
      {
        report.removeGroup(g);
      }
      report.addExpression(new TotalGroupCountVerifyFunction());

      final TotalGroupCountFunction f = new TotalGroupCountFunction();
      f.setName("continent-total-gc");
      f.setGroup("Continent Group");
      f.setDependencyLevel(1);
      report.addExpression(f);

      final TotalGroupCountFunction f2 = new TotalGroupCountFunction();
      f2.setName("total-gc");
      f2.setDependencyLevel(1);
      report.addExpression(f2);

      assertTrue(FunctionalityTestLib.execGraphics2D(report));
      assertTrue(FunctionalityTestLib.execGraphics2D(report));
      assertTrue(FunctionalityTestLib.execGraphics2D(report));
      assertTrue(FunctionalityTestLib.execGraphics2D(report));
    }
    catch (Exception e)
    {
      logger.debug("Failed to parse " + demoHandler.getReportDefinitionSource(), e);
      fail();
    }

  }




  public void testGroupCount3()
  {
    final CountryReportXMLDemoHandler demoHandler =
            new CountryReportXMLDemoHandler();
    executeFromOne(demoHandler);

  }

  private void executeFromOne(final CountryReportXMLDemoHandler demoHandler)
  {
    try
    {
      final MasterReport report = demoHandler.createReport();
      final RelationalGroup g = report.getGroupByName("default");
      if (g != null)
      {
        report.removeGroup(g);
      }
      report.addExpression(new TotalGroupCountVerifyFunction());

      final TotalGroupCountFunction f = new TotalGroupCountFunction();
      f.setName("continent-total-gc");
      f.setGroup("Continent Group");
      f.setDependencyLevel(1);
      report.addExpression(f);

      final TotalGroupCountFunction f2 = new TotalGroupCountFunction();
      f2.setName("total-gc");
      f2.setDependencyLevel(1);
      report.addExpression(f2);


      assertTrue(FunctionalityTestLib.execGraphics2D(report));
    }
    catch (Exception e)
    {
      logger.debug("Failed to parse " + demoHandler.getReportDefinitionSource(), e);
      fail();
    }
  }

  public static void main(String[] args)
  {
    
  }

}


