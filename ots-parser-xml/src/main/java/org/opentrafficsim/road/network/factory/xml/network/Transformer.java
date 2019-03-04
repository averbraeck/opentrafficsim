package org.opentrafficsim.road.network.factory.xml.network;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.xml.bindings.types.GTUPositionType;
import org.opentrafficsim.xml.bindings.types.LengthBeginEnd;

/**
 * Transformer contains common transformations between intermediate classes created by the JAXB Adapters and OTS objects. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public final class Transformer
{
    /** */
    private Transformer()
    {
        // utility class
    }

    /**
     * Parse LengthBeginEnd for a Lane.
     * @param lbe LengthBeginEnd; the begin, end, fraction, or offset from begin or end on the lane
     * @param laneLength Length; the length of the lane
     * @return the offset on the lane
     */
    public static Length parseLengthBeginEnd(final LengthBeginEnd lbe, final Length laneLength)
    {
        if (lbe.isAbsolute())
        {
            if (lbe.isBegin())
                return lbe.getOffset();
            else
                return laneLength.minus(lbe.getOffset());
        }
        else
        {
            return laneLength.multiplyBy(lbe.getFraction());
        }
    }

    /**
     * @param positionType the JAXB position to parse
     * @return the corresponding OTS RelativePosition
     */
    public static RelativePosition.TYPE parseTriggerPosition(final GTUPositionType positionType)
    {
        switch (positionType)
        {
            case FRONT:
                return RelativePosition.FRONT;

            case REAR:
                return RelativePosition.REAR;

            case REFERENCE:
                return RelativePosition.REFERENCE;

            default:
                return RelativePosition.REFERENCE;
        }
    }
    

}

