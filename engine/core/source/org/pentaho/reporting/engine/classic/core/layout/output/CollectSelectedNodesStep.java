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

package org.pentaho.reporting.engine.classic.core.layout.output;

import java.util.ArrayList;

import org.pentaho.reporting.engine.classic.core.layout.model.BlockRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.CanvasRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.InlineRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderableReplacedContentBox;
import org.pentaho.reporting.engine.classic.core.layout.model.ParagraphRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.process.IterateStructuralProcessStep;
import org.pentaho.reporting.engine.classic.core.util.geom.StrictBounds;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class CollectSelectedNodesStep extends IterateStructuralProcessStep
{
  private static final RenderNode[] EMPTY = new RenderNode[0];
  private ArrayList resultList;
  private StrictBounds bounds;
  private StrictBounds nodebounds;
  private String namespace;
  private String name;
  private boolean strictSelection;

  public CollectSelectedNodesStep()
  {
    resultList = new ArrayList();
    strictSelection = true;
    nodebounds = new StrictBounds();
  }

  public boolean isStrictSelection()
  {
    return strictSelection;
  }

  public void setStrictSelection(final boolean strictSelection)
  {
    this.strictSelection = strictSelection;
  }

  public RenderNode[] getNodesAt(final LogicalPageBox logicalPageBox,
                                 final StrictBounds bounds,
                                 final String namespace,
                                 final String name)
  {
    if (logicalPageBox == null)
    {
      throw new NullPointerException();
    }
    if (bounds == null)
    {
      throw new NullPointerException();
    }
    if (ObjectUtilities.equal(bounds, this.bounds) &&
        ObjectUtilities.equal(namespace, this.namespace) &&
        ObjectUtilities.equal(name, this.name))
    {
      if (resultList.isEmpty())
      {
        return CollectSelectedNodesStep.EMPTY;
      }
      return (RenderNode[]) resultList.toArray(new RenderNode[resultList.size()]);
    }

    this.namespace = namespace;
    this.name = name;
    this.bounds = bounds;
    this.resultList.clear();
    startProcessing(logicalPageBox);
    if (resultList.isEmpty())
    {
      return CollectSelectedNodesStep.EMPTY;
    }
    return (RenderNode[]) resultList.toArray(new RenderNode[resultList.size()]);
  }

  protected boolean startCanvasBox(final CanvasRenderBox box)
  {
    return handleNode(box);
  }

  protected void processOtherNode(final RenderNode node)
  {
    handleNode(node);
  }

  protected boolean startBlockBox(final BlockRenderBox box)
  {
    return handleNode(box);
  }

  protected boolean startInlineBox(final InlineRenderBox box)
  {
    return handleNode(box);
  }

  protected void processRenderableContent(final RenderableReplacedContentBox box)
  {
    handleNode(box);
  }

  private boolean handleNode(final RenderNode box)
  {
    if (strictSelection)
    {
      if (box.isBoxVisible(bounds) == false)
      {
        return false;
      }
    }
    else
    {
      nodebounds.setRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
      if (StrictBounds.intersects(nodebounds, bounds) == false)
      {
        return false;
      }
    }

    if (name != null && namespace != null)
    {
      final Object attribute = box.getAttributes().getAttribute(namespace, name);
      if (attribute != null)
      {

        this.resultList.add(box);
      }
    }
    else
    {
      this.resultList.add(box);
    }
    return true;
  }

  protected boolean startOtherBox(final RenderBox box)
  {
    return handleNode(box);
  }

  protected boolean startRowBox(final RenderBox box)
  {
    return handleNode(box);
  }

  protected void processParagraphChilds(final ParagraphRenderBox box)
  {
    processBoxChilds(box);
  }
}
