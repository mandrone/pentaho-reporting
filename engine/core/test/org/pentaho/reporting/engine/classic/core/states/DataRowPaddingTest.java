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

package org.pentaho.reporting.engine.classic.core.states;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.DefaultResourceBundleFactory;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.function.ProcessingContext;
import org.pentaho.reporting.engine.classic.core.layout.output.DefaultProcessingContext;
import org.pentaho.reporting.engine.classic.core.states.crosstab.CrosstabSpecification;
import org.pentaho.reporting.engine.classic.core.states.crosstab.SortedMergeCrosstabSpecification;
import org.pentaho.reporting.engine.classic.core.states.datarow.GlobalMasterRow;
import org.pentaho.reporting.engine.classic.core.states.datarow.MasterDataRow;
import org.pentaho.reporting.engine.classic.core.states.datarow.ReportDataRow;
import org.pentaho.reporting.engine.classic.core.wizard.DefaultDataSchemaDefinition;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;


/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class DataRowPaddingTest extends TestCase
{
  private static final Log logger = LogFactory.getLog(DataRowPaddingTest.class);

  public DataRowPaddingTest()
  {
  }

  private static void addRow(final DefaultTableModel model,
                             final String region,
                             final String product,
                             final String year)
  {
    model.addRow(new Object[]{region, product, year});
  }

  public static TableModel createIncompleteTableModel()
  {
    final DefaultTableModel model = new DefaultTableModel(new String[]{"Region", "Product", "Time"}, 0);
//    addRow(model, "AMEA", "Planes", "2001");
    addRow(model, "AMEA", "Planes", "2002");
    addRow(model, "AMEA", "Planes", "2003");
    addRow(model, "AMEA", "Planes", "2004");
    addRow(model, "Europe", "Planes", "2001");
//    addRow(model, "Europe", "Planes", "2003");
    addRow(model, "Europe", "Planes", "2004");
    addRow(model, "Japan", "Planes", "2002");
    addRow(model, "Japan", "Planes", "2003");
    return model;
  }

  public static TableModel createMoreCompleteTableModel()
  {
    final DefaultTableModel model = new DefaultTableModel(new String[]{"Region", "Product", "Time"}, 0);
//    addRow(model, "AMEA", "Planes", "2001");
    addRow(model, "AMEA", "Planes", "2002");
    addRow(model, "AMEA", "Planes", "2003");
    addRow(model, "AMEA", "Planes", "2004");
    addRow(model, "Europe", "Planes", "2001");
//    addRow(model, "Europe", "Planes", "2003");
    addRow(model, "Europe", "Planes", "2004");
    addRow(model, "Japan", "Planes", "2002");
    addRow(model, "Japan", "Planes", "2003");
    return model;
  }

  public void testBuildCrosstab2()
  {
    doIt(createIncompleteTableModel());
  }

  public void testBuildCrosstab1()
  {
    doIt(createMoreCompleteTableModel());
  }

  private void doIt(final TableModel data)
  {
    final CrosstabSpecification specification = buildCS(data);
    if (specification.size() != 4)
    {
      throw new IllegalStateException("Expected Size of 4 but got " + specification.size());
    }

    // second run. Now with padding ..
    final ProcessingContext prc = new DefaultProcessingContext();
    final GlobalMasterRow gmr = GlobalMasterRow.createReportRow
        (prc, new DefaultDataSchemaDefinition(), new ParameterDataRow(), null, true);
    MasterDataRow wdata = gmr.deriveWithQueryData(new ReportDataRow(data));
    int advanceCount = 1;
    wdata = wdata.startCrosstabMode(specification);
    logger.debug("Region:  " + wdata.getGlobalView().get("Region"));
    logger.debug("Product: " + wdata.getGlobalView().get("Product"));
    logger.debug("Year:    " + wdata.getGlobalView().get("Time"));
    Object grpVal = wdata.getGlobalView().get("Region");
    while (wdata.isAdvanceable())
    {
      logger.debug("-- Advance -- ");
      MasterDataRow nextdata = wdata.advance();
      final Object nextGrpVal = nextdata.getGlobalView().get("Region");
      if (ObjectUtilities.equal(grpVal, nextGrpVal) == false)
      {
        nextdata = nextdata.resetRowCursor();
      }

      logger.debug("Do Advance Count: " + nextdata.getReportDataRow().getCursor());
      logger.debug("Region:  " + nextdata.getGlobalView().get("Region"));
      logger.debug("Product: " + nextdata.getGlobalView().get("Product"));
      logger.debug("Year:    " + nextdata.getGlobalView().get("Time"));
      advanceCount += 1;
      wdata = nextdata;
      grpVal = nextGrpVal;
    }

    assertEquals(advanceCount, (3 * 4));
  }

  private static CrosstabSpecification buildCS(final TableModel data)
  {
    final ProcessingContext prc = new DefaultProcessingContext();
    final GlobalMasterRow gmr = GlobalMasterRow.createReportRow
        (prc, new DefaultDataSchemaDefinition(), new ParameterDataRow(), null, true);

    MasterDataRow masterDataRow = gmr.deriveWithQueryData(new ReportDataRow(data));
    final CrosstabSpecification crosstabSpecification = new SortedMergeCrosstabSpecification
        (new ReportStateKey(), new String[]{"Product", "Time"});

    int advanceCount = 0;
    logger.debug("Building Crosstab: Cursor: " + String.valueOf(masterDataRow.getReportDataRow().getCursor()));
    crosstabSpecification.startRow();
    crosstabSpecification.add(masterDataRow.getGlobalView());
    Object grpVal = masterDataRow.getGlobalView().get("Region");
    while (masterDataRow.isAdvanceable())
    {
      final MasterDataRow nextdata = masterDataRow.advance();
      final Object nextGrpVal = nextdata.getGlobalView().get("Region");
      if (ObjectUtilities.equal(grpVal, nextGrpVal) == false)
      {
        crosstabSpecification.endRow();
        crosstabSpecification.startRow();
      }

      crosstabSpecification.add(nextdata.getGlobalView());
      logger.debug("Prepare Advance Count: " + nextdata.getReportDataRow().getCursor());
      advanceCount += 1;
      masterDataRow = nextdata;
      grpVal = nextGrpVal;
    }
    crosstabSpecification.endRow();
    if (advanceCount != (data.getRowCount() - 1))
    {
      throw new IllegalStateException("Expected 6 but got " + advanceCount);
    }
    return crosstabSpecification;
  }

}
