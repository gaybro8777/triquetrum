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
package org.eclipse.triquetrum.workflow.editor;

import org.apache.commons.lang.StringUtils;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.vergil.kernel.attributes.TextAttribute;

/**
 * Enum to categorize the Ptolemy model elements, so we can group features/behavior in the Triquetrum editor as appropriate.
 *
 * TODO analyse if this can move (partially) to the Triquetrum EMF model or should stay in the editor code.
 *
 */
public enum BoCategory implements Category{
  CompositeActor(ptolemy.actor.CompositeActor.class), 
  Actor(ptolemy.actor.Actor.class), 
  Director(ptolemy.actor.Director.class), 
  Parameter(ptolemy.data.expr.Parameter.class), 
  Attribute(ptolemy.kernel.util.Attribute.class), 
  Annotation(TextAttribute.class), 
  Relation(IORelation.class), 
  Vertex(ptolemy.moml.Vertex.class), 
  Port(IOPort.class);

  private final static Logger LOGGER = LoggerFactory.getLogger(BoCategory.class);
  private final static String BO_CATEGORY_PROPNAME = "__BO_CATEGORY";

  private Class<?> ptBaseClass;

  /**
   * @param ptBaseClass
   */
  private BoCategory(Class<?> ptBaseClass) {
    this.ptBaseClass = ptBaseClass;
  }

  /**
   *
   * @param ptClass
   * @return the first category enum value that matches the given ptolemy class
   */
  public static BoCategory valueOf(Class<?> ptClass) {
    BoCategory result = null;
    for (BoCategory cat : values()) {
      if (cat.matches(ptClass)) {
        result = cat;
        break;
      }
    }
    return result;
  }

  /**
   *
   * @param ptClass
   * @return true if this category enum instance has a ptolemy base class that is assignable from the given ptClass.
   */
  public boolean matches(Class<?> ptClass) {
    return ptBaseClass.isAssignableFrom(ptClass);
  }

  /**
   *
   * @param pe
   * @return the category stored as a property in the given {@link PictogramElement}, or null if no valid category was found.
   */
  public static BoCategory retrieveFrom(PictogramElement pe) {
    String boCategoryStr = Graphiti.getPeService().getPropertyValue(pe, BoCategory.BO_CATEGORY_PROPNAME);
    if (!StringUtils.isBlank(boCategoryStr)) {
      try {
        return BoCategory.valueOf(boCategoryStr);
      } catch (Exception e) {
        LOGGER.error("Invalid BO Category for " + pe, e);
      }
    }
    return null;
  }

  /**
   * Store this category as a property in the given {@link PictogramElement}.
   * 
   * @param pe
   */
  @Override
  public void storeIn(PictogramElement pe) {
    Graphiti.getPeService().setPropertyValue(pe, BoCategory.BO_CATEGORY_PROPNAME, name());
  }
}
