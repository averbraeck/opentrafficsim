package org.opentrafficsim.core.network;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTUDirectionality;

/**
 * Directed link position.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 22 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DirectedLinkPosition extends LinkDirection
{

    /** */
    private static final long serialVersionUID = 20181022L;

    /** The fractional position (between 0.0 and 1.0) of the reference point on the lane. */
    private final double fractionalLongitudinalPosition;

    /**
     * @param link Link; the link
     * @param fractionalLongitudinalPosition double; fractional position
     * @param direction GTUDirectionality; the direction on the link, with or against the design line
     */
    public DirectedLinkPosition(final Link link, final double fractionalLongitudinalPosition, final GTUDirectionality direction)
    {
        super(link, direction);
        this.fractionalLongitudinalPosition = fractionalLongitudinalPosition;
    }

    /**
     * @param link Link; the link
     * @param position Length; position
     * @param direction GTUDirectionality; the direction on the link, with or against the design line
     */
    public DirectedLinkPosition(final Link link, final Length position, final GTUDirectionality direction)
    {
        this(link, position.si / link.getLength().si, direction);
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
    public final Length getLongitudinalPosition()
    {
        return new Length(getLink().getLength().getSI() * getFractionalLongitudinalPosition(), LengthUnit.METER);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "DirectedLinkPosition [link=" + getLink() + ", direction=" + getDirection() + ", fractionalLongitudinalPosition="
                + this.fractionalLongitudinalPosition + "]";
    }

}
