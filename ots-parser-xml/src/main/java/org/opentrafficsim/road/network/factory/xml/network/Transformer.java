package org.opentrafficsim.road.network.factory.xml.network;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.SpeedUnits;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
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
     * @param positionType GTUPositionType; the JAXB position to parse
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
     * @param ocStr String; the overtaking conditions string.
     * @return the overtaking conditions.
     * @throws NetworkException in case of unknown overtaking conditions.
     */
    public static OvertakingConditions parseOvertakingConditions(String ocStr) throws NetworkException
    {
        if (ocStr == null || ocStr.equals("NONE"))
        {
            return new OvertakingConditions.None();
        }
        else if (ocStr.equals("LEFTONLY"))
        {
            return new OvertakingConditions.LeftOnly();
        }
        else if (ocStr.equals("RIGHTONLY"))
        {
            return new OvertakingConditions.RightOnly();
        }
        else if (ocStr.equals("LEFTANDRIGHT"))
        {
            return new OvertakingConditions.LeftAndRight();
        }
        else if (ocStr.equals("SAMELANERIGHT"))
        {
            return new OvertakingConditions.SameLaneRight();
        }
        else if (ocStr.equals("SAMELANELEFT"))
        {
            return new OvertakingConditions.SameLaneLeft();
        }
        else if (ocStr.equals("SAMELANEBOTH"))
        {
            return new OvertakingConditions.SameLaneBoth();
        }
        else if (ocStr.startsWith("LEFTALWAYS RIGHTSPEED"))
        {
            int lb = ocStr.indexOf('(');
            int rb = ocStr.indexOf(')');
            if (lb == -1 || rb == -1 || rb - lb < 3)
            {
                throw new NetworkException("Speed in overtaking conditions string: '" + ocStr + "' not coded right");
            }
            Speed speed = SpeedUnits.parseSpeed(ocStr.substring(lb + 1, rb));
            return new OvertakingConditions.LeftAlwaysRightSpeed(speed);
        }
        else if (ocStr.startsWith("RIGHTALWAYS LEFTSPEED"))
        {
            int lb = ocStr.indexOf('(');
            int rb = ocStr.indexOf(')');
            if (lb == -1 || rb == -1 || rb - lb < 3)
            {
                throw new NetworkException("Speed in overtaking conditions string: '" + ocStr + "' not coded right");
            }
            Speed speed = SpeedUnits.parseSpeed(ocStr.substring(lb + 1, rb));
            return new OvertakingConditions.RightAlwaysLeftSpeed(speed);
        }

        // TODO SETs and JAM
        /*-
        else if (ocStr.startsWith("LEFTSET"))
        {
            int lset1 = ocStr.indexOf('[') + 1;
            int rset1 = ocStr.indexOf(']', lset1);
            int lset2 = ocStr.indexOf('[', ocStr.indexOf("OVERTAKE")) + 1;
            int rset2 = ocStr.indexOf(']', lset2);
            if (lset1 == -1 || rset1 == -1 || rset1 - lset1 < 3 || lset2 == -1 || rset2 == -1 || rset2 - lset2 < 3)
            {
                throw new NetworkException("Sets in overtaking conditions string: '" + ocStr + "' not coded right");
            }
            Set<GTUType> overtakingGTUs = parseGTUTypeSet(ocStr.substring(lset1, rset1));
            Set<GTUType> overtakenGTUs = parseGTUTypeSet(ocStr.substring(lset2, rset2));
            if (ocStr.contains("RIGHTSPEED"))
            {
                int i = ocStr.indexOf("RIGHTSPEED");
                int lb = ocStr.indexOf('(', i);
                int rb = ocStr.indexOf(')', i);
                if (lb == -1 || rb == -1 || rb - lb < 3)
                {
                    throw new NetworkException("Speed in overtaking conditions string: '" + ocStr + "' not coded right");
                }
                Speed speed = SpeedUnits.parseSpeed(ocStr.substring(lb + 1, rb));
                return new OvertakingConditions.LeftSetRightSpeed(overtakingGTUs, overtakenGTUs, speed);
            }
            return new OvertakingConditions.LeftSet(overtakingGTUs, overtakenGTUs);
        }
        else if (ocStr.startsWith("RIGHTSET"))
        {
            int lset1 = ocStr.indexOf('[') + 1;
            int rset1 = ocStr.indexOf(']', lset1);
            int lset2 = ocStr.indexOf('[', ocStr.indexOf("OVERTAKE")) + 1;
            int rset2 = ocStr.indexOf(']', lset2);
            if (lset1 == -1 || rset1 == -1 || rset1 - lset1 < 3 || lset2 == -1 || rset2 == -1 || rset2 - lset2 < 3)
            {
                throw new NetworkException("Sets in overtaking conditions string: '" + ocStr + "' not coded right");
            }
            Set<GTUType> overtakingGTUs = parseGTUTypeSet(ocStr.substring(lset1, rset1));
            Set<GTUType> overtakenGTUs = parseGTUTypeSet(ocStr.substring(lset2, rset2));
            if (ocStr.contains("LEFTSPEED"))
            {
                int i = ocStr.indexOf("LEFTSPEED");
                int lb = ocStr.indexOf('(', i);
                int rb = ocStr.indexOf(')', i);
                if (lb == -1 || rb == -1 || rb - lb < 3)
                {
                    throw new NetworkException("Speed in overtaking conditions string: '" + ocStr + "' not coded right");
                }
                Speed speed = SpeedUnits.parseSpeed(ocStr.substring(lb + 1, rb));
                return new OvertakingConditions.RightSetLeftSpeed(overtakingGTUs, overtakenGTUs, speed);
            }
            return new OvertakingConditions.RightSet(overtakingGTUs, overtakenGTUs);
        }
        */
        throw new NetworkException("Unknown overtaking conditions string: " + ocStr);
    }

}
