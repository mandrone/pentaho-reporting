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

package org.pentaho.reporting.designer.core.actions.global;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.pentaho.reporting.designer.core.actions.AbstractElementSelectionAction;
import org.pentaho.reporting.designer.core.actions.ActionMessages;
import org.pentaho.reporting.designer.core.editor.ReportRenderContext;
import org.pentaho.reporting.designer.core.model.ModelUtility;
import org.pentaho.reporting.designer.core.util.IconLoader;
import org.pentaho.reporting.designer.core.util.dnd.ClipboardManager;
import org.pentaho.reporting.designer.core.util.dnd.InsertationUtil;
import org.pentaho.reporting.designer.core.util.undo.BandedSubreportEditUndoEntry;
import org.pentaho.reporting.designer.core.util.undo.CompoundUndoEntry;
import org.pentaho.reporting.designer.core.util.undo.DataSourceEditUndoEntry;
import org.pentaho.reporting.designer.core.util.undo.ElementEditUndoEntry;
import org.pentaho.reporting.designer.core.util.undo.ExpressionAddedUndoEntry;
import org.pentaho.reporting.designer.core.util.undo.ParameterEditUndoEntry;
import org.pentaho.reporting.designer.core.util.undo.UndoEntry;
import org.pentaho.reporting.engine.classic.core.AbstractReportDefinition;
import org.pentaho.reporting.engine.classic.core.CompoundDataFactory;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.RootLevelBand;
import org.pentaho.reporting.engine.classic.core.Section;
import org.pentaho.reporting.engine.classic.core.SubReport;
import org.pentaho.reporting.engine.classic.core.function.Expression;
import org.pentaho.reporting.engine.classic.core.parameters.ParameterDefinitionEntry;
import org.pentaho.reporting.engine.classic.core.parameters.ReportParameterDefinition;
import org.pentaho.reporting.libraries.designtime.swing.FocusTracker;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public class PasteAction extends AbstractElementSelectionAction implements ChangeListener
{
  private class FocusUpdateHandler extends FocusTracker
  {
    protected void focusChanged(final Component c)
    {
      updateSelection();
    }
  }

  private enum ClipboardStatus
  {
    EMPTY,           // Nothing valid in the clipboard
    UNKNOWN,         // Not checked, as we have no insertation point
    GENERIC_ELEMENT  // its a generic element, insertation point has been checked to be valid.
  }

  private ClipboardStatus clipboardStatus;
  private Object[] clipboardContents;

  private boolean selectionActive;
  private FocusTracker focusTracker;

  public PasteAction()
  {
    putValue(Action.NAME, ActionMessages.getString("PasteAction.Text"));
    putValue(Action.SHORT_DESCRIPTION, ActionMessages.getString("PasteAction.Description"));
    putValue(Action.MNEMONIC_KEY, ActionMessages.getOptionalMnemonic("PasteAction.Mnemonic"));
    putValue(Action.ACCELERATOR_KEY, ActionMessages.getOptionalKeyStroke("PasteAction.Accelerator"));
    putValue(Action.SMALL_ICON, IconLoader.getInstance().getPasteIcon());

    setEnabled(false);
    ClipboardManager.getManager().addChangeListener(this);
    // update from system clipboard status
    stateChanged(null);
    focusTracker = new FocusUpdateHandler();
  }

  protected void updateSelection()
  {
    final ReportRenderContext activeContext = getActiveContext();
    if (activeContext == null)
    {
      setSelectionActive(false);
      return;
    }

    final Object rawLeadSelection = InsertationUtil.getInsertationPoint(activeContext);
    if (rawLeadSelection == null)
    {
      setSelectionActive(false);
      return;
    }
    setSelectionActive(true);
  }

  public void setSelectionActive(final boolean selectionActive)
  {
    this.selectionActive = selectionActive;
    if (selectionActive)
    {
      setEnabled(isGenericElementInsertable());
    }
    else
    {
      setEnabled(false);
    }
  }

  /**
   * Invoked when an action occurs.
   */
  public void actionPerformed(final ActionEvent e)
  {
    final ReportRenderContext activeContext = getActiveContext();
    if (activeContext == null)
    {
      return;
    }
    final Object rawLeadSelection = InsertationUtil.getInsertationPoint(activeContext);
    if (rawLeadSelection == null)
    {
      return;
    }

    final Object[] fromClipboardArray = InsertationUtil.getFromClipboard();
    if (fromClipboardArray.length == 0)
    {
      return;
    }
    final Object[] selectedElements = new Object[fromClipboardArray.length];
    final AbstractReportDefinition report = activeContext.getReportDefinition();
    final ArrayList<UndoEntry> undos = new ArrayList<UndoEntry>();
    try
    {
      for (int i = 0; i < fromClipboardArray.length; i++)
      {
        final Object o = fromClipboardArray[i];
        final Object insertResult = InsertationUtil.insert(rawLeadSelection, report, o);
				selectedElements[i] = insertResult;
        if (insertResult instanceof Element)
        {
          final Element insertElement = (Element) insertResult;
          final Section parent = insertElement.getParentSection();
          if (parent == null)
          {
            throw new IllegalStateException("A newly inserted section must have a parent."); // NON-NLS
          }
          final int position = ModelUtility.findIndexOf(parent, insertElement);
          if (position == -1)
          {
            if (insertElement instanceof SubReport && parent instanceof RootLevelBand)
            {
              final SubReport subReport = (SubReport) insertElement;
              final RootLevelBand arb = (RootLevelBand) parent;
              final int subreportPosition = ModelUtility.findSubreportIndexOf(arb, subReport);
              if (subreportPosition == -1)
              {
                throw new IllegalStateException("A newly inserted section must have a position within its parent.");
              }
              else
              {
                undos.add(new BandedSubreportEditUndoEntry(parent.getObjectID(), arb.getSubReportCount(), null, subReport));
              }
            }
            else
            {
              throw new IllegalStateException("A newly inserted section must have a position within its parent.");
            }
          }
          else
          {
            undos.add(new ElementEditUndoEntry(parent.getObjectID(), position, null, insertElement));
          }
        }
        else if (insertResult instanceof Expression)
        {
          final Expression insertExpression = (Expression) insertResult;
          final int index = activeContext.getReportDefinition().getExpressions().indexOf(insertExpression);
          undos.add(new ExpressionAddedUndoEntry(index, insertExpression));
        }
        else if (insertResult instanceof ParameterDefinitionEntry)
        {
          final ParameterDefinitionEntry insertParam = (ParameterDefinitionEntry) insertResult;
          final ReportParameterDefinition definition = activeContext.getMasterReportElement().getParameterDefinition();
          final int index = definition.getParameterCount() - 1;
          undos.add(new ParameterEditUndoEntry(index, null, insertParam));
        }
        else if (insertResult instanceof DataFactory)
        {
          final DataFactory insertDataFactory = (DataFactory) insertResult;
          final CompoundDataFactory compoundDataFactory = (CompoundDataFactory) activeContext.getReportDefinition().getDataFactory();
          final int index = compoundDataFactory.size() - 1;
          undos.add(new DataSourceEditUndoEntry(index, null, insertDataFactory));
        }

      }
      getSelectionModel().setSelectedElements(selectedElements);
    }
    finally
    {
      getActiveContext().getUndo().addChange(ActionMessages.getString("PasteAction.Text"),
          new CompoundUndoEntry(undos.toArray(new UndoEntry[undos.size()])));
    }
  }

  public ClipboardStatus getClipboardStatus()
  {
    return clipboardStatus;
  }

  public void setClipboardStatus(final ClipboardStatus clipboardStatus)
  {
    this.clipboardStatus = clipboardStatus;
    if (clipboardStatus == ClipboardStatus.UNKNOWN)
    {
      setEnabled(false);
    }
    else
    {
      setEnabled(isGenericElementInsertable());
    }
  }

  public void stateChanged(final ChangeEvent e)
  {
    clipboardContents = null;

    if (ClipboardManager.getManager().isDataAvailable())
    {
      setClipboardStatus(ClipboardStatus.GENERIC_ELEMENT);
    }
    else
    {
      setClipboardStatus(ClipboardStatus.EMPTY);
    }
  }

  private boolean isGenericElementInsertable()
  {
    final ReportRenderContext activeContext = getActiveContext();
    if (activeContext == null)
    {
      return false;
    }

    if (selectionActive == false)
    {
      return false;
    }
    if (clipboardStatus == ClipboardStatus.GENERIC_ELEMENT)
    {

      final Object[] dataArray;
      if (clipboardContents == null)
      {
        dataArray = InsertationUtil.getFromClipboard();
        clipboardContents = dataArray;
      }
      else
      {
        dataArray = clipboardContents;
      }
      final Object rawLeadSelection = InsertationUtil.getInsertationPoint(activeContext);
      for (int i = 0; i < dataArray.length; i++)
      {
        final Object o = dataArray[i];
        if (InsertationUtil.isInsertAllowed(rawLeadSelection, o))
        {
          return true;
        }
      }
    }

    return false;
  }
}
