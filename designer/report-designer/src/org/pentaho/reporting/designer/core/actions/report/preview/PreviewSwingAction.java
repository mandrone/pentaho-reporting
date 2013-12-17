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

package org.pentaho.reporting.designer.core.actions.report.preview;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.pentaho.reporting.designer.core.ReportDesignerBoot;
import org.pentaho.reporting.designer.core.actions.AbstractReportContextAction;
import org.pentaho.reporting.designer.core.actions.ActionMessages;
import org.pentaho.reporting.designer.core.util.GUIUtils;
import org.pentaho.reporting.designer.core.util.IconLoader;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.PreviewDialog;
import org.pentaho.reporting.engine.classic.core.modules.gui.commonswing.SwingUtil;
import org.pentaho.reporting.libraries.designtime.swing.LibSwingUtil;

/**
 * Todo: Document Me
 *
 * @author Thomas Morgner
 */
public final class PreviewSwingAction extends AbstractReportContextAction
{
  private static class PreviewSizeMonitor extends WindowAdapter
  {
    private MasterReport report;

    private PreviewSizeMonitor(final MasterReport report)
    {
      if (report == null)
      {
        throw new NullPointerException();
      }
      this.report = report;
    }

    /**
     * Invoked when the user attempts to close the window
     * from the window's system menu.
     */
    public void windowClosing(final WindowEvent e)
    {
      this.report.setAttribute(ReportDesignerBoot.DESIGNER_NAMESPACE, "preview-dialog-bounds",//$NON-NLS-1$
          GUIUtils.rectangleToString(e.getWindow().getBounds()));
    }
  }

  public PreviewSwingAction()
  {
    putValue(Action.NAME, ActionMessages.getString("PreviewSwingAction.Text"));
    putValue(Action.SHORT_DESCRIPTION, ActionMessages.getString("PreviewSwingAction.Description"));
    putValue(Action.MNEMONIC_KEY, ActionMessages.getOptionalMnemonic("PreviewSwingAction.Mnemonic"));
    putValue(Action.ACCELERATOR_KEY, ActionMessages.getOptionalKeyStroke("PreviewSwingAction.Accelerator"));
    putValue(Action.SMALL_ICON, IconLoader.getInstance().getCreateReportIcon());
  }

  /**
   * Invoked when an action occurs.
   */
  public void actionPerformed(final ActionEvent e)
  {
    if (getActiveContext() == null)
    {
      return;
    }

    final MasterReport reportElement = getActiveContext().getMasterReportElement();

    final Component parent = getReportDesignerContext().getParent();
    final Window window = SwingUtil.getWindowAncestor(parent);
    final PreviewDialog dialog;
    if (window instanceof JDialog)
    {
      dialog = new PreviewDialog((JDialog) window);
    }
    else if (window instanceof JFrame)
    {
      dialog = new PreviewDialog((JFrame) window);
    }
    else
    {
      dialog = new PreviewDialog();
    }

    dialog.addWindowListener(new PreviewSizeMonitor(reportElement));
    dialog.setReportJob(reportElement);
    dialog.pack();

    final Object attribute = reportElement.getAttribute
        (ReportDesignerBoot.DESIGNER_NAMESPACE, "preview-dialog-bounds");//$NON-NLS-1$
    if (attribute instanceof String)
    {
      final Rectangle rectangle = GUIUtils.parseRectangle(attribute.toString());
      if (rectangle != null)
      {
        if (LibSwingUtil.safeRestoreWindow(dialog, rectangle))
        {
          dialog.setReportJob(reportElement);
          return;
        }
      }
    }

    dialog.setSize(700, 700);
    SwingUtil.centerDialogInParent(dialog);
    dialog.setVisible(true);
  }

}
