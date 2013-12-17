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
 * Copyright (c) 2001 - 2009 Object Refinery Ltd, Pentaho Corporation and Contributors.  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.extensions.parsers.reportdesigner.datasets;

import java.util.ArrayList;

import org.pentaho.reporting.engine.classic.core.AbstractReportDefinition;
import org.pentaho.reporting.engine.classic.core.function.Expression;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.ReportParserUtil;
import org.pentaho.reporting.libraries.xmlns.parser.AbstractXmlReadHandler;
import org.pentaho.reporting.libraries.xmlns.parser.IgnoreAnyChildReadHandler;
import org.pentaho.reporting.libraries.xmlns.parser.XmlReadHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class ReportFunctionsReadHandler extends AbstractXmlReadHandler
{
  private ArrayList functions;

  public ReportFunctionsReadHandler()
  {
    functions = new ArrayList();
  }

  /**
   * Returns the handler for a child element.
   *
   * @param uri     the URI of the namespace of the current element.
   * @param tagName the tag name.
   * @param atts    the attributes.
   * @return the handler or null, if the tagname is invalid.
   * @throws SAXException if there is a parsing error.
   */
  protected XmlReadHandler getHandlerForChild(final String uri, final String tagName, final Attributes atts) throws SAXException
  {
    if (isSameNamespace(uri) == false)
    {
      return null;
    }
    if ("padding".equals(tagName))
    {
      return new IgnoreAnyChildReadHandler();
    }

    if ("child".equals(tagName))
    {
      final ReportFunctionReadHandler readHandler = new ReportFunctionReadHandler();
      functions.add(readHandler);
      return readHandler;
    }
    if ("property".equals(tagName))
    {
      return new IgnoreAnyChildReadHandler();
    }

    return null;
  }

  /**
   * Done parsing.
   *
   * @throws SAXException if there is a parsing error.
   */
  protected void doneParsing() throws SAXException
  {
    super.doneParsing();
    final AbstractReportDefinition report = (AbstractReportDefinition)
        getRootHandler().getHelperObject(ReportParserUtil.HELPER_OBJ_REPORT_NAME);

    for (int i = 0; i < functions.size(); i++)
    {
      final ReportFunctionReadHandler handler = (ReportFunctionReadHandler) functions.get(i);
      report.addExpression((Expression) handler.getObject());
    }
  }

  /**
   * Returns the object for this element or null, if this element does
   * not create an object.
   *
   * @return the object.
   * @throws SAXException if an parser error occured.
   */
  public Object getObject() throws SAXException
  {
    return null;
  }
}
