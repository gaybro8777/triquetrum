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
package org.eclipse.triquetrum.workflow.editor.palette.ui;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.SimpleObjectTransfer;

/**
 * duplicated from org.eclipse.gef.ui.parts
 */
public class TreeViewerTransfer extends SimpleObjectTransfer {

  private static final TreeViewerTransfer INSTANCE = new TreeViewerTransfer();
  private static final String TYPE_NAME = "Local Transfer"//$NON-NLS-1$
      + System.currentTimeMillis() + ":" + INSTANCE.hashCode();//$NON-NLS-1$
  private static final int TYPEID = registerType(TYPE_NAME);

  private static EditPartViewer viewer;

  /**
   * Returns the singleton instance.
   *
   * @return The singleton instance
   */
  public static TreeViewerTransfer getInstance() {
    return INSTANCE;
  }

  private TreeViewerTransfer() {
  }

  /**
   * @see org.eclipse.swt.dnd.Transfer#getTypeIds()
   */
  @Override
  protected int[] getTypeIds() {
    return new int[] { TYPEID };
  }

  /**
   * @see org.eclipse.swt.dnd.Transfer#getTypeNames()
   */
  @Override
  protected String[] getTypeNames() {
    return new String[] { TYPE_NAME };
  }

  /**
   * Returns the viewer where the drag started.
   *
   * @return The viewer where the drag started
   */
  public EditPartViewer getViewer() {
    return viewer;
  }

  /**
   * Sets the viewer where the drag started.
   *
   * @param epv
   *          The viewer
   */
  public void setViewer(EditPartViewer epv) {
    viewer = epv;
  }

}