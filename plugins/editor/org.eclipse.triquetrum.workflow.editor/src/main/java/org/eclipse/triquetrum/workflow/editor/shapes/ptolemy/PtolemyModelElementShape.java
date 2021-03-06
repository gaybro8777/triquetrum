/*******************************************************************************
 * Copyright (c) 2016,2019 iSencia Belgium NV.
 *  
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Erwin De Ley - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.triquetrum.workflow.editor.shapes.ptolemy;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.platform.ga.IRendererContext;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.triquetrum.workflow.editor.TriqDiagramBehavior;
import org.eclipse.triquetrum.workflow.editor.shapes.AbstractCustomModelElementShape;
import org.eclipse.triquetrum.workflow.util.WorkflowUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.ArcAttribute;
import ptolemy.vergil.kernel.attributes.ArrowAttribute;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;
import ptolemy.vergil.kernel.attributes.ImageAttribute;
import ptolemy.vergil.kernel.attributes.LineAttribute;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;
import ptolemy.vergil.kernel.attributes.ResizablePolygonAttribute;
import ptolemy.vergil.kernel.attributes.TextAttribute;
import ptolemy.vergil.kernel.attributes.VisibleAttribute;

/**
 * Custom shape rendering for Ptolemy II's MoML icon definitions, using assemblies of VisibleAttributes.
 * <p>
 * The actual drawing logic is delegated to DrawingStrategy implementations, matching the different possible specializations
 * of VisibleAttribute.
 * </p>
 *
 */
public class PtolemyModelElementShape extends AbstractCustomModelElementShape {
  private final static Logger LOGGER = LoggerFactory.getLogger(PtolemyModelElementShape.class);

  // Remark that the Map should have keys and values that, for each entry individually, have equal VisibleAttribute implementation classes as generic types.
  // But I've found no way to express this using Java generics and it's not worth the trouble to write/use custom Maps for this. 
  private static Map<Class<? extends VisibleAttribute>, DrawingStrategy<? extends VisibleAttribute>> drawingStrategies = new HashMap<>();

  static {
    drawingStrategies.put(ArcAttribute.class, new ArcDrawingStrategy());
    drawingStrategies.put(ArrowAttribute.class, new ArrowDrawingStrategy());
    drawingStrategies.put(EllipseAttribute.class, new EllipseDrawingStrategy());
    drawingStrategies.put(ImageAttribute.class, new ImageDrawingStrategy());
    drawingStrategies.put(LineAttribute.class, new LineDrawingStrategy());
    drawingStrategies.put(RectangleAttribute.class, new RectangleDrawingStrategy());
    drawingStrategies.put(ResizablePolygonAttribute.class, new ResizablePolygonDrawingStrategy());
    drawingStrategies.put(TextAttribute.class, new TextDrawingStrategy());
  }

  private Rectangle ptShapeBounds;
  private EditorIcon iconDef;
  private ResourceManager resourceManager;

  public PtolemyModelElementShape(IRendererContext rendererContext) {
    super(rendererContext);
    this.resourceManager = ((TriqDiagramBehavior) rendererContext.getDiagramTypeProvider().getDiagramBehavior()).getResourceManager();
  }

  @Override
  protected void fillShape(Graphics graphics) {
    LOGGER.trace("Ptolemy fillShape - entry - for {}", getIconURI());
    try {
      iconDef = iconDef != null ? iconDef : (EditorIcon) WorkflowUtils.readFrom(URI.create(getIconURI()));
      // As Ptolemy II icon definitions often use negative coordinates,
      // while draw2d graphics assumes a top-left corner at (0,0),
      // the overall icon shape drawing must first determine the most extreme
      // boundaries as defined in the icon MOML and translate the draw2d coordinates space
      // accordingly before starting the effective drawing.
      ptShapeBounds = ptShapeBounds != null ? ptShapeBounds : determineExtremeBounds(iconDef, graphics);
      LOGGER.debug("Extreme bounds for {} : {}", getIconURI(), ptShapeBounds);

      int width = ptShapeBounds.width;
      int height = ptShapeBounds.height;

      Rectangle bnds = getBounds();
      graphics.setAntialias(SWT.ON);
      graphics.setTextAntialias(SWT.ON);
      graphics.drawRectangle(bnds.x, bnds.y, width, height);
      graphics.translate(getLocation());
      graphics.translate(ptShapeBounds.getTopLeft().getNegated().getTranslated(1, 1));
      for (VisibleAttribute a : iconDef.attributeList(VisibleAttribute.class)) {
        DrawingStrategy drawingStrategy = drawingStrategies.get(a.getClass());
        if (drawingStrategy != null) {
          drawingStrategy.draw(a, graphics, resourceManager);
        }
      }
      setInitialSize(ga, width + 2, height + 2);
    } catch (Exception e) {
      LOGGER.error("Error drawing ptolemy shape " + getIconURI(), e);
    }
    LOGGER.trace("Ptolemy fillShape - exit - for {}", getIconURI());
  }

  @Override
  protected void outlineShape(Graphics graphics) {
    float lineInset = Math.max(1.0f, getLineWidthFloat()) / 2.0f;
    int inset1 = (int) Math.floor(lineInset);
    int inset2 = (int) Math.ceil(lineInset);

    Rectangle r = Rectangle.SINGLETON.setBounds(getBounds());
    r.x += inset1;
    r.y += inset1;
    r.width -= inset1 + inset2;
    r.height -= inset1 + inset2;

    graphics.drawRectangle(r);
  }

  private Rectangle determineExtremeBounds(EditorIcon iconDef, Graphics graphics) {
    LOGGER.trace("Ptolemy determineExtremeBounds - entry - for {}", iconDef.getName());
    Point tlp = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
    Point brp = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
    for (VisibleAttribute a : iconDef.attributeList(VisibleAttribute.class)) {
      DrawingStrategy drawingStrategy = drawingStrategies.get(a.getClass());
      if (drawingStrategy != null) {
        Rectangle aBounds = drawingStrategy.getBounds(a, resourceManager);
        LOGGER.debug("Bounds for {} : {}", a, aBounds);
        tlp.x = Math.min(tlp.x, aBounds.x);
        tlp.y = Math.min(tlp.y, aBounds.y);
        brp.x = Math.max(brp.x, aBounds.x + aBounds.width);
        brp.y = Math.max(brp.y, aBounds.y + aBounds.height);
      }
    }
    Rectangle result = new Rectangle(tlp, brp);
    LOGGER.trace("Ptolemy determineExtremeBounds - exit - for {} - bounds {}", iconDef.getName(), result);
    return result;
  }
}
