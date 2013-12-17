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
 * Copyright (c) 2005-2011 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.reporting.engine.classic.core.layout.process;

import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderLength;
import org.pentaho.reporting.engine.classic.core.layout.model.context.BoxDefinition;

public class MinorAxisLayoutStepUtil
{

  public static final RenderLength FULL_WIDTH_LENGTH = RenderLength.createFromRaw(-100);

  private MinorAxisLayoutStepUtil()
  {
  }

  /**
   * Calculates the minimum area a element will consume. The returned value references the border-box,
   * the area that includes border, padding and content-box.
   *
   * @param box
   * @return
   */
  public static long resolveNodeWidthForMinChunkCalculation(final RenderBox box)
  {
    final BoxDefinition boxDef = box.getBoxDefinition();
    final RenderLength minLength = boxDef.getMinimumWidth();
    final RenderLength prefLength = boxDef.getPreferredWidth();
    final RenderLength maxLength = boxDef.getMaximumWidth();

    final long min = minLength.resolve(0, 0);
    final long max = maxLength.resolve(0, ComputeStaticPropertiesProcessStep.MAX_AUTO);
    if (box.getBoxDefinition().isSizeSpecifiesBorderBox())
    {
      // We are assuming that any size specified by the user already includes the padding and borders.
      // min-chunk-width must take insets into account. We will not add the insets to the computed width.
      final long pref = prefLength.resolve(0, box.getInsets());
      return ProcessUtility.computeLength(min, max, pref);
    }
    else
    {
      // We are assuming that any size specified by the user does not include padding or border.
      // min-chunk-width is used without borders. We will add the insets unconditionally later.
      final long pref = prefLength.resolve(0, 0);
      return ProcessUtility.computeLength(min, max, pref) + box.getInsets();
    }

  }
}
