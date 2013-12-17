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

package org.pentaho.reporting.engine.classic.testcases.base.basic;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import javax.swing.table.DefaultTableModel;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.ElementAlignment;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.TableDataFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.LabelElementFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.TextFieldElementFactory;
import org.pentaho.reporting.engine.classic.core.modules.parser.extwriter.ReportWriterException;
import org.pentaho.reporting.engine.classic.core.style.BorderStyle;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.FontDefinition;
import org.pentaho.reporting.engine.classic.testcases.base.functionality.FunctionalityTestLib;

/**
 * @deprecated moved into engine core
 */
public class BorderTest extends TestCase
{
  public BorderTest(final String arg0)
  {
    super(arg0);
  }

  public BorderTest()
  {
  }

  public void setUp() throws IOException
  {
    ClassicEngineBoot.getInstance().start();
  }

  public static void testBorder() throws IOException, ReportWriterException
  {
    Object[] columnNames = new Object[]{"Customer", "City", "Number"};

    DefaultTableModel reportTableModel = new DefaultTableModel(
        new Object[][]{{"Customer_ASDFSDFSDFSDFSaasdasdasdasweruzweurzwiezrwieuzriweuzriweu", "Bern", "123"},
            {"Hugo", "Z?rich", "2234"},},
        columnNames);

    MasterReport report = new MasterReport();

    report.setName("BorderTest");


    report.getItemBand().addElement(LabelElementFactory.createLabelElement("CustomerLabel",
        new Rectangle2D.Double(0, 0, 200, 100),
        Color.RED,
        ElementAlignment.LEFT,
        new FontDefinition("Arial", 12),
        "CustomerLabel"));

    Element element = TextFieldElementFactory.createStringElement(
        "CustomerField",
        new Rectangle2D.Double(210, 0, 150, 30),
        Color.black,
        ElementAlignment.LEFT,
        ElementAlignment.TOP,
        null, // font
        "-", // null string
        "Customer"
    );


    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_TOP_COLOR, Color.RED);
    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_TOP_WIDTH, new Float(1));
    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_TOP_STYLE, BorderStyle.SOLID);

    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_TOP_LEFT_RADIUS, new Dimension(5, 5));
    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_TOP_RIGHT_RADIUS, new Dimension(5, 5));
    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_BOTTOM_LEFT_RADIUS, new Dimension(5, 5));
    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_BOTTOM_RIGHT_RADIUS, new Dimension(5, 5));

    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_LEFT_COLOR, Color.GREEN);
    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_LEFT_WIDTH, new Float(1));
    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_LEFT_STYLE, BorderStyle.SOLID);

    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_RIGHT_COLOR, Color.YELLOW);
    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_RIGHT_WIDTH, new Float(5));
    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_RIGHT_STYLE, BorderStyle.SOLID);

    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_BOTTOM_COLOR, Color.CYAN);
    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_BOTTOM_WIDTH, new Float(5));
    element.getStyle().setStyleProperty(ElementStyleKeys.BORDER_BOTTOM_STYLE, BorderStyle.SOLID);

    element.getStyle().setStyleProperty(ElementStyleKeys.BACKGROUND_COLOR, new Color(255, 127, 127, 120));
    element.getStyle().setStyleProperty(ElementStyleKeys.PADDING_LEFT, new Float(5));
    element.getStyle().setStyleProperty(ElementStyleKeys.PADDING_TOP, new Float(5));
    element.getStyle().setStyleProperty(ElementStyleKeys.OVERFLOW_X, Boolean.TRUE);

    report.getItemBand().addElement(element);
    report.setQuery("default");
    report.setDataFactory(new TableDataFactory("default", reportTableModel));

    ClassicEngineBoot.getInstance().start();
    assertTrue(FunctionalityTestLib.execGraphics2D(report));
  }
}