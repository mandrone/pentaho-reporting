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

package org.pentaho.reporting.engine.classic.core.modules.parser.ext.factory.elements;

import org.pentaho.reporting.engine.classic.core.Element;

/**
 * A base class for implementing the {@link ElementFactory} interface.
 *
 * @author Thomas Morgner
 */
public class AbstractElementFactory implements ElementFactory
{
  /**
   * Creates a new element factory.
   */
  public AbstractElementFactory()
  {
  }

  /**
   * Registers an element.
   *
   * @param e the element.
   */
  public void registerElement(final Element e)
  {
  }

  /**
   * Registers an element.
   *
   * @param type the element type.
   * @param e    the element.
   */
  public void registerElement(final String type, final Element e)
  {
  }

  /**
   * Returns an element for the specified type. This implementation assumes, that all elements have a public default
   * constructor and uses Class.newInstance() to create a new instance of that element.
   *
   * @param type the type.
   * @return The element.
   */
  public Element getElementForType(final String type)
  {
    return new Element();
  }

  /**
   * Compares this object with the given object for equality. The object will be considered equal if it is a element
   * factory and contains the same elements.
   *
   * @param o the object that should be compared.
   * @return true, if the given object is equal, false otherwise.
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (!(o instanceof AbstractElementFactory))
    {
      return false;
    }
    return true;
  }

  /**
   * Computes an hashcode for this factory.
   *
   * @return the hashcode.
   * @see java.lang.Object#hashCode()
   */
  public int hashCode()
  {
    return 0;
  }
}
