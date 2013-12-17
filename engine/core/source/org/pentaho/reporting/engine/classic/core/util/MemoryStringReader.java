package org.pentaho.reporting.engine.classic.core.util;

import java.io.IOException;
import java.io.Reader;

/**
 * Todo: Document me!
 * <p/>
 * Date: 14.07.2010
 * Time: 16:49:46
 *
 * @author Thomas Morgner.
 */
public class MemoryStringReader extends Reader
{
  private char[] backend;
  private int offset;
  private int length;

  public MemoryStringReader(final char[] backend, final int offset, final int length)
  {
    this.backend = backend;
    this.offset = offset;
    this.length = length;
  }

  public int read(final char[] chars, final int offset, final int length) throws IOException
  {
    if (length == 0)
    {
      return 0;
    }
    if (this.length == 0)
    {
      // signal eof
      return -1;
    }
    final int readLength = Math.min (length, this.length);
    System.arraycopy(backend, this.offset, chars, offset, readLength);
    this.offset += readLength;
    this.length -= readLength;
    return readLength;
  }

  public void close() throws IOException
  {
    // do nothing ..
  }
}
