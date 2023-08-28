package org.opentrafficsim.road.network.factory.xml.utils;

import java.lang.reflect.Field;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.road.gtu.generator.CfBaRoomChecker;
import org.opentrafficsim.road.gtu.generator.CfRoomChecker;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator.RoomChecker;
import org.opentrafficsim.road.gtu.generator.TtcRoomChecker;
import org.opentrafficsim.road.gtu.generator.headway.ArrivalsHeadwayGenerator.HeadwayDistribution;
import org.opentrafficsim.xml.bindings.types.LengthBeginEnd;
import org.opentrafficsim.xml.generated.RoomCheckerType;

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
     * Parse room checker.
     * @param roomChecker RoomCheckerType; room checker type
     * @return RoomChecker; parsed room checker
     */
    public static RoomChecker parseRoomChecker(final RoomCheckerType roomChecker)
    {
        if (roomChecker == null || roomChecker.getCf() != null)
        {
            return new CfRoomChecker();
        }
        else if (roomChecker.getCfBa() != null)
        {
            return new CfBaRoomChecker();
        }
        return new TtcRoomChecker(roomChecker.getTtc());
    }

    /**
     * Parse headway distribution.
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
