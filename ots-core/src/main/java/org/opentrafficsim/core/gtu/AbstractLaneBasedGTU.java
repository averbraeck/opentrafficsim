package org.opentrafficsim.core.gtu;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.linearref.LengthIndexedLine;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Oct 22, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> The type of ID, e.g., String or Integer
 */
public abstract class AbstractLaneBasedGTU<ID> extends AbstractGTU<ID> implements LaneBasedGTU<ID>
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** Time of last evaluation. */
    private DoubleScalar.Abs<TimeUnit> lastEvaluationTime;

    /**
     * Longitudinal positions of the reference point of the GTU (currently the front) on one or more lanes at the
     * lastEvaluationTime. Because the front of the GTU is not on all the lanes the GTU is registered on, the longitudinal
     * positions can be more than the length of the lane, or less than zero.
     */
    private final Map<Lane, DoubleScalar.Rel<LengthUnit>> longitudinalPositions;

    /** Speed at lastEvaluationTime. */
    private DoubleScalar.Abs<SpeedUnit> speed;

    /** lateral velocity at lastEvaluationTime. */
    private DoubleScalar.Abs<SpeedUnit> lateralVelocity;

    /** acceleration (negative values indicate deceleration) at the lastEvaluationTime. */
    private DoubleScalar.Abs<AccelerationUnit> acceleration = new DoubleScalar.Abs<AccelerationUnit>(0,
        AccelerationUnit.METER_PER_SECOND_2);

    /** CarFollowingModel used by this Car. */
    private final GTUFollowingModel gtuFollowingModel;

    /**
     * @param id the id of the GTU, could be String or Integer.
     * @param gtuType the type of GTU, e.g. TruckType, CarType, BusType.
     * @param gtuFollowingModel the following model, including a reference to the simulator.
     * @param initialLongitudinalPositions the initial positions of the car on one or more lanes.
     * @param initialSpeed the initial speed of the car on the lane.
     * @param currentTime to initialize the evaluation. getSimulator() does not work yet, because the super constructors have
     *            not finished yet.
     */
    public AbstractLaneBasedGTU(final ID id, final GTUType<?> gtuType, final GTUFollowingModel gtuFollowingModel,
        final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
        final DoubleScalar.Abs<SpeedUnit> initialSpeed, final DoubleScalar.Abs<TimeUnit> currentTime)
    {
        super(id, gtuType);
        this.gtuFollowingModel = gtuFollowingModel;
        this.lastEvaluationTime = new DoubleScalar.Abs<TimeUnit>(currentTime);
        this.longitudinalPositions = new HashMap<>(initialLongitudinalPositions);

        // register the GTUs on the lane
        for (Lane lane : initialLongitudinalPositions.keySet())
        {
            lane.addGTU(this);
        }

        // Duplicate the other arguments as these are modified in this class and may be re-used by the caller
        this.speed = new DoubleScalar.Abs<SpeedUnit>(initialSpeed);
        this.lateralVelocity = new DoubleScalar.Abs<SpeedUnit>(0.0, SpeedUnit.METER_PER_SECOND);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getLongitudinalVelocity(final DoubleScalar.Abs<TimeUnit> when)
    {
        DoubleScalar.Rel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        return DoubleScalar.plus(this.speed, Calc.accelerationTimesTime(this.getAcceleration(when), dT)).immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getLongitudinalVelocity() throws RemoteException
    {
        return getLongitudinalVelocity(getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<TimeUnit> getLastEvaluationTime()
    {
        return new DoubleScalar.Abs<TimeUnit>(this.lastEvaluationTime);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<TimeUnit> getNextEvaluationTime()
    {
        return DoubleScalar.plus(this.lastEvaluationTime, this.gtuFollowingModel.getStepSize()).immutable();
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<AccelerationUnit> getAcceleration(final DoubleScalar.Abs<TimeUnit> when)
    {
        // Currently the acceleration is independent of when; it is constant during the evaluation interval
        return new DoubleScalar.Abs<AccelerationUnit>(this.acceleration);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<AccelerationUnit> getAcceleration() throws RemoteException
    {
        return getAcceleration(getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<SpeedUnit> getLateralVelocity()
    {
        return new DoubleScalar.Abs<SpeedUnit>(this.lateralVelocity);
    }

    /** {@inheritDoc} */
    @Override
    public final void addLane(final Lane lane)
    {
        addLane(lane, new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER));
    }

    /** {@inheritDoc} */
    @Override
    public final void addLane(final Lane lane, final DoubleScalar.Rel<LengthUnit> position)
    {
        this.longitudinalPositions.put(lane, position);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeLane(final Lane lane)
    {
        this.longitudinalPositions.remove(lane);
    }

    /**
     * Set the new state.
     * @param cfmr GTUFollowingModelResult; the new state of this GTU
     * @throws RemoteException when simulator time could not be retrieved or sensor trigger scheduling fails.
     * @throws NetworkException when the vehicle is not on the given lane.
     * @throws SimRuntimeException when sensor trigger(s) cannot be scheduled on the simulator.
     */
    public final void setState(final GTUFollowingModelResult cfmr) throws RemoteException, NetworkException,
        SimRuntimeException
    {
        DoubleScalar.Abs<TimeUnit> nextEvaluationTime = getNextEvaluationTime();
        for (Lane lane : this.longitudinalPositions.keySet())
        {
            this.longitudinalPositions.put(lane, position(lane, getFront(), nextEvaluationTime));
        }

        this.speed = getLongitudinalVelocity(nextEvaluationTime);

        this.lastEvaluationTime = nextEvaluationTime;
        this.acceleration = cfmr.getAcceleration();

        // for now: schedule all sensor triggers that are going to happen in the next timestep.
        for (Lane lane : this.longitudinalPositions.keySet())
        {
            lane.scheduleTriggers(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, DoubleScalar.Rel<LengthUnit>> positions(final RelativePosition relativePosition)
        throws NetworkException, RemoteException
    {
        return positions(relativePosition, getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, DoubleScalar.Rel<LengthUnit>> positions(final RelativePosition relativePosition,
        final DoubleScalar.Abs<TimeUnit> when) throws NetworkException
    {
        Map<Lane, DoubleScalar.Rel<LengthUnit>> positions = new HashMap<>();
        for (Lane lane : this.longitudinalPositions.keySet())
        {
            positions.put(lane, position(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> position(final Lane lane, final RelativePosition relativePosition)
        throws NetworkException, RemoteException
    {
        return position(lane, relativePosition, getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> position(final Lane lane, final RelativePosition relativePosition,
        final DoubleScalar.Abs<TimeUnit> when) throws NetworkException
    {
        DoubleScalar.Rel<LengthUnit> longitudinalPosition = this.longitudinalPositions.get(lane);
        if (longitudinalPosition == null)
        {
            throw new NetworkException("GetPosition: GTU " + toString() + " not in lane " + lane);
        }
        DoubleScalar.Rel<TimeUnit> dT = DoubleScalar.minus(when, this.lastEvaluationTime).immutable();
        DoubleScalar.Rel<LengthUnit> loc =
            DoubleScalar.plus(
                DoubleScalar.plus(DoubleScalar.plus(longitudinalPosition, Calc.speedTimesTime(this.speed, dT)).immutable(),
                    Calc.accelerationTimesTimeSquaredDiv2(this.getAcceleration(when), dT)).immutable(),
                relativePosition.getDx()).immutable();
        return loc;
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition) throws NetworkException,
        RemoteException
    {
        return fractionalPositions(relativePosition, getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final Map<Lane, Double> fractionalPositions(final RelativePosition relativePosition,
        final DoubleScalar.Abs<TimeUnit> when) throws NetworkException
    {
        Map<Lane, Double> positions = new HashMap<>();
        for (Lane lane : this.longitudinalPositions.keySet())
        {
            positions.put(lane, fractionalPosition(lane, relativePosition, when));
        }
        return positions;
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition,
        final DoubleScalar.Abs<TimeUnit> when) throws NetworkException
    {
        return position(lane, relativePosition, when).getSI() / lane.getLength().getSI();
    }

    /** {@inheritDoc} */
    @Override
    public final double fractionalPosition(final Lane lane, final RelativePosition relativePosition)
        throws NetworkException, RemoteException
    {
        return position(lane, relativePosition).getSI() / lane.getLength().getSI();
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in forward direction.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the GTU
     *            when we measure in the lane where the original GTU is positioned, and 0.0 for each subsequent lane
     * @param cumDistanceSI the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls
     * @param when the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE meters when
     *         no other GTU could not be found within maxDistanceSI meters
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private GTUDistanceSI headwayRecursiveForwardSI(final Lane lane, final double lanePositionSI,
        final double cumDistanceSI, final double maxDistanceSI, final DoubleScalar.Abs<TimeUnit> when)
        throws RemoteException, NetworkException
    {
        LaneBasedGTU<?> otherGTU =
            lane.getGtuAfter(new DoubleScalar.Rel<LengthUnit>(lanePositionSI, LengthUnit.METER), RelativePosition.REAR, when);
        if (otherGTU != null)
        {
            double distanceM = cumDistanceSI + otherGTU.position(lane, otherGTU.getRear(), when).getSI() - lanePositionSI;
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return new GTUDistanceSI(otherGTU, distanceM);
            }
            return new GTUDistanceSI(null, Double.MAX_VALUE);
        }

        // Continue search on successor lanes.
        if (cumDistanceSI + lane.getLength().getSI() - lanePositionSI < maxDistanceSI)
        {
            // is there a successor link?
            Set<Lane> nextLanes = lane.nextLanes();
            if (nextLanes.size() > 0)
            {
                GTUDistanceSI foundMaxGTUDistanceSI = new GTUDistanceSI(null, Double.MAX_VALUE);
                for (Lane nextLane : nextLanes)
                {
                    // Only follow links on the Route if there is a Route
                    if (this.getRoute() == null
                        || (this.getRoute() != null && this.getRoute().containsLink(lane.getParentLink())))
                    {
                        double traveledDistanceSI = cumDistanceSI + lane.getLength().getSI() - lanePositionSI;
                        GTUDistanceSI closest =
                            headwayRecursiveForwardSI(nextLane, 0.0, traveledDistanceSI, maxDistanceSI, when);
                        if (closest.getDistanceSI() < maxDistanceSI
                            && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
                        {
                            foundMaxGTUDistanceSI = closest;
                        }
                    }
                }
                return foundMaxGTUDistanceSI;
            }
        }

        // No other GTU was not on one of the current lanes or their successors.
        return new GTUDistanceSI(null, Double.MAX_VALUE);
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in backward direction (so between our back, and the other
     * GTU's front). Note: this method returns a POSITIVE number.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the rear of
     *            the GTU when we measure in the lane where the original GTU is positioned, and lane.getLength() for each
     *            subsequent lane.
     * @param cumDistanceSI the distance we have already covered searching on previous lanes. Note: This is a POSITIVE number.
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls. Note: this is a
     *            POSITIVE number.
     * @param when the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE meters when
     *         no other GTU could not be found within maxDistanceSI meters
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private GTUDistanceSI headwayRecursiveBackwardSI(final Lane lane, final double lanePositionSI,
        final double cumDistanceSI, final double maxDistanceSI, final DoubleScalar.Abs<TimeUnit> when)
        throws RemoteException, NetworkException
    {
        LaneBasedGTU<?> otherGTU =
            lane.getGtuBefore(new DoubleScalar.Rel<LengthUnit>(lanePositionSI, LengthUnit.METER), RelativePosition.FRONT,
                when);
        if (otherGTU != null)
        {
            double distanceM = cumDistanceSI + lanePositionSI - otherGTU.position(lane, otherGTU.getFront(), when).getSI();
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return new GTUDistanceSI(otherGTU, distanceM);
            }
            return new GTUDistanceSI(null, Double.MAX_VALUE);
        }

        // Continue search on predecessor lanes.
        if (cumDistanceSI + lanePositionSI < maxDistanceSI)
        {
            // is there a predecessor link?
            Set<Lane> prevLanes = lane.prevLanes();
            if (prevLanes.size() > 0)
            {
                GTUDistanceSI foundMaxGTUDistanceSI = new GTUDistanceSI(null, Double.MAX_VALUE);
                for (Lane prevLane : prevLanes)
                {
                    // What is behind us is INDEPENDENT of the followed route!
                    double traveledDistanceSI = cumDistanceSI + lanePositionSI;
                    GTUDistanceSI closest =
                        headwayRecursiveBackwardSI(prevLane, prevLane.getLength().getSI(), traveledDistanceSI,
                            maxDistanceSI, when);
                    if (closest.getDistanceSI() < maxDistanceSI
                        && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
                    {
                        foundMaxGTUDistanceSI = closest;
                    }
                }
                return foundMaxGTUDistanceSI;
            }
        }

        // No other GTU was not on one of the current lanes or their successors.
        return new GTUDistanceSI(null, Double.MAX_VALUE);
    }

    /**
     * @param maxDistanceSI the maximum distance to look for in SI units
     * @return the nearest GTU and the net headway to this GTU in SI units when we have found the GTU, or a null GTU with a
     *         distance of Double.MAX_VALUE meters when no other GTU could not be found within maxDistanceSI meters
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private GTUDistanceSI headwayGTUSI(final double maxDistanceSI) throws RemoteException, NetworkException
    {
        DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
        GTUDistanceSI foundMaxGTUDistanceSI = new GTUDistanceSI(null, Double.MAX_VALUE);
        // search for the closest GTU on all current lanes we are registered on.
        if (maxDistanceSI > 0.0)
        {
            // look forward.
            for (Lane lane : positions(getFront()).keySet())
            {
                GTUDistanceSI closest =
                    headwayRecursiveForwardSI(lane, this.position(lane, this.getFront(), when).getSI(), 0.0, maxDistanceSI,
                        when);
                if (closest.getDistanceSI() < maxDistanceSI
                    && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
                {
                    foundMaxGTUDistanceSI = closest;
                }
            }
        }
        else
        {
            // look backward.
            for (Lane lane : positions(getRear()).keySet())
            {
                GTUDistanceSI closest =
                    headwayRecursiveBackwardSI(lane, this.position(lane, this.getRear(), when).getSI(), 0.0, -maxDistanceSI,
                        when);
                if (closest.getDistanceSI() < -maxDistanceSI
                    && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
                {
                    foundMaxGTUDistanceSI = closest;
                }
            }
        }
        return foundMaxGTUDistanceSI;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headway(final DoubleScalar.Rel<LengthUnit> maxDistance)
        throws RemoteException, NetworkException
    {
        return new DoubleScalar.Rel<LengthUnit>(headwayGTUSI(maxDistance.getSI()).getDistanceSI(), LengthUnit.METER);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headway(final Lane lane, final DoubleScalar.Rel<LengthUnit> maxDistance)
        throws RemoteException, NetworkException
    {
        DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
        if (maxDistance.getSI() > 0.0)
        {
            return new DoubleScalar.Rel<LengthUnit>(headwayRecursiveForwardSI(lane,
                this.position(lane, this.getFront(), when).getSI(), 0.0, maxDistance.getSI(), when).getDistanceSI(),
                LengthUnit.METER);
        }
        else
        {
            return new DoubleScalar.Rel<LengthUnit>(headwayRecursiveBackwardSI(lane,
                this.position(lane, this.getRear(), when).getSI(), 0.0, -maxDistance.getSI(), when).getDistanceSI(),
                LengthUnit.METER);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU<?> headwayGTU(final Lane lane, final DoubleScalar.Rel<LengthUnit> maxDistance)
        throws RemoteException, NetworkException
    {
        DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
        if (maxDistance.getSI() > 0.0)
        {
            return headwayRecursiveForwardSI(lane, this.position(lane, this.getFront(), when).getSI(), 0.0,
                maxDistance.getSI(), when).getOtherGTU();
        }
        else
        {
            return headwayRecursiveBackwardSI(lane, this.position(lane, this.getRear(), when).getSI(), 0.0,
                -maxDistance.getSI(), when).getOtherGTU();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final LaneBasedGTU<?> headwayGTU(final DoubleScalar.Rel<LengthUnit> maxDistance) throws RemoteException,
        NetworkException
    {
        return headwayGTUSI(maxDistance.getSI()).getOtherGTU();
    }

    /**
     * Calculate the headway to a GTU, possibly on subsequent lanes, in forward direction.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the (front
     *            of the) GTU when we measure in the lane where the original GTU is positioned, and 0.0 for each subsequent lane
     * @param otherGTU the GTU to which the headway must be returned
     * @param cumDistanceSI the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for; stays the same in subsequent calls
     * @param when the future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or Double.MAX_VALUE when the otherGTU could not be found
     *         within maxDistanceSI
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private double headwayRecursiveForwardSI(final Lane lane, final double lanePositionSI, final LaneBasedGTU<?> otherGTU,
        final double cumDistanceSI, final double maxDistanceSI, final DoubleScalar.Abs<TimeUnit> when)
        throws RemoteException, NetworkException
    {
        if (lane.getGtuList().contains(otherGTU))
        {
            double distanceM = cumDistanceSI + otherGTU.position(lane, otherGTU.getRear(), when).getSI() - lanePositionSI;
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return distanceM;
            }
            return Double.MAX_VALUE;
        }

        // Continue search on successor lanes.
        if (cumDistanceSI + lane.getLength().getSI() - lanePositionSI < maxDistanceSI)
        {
            // is there a successor link?
            Set<Lane> nextLanes = lane.nextLanes();
            if (nextLanes.size() > 0)
            {
                for (Lane nextLane : nextLanes)
                {
                    // Only follow links on the Route if there is a Route
                    if (this.getRoute() == null
                        || (this.getRoute() != null && this.getRoute().containsLink(lane.getParentLink())))
                    {
                        double traveledDistanceSI = cumDistanceSI + lane.getLength().getSI() - lanePositionSI;
                        double headwaySuccessor =
                            headwayRecursiveForwardSI(nextLane, 0.0, otherGTU, traveledDistanceSI, maxDistanceSI, when);
                        if (headwaySuccessor < maxDistanceSI)
                        {
                            return headwaySuccessor;
                        }
                    }
                }
            }
        }

        // The otherGTU was not on one of the current lanes or their successors.
        return Double.MAX_VALUE;
    }

    /**
     * Calculate the headway to a GTU, possibly on subsequent lanes, in backward direction.
     * @param lane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the (back
     *            of) the GTU when we measure in the lane where the original GTU is positioned, and the length of the lane for
     *            each subsequent lane
     * @param otherGTU the GTU to which the headway must be returned
     * @param cumDistanceSI the distance we have already covered searching on previous lanes, as a POSITIVE number
     * @param maxDistanceSI the maximum distance to look for; stays the same in subsequent calls, as a POSITIVE number
     * @param when the future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or Double.MAX_VALUE when the otherGTU could not be found
     *         within maxDistanceSI
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private double headwayRecursiveBackwardSI(final Lane lane, final double lanePositionSI, final LaneBasedGTU<?> otherGTU,
        final double cumDistanceSI, final double maxDistanceSI, final DoubleScalar.Abs<TimeUnit> when)
        throws RemoteException, NetworkException
    {
        if (lane.getGtuList().contains(otherGTU))
        {
            double distanceM = cumDistanceSI + lanePositionSI - otherGTU.position(lane, otherGTU.getFront(), when).getSI();
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return distanceM;
            }
            return Double.MAX_VALUE;
        }

        // Continue search on successor lanes.
        if (cumDistanceSI + lanePositionSI < maxDistanceSI)
        {
            // is there a successor link?
            Set<Lane> prevLanes = lane.prevLanes();
            if (prevLanes.size() > 0)
            {
                for (Lane prevLane : prevLanes)
                {
                    // Routes are NOT IMPORTANT when we look backward.
                    double traveledDistanceSI = cumDistanceSI + lanePositionSI;
                    double headwayPredecessor =
                        headwayRecursiveForwardSI(prevLane, prevLane.getLength().getSI(), otherGTU, traveledDistanceSI,
                            maxDistanceSI, when);
                    if (headwayPredecessor < maxDistanceSI)
                    {
                        return headwayPredecessor;
                    }
                }
            }
        }

        // The otherGTU was not on one of the current lanes or their successors.
        return Double.MAX_VALUE;
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headway(final LaneBasedGTU<?> otherGTU,
        final DoubleScalar.Rel<LengthUnit> maxDistance) throws RemoteException, NetworkException
    {
        return headway(otherGTU, maxDistance, getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<LengthUnit> headway(final LaneBasedGTU<?> otherGTU,
        final DoubleScalar.Rel<LengthUnit> maxDistance, final DoubleScalar.Abs<TimeUnit> when) throws RemoteException,
        NetworkException
    {
        if (otherGTU == null)
        {
            return new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        }

        // search for the otherGTU on the current lanes we are registered on.
        if (maxDistance.getSI() > 0.0)
        {
            for (Lane lane : positions(getFront()).keySet())
            {
                // call an internal recursive method
                return new DoubleScalar.Rel<LengthUnit>(headwayRecursiveForwardSI(lane, this.position(lane, this.getFront(),
                    when).getSI(), otherGTU, 0.0, maxDistance.getSI(), when), LengthUnit.METER);
            }
            // other GTU not found within maxDistance
            return new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        }
        else
        {
            for (Lane lane : positions(getRear()).keySet())
            {
                // call an internal recursive method
                return new DoubleScalar.Rel<LengthUnit>(headwayRecursiveBackwardSI(lane, this.position(lane, this.getRear(),
                    when).getSI(), otherGTU, 0.0, maxDistance.getSI(), when), LengthUnit.METER);
            }
            // other GTU not found within maxDistance
            return new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Set<LaneBasedGTU<?>> parallel(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
        throws RemoteException, NetworkException
    {
        Set<LaneBasedGTU<?>> gtuSet = new HashSet<LaneBasedGTU<?>>();
        for (Lane l : this.longitudinalPositions.keySet())
        {
            // only take lanes that we can compare based on a shared design line
            if (l.getParentLink().equals(lane.getParentLink()))
            {
                // compare based on fractional positions.
                double posFractionFront = Math.max(0.0, this.fractionalPosition(l, getFront(), when));
                double posFractionRear = Math.min(1.0, this.fractionalPosition(l, getRear(), when));
                for (LaneBasedGTU<?> gtu : lane.getGtuList())
                {
                    if (!gtu.equals(this))
                    {
                        double gtuFractionFront = Math.max(0.0, gtu.fractionalPosition(l, getFront(), when));
                        double gtuFractionRear = Math.min(1.0, gtu.fractionalPosition(l, getRear(), when));
                        if ((gtuFractionFront >= posFractionFront && gtuFractionFront <= posFractionRear)
                            || (gtuFractionRear >= posFractionFront && gtuFractionRear <= posFractionRear))
                        {
                            gtuSet.add(gtu);
                        }
                    }
                }
            }
        }
        return gtuSet;
    }

    /** {@inheritDoc} */
    @Override
    public final Set<LaneBasedGTU<?>> parallel(final LateralDirectionality lateralDirection,
        final DoubleScalar.Abs<TimeUnit> when) throws RemoteException, NetworkException
    {
        Set<Lane> adjacentLanes = new HashSet<Lane>();
        for (Lane lane : this.longitudinalPositions.keySet())
        {
            adjacentLanes.addAll(lane.accessibleAdjacentLanes(lateralDirection, getGTUType()));
        }

        Set<LaneBasedGTU<?>> gtuSet = new HashSet<LaneBasedGTU<?>>();

        return new HashSet<LaneBasedGTU<?>>();
    }

    /**
     * Retrieve the GTUFollowingModel of this GTU.
     * @return GTUFollowingModel
     */
    public final GTUFollowingModel getGTUFollowingModel()
    {
        return this.gtuFollowingModel;
    }

    /** {@inheritDoc} */
    @Override
    public final DirectedPoint getLocation() throws RemoteException
    {
        Lane lane = this.longitudinalPositions.keySet().iterator().next();
        try
        {
            // TODO solve problem when point is still on previous lane.
            double fraction = (position(lane, getFront()).getSI() - getLength().getSI() / 2.0) / lane.getLength().getSI();
            LineString line = lane.getCenterLine();
            LengthIndexedLine lil = new LengthIndexedLine(line);
            Coordinate c = lil.extractPoint(fraction * line.getLength());
            // HACK (FIXME)
            c.z = 0d;
            Coordinate ca =
                fraction <= 0.01 ? lil.extractPoint(0.0) : lil.extractPoint((fraction - 0.01) * line.getLength());
            Coordinate cb =
                fraction >= 0.99 ? lil.extractPoint(1.0) : lil.extractPoint((fraction + 0.01) * line.getLength());
            double angle = Math.atan2(cb.y - ca.y, cb.x - ca.x);
            return new DirectedPoint(c.x, c.y, c.z + 0.01 /* raise it slightly above the lane surface */, 0.0, 0.0, angle);
        }
        catch (NetworkException ne)
        {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Bounds getBounds() throws RemoteException
    {
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new BoundingBox(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    /**
     * Description of Car at specified time.
     * @param lane the position on this lane will be returned.
     * @param when DoubleScalarAbs&lt;TimeUnit&gt;; the time
     * @return String; description of this Car at the specified time
     */
    public final String toString(final Lane lane, final DoubleScalar.Abs<TimeUnit> when)
    {
        double pos = Double.NaN;
        try
        {
            pos = this.position(lane, getFront(), when).getSI();
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
        }
        // A space in the format after the % becomes a space for positive numbers or a minus for negative numbers
        return String.format("Car %5d lastEval %6.1fs, nextEval %6.1fs, % 9.3fm, v % 6.3fm/s, a % 6.3fm/s/s", getId(),
            this.lastEvaluationTime.getSI(), getNextEvaluationTime().getSI(), pos, this.getLongitudinalVelocity(when)
                .getSI(), this.getAcceleration(when).getSI());
    }

    /**
     * Helper class to store another GTU and the distance of this GTU to the other GTU.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version Jan 21, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     */
    private class GTUDistanceSI
    {
        /** the other GTU. */
        private final LaneBasedGTU<?> otherGTU;

        /** the distance to the GTU in meters. */
        private final double distanceSI;

        /**
         * @param otherGTU the other GTU
         * @param distanceSI the distance to the other GTU in meters
         */
        public GTUDistanceSI(final LaneBasedGTU<?> otherGTU, final double distanceSI)
        {
            super();
            this.otherGTU = otherGTU;
            this.distanceSI = distanceSI;
        }

        /**
         * @return the other GTU.
         */
        public final LaneBasedGTU<?> getOtherGTU()
        {
            return this.otherGTU;
        }

        /**
         * @return distanceSI the distance to the other GTU in meters.
         */
        public final double getDistanceSI()
        {
            return this.distanceSI;
        }

    }
}
