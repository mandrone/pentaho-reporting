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

package org.pentaho.reporting.engine.classic.testcases.table.parser;

import org.pentaho.reporting.engine.classic.core.elementfactory.ElementFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.VerticalLineElementFactory;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.PropertyAttributes;
import org.pentaho.reporting.engine.classic.core.modules.parser.simple.readhandlers.AbstractElementReadHandler;
import org.xml.sax.SAXException;

/**
 * Creation-Date: 20.08.2007, 20:31:53
 *
 * @author Thomas Morgner
 */
public class VerticalLineElementReadHandler extends AbstractElementReadHandler
{
  private VerticalLineElementFactory factory;

  public VerticalLineElementReadHandler()
  {
    factory = new VerticalLineElementFactory();
  }

  protected ElementFactory getElementFactory()
  {
    return factory;
  }

  protected void startParsing(final PropertyAttributes atts) throws SAXException
  {
    super.startParsing(atts);

    factory.setName(atts.getValue(getUri(), "name"));
    factory.setScale(Boolean.TRUE);
    factory.setKeepAspectRatio(Boolean.FALSE);
    factory.setShouldFill(Boolean.FALSE);
    factory.setShouldDraw(Boolean.TRUE);
  }
}
