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

import java.awt.Color;

import org.pentaho.reporting.engine.classic.core.Band;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;

/**
 * A function that alternates between true and false for each item within a group. The functions value affects a defined
 * elements color. If the function evaluates to true, the named element is painted with the colorTrue, else the element
 * is painted with colorFalse.
 * <p/>
 * Use the property <code>element</code> to name an element contained in the ItemBand whose color should be affected by
 * this function. All colors have the color 'black' by default.
 *
 * @author Thomas Morgner
 * @deprecated add a style expression for the 'paint' style instead
 */
public class ElementColorFunction extends AbstractElementFormatFunction
{
  /**
   * the color if the field is TRUE.
   */
  private Color colorTrue;
  /**
   * the color if the field is FALSE.
   */
  private Color colorFalse;

  /**
   * The field from where to read the Boolean value.
   */
  private String field;

  /**
   * Default constructor.
   */
  public ElementColorFunction()
  {
  }

  /**
   * Returns the field used by the function. The field name corresponds to a column name in the report's data-row.
   *
   * @return The field name.
   */
  public String getField()
  {
    return field;
  }

  /**
   * Sets the field name for the function. The field name corresponds to a column name in the report's data-row.
   *
   * @param field the field name.
   */
  public void setField(final String field)
  {
    this.field = field;
  }

  /**
   * Sets the color for true values.
   *
   * @param colorTrue the color.
   */
  public void setColorTrue(final Color colorTrue)
  {
    this.colorTrue = colorTrue;
  }

  /**
   * Sets the color for false values.
   *
   * @param colorFalse the color.
   */
  public void setColorFalse(final Color colorFalse)
  {
    this.colorFalse = colorFalse;
  }

  /**
   * Returns the color for true values.
   *
   * @return A color.
   */
  public Color getColorTrue()
  {
    return colorTrue;
  }

  /**
   * Returns the color for false values.
   *
   * @return A color.
   */
  public Color getColorFalse()
  {
    return colorFalse;
  }

  /**
   * Process the given band and color the named element of that band if it exists.
   *
   * @param b the band that should be colored.
   */
  protected void processRootBand(final Band b)
  {
    final Element[] elements = FunctionUtilities.findAllElements(b, getElement());
    if (elements.length == 0)
    {
      // there is no such element ! (we searched it by its name)
      return;
    }

    final boolean value = isValueTrue();
    final Color color;
    if (value)
    {
      color = getColorTrue();
    }
    else
    {
      color = getColorFalse();
    }

    for (int i = 0; i < elements.length; i++)
    {
      elements[i].getStyle().setStyleProperty(ElementStyleKeys.PAINT, color);
    }
  }

  /**
   * Computes the boolean value. This method returns true only if the value is a java.lang.Boolean with the value true.
   *
   * @return true if the datarow column contains Boolean.TRUE.
   */
  protected boolean isValueTrue()
  {
    final Object o = getDataRow().get(getField());
    return Boolean.TRUE.equals(o);
  }
}
