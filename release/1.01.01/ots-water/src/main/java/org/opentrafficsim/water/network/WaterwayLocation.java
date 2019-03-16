package org.opentrafficsim.water.network;

import java.io.Serializable;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Bounds;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Location along a waterway.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
public class WaterwayLocation implements Locatable, Serializable
{
    /** */
    private static final long serialVersionUID = 20161106L;

    /** the waterway. */
    private final Waterway waterway;

    /** position along the waterway, in the direction of the design line. */
    private final Length position;

    /** cached location. */
    private final DirectedPoint location;

    /**
     * @param waterway Waterway; the waterway
     * @param position Length; position along the waterway, in the direction of the design line
     */
    public WaterwayLocation(final Waterway waterway, final Length position)
    {
        Throw.whenNull(waterway, "waterway cannot be null");
        Throw.whenNull(position, "position cannot be null");
        this.waterway = waterway;
        this.position = position;
        this.location = waterway.getDesignLine().getLocationExtended(position);
    }

    /**
     * @return waterway the waterway
     */
    public final Waterway getWaterway()
    {
        return this.waterway;
    }

    /**
     * @return position position along the waterway, in the direction of the design line
     */
    public final Length getPosition()
    {
        return this.position;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation()
    {
        return this.location;
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds()
    {
        return new BoundingSphere();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return this.waterway + "(" + this.getPosition().getInUnit(LengthUnit.KILOMETER) + ")";
    }

    /**
     * @return short route info
     */
    public final String toShortString()
    {
        return String.format("%1$s(%2$4.2f)", this.waterway.getId(), this.getPosition().getInUnit(LengthUnit.KILOMETER));
    }
}
