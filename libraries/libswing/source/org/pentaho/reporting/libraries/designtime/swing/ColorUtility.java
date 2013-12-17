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
 * Copyright (c) 2009 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.libraries.designtime.swing;

import java.util.HashMap;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.awt.Color;

/**
 * A helper class that enables the mapping of color names and color strings given as hex-strings to be mapped into
 * java.awt.Color objects.
 */
public class ColorUtility
{
  private static final HashMap knownColorNamesByColor;
  private static final HashMap knownColorsByName;

  static
  {
    knownColorNamesByColor = new HashMap();
    knownColorsByName = new HashMap();
    try
    {
      final Field[] fields = Color.class.getFields();
      for (int i = 0; i < fields.length; i++)
      {
        final Field f = fields[i];
        if (Modifier.isPublic(f.getModifiers())
                && Modifier.isFinal(f.getModifiers())
                && Modifier.isStatic(f.getModifiers()))
        {
          final String name = f.getName();
          final Object oColor = f.get(null);
          if (oColor instanceof Color)
          {
            knownColorNamesByColor.put(oColor, name.toLowerCase());
            knownColorsByName.put(name.toLowerCase(), oColor);
          }
        }
      }
    }
    catch (Exception e)
    {
      // ignore ..
    }
  }

  /**
   * Creates a new value converter.
   */
  public ColorUtility()
  {
    super();
  }

  /**
   * Converts the attribute to a string.
   *
   * @param o the attribute ({@link Integer} expected).
   * @return A string representing the {@link Integer} value.
   */
  public static String toAttributeValue (final Object o)
  {
    if (o == null)
    {
      return null;
    }
    
    if (!(o instanceof Color))
    {
      throw new ClassCastException("Is no instance of java.awt.Color");
    }
    final Color c = (Color) o;
    final String name = (String) ColorUtility.knownColorNamesByColor.get(c);
    if (name != null)
    {
      return name;
    }

    // no defined constant color, so this must be a user defined color
    final String color = Integer.toHexString(c.getRGB() & 0x00ffffff);
    final StringBuilder retval = new StringBuilder(7);
    retval.append('#');

    final int fillUp = 6 - color.length();
    for (int i = 0; i < fillUp; i++)
    {
      retval.append('0');
    }

    retval.append(color);
    return retval.toString();
  }

  /**
   * Converts a string to a {@link Integer}.
   *
   * @param value the string.
   * @return a {@link Integer}.
   */
  public static Object toPropertyValue (final String value)
  {
    if (value == null)
    {
      return null;
    }

    final Object o = ColorUtility.knownColorsByName.get(value.toLowerCase());
    if (o != null)
    {
      return o;
    }

    try
    {
      // get color by hex or octal value
      return Color.decode(value.trim());
    }
    catch (NumberFormatException nfe)
    {
      // if we can't decode lets try to get it by name
      throw new IllegalArgumentException
              ("The color string '" + value + "' is not recognized.");
    }
  }

  /**
   * Returns the 64 colors that are defined in Excel.
   *
   * @return the excel standard colors.
   */
  public static Color[] getPredefinedExcelColors()
  {
    return new Color[]{
        new Color(0, 0, 0),
        new Color(255, 255, 255),
        new Color(255, 0, 0),
        new Color(0, 255, 0),
        new Color(0, 0, 255),
        new Color(255, 255, 0),
        new Color(255, 0, 255),
        new Color(0, 255, 255),
        new Color(128, 0, 0),
        new Color(0, 128, 0),
        new Color(0, 0, 128),
        new Color(128, 128, 0),
        new Color(128, 0, 128),
        new Color(0, 128, 128),
        new Color(192, 192, 192),
        new Color(128, 128, 128),
        new Color(153, 153, 255),
        new Color(153, 51, 102),
        new Color(255, 255, 204),
        new Color(204, 255, 255),
        new Color(102, 0, 102),
        new Color(255, 128, 128),
        new Color(0, 102, 204),
        new Color(204, 204, 255),
        new Color(0, 0, 128),
        new Color(255, 0, 255),
        new Color(255, 255, 0),
        new Color(0, 255, 255),
        new Color(128, 0, 128),
        new Color(128, 0, 0),
        new Color(0, 128, 128),
        new Color(0, 0, 255),
        new Color(0, 204, 255),
        new Color(204, 255, 255),
        new Color(204, 255, 204),
        new Color(255, 255, 153),
        new Color(153, 204, 255),
        new Color(255, 153, 204),
        new Color(204, 153, 255),
        new Color(51, 102, 255),
        new Color(51, 204, 204),
        new Color(153, 204, 0),
        new Color(255, 204, 0),
        new Color(255, 153, 0),
        new Color(255, 102, 0),
        new Color(102, 102, 153),
        new Color(150, 150, 150),
        new Color(0, 51, 102),
        new Color(51, 153, 102),
        new Color(0, 51, 0),
        new Color(51, 51, 0),
        new Color(153, 51, 0),
        new Color(153, 51, 102),
        new Color(51, 51, 153),
        new Color(51, 51, 51),
    };
  }
}
