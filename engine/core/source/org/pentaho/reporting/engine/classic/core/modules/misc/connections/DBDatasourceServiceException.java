package org.pentaho.reporting.engine.classic.core.modules.misc.connections;

public class DBDatasourceServiceException extends RuntimeException
{
  public DBDatasourceServiceException()
  {
  }

  public DBDatasourceServiceException(final String message)
  {
    super(message);
  }

  public DBDatasourceServiceException(final String message, final Throwable cause)
  {
    super(message, cause);
  }

  public DBDatasourceServiceException(final Throwable cause)
  {
    super(cause);
  }
}
