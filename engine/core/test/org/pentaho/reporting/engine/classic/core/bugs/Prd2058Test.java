package org.pentaho.reporting.engine.classic.core.bugs;

import java.net.URL;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.function.EventMonitorFunction;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugReportRunner;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;

/**
 * Todo: Document me!
 * <p/>
 * Date: 21.12.2009
 * Time: 16:56:13
 *
 * @author Thomas Morgner.
 */
public class Prd2058Test extends TestCase
{
  public Prd2058Test()
  {
  }

  public Prd2058Test(final String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testRunSample() throws ResourceException
  {
    final URL url = getClass().getResource("Prd-2058.prpt");
    assertNotNull(url);
    final ResourceManager resourceManager = new ResourceManager();
    resourceManager.registerDefaults();
    final Resource directly = resourceManager.createDirectly(url, MasterReport.class);
    final MasterReport report = (MasterReport) directly.getResource();
    report.addExpression(new EventMonitorFunction());

    DebugReportRunner.execGraphics2D(report);
  }
}
