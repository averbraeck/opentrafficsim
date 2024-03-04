/**
 * The map editor allows visual specification of the network.
 * <p>
 * For additional visualization of elements, do the following:
 * <ul>
 * <li>Add the XML node path to {@code EditorMap.TYPES}.</li>
 * <li>Create class for data object that extends {@code MapData}. Under its destroy implementation (which should call
 * {@code super.destroy()}, remove all listeners it creates.</li>
 * <li>Create instance of animation and data object under {@code EditorMap.setValid()}.</li>
 * <li>Add visualization toggle under {@code EditorMap.setAnimationToggles()}.</li>
 * </ul>
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
package org.opentrafficsim.editor.extensions.map;
