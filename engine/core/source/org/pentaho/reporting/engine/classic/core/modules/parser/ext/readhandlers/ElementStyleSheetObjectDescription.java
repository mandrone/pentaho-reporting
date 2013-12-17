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

package org.pentaho.reporting.engine.classic.core.modules.parser.ext.readhandlers;

import java.util.ArrayList;
import java.util.Iterator;

import org.pentaho.reporting.engine.classic.core.modules.parser.ext.factory.base.ObjectDescription;
import org.pentaho.reporting.engine.classic.core.modules.parser.ext.factory.base.ObjectFactoryException;
import org.pentaho.reporting.engine.classic.core.modules.parser.ext.factory.stylekey.StyleKeyFactory;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleSheet;
import org.pentaho.reporting.engine.classic.core.style.StyleKey;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.xmlns.parser.RootXmlReadHandler;

/**
 * Creation-Date: Dec 17, 2006, 2:36:55 PM
 *
 * @author Thomas Morgner
 */
public class ElementStyleSheetObjectDescription implements ObjectDescription
{
  private StyleKeyFactory keyfactory;
  private ElementStyleSheet styleSheet;

  public ElementStyleSheetObjectDescription()
  {
  }

  /**
   * Configures this factory. The configuration contains several keys and their defined values. The given reference to
   * the configuration object will remain valid until the report parsing or writing ends.
   * <p/>
   * The configuration contents may change during the reporting.
   *
   * @param config the configuration, never null
   */
  public void configure(final Configuration config)
  {
  }

  public void init(final RootXmlReadHandler rootHandler,
                   final ElementStyleSheet styleSheet)
  {
    this.keyfactory = (StyleKeyFactory)
        rootHandler.getHelperObject(ReportDefinitionReadHandler.STYLE_FACTORY_KEY);
    this.styleSheet = styleSheet;

  }

  /**
   * Creates an object based on the description.
   *
   * @return The object.
   */
  public Object createObject()
  {
    return styleSheet;
  }

  /**
   * Returns a cloned instance of the object description. The contents of the parameter objects collection are cloned
   * too, so that any already defined parameter value is copied to the new instance.
   * <p/>
   * Parameter definitions are not cloned, as they are considered read-only.
   *
   * @return A cloned instance.
   */
  public ObjectDescription getInstance()
  {
    throw new UnsupportedOperationException("This is a private factory, go away.");
  }

  /**
   * Returns the object class.
   *
   * @return The Class.
   */
  public Class getObjectClass()
  {
    return ElementStyleSheet.class;
  }

  /**
   * Returns the value of a parameter.
   *
   * @param name the parameter name.
   * @return The value.
   */
  public Object getParameter(final String name)
  {
    final StyleKey key = keyfactory.getStyleKey(name);
    if (key == null)
    {
      throw new IllegalArgumentException("There is no handler for the stylekey: " + name);
    }
    return styleSheet.getStyleProperty(key);
  }

  /**
   * Returns a parameter definition. If the parameter is invalid, this function returns null.
   *
   * @param name the definition name.
   * @return The parameter class or null, if the parameter is not defined.
   */
  public Class getParameterDefinition(final String name)
  {
    final StyleKey key = keyfactory.getStyleKey(name);
    if (key == null)
    {
      throw new IllegalArgumentException("There is no handler for the stylekey: " + name);
    }
    return key.getValueType();
  }

  /**
   * Returns an iterator the provides access to the parameter names. This returns all _known_ parameter names, the
   * object description may accept additional parameters.
   *
   * @return The iterator.
   */
  public Iterator getParameterNames()
  {
    // don't say anything ...
    return new ArrayList().iterator();
  }

  /**
   * Returns a cloned instance of the object description. The contents of the parameter objects collection are cloned
   * too, so that any already defined parameter value is copied to the new instance.
   * <p/>
   * Parameter definitions are not cloned, as they are considered read-only.
   * <p/>
   * The newly instantiated object description is not configured. If it need to be configured, then you have to call
   * configure on it.
   *
   * @return A cloned instance.
   */
  public ObjectDescription getUnconfiguredInstance()
  {
    throw new UnsupportedOperationException("This is a private factory, go away.");
  }

  /**
   * Sets the value of a parameter.
   *
   * @param name  the parameter name.
   * @param value the parameter value.
   */
  public void setParameter(final String name, final Object value)
  {
    final StyleKey key = keyfactory.getStyleKey(name);
    if (key == null)
    {
      throw new IllegalArgumentException("There is no handler for the stylekey: " + name);
    }
    styleSheet.setStyleProperty(key, value);
  }

  /**
   * Sets the parameters of this description object to match the supplied object.
   *
   * @param o the object.
   * @throws ObjectFactoryException if there is a problem while reading the properties of the given object.
   */
  public void setParameterFromObject(final Object o)
      throws ObjectFactoryException
  {
    throw new UnsupportedOperationException("This is a private factory, go away.");
  }
}
