package org.pentaho.reporting.engine.classic.core.bugs;

import junit.framework.TestCase;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.filter.types.bands.ItemBandType;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;
import org.pentaho.reporting.engine.classic.core.testsupport.DebugReportRunner;
import org.pentaho.reporting.engine.classic.core.testsupport.selector.MatchFactory;

public class Prd4071Test extends TestCase
{
  public Prd4071Test()
  {
  }

  protected void setUp() throws Exception
  {
    ClassicEngineBoot.getInstance().start();
  }

  public void testExcelExport() throws Exception
  {
    final MasterReport report = DebugReportRunner.parseGoldenSampleReport("Prd-4071-Standalone.prpt");
    report.getItemBand().getElement(0).getStyle().setStyleProperty(ElementStyleKeys.DYNAMIC_HEIGHT, true);
    final org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox logicalPageBox = DebugReportRunner.layoutPage(report, 0);
//    ModelPrinter.INSTANCE.print(logicalPageBox);
    assertEquals(64800000, logicalPageBox.getPageEnd());
    final RenderNode[] elementsByElementType = MatchFactory.findElementsByElementType(logicalPageBox, ItemBandType.INSTANCE);
    assertEquals(7, elementsByElementType.length);
    final RenderNode lastChild = elementsByElementType[6];
    assertEquals(64100000, lastChild.getY() + lastChild.getHeight());
  }

  public void testRealWorldReportEmptyPage2() throws Exception
  {
    if ("false".equals(ClassicEngineBoot.getInstance().getGlobalConfig().getConfigProperty
        ("org.pentaho.reporting.engine.classic.test.ExecuteLongRunningTest")))
    {
      return;
    }
    final MasterReport report = DebugReportRunner.parseGoldenSampleReport("Prd-2087-small.prpt");
    report.getItemBand().getStyle().setStyleProperty(ElementStyleKeys.AVOID_PAGEBREAK_INSIDE, false);
    report.setQueryLimit(100);

    final LogicalPageBox logicalPageBox = DebugReportRunner.layoutPage(report, 1);
//    ModelPrinter.INSTANCE.print(logicalPageBox);
    assertEquals(71700000, logicalPageBox.getPageEnd());
    final RenderNode[] elementsByElementType = MatchFactory.findElementsByElementType(logicalPageBox, ItemBandType.INSTANCE);
    assertEquals(23, elementsByElementType.length);
    // The second page is not empty even though a break-marker box exists at the beginning.
    // This break-marker is ignored as its validity range is wrong for the page it is on.
    // If it were NOT ingnored, there would be no itembands on that page.
  }

  public void testRealWorldReport() throws Exception
  {
    if ("false".equals(ClassicEngineBoot.getInstance().getGlobalConfig().getConfigProperty
        ("org.pentaho.reporting.engine.classic.test.ExecuteLongRunningTest")))
    {
      return;
    }
    final MasterReport report = DebugReportRunner.parseGoldenSampleReport("Prd-2087-small.prpt");
    report.setQueryLimit(100);

    final org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox logicalPageBox = DebugReportRunner.layoutPage(report, 0);
    // ModelPrinter.INSTANCE.print(logicalPageBox);
    assertEquals(71700000, logicalPageBox.getPageEnd());
    final RenderNode[] elementsByElementType = MatchFactory.findElementsByElementType(logicalPageBox, ItemBandType.INSTANCE);
    assertEquals(21, elementsByElementType.length); // 21
    final RenderNode lastChild = elementsByElementType[20];
    // important part is that the y2 is less than the page-end
    assertEquals(68447100, lastChild.getY() + lastChild.getHeight());

  }

  public void testRealWorldReport3() throws Exception
  {
    if ("false".equals(ClassicEngineBoot.getInstance().getGlobalConfig().getConfigProperty
        ("org.pentaho.reporting.engine.classic.test.ExecuteLongRunningTest")))
    {
      return;
    }
    final MasterReport report = DebugReportRunner.parseGoldenSampleReport("Prd-2087-small.prpt");

    final org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox logicalPageBox = DebugReportRunner.layoutPage(report, 46);
//    ModelPrinter.INSTANCE.print(logicalPageBox);
    assertEquals(71700000, logicalPageBox.getPageEnd());
    final RenderNode[] elementsByElementType = MatchFactory.findElementsByElementType(logicalPageBox, ItemBandType.INSTANCE);
    assertEquals(22, elementsByElementType.length); // 22
    final RenderNode lastChild = elementsByElementType[21];
    // important part is that the y2 is less than the page-end
    assertEquals(68097100, lastChild.getY() + lastChild.getHeight());
  }

}
