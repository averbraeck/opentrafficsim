/**
 * 
 */
package org.opentrafficsim.water.network;

/**
 * A link between two points along waterways.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * <p>
 * Based on software from the IDVV project, which is Copyright (c) 2013 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving
 * and licensed without restrictions to Delft University of Technology, including the right to sub-license sources and derived
 * products to third parties.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class WaterwayLink
{
    /** from waterway location. */
    private WaterwayLocation waterwayLocation1;

    /** to waterway location. */
    private WaterwayLocation waterwayLocation2;

    /**
     * @param waterwayLocation1 WaterwayLocation; from waterway location
     * @param waterwayLocation2 WaterwayLocation; to waterway location
     */
    public WaterwayLink(final WaterwayLocation waterwayLocation1, final WaterwayLocation waterwayLocation2)
    {
        this.waterwayLocation1 = waterwayLocation1;
        this.waterwayLocation2 = waterwayLocation2;
    }

    /**
     * @return the from waterwayLocation
     */
    public final WaterwayLocation getWaterwayLocation1()
    {
        return this.waterwayLocation1;
    }

    /**
     * @return the to waterwayLocation
     */
    public final WaterwayLocation getWaterwayLocation2()
    {
        return this.waterwayLocation2;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "WaterwayLink " + this.waterwayLocation1 + " x " + this.waterwayLocation2;
    }

    /**
     * @return short link info
     */
    public final String toShortString()
    {
        return this.waterwayLocation1.toShortString() + "x" + this.waterwayLocation2.toShortString();
    }
}
