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

package org.pentaho.reporting.engine.classic.core.modules.parser.bundle.layout;

import java.util.Iterator;

import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.xmlns.parser.AbstractReadHandlerFactory;

/**
 * Creation-Date: Dec 18, 2006, 1:05:00 PM
 *
 * @author Thomas Morgner
 */
public class BandedRootElementReadHandlerFactory extends AbstractReadHandlerFactory
{
  private static final String PREFIX_SELECTOR =
      "org.pentaho.reporting.engine.classic.core.modules.parser.bundle.banded-root-element-factory-prefix.";

  private static BandedRootElementReadHandlerFactory readHandlerFactory;

  public static synchronized BandedRootElementReadHandlerFactory getInstance()
  {
    if (readHandlerFactory == null)
    {
      readHandlerFactory = new BandedRootElementReadHandlerFactory();
      final Configuration config = ClassicEngineBoot.getInstance().getGlobalConfig();
      final Iterator propertyKeys = config.findPropertyKeys(BandedRootElementReadHandlerFactory.PREFIX_SELECTOR);
      while (propertyKeys.hasNext())
      {
        final String key = (String) propertyKeys.next();
        final String value = config.getConfigProperty(key);
        if (value != null)
        {
          readHandlerFactory.configure(config, value);
        }
      }
    }
    return readHandlerFactory;
  }

  private BandedRootElementReadHandlerFactory()
  {
  }

  protected Class getTargetClass()
  {
    return ElementReadHandler.class;
  }
}
