package org.opentrafficsim.road.network.lane.conflict;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Conflict
{

    /** Conflict type, i.e. crossing, merge or split. */
    private final ConflictType conflictType;

    /** The lane of this conflict. */
    private final Lane lane;

    /** The position (between 0.0 and the length of the Lane) of the sensor on the design line of the lane in SI units. */
    private final double longitudinalPositionSI;

    /** Length of the conflict along the lane centerline. */
    private final Length length;

    /** Conflict rule, i.e. priority, give way, stop or all-stop. */
    private final ConflictRule conflictRule;

    /** The cached location for animation. */
    private DirectedPoint location = null;

    /** Accompanying other conflict. */
    private Conflict otherConflict;

    /**
     * Constructor setting the lane and position.
     * @param conflictType conflict type, i.e. crossing, merge or split
     * @param lane lane where this conflict starts
     * @param longitudinalPositionSI position of start of conflict on lane
     * @param length length of the conflict along the lane centerline
     * @param conflictRule conflict rule, i.e. priority, give way, stop or all-stop
     */
    private Conflict(final ConflictType conflictType, final Lane lane, final double longitudinalPositionSI,
        final Length length, final ConflictRule conflictRule)
    {
        this.conflictType = conflictType;
        this.lane = lane;
        this.longitudinalPositionSI = longitudinalPositionSI;
        this.length = length;
        this.conflictRule = conflictRule;
    }

    /**
     * @return conflictType.
     */
    public ConflictType getConflictType()
    {
        return this.conflictType;
    }

    /**
     * @return lane.
     */
    public Lane getLane()
    {
        return this.lane;
    }

    /**
     * @return longitudinalPositionSI.
     */
    public double getLongitudinalPositionSI()
    {
        return this.longitudinalPositionSI;
    }

    /**
     * @return length.
     */
    public Length getLength()
    {
        return this.length;
    }

    /**
     * @return conflictRule.
     */
    public ConflictRule getConflictRule()
    {
        return this.conflictRule;
    }

    /**
     * @return other conflict.
     */
    public Conflict getOtherConflict()
    {
        return this.otherConflict;
    }

    /**
     * Returns the location.
     * @return location
     */
    public DirectedPoint getLocation()
    {
        if (this.location == null)
        {
            try
            {
                this.location = this.lane.getCenterLine().getLocationSI(this.longitudinalPositionSI);
                this.location.z = this.lane.getLocation().z + 0.01;
            }
            catch (OTSGeometryException exception)
            {
                exception.printStackTrace();
                return null;
            }
        }
        return this.location;
    }

    /**
     * Creates a pair of conflicts.
     * @param conflictType conflict type, i.e. crossing, merge or split
     * @param lane1 lane of conflict 1
     * @param longitudinalPositionSI1 longitudinal position of conflict 1
     * @param length1 {@code Length} of conflict 1
     * @param conflictRule1 conflict rule of conflict 1
     * @param lane2 lane of conflict 2
     * @param longitudinalPositionSI2 longitudinal position of conflict 2
     * @param length2 {@code Length} of conflict 2
     * @param conflictRule2 conflict rule of conflict 2
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void generateConflictPair(final ConflictType conflictType, final Lane lane1,
        final double longitudinalPositionSI1, final Length length1, final ConflictRule conflictRule1, final Lane lane2,
        final double longitudinalPositionSI2, final Length length2, final ConflictRule conflictRule2)
        throws NetworkException
    {
        Throw.whenNull(conflictType, "Conflict type may not be null.");
        Throw.whenNull(lane1, "Lane may not be null.");
        Throw.whenNull(length1, "Length may not be null.");
        Throw.whenNull(conflictRule1, "Conflict rule may not be null.");
        Throw.whenNull(lane2, "Lane may not be null.");
        Throw.whenNull(length2, "Length may not be null.");
        Throw.whenNull(conflictRule2, "Conflict rule may not be null.");
        if (conflictType.equals(ConflictType.SPLIT))
        {
            // Split with split (on split)
            Throw.when(!conflictRule1.equals(ConflictRule.SPLIT) || !conflictRule2.equals(ConflictRule.SPLIT),
                NetworkException.class, "Both conflict rules should be split for conflict type split.");
        }
        else
        {
            // Priority with give-way/stop
            Throw.when(conflictRule1.equals(ConflictRule.PRIORITY) && !conflictRule2.equals(ConflictRule.GIVE_WAY)
                && !conflictRule2.equals(ConflictRule.STOP), NetworkException.class,
                "Conflict rule 'PRIORITY' can only be combined with a conflict rule 'GIVE_WAY' or 'STOP'.");
            Throw.when(conflictRule2.equals(ConflictRule.PRIORITY) && !conflictRule1.equals(ConflictRule.GIVE_WAY)
                && !conflictRule1.equals(ConflictRule.STOP), NetworkException.class,
                "Conflict rule 'PRIORITY' can only be combined with a conflict rule 'GIVE_WAY' or 'STOP'.");
            // All-stop with all-stop
            Throw.when(conflictRule1.equals(ConflictRule.ALL_STOP) && !conflictRule2.equals(ConflictRule.ALL_STOP),
                NetworkException.class, "Conflict rule 'PRIORITY' can only be combined with a conflict rule 'PRIORITY'.");
            Throw.when(conflictRule2.equals(ConflictRule.ALL_STOP) && !conflictRule1.equals(ConflictRule.ALL_STOP),
                NetworkException.class, "Conflict rule 'PRIORITY' can only be combined with a conflict rule 'PRIORITY'.");
            Conflict conf1 = new Conflict(conflictType, lane1, longitudinalPositionSI1, length1, conflictRule1);
            Conflict conf2 = new Conflict(conflictType, lane2, longitudinalPositionSI2, length2, conflictRule2);
            conf1.otherConflict = conf2;
            conf2.otherConflict = conf1;
        }
    }

}
