package org.opentrafficsim.road.network.lane.conflict;

import java.rmi.RemoteException;
import java.util.UUID;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.animation.ConflictAnimation;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.AbstractLaneBasedObject;

import nl.tudelft.simulation.language.Throw;

/**
 * Conflicts deal with traffic on different links/roads that need to consider each other as their paths may be in conflict
 * spatially. A single {@code Conflict} represents the one-sided consideration of a conflicting situation. I.e., what is
 * considered <i>a single conflict in traffic theory, is represented by two {@code Conflict}s</i>, one on each of the
 * conflicting {@code Lane}s.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Conflict extends AbstractLaneBasedObject
{

    /** */
    private static final long serialVersionUID = 20160915L;

    /** Conflict type, i.e. crossing, merge or split. */
    private final ConflictType conflictType;

    /** Conflict rule, i.e. priority, give way, stop or all-stop. */
    private final ConflictRule conflictRule;

    /** Accompanying other conflict. */
    private Conflict otherConflict;

    /** The length of the conflict along the lane centerline. */
    private final Length length;

    /** GTU direction. */
    private final GTUDirectionality direction;

    /** Simulator for animation and timed events. */
    private final OTSDEVSSimulatorInterface simulator;

    /** GTU type. */
    private final GTUType gtuType;

    /** Whether the conflict is a permitted conflict in traffic light control. */
    private final boolean permitted;

    /** Lock object for cloning a pair of conflicts. */
    private final Object cloneLock;

    /**
     * @param lane lane where this conflict starts
     * @param longitudinalPosition position of start of conflict on lane
     * @param length length of the conflict along the lane centerline
     * @param direction GTU direction
     * @param geometry geometry of conflict
     * @param conflictRule conflict rule, i.e. determines priority, give way, stop or all-stop
     * @param conflictType conflict type, i.e. crossing, merge or split
     * @param simulator the simulator for animation and timed events
     * @param permitted whether the conflict is permitted in traffic light control
     * @param gtuType gtu type
     * @param cloneLock lock object for cloning a pair of conflicts
     * @throws NetworkException when the position on the lane is out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private Conflict(final Lane lane, final Length longitudinalPosition, final Length length, final GTUDirectionality direction,
            final OTSLine3D geometry, final ConflictType conflictType, final ConflictRule conflictRule,
            final OTSDEVSSimulatorInterface simulator, final GTUType gtuType, final boolean permitted, final Object cloneLock)
            throws NetworkException
    {
        super(UUID.randomUUID().toString(), lane, Throw.whenNull(direction, "Direction may not be null.").isPlus()
                ? LongitudinalDirectionality.DIR_PLUS : LongitudinalDirectionality.DIR_MINUS, longitudinalPosition, geometry);
        this.length = length;
        this.direction = direction;
        this.conflictType = conflictType;
        this.conflictRule = conflictRule;
        this.simulator = simulator;
        this.gtuType = gtuType;
        this.permitted = permitted;
        this.cloneLock = cloneLock;

        try
        {
            new ConflictAnimation(this, simulator);
        }
        catch (RemoteException | NamingException exception)
        {
            throw new NetworkException(exception);
        }

        // Create conflict end
        if (conflictType.equals(ConflictType.SPLIT) || conflictType.equals(ConflictType.MERGE))
        {
            Length position =
                    conflictType.equals(ConflictType.SPLIT) ? (direction.isPlus() ? length : lane.getLength().minus(length))
                            : (direction.isPlus() ? lane.getLength() : Length.ZERO);
            try
            {
                new ConflictEnd(this, lane,
                        direction.isPlus() ? LongitudinalDirectionality.DIR_PLUS : LongitudinalDirectionality.DIR_MINUS,
                        position);
            }
            catch (OTSGeometryException exception)
            {
                // does not happen
                throw new RuntimeException("Could not create dummy geometry for ConflictEnd.", exception);
            }
        }
    }

    /**
     * @return conflictType.
     */
    public ConflictType getConflictType()
    {
        return this.conflictType;
    }

    /**
     * @return conflictRule.
     */
    public ConflictRule getConflictRule()
    {
        return this.conflictRule;
    }

    /**
     * @return conflictPriority.
     */
    public ConflictPriority conflictPriority()
    {
        return this.conflictRule.determinePriority(this);
    }

    /**
     * @return length.
     */
    public Length getLength()
    {
        return this.length;
    }

    /**
     * @return otherConflict.
     */
    public Conflict getOtherConflict()
    {
        return this.otherConflict;
    }

    /**
     * @return gtuType.
     */
    public GTUType getGtuType()
    {
        return this.gtuType;
    }

    /**
     * If permitted, traffic upstream of traffic lights may not be ignored, as these can have green light.
     * @return permitted.
     */
    public boolean isPermitted()
    {
        return this.permitted;
    }

    /**
     * Creates a pair of conflicts.
     * @param conflictType conflict type, i.e. crossing, merge or split
     * @param conflictRule conflict rule
     * @param permitted whether the conflict is permitted in traffic light control
     * @param lane1 lane of conflict 1
     * @param longitudinalPosition1 longitudinal position of conflict 1
     * @param length1 {@code Length} of conflict 1
     * @param direction1 GTU direction of conflict 1
     * @param geometry1 geometry of conflict 1
     * @param gtuType1 gtu type of conflict 1
     * @param lane2 lane of conflict 2
     * @param longitudinalPosition2 longitudinal position of conflict 2
     * @param length2 {@code Length} of conflict 2
     * @param direction2 GTU direction of conflict 2
     * @param geometry2 geometry of conflict 2
     * @param gtuType2 gtu type of conflict 2
     * @param simulator the simulator for animation and timed events
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void generateConflictPair(final ConflictType conflictType, final ConflictRule conflictRule,
            final boolean permitted, final Lane lane1, final Length longitudinalPosition1, final Length length1,
            final GTUDirectionality direction1, final OTSLine3D geometry1, final GTUType gtuType1, final Lane lane2,
            final Length longitudinalPosition2, final Length length2, final GTUDirectionality direction2,
            final OTSLine3D geometry2, final GTUType gtuType2, final OTSDEVSSimulatorInterface simulator)
            throws NetworkException
    {
        // lane, longitudinalPosition, length and geometry are checked in AbstractLaneBasedObject
        Throw.whenNull(conflictType, "Conflict type may not be null.");

        Object cloneLock = new Object();
        Conflict conf1 = new Conflict(lane1, longitudinalPosition1, length1, direction1, geometry1, conflictType, conflictRule,
                simulator, gtuType1, permitted, cloneLock);
        Conflict conf2 = new Conflict(lane2, longitudinalPosition2, length2, direction2, geometry2, conflictType, conflictRule,
                simulator, gtuType2, permitted, cloneLock);
        conf1.otherConflict = conf2;
        conf2.otherConflict = conf1;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Conflict [conflictType=" + this.conflictType + ", conflictRule=" + this.conflictRule + "]";
    }

    /**
     * Clone of other conflict.
     */
    private Conflict otherClone;

    /** {@inheritDoc} */
    @Override
    public Conflict clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator, final boolean animation)
            throws NetworkException
    {
        Throw.when(!(newCSE instanceof Lane), NetworkException.class, "sensors can only be cloned for Lanes");
        Throw.when(!(newSimulator instanceof OTSDEVSSimulatorInterface), NetworkException.class,
                "simulator should be a DEVSSimulator");
        Conflict out = new Conflict((Lane) newCSE, getLongitudinalPosition(), this.length, this.direction, getGeometry(),
                this.conflictType, this.conflictRule, this.simulator, this.gtuType, this.permitted, this.cloneLock);
        synchronized (this.cloneLock)
        {
            // couple both clones
            if (this.otherClone == null || this.otherClone.simulator != newSimulator)
            {
                // other clone will do it
                this.otherConflict.otherClone = out;
            }
            else
            {
                out.otherConflict = this.otherClone;
                this.otherClone.otherConflict = out;
            }
            // reset successful clone of pair, or remove otherClone from other simulator (or was already null)
            this.otherClone = null;
        }
        return out;
    }

    /**
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 dec. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public class ConflictEnd extends AbstractLaneBasedObject
    {

        /** */
        private static final long serialVersionUID = 20161214L;

        /** Conflict. */
        private final Conflict conflict;

        /**
         * @param conflict conflict
         * @param lane lane
         * @param direction valid direction
         * @param longitudinalPosition position
         * @throws NetworkException on network exception
         * @throws OTSGeometryException does not happen
         */
        ConflictEnd(final Conflict conflict, final Lane lane, final LongitudinalDirectionality direction,
                final Length longitudinalPosition) throws NetworkException, OTSGeometryException
        {
            super(conflict.getId() + "End", lane, direction, longitudinalPosition,
                    new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(1, 0, 0)));
            this.conflict = conflict;
        }

        /**
         * @return conflict
         */
        public final Conflict getConflict()
        {
            return this.conflict;
        }

        /** {@inheritDoc} */
        @Override
        public final AbstractLaneBasedObject clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator,
                final boolean animation) throws NetworkException
        {
            // Constructor of Conflict creates these.
            return null;
        }

    }

}
