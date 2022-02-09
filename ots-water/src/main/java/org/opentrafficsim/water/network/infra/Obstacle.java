package org.opentrafficsim.water.network.infra;

import org.opentrafficsim.water.AbstractNamedLocated;
import org.opentrafficsim.water.network.WaterwayLocation;

/**
 * Obstacle in a waterway.
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
public abstract class Obstacle extends AbstractNamedLocated
{
    /** */
    private static final long serialVersionUID = 20161105L;

    /** the waterway along which it is located, and the location on the waterway. */
    private final WaterwayLocation waterwayLocation;

    /**
     * @param name String; the name of the obstacle
     * @param waterwayLocation WaterwayLocation; location of the obstacle along the waterway
     */
    public Obstacle(final String name, final WaterwayLocation waterwayLocation)
    {
        super(name, waterwayLocation.getLocation());
        this.waterwayLocation = waterwayLocation;
    }

    /**
     * @return waterwayLocation
     */
    protected final WaterwayLocation getWaterwayLocation()
    {
        return this.waterwayLocation;
    }

}
