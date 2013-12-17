package org.pentaho.reporting.engine.classic.extensions.toc.writer;

import java.io.IOException;

import org.pentaho.reporting.engine.classic.core.Element;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterException;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriterState;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.elements.AbstractElementWriteHandler;
import org.pentaho.reporting.engine.classic.extensions.toc.TocElement;
import org.pentaho.reporting.libraries.docbundle.WriteableDocumentBundle;
import org.pentaho.reporting.libraries.xmlns.writer.XmlWriter;

/**
 * Todo: Document me!
 *
 * @author Thomas Morgner.
 */
public class TocElementWriteHandler extends AbstractElementWriteHandler
{
  public TocElementWriteHandler()
  {
  }

  public void writeElement(final WriteableDocumentBundle bundle,
                           final BundleWriterState state,
                           final XmlWriter xmlWriter,
                           final Element element) throws IOException, BundleWriterException
  {
    if (bundle == null)
    {
      throw new NullPointerException();
    }
    if (state == null)
    {
      throw new NullPointerException();
    }
    if (xmlWriter == null)
    {
      throw new NullPointerException();
    }
    if (element == null)
    {
      throw new NullPointerException();
    }

    writeSubReport(bundle, state, xmlWriter, (TocElement) element);
  }
}

