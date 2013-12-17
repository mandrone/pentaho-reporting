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

import junit.framework.Test;
import junit.framework.TestSuite;

public class FunctionalityTestSuite extends TestSuite
{
  public FunctionalityTestSuite(final String s)
  {
    super(s);
    addTestSuite(BandRemoveTest.class);
    addTestSuite(PlainTextExportTest.class);
    addTestSuite(SubBandParsingTest.class);
    addTestSuite(ParseTest.class);
    addTestSuite(WriterTest.class);
    addTestSuite(ExportTest.class);
    addTestSuite(FnStyleSheetCollectionTest.class);
    addTestSuite(EventOrderTest.class);
    addTestSuite(GroupPageBreakTest.class);
    addTestSuite(TotalGroupSumTest.class);
    addTestSuite(TotalGroupCountTest.class);
    addTestSuite(TotalItemCountTest.class);
    addTestSuite(SerializationTest.class);
    addTestSuite(PDFSaveBug.class);
    addTest(new TotalFunctionsSuite(TotalFunctionsSuite.class.getName()));
    addTestSuite(SubReportTest.class);
    addTestSuite(TextAlignmentFailureTest.class);
  }

  public static Test suite()
  {
    return new FunctionalityTestSuite(FunctionalityTestSuite.class.getName());
  }

  /**
   * Dummmy method to silence the checkstyle test.
   */
  public void dummy()
  {
  }

}
