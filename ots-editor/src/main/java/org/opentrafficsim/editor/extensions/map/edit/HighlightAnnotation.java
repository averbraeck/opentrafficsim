package org.opentrafficsim.editor.extensions.map.edit;

import java.awt.Color;

import org.opentrafficsim.animation.DrawLevel;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.MapData;

/**
 * Highlights map data.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class HighlightAnnotation extends SelectionAnnotation
{

    /** Highlight color. */
    static final Color HIGHLIGHT_COLOR = OtsEditor.PROPERTIES_STORE.getColorOrDefault("map.highlightColor", Color.MAGENTA);

    /**
     * Constructor.
     * @param source source
     * @param map map
     */
    public HighlightAnnotation(final MapData source, final EditorMap map)
    {
        super(source, map, DrawLevel.ANNOTATION.getZ());
    }

    /**
     * Returns the color.
     * @return color
     */
    @Override
    protected Color getColor()
    {
        return HIGHLIGHT_COLOR;
    }

}
