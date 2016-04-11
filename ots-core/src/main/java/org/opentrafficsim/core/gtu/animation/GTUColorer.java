package org.opentrafficsim.core.gtu.animation;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;

import org.opentrafficsim.core.gtu.GTU;

/**
 * Determine the fill color for a GTU.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version 27 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface GTUColorer
{
    /**
     * Return the fill color for a GTU.
     * @param gtu GTU; the GTU
     * @return Color; the color for the GTU
     */
    Color getColor(GTU gtu);

    /**
     * Return a list of legend entries (useful to make a legend of the colors used to render the GTUs).
     * @return List&lt;LegendEntry&gt;; the list of legend entries; the caller should not (try to) modify this List
     */
    List<LegendEntry> getLegend();

    /**
     * Packs a Color, a short description and a long description in one object.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @version $Revision$, $LastChangedDate$, by $Author$,
     *          initial version 27 mei 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
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
         * @param color Color; the color of the new LegendEntry
         * @param name String; the name of the new LegendEntry (should be terse)
         * @param description String; description of the new LegendEntry (may use HTML)
         */
        public LegendEntry(final Color color, final String name, final String description)
        {
            this.color = color;
            this.name = name;
            this.description = description;
        }

        /**
         * @return Color; the color of this LegendEntry
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

    }
}
