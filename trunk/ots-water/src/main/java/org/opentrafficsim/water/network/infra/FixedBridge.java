package org.opentrafficsim.water.network.infra;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.water.network.Waterway;
import org.opentrafficsim.water.network.WaterwayLocation;

/**
 * A fixed bridge.
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
public class FixedBridge extends Obstacle implements HeightRestricted
{
    /** */
    private static final long serialVersionUID = 1L;

    /** max height in meters above surface. */
    private Length maxHeight;

    /**
     * @param name String; the name of the bridge
     * @param waterwayLocation WaterwayLocation; the location on the waterway
     * @param maxHeight Length; the max sailing height to pass the bridge
     */
    public FixedBridge(final String name, final WaterwayLocation waterwayLocation, final Length maxHeight)
    {
        super(name, waterwayLocation);
        this.maxHeight = maxHeight;
    }

    /**
     * @param name String; the name of the bridge
     * @param waterway Waterway; the waterway
     * @param km Length; the location on the waterway
     * @param maxHeight Length; the max sailing height to pass the bridge
     */
    public FixedBridge(final String name, final Waterway waterway, final Length km, final Length maxHeight)
    {
        this(name, new WaterwayLocation(waterway, km), maxHeight);
    }

    /** {@inheritDoc} */
    @Override
    public final Length getMaxHeight()
    {
        return this.maxHeight;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "Fixed Bridge " + this.getName() + " at " + this.getWaterwayLocation();
    }
}
