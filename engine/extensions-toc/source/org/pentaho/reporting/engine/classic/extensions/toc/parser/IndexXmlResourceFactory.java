package org.pentaho.reporting.engine.classic.extensions.toc.parser;

import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.ReportParserUtil;
import org.pentaho.reporting.engine.classic.core.modules.parser.base.ReportResource;
import org.pentaho.reporting.engine.classic.extensions.toc.IndexElement;
import org.pentaho.reporting.libraries.base.config.Configuration;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceData;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.xmlns.parser.AbstractXmlResourceFactory;
import org.pentaho.reporting.libraries.xmlns.parser.RootXmlReadHandler;

/**
 * Todo: Document me!
 *
 * @author Thomas Morgner.
 */
public class IndexXmlResourceFactory
    extends AbstractXmlResourceFactory
{
  public IndexXmlResourceFactory()
  {
  }

  protected Configuration getConfiguration()
  {
    return ClassicEngineBoot.getInstance().getGlobalConfig();
  }

  public Class getFactoryType()
  {
    return IndexElement.class;
  }

  protected Object finishResult(final Object res,
                                final ResourceManager manager,
                                final ResourceData data,
                                final ResourceKey context) throws ResourceCreationException, ResourceLoadingException
  {
    final IndexElement report = (IndexElement) res;
    if (report == null)
    {
      throw new ResourceCreationException("Report has not been parsed.");
    }

    // subreports use the content-base of their master-report for now. This is safe for the old platform reports
    // and for bundle-reports.
    report.setDefinitionSource(data.getKey());
    return report;

  }

  protected Resource createResource(final ResourceKey targetKey,
                                    final RootXmlReadHandler handler,
                                    final Object createdProduct,
                                    final Class createdType)
  {
    if (ReportParserUtil.INCLUDE_PARSING_VALUE.equals(handler.getHelperObject(ReportParserUtil.INCLUDE_PARSING_KEY)))
    {
      return new ReportResource
          (targetKey, handler.getDependencyCollector(), createdProduct, createdType, false);
    }
    return new ReportResource
        (targetKey, handler.getDependencyCollector(), createdProduct, createdType, true);
  }


}