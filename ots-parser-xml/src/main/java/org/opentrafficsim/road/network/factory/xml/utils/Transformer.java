package org.opentrafficsim.road.network.factory.xml.utils;

import java.lang.reflect.Field;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.generator.CfBaRoomChecker;
import org.opentrafficsim.road.gtu.generator.CfRoomChecker;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.TtcRoomChecker;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayDistribution;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.xml.bindings.types.GtuPositionType;
import org.opentrafficsim.xml.bindings.types.LengthBeginEnd;

/**
 * Transformer contains common transformations between intermediate classes created by the JAXB Adapters and OTS objects.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
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
            return laneLength.times(lbe.getFraction());
        }
    }

    /**
     * @param positionType GtuPositionType; the JAXB position to parse
     * @return the corresponding OTS RelativePosition
     */
    public static RelativePosition.TYPE parseTriggerPosition(final GtuPositionType positionType)
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

    /**
     * @param lkpStr String; the lane keeping policy string.
     * @return the lane keeping policy.
     * @throws NetworkException in case of unknown policy.
     */
    public static LaneKeepingPolicy parseLaneKeepingPolicy(final String lkpStr) throws NetworkException
    {
        if (lkpStr.equals("KEEPRIGHT"))
        {
            return LaneKeepingPolicy.KEEPRIGHT;
        }
        else if (lkpStr.equals("KEEPLEFT"))
        {
            return LaneKeepingPolicy.KEEPLEFT;
        }
        else if (lkpStr.equals("KEEPLANE"))
        {
            return LaneKeepingPolicy.KEEPLANE;
        }
        throw new NetworkException("Unknown lane keeping policy string: " + lkpStr);
    }

    /**
     * @param v String; XML string value
     * @return RoomChecker; parsed room checker
     */
    public static RoomChecker parseRoomChecker(final String v)
    {
        if (v == null)
        {
            return null;
        }
        if (v.equals("CF"))
        {
            return new CfRoomChecker();
        }
        else if (v.equals("CFBA"))
        {
            return new CfBaRoomChecker();
        }
        else if (v.equals("TTC"))
        {
            return new TtcRoomChecker(Duration.instantiateSI(10));
        }
        return new TtcRoomChecker(Duration.valueOf(v.substring(4, v.length() - 1)));
    }

    /**
     * @param v String; XML string value
     * @return RoomChecker; parsed room checker
     * @throws NoSuchFieldException if {@code HeadwayDistribution} does not have specified field
     * @throws IllegalAccessException if the field is not accessible
     */
    public static HeadwayDistribution parseHeadwayDistribution(final String v)
            throws NoSuchFieldException, IllegalAccessException
    {
        if (v == null)
        {
            return null;
        }
        Field field = ClassUtil.resolveField(HeadwayDistribution.class, v);
        return (HeadwayDistribution) field.get(null);
    }
}
