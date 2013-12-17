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

package org.pentaho.reporting.engine.classic.core.layout.process;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.InvalidReportStateException;
import org.pentaho.reporting.engine.classic.core.layout.model.BreakMarkerRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.LayoutNodeTypes;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.PageBreakPositionList;
import org.pentaho.reporting.engine.classic.core.layout.model.PageBreakPositions;
import org.pentaho.reporting.engine.classic.core.layout.model.ParagraphRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderLength;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.engine.classic.core.layout.model.context.StaticBoxLayoutProperties;
import org.pentaho.reporting.engine.classic.core.layout.process.util.InitialPaginationShiftState;
import org.pentaho.reporting.engine.classic.core.layout.process.util.PaginationShiftState;
import org.pentaho.reporting.engine.classic.core.layout.process.util.PaginationShiftStatePool;
import org.pentaho.reporting.engine.classic.core.layout.process.util.PaginationTableState;
import org.pentaho.reporting.engine.classic.core.states.ReportStateKey;

/**
 * This class uses the concept of shifting to push boxes, which otherwise do not fit on the current page, over the
 * page-boundary of the next page.
 * <p/>
 * We have two shift positions. The normal shift denotes artificial paddings, inserted into the flow where needed to
 * move content to the next page. The header-shift is inserted when a repeatable table-header is processed. This header
 * reserves a virtual padding area in the infinite-canvas flow to push the next assumed pagebreak to the y2-position
 * of the header. A header-shift modifies the pin-position on a box, and modifies where pagebreaks are detected.
 */
public final class PaginationStep extends IterateVisualProcessStep
{
  private static final Log logger = LogFactory.getLog(PaginationStep.class);
  private boolean breakPending;
  private FindOldestProcessKeyStep findOldestProcessKeyStep;
  private PageBreakPositionList basePageBreakList;
  private ReportStateKey visualState;
  private BreakMarkerRenderBox breakIndicatorEncountered;
  private PaginationTableState paginationTableState;
  private PaginationShiftState shiftState;
  private PaginationShiftStatePool shiftStatePool;
  private boolean unresolvedWidowReferenceEncountered;
  private long usablePageHeight;

  public PaginationStep()
  {
    findOldestProcessKeyStep = new FindOldestProcessKeyStep();
    basePageBreakList = new PageBreakPositionList();
    shiftStatePool = new PaginationShiftStatePool();
  }

  public PaginationResult performPagebreak(final LogicalPageBox pageBox)
  {
    PaginationStepLib.assertProgress(pageBox);

    if (logger.isDebugEnabled())
    {
      logger.debug("Start pagination ... " + pageBox.getPageOffset());
    }
    this.unresolvedWidowReferenceEncountered = false;
    this.breakIndicatorEncountered = null;
    this.visualState = null;
    this.shiftState = new InitialPaginationShiftState();
    this.breakPending = false;
    this.usablePageHeight = Long.MAX_VALUE;

    try
    {
      final long[] allCurrentBreaks = pageBox.getPhysicalBreaks(RenderNode.VERTICAL_AXIS);
      if (allCurrentBreaks.length == 0)
      {
        // No maximum height.
        throw new InvalidReportStateException("No page given. This is really bad.");
      }

      // Note: For now, we limit both the header and footer to a single physical
      // page. This safes me a lot of trouble for now.
      final long lastBreakLocal = allCurrentBreaks[allCurrentBreaks.length - 1];
      final long reservedHeight = PaginationStepLib.restrictPageAreaHeights(pageBox, allCurrentBreaks);
      if (reservedHeight >= lastBreakLocal)
      {
        // This is also bad. There will be no space left to print a single element.
        throw new InvalidReportStateException("Header and footer consume the whole page. No space left for normal-flow.");
      }

      PaginationStepLib.configureBreakUtility(basePageBreakList, pageBox, allCurrentBreaks, reservedHeight, lastBreakLocal);

      final long pageEnd = basePageBreakList.getLastMasterBreak();
      final long pageHeight = pageBox.getPageHeight();
      this.paginationTableState = new PaginationTableState
          (pageHeight, pageBox.getPageOffset(), pageEnd, basePageBreakList);

      // now process all the other content (excluding the header and footer area)
      if (startBlockLevelBox(pageBox))
      {
        processBoxChilds(pageBox);
      }
      finishBlockLevelBox(pageBox);

      PaginationStepLib.assertProgress(pageBox);

      final long usedPageHeight = Math.min(pageBox.getHeight(), usablePageHeight);
      final long masterBreak = basePageBreakList.getLastMasterBreak();
      final boolean overflow;
      if (breakIndicatorEncountered != null)
      {
        if (breakIndicatorEncountered.getY() <= pageBox.getPageOffset())
        {
          overflow = usedPageHeight > masterBreak;
        }
        else
        {
          overflow = true;
        }
      }
      else
      {
        overflow = usedPageHeight > masterBreak;
      }
      final boolean nextPageContainsContent = (pageBox.getHeight() > masterBreak);
      return new PaginationResult(basePageBreakList, overflow, nextPageContainsContent, visualState);
    }
    finally
    {
      this.breakIndicatorEncountered = null;
      this.paginationTableState = null;
      this.visualState = null;
      this.shiftState = null;
    }
  }

  protected void processParagraphChilds(final ParagraphRenderBox box)
  {
    processBoxChilds(box);
  }

  protected boolean startBlockLevelBox(final RenderBox box)
  {
    final boolean retval = handleStartBlockLevelBox(box);
    return retval;
  }

  private boolean handleStartBlockLevelBox(final RenderBox box)
  {
    this.shiftState = shiftStatePool.create(box, shiftState);
    final long shift = shiftState.getShiftForNextChild();

    PaginationStepLib.assertBlockPosition(box, shift);
    handleBlockLevelBoxFinishedMarker(box, shift);

    if (shiftState.isManualBreakSuspended() == false)
    {
      if (handleManualBreakOnBox(box, shiftState, breakPending))
      {
        breakPending = false;
        if (logger.isDebugEnabled())
        {
          logger.debug("pending page-break or manual break: " + box);
        }
        return true;
      }
      breakPending = false;
    }

    // If this box does not cross any (major or minor) break, it may need no additional shifting at all.
    final RenderLength fixedPositionLength = box.getBoxDefinition().getFixedPosition();
    if (shiftState.isManualBreakSuspended() ||
        RenderLength.AUTO.equals(fixedPositionLength) ||
        paginationTableState.isFixedPositionProcessingSuspended())
    {
      return handleAutomaticPagebreak(box, shiftState);
    }

    // If you've come this far, this means, that your box has a fixed position defined.
    final long boxY = box.getY();
    final long shiftedBoxPosition = boxY + shift;
    final long fixedPositionResolved = fixedPositionLength.resolve(paginationTableState.getPageHeight(), 0);
    final PageBreakPositions breakUtility = paginationTableState.getBreakPositions();
    final long fixedPositionInFlow = breakUtility.computeFixedPositionInFlow(shiftedBoxPosition, fixedPositionResolved);
    if (fixedPositionInFlow < shiftedBoxPosition)
    {
      // ... but the fixed position is invalid, so treat it as non-defined.
      return handleAutomaticPagebreak(box, shiftState);
    }

    // The computed break seems to be valid.
    // Compute what happens if the whole box can fit on the current page.
    // We have an opportunity to optimize our processing by skipping all content if there are no
    // manual pagebreaks defined on one of the childs.
    if (breakUtility.isCrossingPagebreakWithFixedPosition
        (shiftedBoxPosition, box.getHeight(), fixedPositionResolved) == false)
    {
      return handleFixedPositionWithoutBreakOnBox(box, shift, fixedPositionInFlow);
    }

    // The box will not fit on the current page.
    //
    // A box with a fixed position will always be printed at this position, even if it does not seem
    // to fit there. If we move the box, we would break the explicit layout constraint 'fixed-position' in
    // favour of an implicit one ('page-break: avoid').

    // Treat as if there is enough space available. Start the normal processing.
    final long fixedPositionDelta = fixedPositionInFlow - shiftedBoxPosition;
    shiftState.setShift(shift + fixedPositionDelta);
    box.setY(fixedPositionInFlow);
    BoxShifter.extendHeight(box.getParent(), box, fixedPositionDelta);
    updateStateKey(box);
    return true;
  }

  private boolean handleFixedPositionWithoutBreakOnBox(final RenderBox box,
                                                       final long shift,
                                                       final long fixedPositionInFlow)
  {
    final long boxY = box.getY();
    final long shiftedBoxPosition = boxY + shift;
    final long fixedPositionDelta = fixedPositionInFlow - shiftedBoxPosition;
    final int breakIndicator = box.getManualBreakIndicator();
    if (breakIndicator == RenderBox.INDIRECT_MANUAL_BREAK)
    {
      // One of the children of this box will cause a manual pagebreak. We have to dive deeper into this child.
      // for now, we will only apply the ordinary shift.
      box.setY(fixedPositionInFlow);
      shiftState.setShift(shift + fixedPositionDelta);
      BoxShifter.extendHeight(box.getParent(), box, fixedPositionDelta);
      updateStateKey(box);
      return true;
    }
    else // if (breakIndicator == RenderBox.BreakIndicator.NO_MANUAL_BREAK)
    {
      // The whole box fits on the current page. However, we have to apply the shifting to move the box
      // to its defined fixed-position.
      //
      // As neither this box nor any of the children will cause a pagebreak, we can shift them and skip the processing
      // from here.
      BoxShifter.shiftBox(box, fixedPositionDelta);
      BoxShifter.extendHeight(box.getParent(), box, fixedPositionDelta);
      updateStateKeyDeep(box);
      return false;
    }
  }

  protected void processBlockLevelNode(final RenderNode node)
  {
    final long shift = shiftState.getShiftForNextChild();
    node.setY(node.getY() + shift);
    if (breakPending == false && node.isBreakAfter())
    {
      breakPending = paginationTableState.isOnPageStart(node.getY()) == false;
      if (breakPending)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("BreakPending True for Node:isBreakAfter: " + node);
        }
      }
    }
  }

  protected void finishBlockLevelBox(final RenderBox box)
  {
    if (breakPending == false && box.isBreakAfter())
    {
      breakPending = paginationTableState.isOnPageStart(box.getY() + box.getHeight()) == false;
      if (breakPending)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("BreakPending True for Box:isBreakAfter: " + box);
        }
      }
    }

    shiftState = shiftState.pop();
  }

  // At a later point, we have to do some real page-breaking here. We should check, whether the box fits, and should
  // shift the box if it doesnt.

  protected boolean startCanvasLevelBox(final RenderBox box)
  {
    if (box.isCommited())
    {
      box.setFinishedPaginate(true);
    }

    shiftState = shiftStatePool.create(box, shiftState);
    shiftState.suspendManualBreaks();
    box.setY(box.getY() + shiftState.getShiftForNextChild());
    return true;
  }

  protected void finishCanvasLevelBox(final RenderBox box)
  {
    shiftState = shiftState.pop();
  }

  protected boolean startRowLevelBox(final RenderBox box)
  {
    if (box.isCommited())
    {
      box.setFinishedPaginate(true);
    }

    shiftState = shiftStatePool.create(box, shiftState);
    shiftState.suspendManualBreaks();
    box.setY(box.getY() + shiftState.getShiftForNextChild());
    return true;
  }

  protected void finishRowLevelBox(final RenderBox box)
  {
    shiftState = shiftState.pop();
  }

  protected boolean startInlineLevelBox(final RenderBox box)
  {
    BoxShifter.shiftBox(box, shiftState.getShiftForNextChild());
    return false;
  }

  protected void processInlineLevelNode(final RenderNode node)
  {
    node.setY(node.getY() + shiftState.getShiftForNextChild());
  }

  protected void finishInlineLevelBox(final RenderBox box)
  {
  }

  protected void processCanvasLevelNode(final RenderNode node)
  {
    node.setY(node.getY() + shiftState.getShiftForNextChild());
  }

  protected void processRowLevelNode(final RenderNode node)
  {
    node.setY(node.getY() + shiftState.getShiftForNextChild());
  }

  protected void processOtherLevelChild(final RenderNode node)
  {
    node.setY(node.getY() + shiftState.getShiftForNextChild());
  }

  private void updateStateKey(final RenderBox box)
  {
    if (paginationTableState.isVisualStateCollectionSuspended())
    {
      return;
    }

    final long y = box.getY();
    if (y < paginationTableState.getPageEnd())
    {
      final ReportStateKey stateKey = box.getStateKey();
      if (stateKey != null && stateKey.isInlineSubReportState() == false)
      {
        this.visualState = stateKey;
      }
    }
  }

  private void updateStateKeyDeep(final RenderBox box)
  {
    if (paginationTableState.isVisualStateCollectionSuspended())
    {
      return;
    }

    final long y = box.getY();
    if (y >= paginationTableState.getPageEnd())
    {
      return;
    }

    final ReportStateKey reportStateKey = findOldestProcessKeyStep.find(box);
    if (reportStateKey != null && reportStateKey.isInlineSubReportState() == false)
    {
      this.visualState = reportStateKey;
    }
  }

  private boolean handleAutomaticPagebreak(final RenderBox box,
                                           final PaginationShiftState boxContext)
  {
    final long shift = boxContext.getShiftForNextChild();
    final PageBreakPositions breakUtility = paginationTableState.getBreakPositions();
    final long boxHeightAndWidowArea = Math.max
        (box.getHeight(), PaginationStepLib.getWidowConstraint(box, shiftState, paginationTableState));
    if (breakUtility.isCrossingPagebreak(box.getY(), boxHeightAndWidowArea, shift) == false)
    {
      // The whole box fits on the current page. No need to do anything fancy.
      final int breakIndicator = box.getManualBreakIndicator();
      if (breakIndicator == RenderBox.INDIRECT_MANUAL_BREAK)
      {
        // One of the children of this box will cause a manual pagebreak. We have to dive deeper into this child.
        // for now, we will only apply the ordinary shift.
        final long boxY = box.getY();
        box.setY(boxY + shift);
        updateStateKey(box);
        return true;
      }
      else // if (breakIndicator == RenderBox.BreakIndicator.NO_MANUAL_BREAK)
      {
        // As neither this box nor any of the children will cause a pagebreak, we can shift them and skip the processing
        // from here.
        BoxShifter.shiftBox(box, shift);
        updateStateKeyDeep(box);
        return false;
      }
    }

    // At this point we know, that the box may cause some shifting. It crosses at least one minor or major pagebreak.
    // Right now, we are just evaluating the next break. In a future version, we could search all possible break
    // positions up to the next major break.
    final long boxY = box.getY();
    final long boxYShifted = boxY + shift;
    final long nextMinorBreak = breakUtility.findNextBreakPosition(boxYShifted);
    final long spaceAvailable = nextMinorBreak - boxYShifted;

    // This box sits directly on a pagebreak. This means, the page is empty, and there is no need for additional
    // shifting.
    if (spaceAvailable == 0)
    {
      box.setY(boxYShifted);
      updateStateKey(box);
      if (boxYShifted < nextMinorBreak)
      {
        // this position is shifted, but not header-shifted
        box.markPinned(nextMinorBreak);
      }
      return true;
    }

    final long spaceConsumed = PaginationStepLib.computeNonBreakableBoxHeight(box, boxContext, paginationTableState);
    if (spaceAvailable < spaceConsumed)
    {
      // So we have not enough space to fulfill the layout-constraints. Be it so. Lets shift the box to the next
      // break.
      // check whether we can actually shift the box. We will have to take the previous widow/orphan operations
      // into account.
      //
      // A explicit keep-together on a box overrides a orphan restraint.
      final long nextShift = nextMinorBreak - boxY;
      final long shiftDelta = nextShift - shift;
      if (shiftDelta > 0)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug("Automatic pagebreak, after orphan-opt-out: " + box);
          logger.debug("Automatic pagebreak                      : " + visualState);
        }
      }
      box.setY(boxY + nextShift);
      BoxShifter.extendHeight(box.getParent(), box, shiftDelta);
      boxContext.setShift(nextShift);
      updateStateKey(box);
      if (box.getY() < nextMinorBreak)
      {
        box.markPinned(nextMinorBreak);
      }
      return true;
    }

    // OK, there *is* enough space available. Start the normal processing
    box.setY(boxYShifted);
    updateStateKey(box);
    return true;
  }

  private boolean handleManualBreakOnBox(final RenderBox box,
                                         final PaginationShiftState boxContext,
                                         final boolean breakPending)
  {
    final int breakIndicator = box.getManualBreakIndicator();
    // First check the simple cases:
    // If the box wants to break, then there's no point in waiting: Shift the box and continue.
    if (breakIndicator != RenderBox.DIRECT_MANUAL_BREAK && breakPending == false)
    {
      return false;
    }

    final long shift = boxContext.getShiftForNextChild();
    if (box.getNodeType() == LayoutNodeTypes.TYPE_BOX_BREAKMARK)
    {
      final BreakMarkerRenderBox bmrb = (BreakMarkerRenderBox) box;
      final long pageOffsetForMarker = bmrb.getValidityRange();
      final long pageEndForOffset = paginationTableState.getBreakPositions().findPageEndForPageStartPosition(pageOffsetForMarker);
      if ((box.getY() + shift) > pageEndForOffset)
      {
        // we ignore this one. It has been pushed outside of the page for which it was generated.
        return false;
      }

      if (this.breakIndicatorEncountered == null ||
          this.breakIndicatorEncountered.getY() < (bmrb.getY() + shift))
      {
        this.breakIndicatorEncountered = bmrb;
      }
    }

    final PageBreakPositions breakUtility = paginationTableState.getBreakPositions();
    final RenderLength fixedPosition = box.getBoxDefinition().getFixedPosition();
    final long fixedPositionResolved = fixedPosition.resolve(paginationTableState.getPageHeight(), 0);
    final long boxY = box.getY();
    final long shiftedBoxY = boxY + shift;
    final long nextNonShiftedMajorBreak = breakUtility.findNextMajorBreakPosition(shiftedBoxY);
    final long fixedPositionOnNextPage =
        breakUtility.computeFixedPositionInFlow(nextNonShiftedMajorBreak, fixedPositionResolved);
    final long nextMajorBreak = Math.max(nextNonShiftedMajorBreak, fixedPositionOnNextPage);
    if (nextMajorBreak < shiftedBoxY)
    {
      // This band will be outside the last pagebreak. We can only shift it normally, but there is no way
      // that we could shift it to the final position yet.
      box.setY(boxY + shift);
    }
    else
    {
      final long nextShift = nextMajorBreak - boxY;
      final long shiftDelta = nextShift - shift;
      box.setY(boxY + nextShift);
      BoxShifter.extendHeight(box.getParent(), box, shiftDelta);
      boxContext.setShift(nextShift);
    }

    updateStateKey(box);
    final long pageEnd = paginationTableState.getPageEnd();
    if (box.getY() < pageEnd)
    {
      box.markPinned(pageEnd);
    }
    return true;
  }

  private void handleBlockLevelBoxFinishedMarker(final RenderBox box, final long shift)
  {
    if (box.isFinishedPaginate() != false)
    {
      return;
    }

    if (box.isCommited())
    {
      box.setFinishedPaginate(true);
    }
    else
    {
      final StaticBoxLayoutProperties sblp = box.getStaticBoxLayoutProperties();
      if (sblp.isAvoidPagebreakInside() || sblp.getWidows() > 0 || sblp.getOrphans() > 0)
      {
        // Check, whether this box sits on a break-position. In that case, we can call that box finished as well.
        final long boxY = box.getY();
        final PageBreakPositions breakUtility = paginationTableState.getBreakPositions();
        final long nextMinorBreak = breakUtility.findNextBreakPosition(boxY + shift);
        final long spaceAvailable = nextMinorBreak - (boxY + shift);

        // This box sits directly on a pagebreak. No matter how much content we fill in the box, it will not move.
        // This makes this box a finished box.
        if (spaceAvailable == 0 || box.isPinned())
        {
          box.setFinishedPaginate(true);
        }
      }
      else
      {
        // This box defines no constraints that would cause a shift of it later in the process. We can treat it as
        // if it is finished already ..
        box.setFinishedPaginate(true);
      }
    }
  }
}
