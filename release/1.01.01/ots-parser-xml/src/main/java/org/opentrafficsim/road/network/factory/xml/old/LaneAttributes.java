package org.opentrafficsim.road.network.factory.xml.old;

import static org.opentrafficsim.core.gtu.GTUType.VEHICLE;

import java.util.HashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.xml.units.SpeedUnits;
import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Jul 23, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class LaneAttributes
{
    /** Utility class. */
    private LaneAttributes()
    {
        // do not instantiate
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
     * @param parser XmlNetworkLaneParserOld; the parser to get access to the defined GTUTypes.
     * @throws NetworkException in case of unknown overtaking conditions.
     */
    public static OvertakingConditions parseOvertakingConditions(final String ocStr, final XmlNetworkLaneParserOld parser)
            throws NetworkException
    {
        if (ocStr.equals("LEFTONLY"))
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
        else if (ocStr.equals("NONE"))
        {
            return new OvertakingConditions.None();
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
            Set<GTUType> overtakingGTUs = parseGTUTypeSet(ocStr.substring(lset1, rset1), parser);
            Set<GTUType> overtakenGTUs = parseGTUTypeSet(ocStr.substring(lset2, rset2), parser);
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
            Set<GTUType> overtakingGTUs = parseGTUTypeSet(ocStr.substring(lset1, rset1), parser);
            Set<GTUType> overtakenGTUs = parseGTUTypeSet(ocStr.substring(lset2, rset2), parser);
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
        throw new NetworkException("Unknown overtaking conditions string: " + ocStr);
    }

    /**
     * @param set String; the string with the GTUTypes ike "CAR, TRUCK" or "ALL"
     * @param parser XmlNetworkLaneParserOld; the parser to get access to the defined GTUTypes.
     * @return a parsed set of GTUTypes
     */
    private static Set<GTUType> parseGTUTypeSet(final String set, final XmlNetworkLaneParserOld parser)
    {
        Set<GTUType> gtuTypeSet = new HashSet<GTUType>();
        String[] types = set.trim().split(",");
        for (String type : types)
        {
            GTUType gtuType = parseGTUType(type.trim(), parser);
            gtuTypeSet.add(gtuType);
        }
        return gtuTypeSet;
    }

    /**
     * @param typeName String; the name of the GTU type.
     * @param parser XmlNetworkLaneParserOld; the parser with the lists of information
     * @return the GTUType that was retrieved or created.
     */
    private static GTUType parseGTUType(final String typeName, final XmlNetworkLaneParserOld parser)
    {
        if (!parser.gtuTypes.containsKey(typeName))
        {
            GTUType gtuType = new GTUType(typeName, VEHICLE);
            parser.gtuTypes.put(typeName, gtuType);
        }
        return parser.gtuTypes.get(typeName);
    }

    /**
     * @param priority String; the priority string
     * @return the priority
     * @throws NetworkException in case of unknown priority
     */
    public static Priority parsePriority(final String priority) throws NetworkException
    {
        try
        {
            return Priority.valueOf(priority);
        }
        catch (IllegalArgumentException exception)
        {
            throw new NetworkException("Unknown priority string: " + priority, exception);
        }
    }

}
