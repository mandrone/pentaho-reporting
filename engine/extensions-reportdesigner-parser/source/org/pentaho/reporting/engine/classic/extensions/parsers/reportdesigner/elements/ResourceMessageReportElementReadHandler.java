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
 * Copyright (c) 2008 - 2009 Pentaho Corporation, .  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.extensions.parsers.reportdesigner.elements;

import java.util.Properties;

import org.pentaho.reporting.engine.classic.core.AttributeNames;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.filter.types.ResourceLabelType;
import org.pentaho.reporting.engine.classic.core.filter.types.ResourceMessageType;
import org.xml.sax.SAXException;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class ResourceMessageReportElementReadHandler extends AbstractTextElementReadHandler
{
  public ResourceMessageReportElementReadHandler()
  {
    final Element element = new Element();
    element.setElementType(new ResourceMessageType());
    setElement(element);
  }

  /**
   * Done parsing.
   *
   * @throws SAXException if there is a parsing error.
   */
  protected void doneParsing() throws SAXException
  {
    super.doneParsing();
    final Properties result = getResult();
    final String format = result.getProperty("resourceBase");
    if (format != null)
    {
      getElement().setAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.RESOURCE_IDENTIFIER, format);
    }

    final String value = result.getProperty("formatKey");
    if (value != null)
    {
      getElement().setAttribute(AttributeNames.Core.NAMESPACE, AttributeNames.Core.VALUE, value);
    }
  }
}