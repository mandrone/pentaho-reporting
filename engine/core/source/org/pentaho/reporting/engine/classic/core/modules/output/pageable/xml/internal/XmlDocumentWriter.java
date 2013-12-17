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

package org.pentaho.reporting.engine.classic.core.modules.output.pageable.xml.internal;

import java.awt.Color;
import java.beans.PropertyEditor;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.engine.classic.core.InvalidReportStateException;
import org.pentaho.reporting.engine.classic.core.ReportAttributeMap;
import org.pentaho.reporting.engine.classic.core.layout.model.BlockRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.Border;
import org.pentaho.reporting.engine.classic.core.layout.model.BorderCorner;
import org.pentaho.reporting.engine.classic.core.layout.model.BorderEdge;
import org.pentaho.reporting.engine.classic.core.layout.model.CanvasRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.InlineRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.LayoutNodeTypes;
import org.pentaho.reporting.engine.classic.core.layout.model.LogicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.PageGrid;
import org.pentaho.reporting.engine.classic.core.layout.model.ParagraphRenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.PhysicalPageBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderLength;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderNode;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderableReplacedContent;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderableReplacedContentBox;
import org.pentaho.reporting.engine.classic.core.layout.model.RenderableText;
import org.pentaho.reporting.engine.classic.core.layout.model.SpacerRenderNode;
import org.pentaho.reporting.engine.classic.core.layout.model.context.BoxDefinition;
import org.pentaho.reporting.engine.classic.core.layout.model.context.StaticBoxLayoutProperties;
import org.pentaho.reporting.engine.classic.core.layout.output.LogicalPageKey;
import org.pentaho.reporting.engine.classic.core.layout.output.OutputProcessorMetaData;
import org.pentaho.reporting.engine.classic.core.layout.process.IterateStructuralProcessStep;
import org.pentaho.reporting.engine.classic.core.metadata.AttributeMetaData;
import org.pentaho.reporting.engine.classic.core.metadata.ElementType;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.TextStyleKeys;
import org.pentaho.reporting.engine.classic.core.util.beans.BeanException;
import org.pentaho.reporting.engine.classic.core.util.beans.ColorValueConverter;
import org.pentaho.reporting.engine.classic.core.util.beans.ConverterRegistry;
import org.pentaho.reporting.engine.classic.core.util.geom.StrictBounds;
import org.pentaho.reporting.engine.classic.core.util.geom.StrictGeomUtility;
import org.pentaho.reporting.libraries.base.util.StringUtils;
import org.pentaho.reporting.libraries.fonts.encoding.EncodingRegistry;
import org.pentaho.reporting.libraries.formatting.FastDecimalFormat;
import org.pentaho.reporting.libraries.xmlns.common.AttributeList;
import org.pentaho.reporting.libraries.xmlns.writer.DefaultTagDescription;
import org.pentaho.reporting.libraries.xmlns.writer.XmlWriter;

/**
 * Creation-Date: 20.10.2007, 16:40:57
 *
 * @author Thomas Morgner
 */
public class XmlDocumentWriter extends IterateStructuralProcessStep
{
  private static final String LAYOUT_OUTPUT_NAMESPACE =
      "http://reporting.pentaho.org/namespaces/output/layout-output/pageable/1.0";
  private static final Log logger = LogFactory.getLog(XmlDocumentWriter.class);

  private OutputStream outputStream;
  private String encoding;
  private StrictBounds drawArea;
  private XmlWriter xmlWriter;
  private FastDecimalFormat pointIntConverter;
  private FastDecimalFormat pointConverter;
  private boolean ignoreEmptyBorders = true;
  private OutputProcessorMetaData metaData;

  public XmlDocumentWriter(final OutputStream outputStream,
                           final OutputProcessorMetaData metaData)
  {
    this.metaData = metaData;
    if (outputStream == null)
    {
      throw new NullPointerException();
    }
    this.outputStream = outputStream;
    this.pointConverter = new FastDecimalFormat("0.####", Locale.US);
    this.pointIntConverter = new FastDecimalFormat("0", Locale.US);
  }

  public void open() throws IOException
  {
    final DefaultTagDescription td = new DefaultTagDescription();
    td.addDefaultDefinition(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, false);
    td.addTagDefinition(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "text", true);

    // prepare anything that might needed to be prepared ..
    final String encoding = metaData.getConfiguration().getConfigProperty
        ("org.pentaho.reporting.engine.classic.core.modules.output.pageable.xml.Encoding",
            EncodingRegistry.getPlatformDefaultEncoding());

    final Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, encoding));
    this.xmlWriter = new XmlWriter(writer, td);
    this.xmlWriter.writeXmlDeclaration(encoding);
    final AttributeList attrs = new AttributeList();
    attrs.addNamespaceDeclaration("", XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE);
    xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "layout-output", attrs, XmlWriter.OPEN);
  }

  public void close() throws IOException
  {
    // close all ..
    xmlWriter.writeCloseTag();
    xmlWriter.close();
  }

  public void processPhysicalPage(final PageGrid pageGrid,
                                  final LogicalPageBox logicalPage,
                                  final int row,
                                  final int col)
      throws IOException
  {
    final PhysicalPageBox page = pageGrid.getPage(row, col);
    if (page == null)
    {
      return;
    }

    final float width = (float) StrictGeomUtility.toExternalValue(page.getWidth());
    final float height = (float) StrictGeomUtility.toExternalValue(page.getHeight());
    final float marginLeft = (float) StrictGeomUtility.toExternalValue(page.getImageableX());
    final float marginRight = (float) StrictGeomUtility.toExternalValue
        (page.getWidth() - page.getImageableWidth() - page.getImageableX());
    final float marginTop = (float) StrictGeomUtility.toExternalValue(page.getImageableY());
    final float marginBottom = (float) StrictGeomUtility.toExternalValue
        (page.getHeight() - page.getImageableHeight() - page.getImageableY());

    final AttributeList pageAttributes = new AttributeList();
    pageAttributes.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "page-x", pointConverter.format(
        page.getGlobalX()));
    pageAttributes.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "page-y", pointConverter.format(
        page.getGlobalY()));
    pageAttributes.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "page-width", pointConverter.format(width));
    pageAttributes.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "page-height", pointConverter.format(
        height));
    pageAttributes.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "margin-top", pointConverter.format(
        marginTop));
    pageAttributes.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "margin-left", pointConverter.format(
        marginLeft));
    pageAttributes.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "margin-bottom", pointConverter.format(
        marginBottom));
    pageAttributes.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "margin-right", pointConverter.format(
        marginRight));

    xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "physical-page", pageAttributes, XmlWriter.OPEN);

    // and now process the box ..
    drawArea = new StrictBounds(page.getGlobalX(), page.getGlobalY(),
        page.getImageableWidth(), page.getImageableHeight());
    processPage(logicalPage);

    xmlWriter.writeCloseTag();
  }

  public void processLogicalPage(final LogicalPageKey key,
                                 final LogicalPageBox logicalPage)
      throws IOException
  {

    final float width = (float) StrictGeomUtility.toExternalValue(logicalPage.getPageWidth());
    final float height = (float) StrictGeomUtility.toExternalValue(logicalPage.getPageHeight());

    final AttributeList pageAttributes = new AttributeList();
    pageAttributes.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "logical-page-number", String.valueOf(
        key.getPosition()));
    pageAttributes.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "width", pointConverter.format(width));
    pageAttributes.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "height", pointConverter.format(height));

    xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "logical-page", pageAttributes, XmlWriter.OPEN);

    // and now process the box ..
    drawArea = new StrictBounds(0, 0, logicalPage.getPageWidth(), logicalPage.getPageHeight());
    processPage(logicalPage);

    xmlWriter.writeCloseTag();
  }

  protected void processPage(final LogicalPageBox rootBox)
  {
    final StrictBounds pageBounds = drawArea;
    startProcessing(rootBox.getWatermarkArea());

    final BlockRenderBox headerArea = rootBox.getHeaderArea();
    final BlockRenderBox footerArea = rootBox.getFooterArea();
    final BlockRenderBox repeatFooterArea = rootBox.getRepeatFooterArea();
    final StrictBounds headerBounds =
        new StrictBounds(headerArea.getX(), headerArea.getY(), headerArea.getWidth(), headerArea.getHeight());
    final StrictBounds footerBounds =
        new StrictBounds(footerArea.getX(), footerArea.getY(), footerArea.getWidth(), footerArea.getHeight());
    final StrictBounds repeatFooterBounds = new StrictBounds
        (repeatFooterArea.getX(), repeatFooterArea.getY(), repeatFooterArea.getWidth(), repeatFooterArea.getHeight());
    final StrictBounds contentBounds = new StrictBounds
        (rootBox.getX(), headerArea.getY() + headerArea.getHeight(),
            rootBox.getWidth(), footerArea.getY() - headerArea.getHeight());
    this.drawArea = headerBounds;
    startProcessing(headerArea);
    this.drawArea = contentBounds;
    processBoxChilds(rootBox);
    this.drawArea = repeatFooterBounds;
    startProcessing(repeatFooterArea);
    this.drawArea = footerBounds;
    startProcessing(footerArea);
    this.drawArea = pageBounds;
  }

  private void writeElementAttributes(final RenderNode element) throws IOException
  {
    final ReportAttributeMap attributes = element.getAttributes();
    final ElementType type = element.getElementType();
    final String[] attributeNamespaces = attributes.getNameSpaces();
    Arrays.sort(attributeNamespaces);
    for (int i = 0; i < attributeNamespaces.length; i++)
    {
      final String namespace = attributeNamespaces[i];

      final String[] attributeNames = attributes.getNames(namespace);
      Arrays.sort(attributeNames);
      for (int j = 0; j < attributeNames.length; j++)
      {
        final String name = attributeNames[j];
        final Object value = attributes.getAttribute(namespace, name);
        if (value == null)
        {
          continue;
        }

        final AttributeMetaData attrMeta = type.getMetaData().getAttributeDescription(namespace, name);
        if (attrMeta == null)
        {
          // if you want to use attributes in this output target, declare the attribute's metadata first.
          continue;
        }

        final AttributeList attList = new AttributeList();
        if (value instanceof String)
        {
          final String s = (String) value;
          if (StringUtils.isEmpty(s))
          {
            continue;
          }

          if (xmlWriter.isNamespaceDefined(namespace) == false &&
              attList.isNamespaceUriDefined(namespace) == false)
          {
            attList.addNamespaceDeclaration("autoGenNs", namespace);
          }

          // preserve strings, but discard anything else. Until a attribute has a definition, we cannot
          // hope to understand the attribute's value. String-attributes can be expressed in XML easily,
          // and string is also how all unknown attributes are stored by the parser.
          attList.setAttribute(namespace, name, String.valueOf(value));
          this.xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "attribute", attList, XmlWriter.CLOSE);
          continue;
        }

        if (xmlWriter.isNamespaceDefined(namespace) == false &&
            attList.isNamespaceUriDefined(namespace) == false)
        {
          attList.addNamespaceDeclaration("autoGenNs", namespace);
        }

        try
        {
          final PropertyEditor propertyEditor = attrMeta.getEditor();
          final String textValue;
          if (propertyEditor != null)
          {
            propertyEditor.setValue(value);
            textValue = propertyEditor.getAsText();
          }
          else
          {
            textValue = ConverterRegistry.toAttributeValue(value);

          }
          if (StringUtils.isEmpty(textValue) == false)
          {
            attList.setAttribute(namespace, name, textValue);
            this.xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "attribute", attList, XmlWriter.CLOSE);
          }
          else
          {
            XmlDocumentWriter.logger.warn(
                "Attribute '" + namespace + '|' + name + "' is not convertible to a text - returned null");
          }
        }
        catch (BeanException e)
        {
          if (attrMeta.isTransient() == false)
          {
            XmlDocumentWriter.logger.warn(
                "Attribute '" + namespace + '|' + name + "' is not convertible with the bean-methods");
          }
          else
          {
            XmlDocumentWriter.logger.debug(
                "Attribute '" + namespace + '|' + name + "' is not convertible with the bean-methods");
          }
        }
      }
    }

  }

  protected final boolean isNodeVisible(final RenderNode rect2)
  {
    final long drawAreaX0 = drawArea.getX();
    final long drawAreaY0 = drawArea.getY();
    final long drawAreaX1 = drawAreaX0 + drawArea.getWidth();
    final long drawAreaY1 = drawAreaY0 + drawArea.getHeight();

    final long x = rect2.getX();
    final long y = rect2.getY();
    final long width = rect2.getWidth();
    final long height = rect2.getHeight();
    final long x2 = x + width;
    final long y2 = y + height;

    if (width == 0)
    {
      if (x2 < drawAreaX0)
      {
        return false;
      }
      if (x > drawAreaX1)
      {
        return false;
      }
    }
    else
    {
      if (x2 <= drawAreaX0)
      {
        return false;
      }
      if (x >= drawAreaX1)
      {
        return false;
      }
    }
    if (height == 0)
    {
      if (y2 < drawAreaY0)
      {
        return false;
      }
      if (y > drawAreaY1)
      {
        return false;
      }
    }
    else
    {
      if (y2 <= drawAreaY0)
      {
        return false;
      }
      if (y >= drawAreaY1)
      {
        return false;
      }
    }
    return true;
  }

  private AttributeList createBoxAttributeList(final RenderBox box)
  {
    final AttributeList attributeList = new AttributeList();
    if (StringUtils.isEmpty(box.getName()) == false)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "name", box.getName());
    }
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "type", box.getClass().getSimpleName());

    final BoxDefinition definition = box.getBoxDefinition();
    final Border border = definition.getBorder();
    final StaticBoxLayoutProperties sblp = box.getStaticBoxLayoutProperties();

    final BorderEdge top = border.getTop();
    if (BorderEdge.EMPTY.equals(top) == false || ignoreEmptyBorders == false)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-top-color",
          convertColorToString(top));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-top-width",
          String.valueOf(StrictGeomUtility.toExternalValue(sblp.getBorderTop())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-top-style",
          String.valueOf(top.getBorderStyle()));
    }

    final BorderEdge left = border.getLeft();
    if (BorderEdge.EMPTY.equals(left) == false || ignoreEmptyBorders == false)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-left-color",
          convertColorToString(left));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-left-width",
          String.valueOf(StrictGeomUtility.toExternalValue(sblp.getBorderLeft())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-left-style",
          String.valueOf(left.getBorderStyle()));
    }

    final BorderEdge bottom = border.getBottom();
    if (BorderEdge.EMPTY.equals(bottom) == false || ignoreEmptyBorders == false)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-bottom-color",
          convertColorToString(bottom));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-bottom-width",
          String.valueOf(StrictGeomUtility.toExternalValue(sblp.getBorderBottom())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-bottom-style",
          String.valueOf(bottom.getBorderStyle()));
    }

    final BorderEdge right = border.getRight();
    if (BorderEdge.EMPTY.equals(right) == false || ignoreEmptyBorders == false)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-right-color",
          convertColorToString(right));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-right-width",
          String.valueOf(StrictGeomUtility.toExternalValue(sblp.getBorderRight())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-right-style",
          String.valueOf(right.getBorderStyle()));
    }

    final BorderCorner topLeft = border.getTopLeft();
    if (isEmptyCorner(topLeft) == false)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-top-left-x",
          String.valueOf(StrictGeomUtility.toExternalValue(topLeft.getWidth())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-top-left-y",
          String.valueOf(StrictGeomUtility.toExternalValue(topLeft.getHeight())));
    }

    final BorderCorner topRight = border.getTopRight();
    if (isEmptyCorner(topRight) == false)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-top-right-x",
          String.valueOf(StrictGeomUtility.toExternalValue(topRight.getWidth())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-top-right-y",
          String.valueOf(StrictGeomUtility.toExternalValue(topRight.getHeight())));
    }

    final BorderCorner bottomLeft = border.getBottomLeft();
    if (isEmptyCorner(bottomLeft) == false)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-bottom-left-x",
          String.valueOf(StrictGeomUtility.toExternalValue(bottomLeft.getWidth())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-bottom-left-y",
          String.valueOf(StrictGeomUtility.toExternalValue(bottomLeft.getHeight())));
    }

    final BorderCorner bottomRight = border.getBottomRight();
    if (isEmptyCorner(bottomRight) == false)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-bottom-right-x",
          String.valueOf(StrictGeomUtility.toExternalValue(bottomRight.getWidth())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "border-bottom-right-y",
          String.valueOf(StrictGeomUtility.toExternalValue(bottomRight.getHeight())));
    }

    if (sblp.getMarginTop() > 0)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "margin-top",
          String.valueOf(StrictGeomUtility.toExternalValue(sblp.getMarginTop())));
    }
    if (sblp.getMarginLeft() > 0)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "margin-left",
          String.valueOf(StrictGeomUtility.toExternalValue(sblp.getMarginLeft())));
    }
    if (sblp.getMarginBottom() > 0)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "margin-bottom",
          String.valueOf(StrictGeomUtility.toExternalValue(sblp.getMarginBottom())));
    }
    if (sblp.getMarginRight() > 0)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "margin-right",
          String.valueOf(StrictGeomUtility.toExternalValue(sblp.getMarginRight())));
    }

    if (definition.getPaddingTop() > 0)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "padding-top",
          String.valueOf(StrictGeomUtility.toExternalValue(definition.getPaddingTop())));
    }
    if (definition.getPaddingLeft() > 0)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "padding-left",
          String.valueOf(StrictGeomUtility.toExternalValue(definition.getPaddingLeft())));
    }
    if (definition.getPaddingBottom() > 0)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "padding-bottom",
          String.valueOf(StrictGeomUtility.toExternalValue(definition.getPaddingBottom())));
    }
    if (definition.getPaddingRight() > 0)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "padding-right",
          String.valueOf(StrictGeomUtility.toExternalValue(definition.getPaddingRight())));
    }

    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "x",
        String.valueOf(StrictGeomUtility.toExternalValue(box.getX())));
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "y",
        String.valueOf(StrictGeomUtility.toExternalValue(box.getY())));
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "width",
        String.valueOf(StrictGeomUtility.toExternalValue(box.getWidth())));
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "height",
        String.valueOf(StrictGeomUtility.toExternalValue(box.getHeight())));

    final Color backgroundColor = (Color) box.getStyleSheet().getStyleProperty(ElementStyleKeys.BACKGROUND_COLOR);
    if (backgroundColor != null)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "background-color",
          ColorValueConverter.colorToString(backgroundColor));
    }

    final Color color = (Color) box.getStyleSheet().getStyleProperty(ElementStyleKeys.PAINT);
    if (color != null)
    {
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "color",
          ColorValueConverter.colorToString(color));
    }
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "font-face",
        (String) box.getStyleSheet().getStyleProperty(TextStyleKeys.FONT));
    final Object o = box.getStyleSheet().getStyleProperty(TextStyleKeys.FONTSIZE);
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "font-size", pointIntConverter.format(o));
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "font-style-bold", String.valueOf(
        box.getStyleSheet().getStyleProperty(TextStyleKeys.BOLD)));
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "font-style-italics", String.valueOf(
        box.getStyleSheet().getStyleProperty(TextStyleKeys.ITALIC)));
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "font-style-underline", String.valueOf(
        box.getStyleSheet().getStyleProperty(TextStyleKeys.UNDERLINED)));
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "font-style-strikethrough", String.valueOf(
        box.getStyleSheet().getStyleProperty(TextStyleKeys.STRIKETHROUGH)));

    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "anchor",
        (String) box.getStyleSheet().getStyleProperty(ElementStyleKeys.ANCHOR_NAME));
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "href",
        (String) box.getStyleSheet().getStyleProperty(ElementStyleKeys.HREF_TARGET));
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "href-window",
        (String) box.getStyleSheet().getStyleProperty(ElementStyleKeys.HREF_WINDOW));
    attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "href-title",
        (String) box.getStyleSheet().getStyleProperty(ElementStyleKeys.HREF_TITLE));

    return attributeList;
  }

  private String convertColorToString(final BorderEdge borderEdge)
  {
    final Color o = borderEdge.getColor();
    if (o == null)
    {
      return null;
    }
    return ColorValueConverter.colorToString(o);
  }

  private boolean isEmptyCorner(final BorderCorner corner)
  {
    if (ignoreEmptyBorders == false)
    {
      return false;
    }
    return corner.getWidth() == 0 && corner.getHeight() == 0;
  }

  protected boolean startBlockBox(final BlockRenderBox box)
  {

    try
    {
      if ((box.getNodeType() & LayoutNodeTypes.MASK_BOX_PAGEAREA) == LayoutNodeTypes.MASK_BOX_PAGEAREA)
      {
        final AttributeList list = createBoxAttributeList(box);
        list.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "type", box.getName());
        xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "page-area", list, XmlWriter.OPEN);
      }
      else if (box.getNodeType() == LayoutNodeTypes.TYPE_BOX_PARAGRAPH)
      {
        xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "p", createBoxAttributeList(box), XmlWriter.OPEN);
      }
      else
      {
        xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "block", createBoxAttributeList(box),
            XmlWriter.OPEN);
      }
      writeElementAttributes(box);
      return true;
    }
    catch (IOException e)
    {
      throw new InvalidReportStateException(e.getMessage(), e);
    }
  }

  protected void finishBlockBox(final BlockRenderBox box)
  {
    try
    {
      xmlWriter.writeCloseTag();
    }
    catch (IOException e)
    {
      throw new InvalidReportStateException(e.getMessage(), e);
    }
  }

  protected boolean startInlineBox(final InlineRenderBox box)
  {
    try
    {
      if (box.getNodeType() == LayoutNodeTypes.TYPE_BOX_LINEBOX)
      {
        xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "line", createBoxAttributeList(box),
            XmlWriter.OPEN);
      }
      else
      {
        xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "inline", createBoxAttributeList(box),
            XmlWriter.OPEN);
      }
      writeElementAttributes(box);
      return true;
    }
    catch (IOException e)
    {
      throw new InvalidReportStateException(e.getMessage(), e);
    }
  }

  protected void finishInlineBox(final InlineRenderBox box)
  {
    try
    {
      xmlWriter.writeCloseTag();
    }
    catch (IOException e)
    {
      throw new InvalidReportStateException(e.getMessage(), e);
    }
  }

  protected boolean startOtherBox(final RenderBox box)
  {
    try
    {
      xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "other-box", createBoxAttributeList(box),
          XmlWriter.OPEN);
      writeElementAttributes(box);
      return true;
    }
    catch (IOException e)
    {
      throw new InvalidReportStateException(e.getMessage(), e);
    }
  }

  protected void finishOtherBox(final RenderBox box)
  {
    try
    {
      xmlWriter.writeCloseTag();
    }
    catch (IOException e)
    {
      throw new InvalidReportStateException(e.getMessage(), e);
    }
  }

  protected boolean startCanvasBox(final CanvasRenderBox box)
  {
    try
    {
      xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "canvas", createBoxAttributeList(box),
          XmlWriter.OPEN);
      writeElementAttributes(box);
      return true;
    }
    catch (IOException e)
    {
      throw new InvalidReportStateException(e.getMessage(), e);
    }
  }

  protected void finishCanvasBox(final CanvasRenderBox box)
  {
    try
    {
      xmlWriter.writeCloseTag();
    }
    catch (IOException e)
    {
      throw new InvalidReportStateException(e.getMessage(), e);
    }
  }

  protected void processOtherNode(final RenderNode node)
  {
    try
    {
      final int nodeType = node.getNodeType();
      if (nodeType == LayoutNodeTypes.TYPE_NODE_TEXT)
      {
        final RenderableText text = (RenderableText) node;
        final AttributeList attributeList = new AttributeList();
        attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "x",
            String.valueOf(StrictGeomUtility.toExternalValue(node.getX())));
        attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "y",
            String.valueOf(StrictGeomUtility.toExternalValue(node.getY())));
        attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "width",
            String.valueOf(StrictGeomUtility.toExternalValue(node.getWidth())));
        attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "height",
            String.valueOf(StrictGeomUtility.toExternalValue(node.getHeight())));
        xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "text", attributeList, XmlWriter.OPEN);
        xmlWriter.writeTextNormalized(text.getRawText(), true);
        xmlWriter.writeCloseTag();

      }
      else if (nodeType == LayoutNodeTypes.TYPE_NODE_SPACER)
      {
        final SpacerRenderNode spacer = (SpacerRenderNode) node;
        final AttributeList attributeList = new AttributeList();
        attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "width",
            String.valueOf(StrictGeomUtility.toExternalValue(node.getWidth())));
        attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "height",
            String.valueOf(StrictGeomUtility.toExternalValue(node.getHeight())));
        attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "preserve",
            String.valueOf(spacer.isDiscardable() == false));
        xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "spacer", attributeList, XmlWriter.CLOSE);
      }
    }
    catch (IOException e)
    {
      throw new InvalidReportStateException(e.getMessage(), e);
    }
  }

  protected void processRenderableContent(final RenderableReplacedContentBox node)
  {
    try
    {
      final RenderableReplacedContent prc = node.getContent();
      final AttributeList attributeList = new AttributeList();
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "x",
          String.valueOf(StrictGeomUtility.toExternalValue(node.getX())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "y",
          String.valueOf(StrictGeomUtility.toExternalValue(node.getY())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "width",
          String.valueOf(StrictGeomUtility.toExternalValue(node.getWidth())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "height",
          String.valueOf(StrictGeomUtility.toExternalValue(node.getHeight())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "source", String.valueOf(prc.getSource()));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "content-width",
          String.valueOf(StrictGeomUtility.toExternalValue(prc.getContentWidth())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "content-height",
          String.valueOf(StrictGeomUtility.toExternalValue(prc.getContentHeight())));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "requested-width",
          convertRenderLength(prc.getRequestedWidth()));
      attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "requested-height",
          convertRenderLength(prc.getRequestedHeight()));

      final Object o = prc.getRawObject();
      if (o != null)
      {
        attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "raw-object-type",
            o.getClass().getName());
      }
      else
      {
        attributeList.setAttribute(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "raw-object-type", "null");
      }
      xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "replaced-content", attributeList, XmlWriter.OPEN);
      writeElementAttributes(node);
      xmlWriter.writeCloseTag();
    }
    catch (IOException e)
    {
      throw new InvalidReportStateException(e.getMessage(), e);
    }
  }

  private String convertRenderLength(final RenderLength length)
  {
    if (length == null)
    {
      return null;
    }
    if (RenderLength.AUTO.equals(length))
    {
      return "auto";
    }
    if (length.isPercentage())
    {
      return pointConverter.format(StrictGeomUtility.toExternalValue(-length.getValue())) + '%';
    }
    return pointConverter.format(StrictGeomUtility.toExternalValue(length.getValue()));
  }

  protected void processParagraphChilds(final ParagraphRenderBox box)
  {
    processBoxChilds(box);
  }


  protected boolean startRowBox(final RenderBox box)
  {

    try
    {
      xmlWriter.writeTag(XmlDocumentWriter.LAYOUT_OUTPUT_NAMESPACE, "row-box", createBoxAttributeList(box), XmlWriter.OPEN);
      writeElementAttributes(box);
      return true;
    }
    catch (IOException e)
    {
      throw new InvalidReportStateException(e.getMessage(), e);
    }
  }

  protected void finishRowBox(final RenderBox box)
  {
    try
    {
      xmlWriter.writeCloseTag();
    }
    catch (IOException e)
    {
      throw new InvalidReportStateException(e.getMessage(), e);
    }
  }

}