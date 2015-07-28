package org.opentrafficsim.core.network;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * "1D" implementation. Mapping on the design line (often the center line) of a road.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <NODEID> the ID type of the Node, e.g., String.
 * @param <LINKID> the ID type of the Link, e.g., String.
 */
public class LinkLocation<LINKID, NODEID>
{
    /** The link of the location of a point relative to the GTU. */
    private final Link<LINKID, NODEID> link;

    /** The fractional position (between 0.0 and 1.0) of the reference point on the lane. */
    private final double fractionalLongitudinalPosition;

    /**
     * @param link The link of the location of a point relative to the GTU.
     * @param fractionalLongitudinalPosition The fractional position (between 0.0 and 1.0) of the reference point on the link.
     */
    public LinkLocation(final Link<LINKID, NODEID> link, final double fractionalLongitudinalPosition)
    {
        super();
        this.link = link;
        this.fractionalLongitudinalPosition = fractionalLongitudinalPosition;
    }

    /**
     * @param link The link of the location of a point relative to the GTU.
     * @param position The position as a length of the reference point on the link.
     */
    public LinkLocation(final Link<LINKID, NODEID> link, final DoubleScalar.Rel<LengthUnit> position)
    {
        super();
        this.link = link;
        this.fractionalLongitudinalPosition = DoubleScalar.divide(position, this.link.getLength()).doubleValue();
    }

    /**
     * @return lane.
     */
    public final Link<LINKID, NODEID> getLink()
    {
        return this.link;
    }

    /**
     * @return fractionalLongitudinalPosition.
     */
    public final double getFractionalLongitudinalPosition()
    {
        return this.fractionalLongitudinalPosition;
    }

    /**
     * @return position as a length as a traveled length on this link.
     */
    public final DoubleScalar.Rel<LengthUnit> getLongitudinalPosition()
    {
        return new DoubleScalar.Rel<LengthUnit>(this.link.getLength().getSI() * getFractionalLongitudinalPosition(),
            LengthUnit.METER);
    }

    /**
     * Returns the distance to another LinkLocation. If the other location is in front of us, the distance is positive. If it is
     * behind us, it is negative.
     * @param loc the link location to find the distance to.
     * @return the distance to another LinkLocation.
     */
    public final DoubleScalar.Rel<LengthUnit> distance(final LinkLocation<LINKID, NODEID> loc)
    {
        if (this.link.equals(loc.getLink()))
        {
            return DoubleScalar.minus(loc.getLongitudinalPosition(), this.getLongitudinalPosition()).immutable();
        }

        // TODO not on the same link. Find shortest path...
        return null;
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return String.format("%s %.3f%s", getLink(), getLongitudinalPosition().getInUnit(), getLongitudinalPosition()
            .getUnit());
    }
}
