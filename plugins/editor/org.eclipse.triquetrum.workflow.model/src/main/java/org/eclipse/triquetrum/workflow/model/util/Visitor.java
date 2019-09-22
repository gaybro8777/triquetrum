/*******************************************************************************
 * Copyright (c) 2015,2019 iSencia Belgium NV.
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
package org.eclipse.triquetrum.workflow.model.util;

import org.eclipse.triquetrum.workflow.model.NamedObj;

/**
 * Visitor interface to traverse & visit a model's elements.
 *
 */
public interface Visitor {

  /**
   *
   * @param modelElement
   */
  void visit(NamedObj modelElement);

}
