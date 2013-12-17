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

package org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.base.PageableReportProcessor;

/**
 * Utility class to provide an easy to use default implementation of PDF exports.
 *
 * @author Thomas Morgner
 * @author Cedric Pronzato
 */
public final class PdfReportUtil
{
  private static final Log logger = LogFactory.getLog(PdfReportUtil.class);

  /**
   * DefaultConstructor.
   */
  private PdfReportUtil()
  {
  }

  /**
   * Saves a report to PDF format.
   *
   * @param report   the report.
   * @param fileName target file.
   * @return true if the report has been successfully exported, false otherwise.
   */
  public static boolean createPDF(final MasterReport report,
                                  final File fileName)
  {
    if (report == null)
    {
      throw new NullPointerException();
    }
    if (fileName == null)
    {
      throw new NullPointerException();
    }

    OutputStream out = null;
    try
    {
      out = new BufferedOutputStream(new FileOutputStream(fileName));
      final boolean retval = createPDF(report, out);
      out.close();
      out = null;
      return retval;
    }
    catch (Exception e)
    {
      logger.error("Writing PDF failed.", e);
      return false;
    }
    finally
    {
      try
      {
        if (out != null)
        {
          out.close();
        }
      }
      catch (Exception e)
      {
        logger.error("Saving PDF failed.", e);
      }
    }
  }

  /**
   * Saves a report to PDF format.
   *
   * @param report the report.
   * @param out    target output stream.
   * @return true if the report has been successfully exported, false otherwise.
   */
  public static boolean createPDF(final MasterReport report,
                                  final OutputStream out)
  {
    if (report == null)
    {
      throw new NullPointerException();
    }
    if (out == null)
    {
      throw new NullPointerException();
    }

    PageableReportProcessor proc = null;
    try
    {

      final PdfOutputProcessor outputProcessor = new PdfOutputProcessor(report.getConfiguration(), out,
          report.getResourceManager());
      proc = new PageableReportProcessor(report, outputProcessor);
      proc.processReport();
      return true;
    }
    catch (Exception e)
    {
      logger.error("Writing PDF failed.", e);
      return false;
    }
    catch (Error e)
    {
      logger.error("Writing PDF failed.", e);
      return false;
    }
    finally
    {
      if (proc != null)
      {
        proc.close();
      }
    }
  }

  /**
   * Concates and saves a list of reports to PDF format.
   *
   * @param report   the report.
   * @param fileName target file name.
   * @return true if the report has been successfully exported, false otherwise.
   */
  public static boolean createPDF(final MasterReport report,
                                  final String fileName)
  {
    return createPDF(report, new File(fileName));
  }
}
