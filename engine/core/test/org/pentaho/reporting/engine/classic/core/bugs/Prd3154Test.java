package org.pentaho.reporting.engine.classic.core.bugs;

import java.net.URL;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.modules.gui.base.PreviewDialog;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugReportRunner;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

/**
 * Todo: Document me!
 * <p/>
 * Date: 21.02.11
 * Time: 14:13
 *
 * @author Thomas Morgner.
 */
public class Prd3154Test extends TestCase
{
  public Prd3154Test()
  {
  }

  public Prd3154Test(final String name)
  {
    super(name);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testReport() throws ResourceException
  {
    final URL url = getClass().getResource("Prd-3154.prpt");
    assertNotNull(url);
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    final Resource directly = resourceManager.createDirectly(url, MasterReport.class);
    final MasterReport report = (MasterReport) directly.getResource();
    DebugReportRunner.execGraphics2D(report);
/*
    final PreviewDialog previewDialog = new PreviewDialog(report);
    previewDialog.pack();
    previewDialog.setModal(true);
    previewDialog.setVisible(true);
*/
  }

}
