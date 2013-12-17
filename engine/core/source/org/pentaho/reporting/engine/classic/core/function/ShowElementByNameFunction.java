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

package org.pentaho.reporting.engine.classic.core.function;

import java.util.ArrayList;
import java.util.Arrays;

import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;

/**
 * This function hiddes the elements with the name specified in the 'element' parameter, if the given field has one of
 * the values specified in the values array.
 *
 * @author Thomas Morgner
 * @deprecated This can be done easier using style-expressions
 */
public class ShowElementByNameFunction extends AbstractElementFormatFunction
{
  /**
   * The field from where to read the compare value.
   */
  private String field;
  /**
   * The list of values.
   */
  private ArrayList values;

  /**
   * Default Constructor.
   */
  public ShowElementByNameFunction()
  {
    values = new ArrayList();
  }

  /**
   * Returns the name of the field from where the compare value is read.
   *
   * @return the name of the field.
   */
  public String getField()
  {
    return field;
  }

  /**
   * Defines the name of the field from where the compare value is read.
   *
   * @param field the name of the field.
   */
  public void setField(final String field)
  {
    this.field = field;
  }

  /**
   * Defines one of the values that hide the element. This defines the value at the given index in the list.
   *
   * @param value the compare value.
   * @param index the position in the list of all values.
   */
  public void setValues(final int index, final Object value)
  {
    if (values.size() == index)
    {
      values.add(value);
    }
    else
    {
      values.set(index, value);
    }
  }

  /**
   * Returns one of the values that hide the element. This returns the defined value at the given index in the list.
   *
   * @param index the position in the list of all values.
   * @return the value at the given position.
   */
  public Object getValues(final int index)
  {
    return values.get(index);
  }

  /**
   * Returns all known compare values as array.
   *
   * @return the values as array.
   */
  public Object[] getValues()
  {
    return values.toArray();
  }

  /**
   * Defines all values using the object from the value-array.
   *
   * @param values the new list of compare values.
   */
  public void setValues(final Object[] values)
  {
    this.values.clear();
    this.values.addAll(Arrays.asList(values));
  }

  /**
   * Returns the number of values in the compare list.
   *
   * @return the number of values.
   */
  public int getValuesCount()
  {
    return this.values.size();
  }

  /**
   * Processes the root-band. This updates the visibility of all elements with the name specified in the 'element'
   * property if the value read from the field matches one of the specified compare values.
   *
   * @param b the root band.
   */
  protected void processRootBand(final Band b)
  {
    // show, if the value in the field is not equal to the element's name.
    // this is the opposite of the HideElementByName function.
    final boolean visible = isVisible();
    final Element[] elements = FunctionUtilities.findAllElements(b, getElement());
    for (int i = 0; i < elements.length; i++)
    {
      final Element element = elements[i];
      element.setVisible(visible);
    }
  }

  /**
   * Computes the visiblity.
   *
   * @return true, if the field value matches, false otherwise.
   */
  private boolean isVisible()
  {
    final Object fieldValue = getDataRow().get(getField());
    for (int i = 0; i < values.size(); i++)
    {
      final Object o = values.get(i);
      if (ObjectUtilities.equal(fieldValue, o))
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Return a completly separated copy of this function. The copy does no longer share any changeable objects with the
   * original function.
   *
   * @return a copy of this function.
   */
  public Expression getInstance()
  {
    final ShowElementByNameFunction ex = (ShowElementByNameFunction) super.getInstance();
    ex.values = (ArrayList) values.clone();
    return ex;
  }


}
