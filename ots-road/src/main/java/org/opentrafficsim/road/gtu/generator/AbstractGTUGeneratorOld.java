package org.opentrafficsim.road.gtu.generator;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.draw.point.Point3d;
import org.djutils.event.EventProducer;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bounds;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU.LaneBasedIndividualCarBuilder;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayDistance;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUSimple;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Common code for LaneBasedGTU generators that may have to postpone putting a GTU on the road due to congestion growing into
 * the generator. <br>
 * Generally, these generators will discover that there is not enough room AFTER having decided what kind (particular length) of
 * GTU will be constructed next. When this happens, the generator must remember the properties of the GTU, but postpone actual
 * generation until there is enough room.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractGTUGeneratorOld extends EventProducer implements Serializable, GtuGeneratorQueue
{
    /** */
    private static final long serialVersionUID = 20150202L;

    /** The generator name. Will be used for generated GTUs as Name:# where # is the id of the GTU when ID is a String. */
    private final String name;

    /** The type of GTU to generate. */
    private final GTUType gtuType;

    /** The GTU class to instantiate. */
    private final Class<?> gtuClass;

    /** Distribution of the initial speed of the GTU. */
    private final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist;

    /** Distribution of the interarrival time. */
    private final ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> interarrivelTimeDist;

    /** Generated number of GTUs. */
    private long generatedGTUs = 0;

    /** Maximum number of GTUs to generate. */
    private final long maxGTUs;

    /** Start time of generation (delayed start). */
    private final Time startTime;

    /** End time of generation. */
    private final Time endTime;

    /** Lane to generate the GTU on -- at the end for now. */
    private final Lane lane;

    /** Position on the lane, relative to the design line of the link. */
    private final Length position;

    /** The direction in which the GTU has to be generated; DIR_PLUS or DIR_MINUS. */
    private final GTUDirectionality direction;

    /** The lane-based strategical planner factory to use. */
    private final LaneBasedStrategicalPlannerFactory<? extends LaneBasedStrategicalPlanner> strategicalPlannerFactory;

    /** Route generator. */
    private final Generator<Route> routeGenerator;

    /** The network. */
    private final OTSRoadNetwork network;

    /** Car builder list. */
    private List<LaneBasedIndividualCarBuilder> carBuilderList = new ArrayList<>();

    /** Number of generated GTUs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected long numberGTUs = 0;

    /** Bounds for animation. */
    private final Bounds bounds;

    /**
     * @param name String; the name of the generator
     * @param simulator OTSSimulatorInterface; the simulator to schedule the start of the generation
     * @param gtuType GTUType; the type of GTU to generate
     * @param gtuClass Class&lt;?&gt;; the GTU class to instantiate
     * @param initialSpeedDist ContinuousDistDoubleScalar.Rel&lt;Speed,SpeedUnit&gt;; distribution of the initial speed of the
     *            GTU
     * @param interarrivelTimeDist ContinuousDistDoubleScalar.Rel&lt;Duration,DurationUnit&gt;; distribution of the interarrival
     *            time
     * @param maxGTUs long; maximum number of GTUs to generate
     * @param startTime Time; start time of generation (delayed start)
     * @param endTime Time; end time of generation
     * @param lane Lane; the lane to generate the GTU on
     * @param position Length; position on the lane, relative to the design line of the link
     * @param direction GTUDirectionality; the direction on the lane in which the GTU has to be generated (DIR_PLUS, or
     *            DIR_MINUS)
     * @param strategicalPlannerFactory LaneBasedStrategicalPlannerFactory&lt;? extends LaneBasedStrategicalPlanner&gt;; the
     *            lane-based strategical planner factory to use
     * @param routeGenerator Generator&lt;Route&gt;; route generator
     * @param network OTSRoadNetwork; the network to register the generated GTUs into
     * @throws SimRuntimeException when simulation scheduling fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractGTUGeneratorOld(final String name, final OTSSimulatorInterface simulator, final GTUType gtuType,
            final Class<?> gtuClass, final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist,
            final ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> interarrivelTimeDist, final long maxGTUs,
            final Time startTime, final Time endTime, final Lane lane, final Length position, final GTUDirectionality direction,
            final LaneBasedStrategicalPlannerFactory<? extends LaneBasedStrategicalPlanner> strategicalPlannerFactory,
            final Generator<Route> routeGenerator, final OTSRoadNetwork network) throws SimRuntimeException
    {
        this.name = name;
        this.gtuType = gtuType;
        this.gtuClass = gtuClass;
        this.initialSpeedDist = initialSpeedDist;
        this.interarrivelTimeDist = interarrivelTimeDist;
        this.maxGTUs = maxGTUs;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lane = lane;
        this.position = position;
        this.direction = direction;
        this.strategicalPlannerFactory = strategicalPlannerFactory;
        this.routeGenerator = routeGenerator;
        this.network = network;
        DirectedPoint p;
        p = this.getLocation();
        this.bounds = new Bounds(new Point3d(p.x - 1, p.y - 1, 0.0), new Point3d(p.x + 1, p.y + 1, 0.0));
        simulator.scheduleEventAbsTime(startTime, this, this, "generate", null);

        // notify the potential animation of the existence of a GTUGenerator
        fireTimedEvent(Network.GENERATOR_ADD_EVENT, name, simulator.getSimulatorTime());
        fireTimedEvent(Network.ANIMATION_GENERATOR_ADD_EVENT, this, simulator.getSimulatorTime());
    }

    /**
     * Generate a GTU.
     * @throws Exception when something in the generation fails.
     */
    protected final void generate() throws Exception
    {
        // check if we are after the end time
        if (getSimulator().getSimulatorAbsTime().gt(this.endTime))
        {
            return;
        }

        // check if we have generated sufficient GTUs
        if (this.generatedGTUs >= this.maxGTUs)
        {
            return;
        }

        // create a unique id
        this.numberGTUs++;
        String id = this.name + ":" + this.numberGTUs;

        // create the GTU
        if (LaneBasedIndividualGTU.class.isAssignableFrom(getGtuClass()))
        {
            LaneBasedIndividualCarBuilder carBuilder = new LaneBasedIndividualCarBuilder();
            carBuilder.setId(id);
            carBuilder.setGtuType(getGtuType());
            Length carLength = getLengthDist().draw();
            carBuilder.setLength(carLength);
            carBuilder.setFront(carLength.times(0.75));
            carBuilder.setWidth(getWidthDist().draw());
            carBuilder.setMaximumSpeed(getMaximumSpeedDist().draw());
            carBuilder.setInitialSpeed(getInitialSpeedDist().draw());
            carBuilder.setSimulator(getSimulator());
            Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
            initialLongitudinalPositions.add(new DirectedLanePosition(this.lane, this.position, this.direction));
            carBuilder.setInitialLongitudinalPositions(initialLongitudinalPositions);
            carBuilder.setNetwork(this.network);
            carBuilder.setMaximumAcceleration(Acceleration.instantiateSI(3.0));
            carBuilder.setMaximumDeceleration(Acceleration.instantiateSI(-8.0));
            this.generatedGTUs++;

            if (enoughSpace(carBuilder))
            {
                carBuilder.build(this.strategicalPlannerFactory, this.routeGenerator.draw(), null, null);
            }
            else
            {
                // put the car in the queue and take it from there -- if the headway is enough, build the car.
                this.carBuilderList.add(carBuilder);
                // System.out.println("GTUGenerator - backlog = " + this.carBuilderList.size());
                if (this.carBuilderList.size() == 1)
                {
                    // first entry in list - start the watch thread
                    getSimulator().scheduleEventRel(new Duration(0.1, DurationUnit.SECOND), this, this, "checkCarBuilderList",
                            null);
                }
            }
        }
        else
        {
            throw new GTUException("GTU class " + getGtuClass().getName() + ": cannot instantiate, no builder.");
        }

        // reschedule next arrival
        Time nextTime = getSimulator().getSimulatorAbsTime().plus(this.interarrivelTimeDist.draw());
        if (nextTime.le(this.endTime))
        {
            getSimulator().scheduleEventAbsTime(nextTime, this, this, "generate", null);
        }
    }

    /**
     * Check if the car to be built is not overlapping with another GTU on the same lane, and if it has enough headway to be
     * generated safely.
     * @param carBuilder LaneBasedIndividualCarBuilder; the car to be generated
     * @return true if car can be safely built, false otherwise.
     * @throws NetworkException when the speed limit of the lane is not known
     * @throws GTUException if GTU does not have a position on the lane where it is registered
     */
    protected final boolean enoughSpace(final LaneBasedIndividualCarBuilder carBuilder) throws NetworkException, GTUException
    {
        DirectedLanePosition directedLanePosition = carBuilder.getInitialLongitudinalPositions().iterator().next();
        Lane generatorLane = directedLanePosition.getLane();
        double genPosSI = directedLanePosition.getPosition().getSI();
        // GTUDirectionality direction = directedLanePosition.getGtuDirection();
        // XXX different from this.direction?
        double lengthSI = generatorLane.getLength().getSI();
        double frontNew = (genPosSI + carBuilder.getLength().getSI()) / lengthSI;
        double rearNew = genPosSI / lengthSI;

        // test for overlap with other GTUs
        for (LaneBasedGTU gtu : generatorLane.getGtuList())
        {
            double frontGTU = gtu.fractionalPosition(generatorLane, gtu.getFront());
            double rearGTU = gtu.fractionalPosition(generatorLane, gtu.getRear());
            if ((frontNew >= rearGTU && frontNew <= frontGTU) || (rearNew >= rearGTU && rearNew <= frontGTU)
                    || (frontGTU >= rearNew && frontGTU <= frontNew) || (rearGTU >= rearNew && rearGTU <= frontNew))
            {
                // System.out.println(getSimulator().getSimulatorTime() + ", generator overlap with GTU " + gtu);
                return false;
            }
        }

        // test for sufficient headway
        GTUFollowingModelOld followingModel = new IDMPlusOld();
        // carBuilder.getStrategicalPlanner().getBehavioralCharacteristics().getGTUFollowingModel();

        Headway headway = headway(new Length(250.0, LengthUnit.METER), generatorLane);
        Length minimumHeadway = new Length(0.0, LengthUnit.METER);
        if (headway.getObjectType().isGtu())
        {
            minimumHeadway = followingModel.minimumHeadway(carBuilder.getInitialSpeed(), headway.getSpeed(),
                    new Length(1.0, LengthUnit.CENTIMETER), new Length(250.0, LengthUnit.METER),
                    generatorLane.getSpeedLimit(carBuilder.getGtuType()), carBuilder.getMaximumSpeed());
            // WS: changed mininumHeadway to headway.getDistance()
            double acc = followingModel.computeAcceleration(carBuilder.getInitialSpeed(), carBuilder.getMaximumSpeed(),
                    headway.getSpeed(), headway.getDistance(), carBuilder.getMaximumSpeed()).getSI();
            if (acc < 0)
            {
                // System.err.println(getSimulator().getSimulatorTime() + ", generator headway for GTU " + headway.getId()
                // + ", distance " + headway.getDistance().si + " m, max " + minimumHeadway + ", has to brake with a="
                // + acc + " m/s^2");
                return false;
            }
        }

        // System.out.println(getSimulator().getSimulatorTime() + ", generator headway for GTU " + headwayGTU.getOtherGTU()
        // + ", distance " + headwayGTU.getDistance().si + " m, max " + minimumHeadway);
        return headway.getDistance().ge(minimumHeadway);
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in DIR_PLUS direction.
     * @param theLane Lane; the lane where we are looking right now
     * @param lanePositionSI double; from which position on this lane do we start measuring? This is the current position of the
     *            GTU when we measure in the lane where the original GTU is positioned, and 0.0 for each subsequent lane
     * @param cumDistanceSI double; the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls
     * @param when Time; the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE meters when
     *         no other GTU could not be found within maxDistanceSI meters
     * @throws GTUException when there is a problem with the geometry of the network
     */
    private Headway headwayRecursiveForwardSI(final Lane theLane, final double lanePositionSI, final double cumDistanceSI,
            final double maxDistanceSI, final Time when) throws GTUException
    {
        // TODO: THIS METHOD IS ALSO IN PERCEPTION -- DON'T DUPLICATE; ALSO, THIS VERSION IS WRONG.
        LaneBasedGTU otherGTU = theLane.getGtuAhead(new Length(lanePositionSI, LengthUnit.METER), GTUDirectionality.DIR_PLUS,
                RelativePosition.REAR, when);
        if (otherGTU != null)
        {
            double distanceM = cumDistanceSI + otherGTU.position(theLane, otherGTU.getRear(), when).getSI() - lanePositionSI;
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return new HeadwayGTUSimple(otherGTU.getId(), otherGTU.getGTUType(), new Length(distanceM, LengthUnit.SI),
                        otherGTU.getLength(), otherGTU.getWidth(), otherGTU.getSpeed(), otherGTU.getAcceleration(), null);
            }
            return new HeadwayDistance(Double.MAX_VALUE);
        }

        // Continue search on successor lanes.
        if (cumDistanceSI + theLane.getLength().getSI() - lanePositionSI < maxDistanceSI)
        {
            // is there a successor link?
            if (theLane.nextLanes(this.gtuType).size() > 0)
            {
                Headway foundMaxGTUDistanceSI = new HeadwayDistance(Double.MAX_VALUE);
                for (Lane nextLane : theLane.nextLanes(this.gtuType).keySet())
                {
                    // TODO Only follow links on the Route if there is a "real" Route
                    // if (routeNavigator.getRoute() == null || routeNavigator.getRoute().size() == 0 /* XXXXX STUB dummy route
                    // */
                    // || routeNavigator.getRoute().containsLink((Link) theLane.getParentLink()))
                    {
                        double traveledDistanceSI = cumDistanceSI + theLane.getLength().getSI() - lanePositionSI;
                        Headway closest = headwayRecursiveForwardSI(nextLane, 0.0, traveledDistanceSI, maxDistanceSI, when);
                        if (closest.getDistance().si < maxDistanceSI
                                && closest.getDistance().si < foundMaxGTUDistanceSI.getDistance().si)
                        {
                            foundMaxGTUDistanceSI = closest;
                        }
                    }
                }
                return foundMaxGTUDistanceSI;
            }
        }

        // No other GTU was not on one of the current lanes or their successors.
        return new HeadwayDistance(Double.MAX_VALUE);
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in DIR_MINUS direction.
     * @param theLane Lane; the lane where we are looking right now
     * @param lanePositionSI double; from which position on this lane do we start measuring? This is the current position of the
     *            GTU when we measure in the lane where the original GTU is positioned, and 0.0 for each subsequent lane
     * @param cumDistanceSI double; the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls
     * @param when Time; the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE meters when
     *         no other GTU could not be found within maxDistanceSI meters
     * @throws GTUException when there is a problem with the geometry of the network
     */
    private Headway headwayRecursiveBackwardSI(final Lane theLane, final double lanePositionSI, final double cumDistanceSI,
            final double maxDistanceSI, final Time when) throws GTUException
    {
        // TODO: THIS METHOD IS ALSO IN PERCEPTION -- DON'T DUPLICATE; ALSO, THIS VERSION IS WRONG.
        LaneBasedGTU otherGTU = theLane.getGtuBehind(new Length(lanePositionSI, LengthUnit.METER), GTUDirectionality.DIR_PLUS,
                RelativePosition.FRONT, when);
        if (otherGTU != null)
        {
            double distanceM = cumDistanceSI + otherGTU.position(theLane, otherGTU.getFront(), when).getSI() - lanePositionSI;
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return new HeadwayGTUSimple(otherGTU.getId(), otherGTU.getGTUType(), new Length(distanceM, LengthUnit.SI),
                        otherGTU.getLength(), otherGTU.getWidth(), otherGTU.getSpeed(), otherGTU.getAcceleration(), null);
            }
            return new HeadwayDistance(Double.MAX_VALUE);
        }

        // Continue search on all predecessor lanes.
        if (cumDistanceSI + theLane.getLength().getSI() - lanePositionSI < maxDistanceSI)
        {
            // is there a predecessor link?
            if (theLane.prevLanes(this.gtuType).size() > 0)
            {
                Headway foundMaxGTUDistanceSI = new HeadwayDistance(Double.MAX_VALUE);
                for (Lane prevLane : theLane.prevLanes(this.gtuType).keySet())
                {
                    // TODO Only follow links on the Route if there is a "real" Route
                    // if (routeNavigator.getRoute() == null || routeNavigator.getRoute().size() == 0 /* XXXXX STUB dummy route
                    // */
                    // || routeNavigator.getRoute().containsLink((Link) theLane.getParentLink()))
                    {
                        double traveledDistanceSI = cumDistanceSI + theLane.getLength().getSI() - lanePositionSI;
                        Headway closest = headwayRecursiveBackwardSI(prevLane, 0.0, traveledDistanceSI, maxDistanceSI, when);
                        if (closest.getDistance().si < maxDistanceSI
                                && closest.getDistance().si < foundMaxGTUDistanceSI.getDistance().si)
                        {
                            foundMaxGTUDistanceSI = closest;
                        }
                    }
                }
                return foundMaxGTUDistanceSI;
            }
        }

        // No other GTU was not on one of the current lanes or their successors.
        return new HeadwayDistance(Double.MAX_VALUE);
    }

    /**
     * Find the first GTU starting on the specified lane following the specified route.
     * @param maxDistanceSI double; the maximum distance to look for in SI units
     * @param generatorLane Lane; the lane on which the the search for a leader starts
     * @return the nearest GTU and the net headway to this GTU in SI units when we have found the GTU, or a null GTU with a
     *         distance of Double.MAX_VALUE meters when no other GTU could not be found within maxDistanceSI meters
     * @throws GTUException when there is a problem with the geometry of the network
     */
    private Headway headwayGTUSIForward(final double maxDistanceSI, final Lane generatorLane) throws GTUException
    {
        Time when = getSimulator().getSimulatorAbsTime();
        Headway foundMaxGTUDistanceSI = new HeadwayDistance(Double.MAX_VALUE);
        // search for the closest GTU on all current lanes we are registered on.
        Headway closest;
        if (this.direction.equals(GTUDirectionality.DIR_PLUS))
        {
            closest = headwayRecursiveForwardSI(this.lane, 0.0, 0.0, maxDistanceSI, when);
        }
        else
        {
            closest = headwayRecursiveBackwardSI(this.lane, generatorLane.getLength().getSI(), 0.0, maxDistanceSI, when);
        }
        if (closest.getDistance().si < maxDistanceSI && closest.getDistance().si < foundMaxGTUDistanceSI.getDistance().si)
        {
            foundMaxGTUDistanceSI = closest;
        }
        return foundMaxGTUDistanceSI;
    }

    /**
     * Check the available headway for GTU that is about to be constructed.
     * @param maxDistance Length; the maximum distance to look for a leader
     * @param generatorLane Lane; the lane on which the GTU is generated
     * @return HeadwayGTU; the available headway and the GTU at that headway
     * @throws GTUException on network inconsistency
     */
    public final Headway headway(final Length maxDistance, final Lane generatorLane) throws GTUException
    {
        return headwayGTUSIForward(maxDistance.getSI(), generatorLane);
    }

    /**
     * Check if car can be generated.
     * @throws Exception on any problem
     */
    protected final void checkCarBuilderList() throws Exception
    {
        if (!this.carBuilderList.isEmpty())
        {
            LaneBasedIndividualCarBuilder carBuilder = this.carBuilderList.get(0);
            if (enoughSpace(carBuilder))
            {
                this.carBuilderList.remove(0);
                carBuilder.build(this.strategicalPlannerFactory, this.routeGenerator.draw(), null, null);
            }
        }

        // only reschedule if list not empty
        if (!this.carBuilderList.isEmpty())
        {
            getSimulator().scheduleEventRel(new Duration(0.1, DurationUnit.SECOND), this, this, "checkCarBuilderList", null);
        }
    }

    /** @return simulator. */
    public abstract OTSSimulatorInterface getSimulator();

    /** @return lengthDist. */
    public abstract ContinuousDistDoubleScalar.Rel<Length, LengthUnit> getLengthDist();

    /** @return widthDist. */
    public abstract ContinuousDistDoubleScalar.Rel<Length, LengthUnit> getWidthDist();

    /** @return maximumSpeedDist. */
    public abstract ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> getMaximumSpeedDist();

    /**
     * @return name.
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * @return gtuType.
     */
    public final GTUType getGtuType()
    {
        return this.gtuType;
    }

    /**
     * @return gtuClass.
     */
    public final Class<?> getGtuClass()
    {
        return this.gtuClass;
    }

    /**
     * @return initialSpeedDist.
     */
    public final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> getInitialSpeedDist()
    {
        return this.initialSpeedDist;
    }

    /**
     * @return interarrivelTimeDist.
     */
    public final ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> getInterarrivelTimeDist()
    {
        return this.interarrivelTimeDist;
    }

    /**
     * @return maxGTUs.
     */
    public final long getMaxGTUs()
    {
        return this.maxGTUs;
    }

    /**
     * @return startTime.
     */
    public final Time getStartTime()
    {
        return this.startTime;
    }

    /**
     * @return endTime.
     */
    public final Time getEndTime()
    {
        return this.endTime;
    }

    /**
     * @return strategicalPlanner
     */
    public final LaneBasedStrategicalPlannerFactory<? extends LaneBasedStrategicalPlanner> getStrategicalPlannerFactory()
    {
        return this.strategicalPlannerFactory;
    }

    /** {@inheritDoc} */
    @Override
    public DirectedPoint getLocation()
    {
        try
        {
            return this.lane.getCenterLine().getLocation(this.position);
        }
        catch (OTSGeometryException exception)
        {
            return this.lane.getLocation();
        }
    }

    /** {@inheritDoc} */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        return this.bounds;
    }

    /** {@inheritDoc} */
    @Override
    public Map<DirectedPoint, Integer> getQueueLengths()
    {
        Map<DirectedPoint, Integer> map = new LinkedHashMap<>();
        map.put(getLocation(), this.carBuilderList.size());
        return map;
    }

}
