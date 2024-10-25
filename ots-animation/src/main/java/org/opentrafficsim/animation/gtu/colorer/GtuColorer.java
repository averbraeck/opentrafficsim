package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;

import org.opentrafficsim.animation.Colorer;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * Determine the fill color for a GTU.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public interface GtuColorer extends Colorer<Gtu>
{
    /**
     * Return a list of legend entries (useful to make a legend of the colors used to render the GTUs).
     * @return the list of legend entries; the caller should not (try to) modify this List
     */
    List<LegendEntry> getLegend();

    /**
     * Packs a Color, a short description and a long description in one object.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     */
    class LegendEntry implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20150000L;

        /** The Color. */
        private final Color color;

        /** Name of the legend entry (should be terse). */
        private final String name;

        /** Description of the legend entry (may use HTML). */
        private final String description;

        /**
         * Construct a new LegendEntry.
         * @param color the color of the new LegendEntry
         * @param name the name of the new LegendEntry (should be terse)
         * @param description description of the new LegendEntry (may use HTML)
         */
        public LegendEntry(final Color color, final String name, final String description)
        {
            this.color = color;
            this.name = name;
            this.description = description;
        }

        /**
         * @return the color of this LegendEntry
         */
        public final Color getColor()
        {
            return this.color;
        }

        /**
         * @return name.
         */
        public final String getName()
        {
            return this.name;
        }

        /**
         * @return description.
         */
        public final String getDescription()
        {
            return this.description;
        }

        @Override
        public final String toString()
        {
            return "LegendEntry [color=" + this.color + ", name=" + this.name + ", description=" + this.description + "]";
        }

    }
}
