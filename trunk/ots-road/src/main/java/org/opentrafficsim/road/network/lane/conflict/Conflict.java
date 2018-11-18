package org.opentrafficsim.road.network.lane.conflict;

import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.AbstractPerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.AbstractPerceptionReiterable;
import org.opentrafficsim.road.gtu.lane.perception.DownstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.LaneDirectionRecord;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.UpstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUReal;
import org.opentrafficsim.road.network.animation.ConflictAnimation;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.object.AbstractLaneBasedObject;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * Conflicts deal with traffic on different links/roads that need to consider each other as their paths may be in conflict
 * spatially. A single {@code Conflict} represents the one-sided consideration of a conflicting situation. I.e., what is
 * considered <i>a single conflict in traffic theory, is represented by two {@code Conflict}s</i>, one on each of the
 * conflicting {@code Lane}s.<br>
 * <br>
 * This class provides easy access to upstream and downstream GTUs through {@code PerceptionIterable}s using methods
 * {@code getUpstreamGtus} and {@code getDownstreamGtus}. These methods are efficient in that they reuse underlying data
 * structures if the GTUs are requested at the same time by another GTU.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private final SimulatorInterface.TimeDoubleUnit simulator;

    /** GTU type. */
    private final GTUType gtuType;

    /** Whether the conflict is a permitted conflict in traffic light control. */
    private final boolean permitted;

    /** Lock object for cloning a pair of conflicts. */
    private final Object cloneLock;

    /////////////////////////////////////////////////////////////////
    // Properties regarding upstream and downstream GTUs provision //
    /////////////////////////////////////////////////////////////////

    /** Root for GTU search. */
    private final LaneDirectionRecord root;

    /** Position on the root. */
    private final Length rootPosition;

    /** Current upstream GTUs provider. */
    private AbstractPerceptionIterable<HeadwayGTU, LaneBasedGTU, Integer> upstreamGtus;

    /** Upstream GTUs update time. */
    private Time upstreamTime;

    /** Current downstream GTUs provider. */
    private AbstractPerceptionIterable<HeadwayGTU, LaneBasedGTU, Integer> downstreamGtus;

    /** Downstream GTUs update time. */
    private Time downstreamTime;

    /** Headway type for the provided GTUs. */
    private final HeadwayGtuType conflictGtuType = new ConflictGtuType();

    /** Distance within which upstreamGTUs are provided (is automatically enlarged). */
    private Length maxUpstreamVisibility = Length.ZERO;

    /** Distance within which downstreamGTUs are provided (is automatically enlarged). */
    private Length maxDownstreamVisibility = Length.ZERO;

    /////////////////////////////////////////////////////////////////

    /**
     * @param lane Lane; lane where this conflict starts
     * @param longitudinalPosition Length; position of start of conflict on lane
     * @param length Length; length of the conflict along the lane centerline
     * @param direction GTUDirectionality; GTU direction
     * @param geometry OTSLine3D; geometry of conflict
     * @param conflictRule ConflictRule; conflict rule, i.e. determines priority, give way, stop or all-stop
     * @param conflictType ConflictType; conflict type, i.e. crossing, merge or split
     * @param simulator SimulatorInterface.TimeDoubleUnit; the simulator for animation and timed events
     * @param permitted boolean; whether the conflict is permitted in traffic light control
     * @param gtuType GTUType; gtu type
     * @param cloneLock Object; lock object for cloning a pair of conflicts
     * @throws NetworkException when the position on the lane is out of bounds
     */
    @SuppressWarnings("checkstyle:parameternumber")
    private Conflict(final Lane lane, final Length longitudinalPosition, final Length length, final GTUDirectionality direction,
            final OTSLine3D geometry, final ConflictType conflictType, final ConflictRule conflictRule,
            final SimulatorInterface.TimeDoubleUnit simulator, final GTUType gtuType, final boolean permitted,
            final Object cloneLock) throws NetworkException
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

        // Lane record for GTU provision
        this.rootPosition = direction.isPlus() ? longitudinalPosition : lane.getLength().minus(longitudinalPosition);
        this.root = new LaneDirectionRecord(lane, direction, this.rootPosition.neg(), GTUType.VEHICLE);
    }

    /**
     * Make sure the conflict can provide the given upstream visibility.
     * @param visibility Length; visibility to guarantee
     */
    private void provideUpstreamVisibility(final Length visibility)
    {
        if (visibility.gt(this.maxUpstreamVisibility))
        {
            this.maxUpstreamVisibility = visibility;
            this.upstreamTime = null;
            this.downstreamTime = null;
        }
    }

    /**
     * Make sure the conflict can provide the given downstream visibility.
     * @param visibility Length; visibility to guarantee
     */
    private void provideDownstreamVisibility(final Length visibility)
    {
        if (visibility.gt(this.maxDownstreamVisibility))
        {
            this.maxDownstreamVisibility = visibility;
            this.upstreamTime = null;
            this.downstreamTime = null;
        }
    }

    /**
     * Provides the upstream GTUs.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param headwayGtuType HeadwayGtuType; headway GTU type to use
     * @param visibility Length; distance over which GTU's are provided
     * @return PerceptionIterable&lt;HeadwayGtU&gt;; iterable over the upstream GTUs
     */
    public PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getUpstreamGtus(final LaneBasedGTU perceivingGtu,
            final HeadwayGtuType headwayGtuType, final Length visibility)
    {
        provideUpstreamVisibility(visibility);
        Time time = this.getLane().getParentLink().getSimulator().getSimulatorTime();
        if (this.upstreamTime == null || !time.eq(this.upstreamTime))
        {
            // setup a base iterable to provide the GTUs
            this.upstreamGtus =
                    new UpstreamNeighborsIterable(perceivingGtu, this.root, this.rootPosition, this.maxUpstreamVisibility,
                            RelativePosition.REFERENCE_POSITION, this.conflictGtuType, RelativeLane.CURRENT);
            this.upstreamTime = time;
        }
        // return iterable that uses the base iterable
        return new ConflictGtuIterable(perceivingGtu, headwayGtuType, visibility, false, this.upstreamGtus);
    }

    /**
     * Provides the downstream GTUs.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param headwayGtuType HeadwayGtuType; headway GTU type to use
     * @param visibility Length; distance over which GTU's are provided
     * @return PerceptionIterable&lt;HeadwayGtU&gt;; iterable over the downstream GTUs
     */
    public PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getDownstreamGtus(final LaneBasedGTU perceivingGtu,
            final HeadwayGtuType headwayGtuType, final Length visibility)
    {
        provideDownstreamVisibility(visibility);
        Time time = this.getLane().getParentLink().getSimulator().getSimulatorTime();
        if (this.downstreamTime == null || !time.eq(this.downstreamTime))
        {
            // setup a base iterable to provide the GTUs
            boolean ignoreIfUpstream = false;
            this.downstreamGtus = new DownstreamNeighborsIterable(perceivingGtu, this.root, this.rootPosition,
                    this.maxDownstreamVisibility, RelativePosition.REFERENCE_POSITION, this.conflictGtuType, null,
                    RelativeLane.CURRENT, ignoreIfUpstream);
            this.downstreamTime = time;
        }
        // return iterable that uses the base iterable
        return new ConflictGtuIterable(perceivingGtu, new OverlapHeadway(headwayGtuType), visibility, true,
                this.downstreamGtus);
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
     * @param conflictType ConflictType; conflict type, i.e. crossing, merge or split
     * @param conflictRule ConflictRule; conflict rule
     * @param permitted boolean; whether the conflict is permitted in traffic light control
     * @param lane1 Lane; lane of conflict 1
     * @param longitudinalPosition1 Length; longitudinal position of conflict 1
     * @param length1 Length; {@code Length} of conflict 1
     * @param direction1 GTUDirectionality; GTU direction of conflict 1
     * @param geometry1 OTSLine3D; geometry of conflict 1
     * @param gtuType1 GTUType; gtu type of conflict 1
     * @param lane2 Lane; lane of conflict 2
     * @param longitudinalPosition2 Length; longitudinal position of conflict 2
     * @param length2 Length; {@code Length} of conflict 2
     * @param direction2 GTUDirectionality; GTU direction of conflict 2
     * @param geometry2 OTSLine3D; geometry of conflict 2
     * @param gtuType2 GTUType; gtu type of conflict 2
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; the simulator for animation and timed events
     * @throws NetworkException if the combination of conflict type and both conflict rules is not correct
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public static void generateConflictPair(final ConflictType conflictType, final ConflictRule conflictRule,
            final boolean permitted, final Lane lane1, final Length longitudinalPosition1, final Length length1,
            final GTUDirectionality direction1, final OTSLine3D geometry1, final GTUType gtuType1, final Lane lane2,
            final Length longitudinalPosition2, final Length length2, final GTUDirectionality direction2,
            final OTSLine3D geometry2, final GTUType gtuType2, final DEVSSimulatorInterface.TimeDoubleUnit simulator)
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
    public Conflict clone(final CrossSectionElement newCSE, final SimulatorInterface.TimeDoubleUnit newSimulator,
            final boolean animation) throws NetworkException
    {
        Throw.when(!(newCSE instanceof Lane), NetworkException.class, "sensors can only be cloned for Lanes");
        Throw.when(!(newSimulator instanceof DEVSSimulatorInterface.TimeDoubleUnit), NetworkException.class,
                "simulator should be a DEVSSimulator");
        Conflict out = new Conflict((Lane) newCSE, getLongitudinalPosition(), this.length, this.direction, getGeometry(),
                this.conflictType, this.conflictRule.clone(newSimulator), newSimulator, this.gtuType, this.permitted,
                this.cloneLock);
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
     * Light-weight lane based object to indicate the end of a conflict. It is used to perceive conflicts when a GTU is on the
     * conflict area, and hence the conflict lane based object is usptream.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
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
         * @param conflict Conflict; conflict
         * @param lane Lane; lane
         * @param direction LongitudinalDirectionality; valid direction
         * @param longitudinalPosition Length; position
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
        public final AbstractLaneBasedObject clone(final CrossSectionElement newCSE,
                final SimulatorInterface.TimeDoubleUnit newSimulator, final boolean animation) throws NetworkException
        {
            // Constructor of Conflict creates these.
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "ConflictEnd [conflict=" + this.conflict + "]";
        }
    }

    /**
     * HeadwayGTU that is returned by base iterators for upstream and downstream GTUs. This class is used with both
     * {@code UpstreamNeighborsIterable} and {@code DownstreamNeighborsIterable} which work with HeadwayGTU. The role of this
     * class is however to simply provide the GTU itself such that other specific HeadwayGTU types can be created with it.
     * Therefore, it extends HeadwayGTUReal which simply wraps the GTU. As the HeadwayGTUReal class has the actual GTU hidden,
     * this class can provide it.
     * <p>
     * FIXME: why not create a getter for the gtu in the super class?
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class ConflictGtu extends HeadwayGTUReal
    {
        /** */
        private static final long serialVersionUID = 20180221L;

        /** Visible pointer to the GTU (which HeadwayGTUReal has not). */
        private final LaneBasedGTU gtu;

        /**
         * Constructor.
         * @param gtu LaneBasedGTU; gtu
         * @param overlapFront Length; front overlap
         * @param overlap Length; overlap
         * @param overlapRear Length; rear overlap
         * @throws GTUException on exception
         */
        ConflictGtu(final LaneBasedGTU gtu, final Length overlapFront, final Length overlap, final Length overlapRear)
                throws GTUException
        {
            super(gtu, overlapFront, overlap, overlapRear, true);
            this.gtu = gtu;
        }

        /**
         * Constructor.
         * @param gtu LaneBasedGTU; gtu
         * @param distance Length; distance
         * @throws GTUException on exception
         */
        ConflictGtu(final LaneBasedGTU gtu, final Length distance) throws GTUException
        {
            super(gtu, distance, true);
            this.gtu = gtu;
        }
    }

    /**
     * HeadwayGtuType that generates ConflictGtu's, for use within the base iterators for upstream and downstream neighbors.
     * This result is used by secondary iterators (ConflictGtuIterable) to provide the requested specific HeadwatGtuType.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class ConflictGtuType implements HeadwayGtuType
    {
        /** Constructor. */
        ConflictGtuType()
        {
            //
        }

        /** {@inheritDoc} */
        @Override
        public ConflictGtu createHeadwayGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance, final boolean downstream) throws GTUException
        {
            return new ConflictGtu(perceivedGtu, distance);
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGTU createDownstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance) throws GTUException, ParameterException
        {
            throw new UnsupportedOperationException("ConflictGtuType is a pass-through type, no actual perception is allowed.");
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGTU createUpstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance) throws GTUException, ParameterException
        {
            throw new UnsupportedOperationException("ConflictGtuType is a pass-through type, no actual perception is allowed.");
        }

        /** {@inheritDoc} */
        @Override
        public ConflictGtu createParallelGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GTUException
        {
            throw new UnsupportedOperationException("ConflictGtuType is a pass-through type, no actual perception is allowed.");
        }
    }

    /**
     * HeadwayGtuType that changes a negative headway in to an overlapping headway, by forwarding the request to a wrapped
     * HeadwayGtuType. This is used for downstream GTUs of the conflict, accounting also for the length of the conflict. Hence,
     * overlap information concerns the conflict and a downstream GTU (downstream of the start of the conflict).
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class OverlapHeadway implements HeadwayGtuType
    {
        /** Wrapped headway type. */
        private HeadwayGtuType wrappedType;

        /**
         * Constructor.
         * @param wrappedType HeadwayGtuType; wrapped headway type
         */
        OverlapHeadway(final HeadwayGtuType wrappedType)
        {
            this.wrappedType = wrappedType;
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGTU createHeadwayGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu, final Length dist,
                final boolean downstream) throws GTUException, ParameterException
        {
            if (dist.ge(getLength()))
            {
                // GTU fully downstream of the conflict
                return this.wrappedType.createHeadwayGtu(perceivingGtu, perceivedGtu, dist.minus(getLength()), downstream);
            }
            else
            {
                Length overlapRear = dist;
                Length overlap = getLength(); // start with conflict length
                Length overlapFront = dist.plus(perceivedGtu.getLength()).minus(getLength());
                if (overlapFront.lt0())
                {
                    overlap = overlap.plus(overlapFront); // subtract front being before the conflict end
                }
                if (overlapRear.gt0())
                {
                    overlap = overlap.minus(overlapRear); // subtract rear being past the conflict start
                }
                return createParallelGtu(perceivingGtu, perceivedGtu, overlapFront, overlap, overlapRear);
            }
        }
        
        /** {@inheritDoc} */
        @Override
        public HeadwayGTU createDownstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance) throws GTUException, ParameterException
        {
            throw new UnsupportedOperationException("OverlapHeadway is a pass-through type, no actual perception is allowed.");
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGTU createUpstreamGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length distance) throws GTUException, ParameterException
        {
            throw new UnsupportedOperationException("OverlapHeadway is a pass-through type, no actual perception is allowed.");
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGTU createParallelGtu(final LaneBasedGTU perceivingGtu, final LaneBasedGTU perceivedGtu,
                final Length overlapFront, final Length overlap, final Length overlapRear) throws GTUException
        {
            return this.wrappedType.createParallelGtu(perceivingGtu, perceivedGtu, overlapFront, overlap, overlapRear);
        }
    }

    /**
     * Iterable for upstream and downstream GTUs of a conflict, which uses a base iterable.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 21 feb. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class ConflictGtuIterable extends AbstractPerceptionReiterable<HeadwayGTU, LaneBasedGTU>
    {
        /** HeadwayGTU type. */
        private final HeadwayGtuType headwayGtuType;

        /** Guaranteed visibility. */
        private final Length visibility;

        /** Downstream (or upstream) neighbors. */
        private final boolean downstream;

        /** Base iterator of the base iterable. */
        private final Iterator<HeadwayGTU> baseIterator;

        /**
         * @param perceivingGtu LaneBasedGTU; perceiving GTU
         * @param headwayGtuType HeadwayGtuType; HeadwayGTU type
         * @param visibility Length; guaranteed visibility
         * @param downstream boolean; downstream (or upstream) neighbors
         * @param base AbstractPerceptionIterable&lt;HeadwayGTU, LaneBasedGTU, Integer&gt;; base iterable from the conflict
         */
        ConflictGtuIterable(final LaneBasedGTU perceivingGtu, final HeadwayGtuType headwayGtuType, final Length visibility,
                final boolean downstream, final AbstractPerceptionIterable<HeadwayGTU, LaneBasedGTU, Integer> base)
        {
            super(perceivingGtu);
            this.headwayGtuType = headwayGtuType;
            this.visibility = visibility;
            this.downstream = downstream;
            this.baseIterator = base.iterator();
        }

        /** {@inheritDoc} */
        @Override
        protected Iterator<PrimaryIteratorEntry> primaryIterator()
        {
            /**
             * Iterator that iterates over PrimaryIteratorEntry objects.
             */
            class ConflictGtuIterator implements Iterator<PrimaryIteratorEntry>
            {
                /** Next entry. */
                private PrimaryIteratorEntry next;

                /** {@inheritDoc} */
                @SuppressWarnings("synthetic-access")
                @Override
                public boolean hasNext()
                {
                    if (this.next == null)
                    {
                        if (ConflictGtuIterable.this.baseIterator.hasNext())
                        {
                            // ConflictGtuIterable is a private class, only used with ConflictGtuType
                            ConflictGtu gtu = (ConflictGtu) ConflictGtuIterable.this.baseIterator.next();
                            if (gtu.getDistance() == null || gtu.getDistance().le(ConflictGtuIterable.this.visibility))
                            {
                                this.next = new PrimaryIteratorEntry(gtu.gtu, gtu.getDistance());
                            }
                        }
                    }
                    return this.next != null;
                }

                /** {@inheritDoc} */
                @Override
                public PrimaryIteratorEntry next()
                {
                    if (hasNext())
                    {
                        PrimaryIteratorEntry out = this.next;
                        this.next = null;
                        return out;
                    }
                    throw new NoSuchElementException();
                }
            }
            return new ConflictGtuIterator();
        }

        /** {@inheritDoc} */
        @Override
        protected HeadwayGTU perceive(final LaneBasedGTU perceivingGtu, final LaneBasedGTU object, final Length distance)
                throws GTUException, ParameterException
        {
            Length dist = this.downstream ? distance : distance.neg();
            return this.headwayGtuType.createHeadwayGtu(perceivingGtu, object, dist, this.downstream);
        }
    }

}
