package org.pentaho.reporting.libraries.pensol.vfs;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
* Todo: Document me!
* <p/>
* Date: 10.02.2010
* Time: 19:45:37
*
* @author Thomas Morgner.
*/
public class FileInfo
{
  private boolean directory;
  private boolean visible;
  private String name;
  private String localizedName;
  private String description;
  private long lastModifiedDate;
  private String parameterServiceURL;
  private String title;
  private String url;
  private FileInfo parent;
  private ArrayList<FileInfo> childs;

  public FileInfo()
  {
    directory = true;
    visible = true;
    name = "";
    localizedName = "";
    description = "Content Root";
    lastModifiedDate = System.currentTimeMillis();
    childs = new ArrayList<FileInfo>();
  }

  public FileInfo(final FileInfo parent, final String name, final String description)
  {
    if (parent == null)
    {
      throw new NullPointerException();
    }

    if (name == null)
    {
      throw new NullPointerException();
    }
    directory = true;
    visible = true;
    this.name = name;
    localizedName = "";
    this.description = description;
    lastModifiedDate = System.currentTimeMillis();
    childs = new ArrayList<FileInfo>();
    this.parent = parent;
    this.parent.childs.add(this);
  }

  public FileInfo(final FileInfo parent, final Attributes element) throws SAXException
  {
    if (parent == null)
    {
      throw new SAXException();
    }
    if (element == null)
    {
      throw new SAXException();
    }

    name = element.getValue("name");
    if (name == null)
    {
      throw new IllegalStateException();
    }
    localizedName = element.getValue("localized-name");
    if (localizedName == null)
    {
      localizedName = name;
    }

    directory = "true".equals(element.getValue("isDirectory"));
    visible = "true".equals(element.getValue("visible"));
    final String lastModifiedRaw = element.getValue("lastModifiedDate");
    if (lastModifiedRaw != null)
    {
      try
      {
        lastModifiedDate = Long.parseLong(lastModifiedRaw);
      }
      catch (final NumberFormatException nfe)
      {
        throw new SAXException();
      }
    }
    description = element.getValue("description");
    title = element.getValue("title");
    if (title == null)
    {
      title = element.getValue("url_name");
    }

    url = element.getValue("url");
    parameterServiceURL = element.getValue("param-service-url");

    childs = new ArrayList<FileInfo>();
    this.parent = parent;
    this.parent.childs.add(this);
  }

  public boolean isDirectory()
  {
    return directory;
  }

  public boolean isVisible()
  {
    return visible;
  }

  public String getName()
  {
    return name;
  }

  public String getLocalizedName()
  {
    return localizedName;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(final String description)
  {
    this.description = description;
  }

  public long getLastModifiedDate()
  {
    return lastModifiedDate;
  }

  public String getParameterServiceURL()
  {
    return parameterServiceURL;
  }

  public String getTitle()
  {
    return title;
  }

  public String getUrl()
  {
    return url;
  }

  public FileInfo getParent()
  {
    return parent;
  }

  public FileInfo[] getChilds()
  {
    return childs.toArray(new FileInfo[childs.size()]);
  }

  public FileInfo getChild(final String name)
  {
    if (name == null)
    {
      throw new NullPointerException();
    }

    for (int i = 0; i < childs.size(); i++)
    {
      final FileInfo fileInfo = childs.get(i);
      if (name.equals(fileInfo.getName()))
      {
        return fileInfo;
      }
    }
    return null;
  }
}
