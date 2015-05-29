package org.opentrafficsim.core.gtu.animation;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.List;

import org.opentrafficsim.core.gtu.GTU;

/**
 * Determine the fill color for a GTU.
 * <p>
 * The result of the toString method of a GTUColorer is used to label the choices in the ColorControlPanel.
 * Implementations of GTUColorer should ensure that the toString method returns something in the users locale.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 27 mei 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface GTUColorer
{
    /**
     * Return the fill color for a GTU.
     * @param gtu GTU; the GTU
     * @return Color; the color for the GTU
     * @throws RemoteException on communications failure
     */
    public Color getColor(GTU<?> gtu) throws RemoteException;

    /**
     * Return a list of legend entries (useful to make a legend of the colors used to render the GTUs)
     * @return List&lt;LegendEntry&gt;; the list of legend entries; the caller should not (try to) modify this List
     */
    public List<LegendEntry> getLegend();

    /**
     * Packs a Color, a short description and a long description in one object.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
     * reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version 27 mei 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class LegendEntry
    {
        /** The Color. */
        final Color color;

        /** Name of the legend entry (should be terse). */
        final String name;

        /** Description of the legend entry (may use HTML). */
        final String description;

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
        public Color getColor()
        {
            return this.color;
        }
        
        /**
         * @return name.
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * @return description.
         */
        public String getDescription()
        {
            return this.description;
        }

    }
}
