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
 * Copyright (c) 2006 - 2009 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.reporting.designer.core.util;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.pentaho.openformula.ui.FormulaEditorDialog;
import org.pentaho.openformula.ui.FunctionParameterEditor;
import org.pentaho.reporting.designer.core.ReportDesignerBoot;
import org.pentaho.reporting.designer.core.ReportDesignerContext;
import org.pentaho.reporting.engine.classic.core.modules.gui.commonswing.SwingUtil;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.base.util.DebugLog;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;

/**
 * User: Martin Date: 07.02.2006 Time: 14:17:28
 */
public class GUIUtils
{
  private static final String FUNCTION_EDITOR_CONFIX_PREFIX = "org.pentaho.reporting.designer.core.function-editor.";

  private GUIUtils()
  {
  }

  private static void ensureMinimumDialogWidth(final Dialog dialog, final int minWidth)
  {
    if (dialog.getWidth() < minWidth)
    {
      dialog.setSize(minWidth, dialog.getHeight());
    }
  }


  private static void ensureMinimumDialogHeight(final Dialog dialog, final int minHeight)
  {
    if (dialog.getHeight() < minHeight)
    {
      dialog.setSize(dialog.getWidth(), minHeight);
    }
  }


  private static void ensureMaximumDialogWidth(final Dialog dialog, final int maxWidth)
  {
    if (dialog.getWidth() > maxWidth)
    {
      dialog.setSize(maxWidth, dialog.getHeight());
    }
  }


  private static void ensureMaximumDialogHeight(final Dialog dialog, final int maxHeight)
  {
    if (dialog.getHeight() > maxHeight)
    {
      dialog.setSize(dialog.getWidth(), maxHeight);
    }
  }


  public static void ensureMinimumDialogSize(final Dialog dialog, final int minWidth, final int minHeight)
  {
    ensureMinimumDialogWidth(dialog, minWidth);
    ensureMinimumDialogHeight(dialog, minHeight);
  }


  public static void ensureMaximumDialogSize(final Dialog dialog, final int maxWidth, final int maxHeight)
  {
    ensureMaximumDialogWidth(dialog, maxWidth);
    ensureMaximumDialogHeight(dialog, maxHeight);
  }

  public static String rectangleToString(final Rectangle rectangle)
  {
    final StringBuilder buffer = new StringBuilder();
    buffer.append(rectangle.getX());
    buffer.append(",");
    buffer.append(rectangle.getY());
    buffer.append(",");
    buffer.append(rectangle.getWidth());
    buffer.append(",");
    buffer.append(rectangle.getHeight());
    return buffer.toString();
  }

  public static Rectangle parseRectangle(final String boundsAsText)
  {
    try
    {
      final StringTokenizer tokenizer = new StringTokenizer(boundsAsText, ",");
      if (tokenizer.countTokens() == 4)
      {
        final double x = Double.parseDouble(tokenizer.nextToken());
        final double y = Double.parseDouble(tokenizer.nextToken());
        final double width = Double.parseDouble(tokenizer.nextToken());
        final double height = Double.parseDouble(tokenizer.nextToken());

        final Rectangle rectangle = new Rectangle();
        rectangle.setRect(x, y, width, height);
        return rectangle;
      }
      return null;
    }
    catch (Exception e)
    {
      DebugLog.log("Error while getting initial frame bounds.", e); // NON-NLS
      return null;
    }
  }

  public static FormulaEditorDialog createFormulaEditorDialog(final ReportDesignerContext context,
                                                              final Component parent)
  {
    final Window window = SwingUtil.getWindowAncestor(parent);
    final DesignerFormulaEditorDialog editorDialog;
    if (window instanceof Frame)
    {
      editorDialog = new DesignerFormulaEditorDialog((Frame) window);
    }
    else if (window instanceof Dialog)
    {
      editorDialog = new DesignerFormulaEditorDialog((Dialog) window);
    }
    else
    {
      editorDialog = new DesignerFormulaEditorDialog();
    }

    final Configuration configuration = ReportDesignerBoot.getInstance().getGlobalConfig();
    final Iterator propertyKeys = configuration.findPropertyKeys(FUNCTION_EDITOR_CONFIX_PREFIX);
    while (propertyKeys.hasNext())
    {
      final String key = (String) propertyKeys.next();
      final String function = key.substring(FUNCTION_EDITOR_CONFIX_PREFIX.length());
      final String editor = configuration.getConfigProperty(key);
      final FunctionParameterEditor fnEditor = (FunctionParameterEditor)
          ObjectUtilities.loadAndInstantiate(editor, GUIUtils.class, FunctionParameterEditor.class);
      if (fnEditor instanceof ReportDesignerFunctionParameterEditor)
      {
        final ReportDesignerFunctionParameterEditor rfn = (ReportDesignerFunctionParameterEditor) fnEditor;
        rfn.setReportDesignerContext(context);
      }
      editorDialog.setEditor(function, fnEditor);
    }
    return editorDialog;
  }

}
