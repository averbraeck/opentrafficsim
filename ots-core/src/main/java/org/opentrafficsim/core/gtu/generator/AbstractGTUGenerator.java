package org.opentrafficsim.core.gtu.generator;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.reflection.ClassUtil;

import org.opentrafficsim.core.car.DefaultCarAnimation;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.car.LaneBasedIndividualCar.LaneBasedIndividualCarBuilder;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DistContinuousDoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Feb 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <ID> the ID type of the GTU (e.g., String or Integer)
 */
public abstract class AbstractGTUGenerator<ID>
{
    /** The generator name. Will be used for generated GTUs as Name:# where # is the id of the gtu when ID is a String. */
    private final String name;

    /** The type of GTU to generate. */
    private final GTUType<ID> gtuType;

    /** The gtu class to instantiate. */
    private final Class<?> gtuClass;

    /** The GTU following model to use. */
    private final GTUFollowingModel gtuFollowingModel;

    /** The lane change model to use. */
    private final LaneChangeModel laneChangeModel;

    /** Distribution of the initial speed of the GTU. */
    private final DistContinuousDoubleScalar.Abs<SpeedUnit> initialSpeedDist;

    /** Distribution of the interarrival time. */
    private final DistContinuousDoubleScalar.Rel<TimeUnit> interarrivelTimeDist;

    /** Maximum number of GTUs to generate. */
    private final long maxGTUs;

    /** Start time of generation (delayed start). */
    private final DoubleScalar.Abs<TimeUnit> startTime;

    /** End time of generation. */
    private final DoubleScalar.Abs<TimeUnit> endTime;

    /** Lane to generate the GTU on -- at the end for now. */
    private final Lane lane;

    /** Route generator used to create a route for each generated GTU. */
    private RouteGenerator routeGenerator;

    /** Car builder list. */
    private List<LaneBasedIndividualCarBuilder<?>> carBuilderList = new ArrayList<>();

    /** Number of generated GTUs. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected long numberGTUs = 0;

    /**
     * @param name the name of the generator
     * @param simulator the simulator to schedule the start of the generation
     * @param gtuType the type of GTU to generate
     * @param gtuClass the GTU class to instantiate
     * @param gtuFollowingModel the GTU following model to use
     * @param laneChangeModel the lane change model to use
     * @param initialSpeedDist distribution of the initial speed of the GTU
     * @param interarrivelTimeDist distribution of the interarrival time
     * @param maxGTUs maximum number of GTUs to generate
     * @param startTime start time of generation (delayed start)
     * @param endTime end time of generation
     * @param lane the lane to generate the GTU on -- at the end for now
     * @param routeGenerator RouteGenerator; the route generator that will create a route for each generated GTU
     * @throws SimRuntimeException when simulation scheduling fails
     * @throws RemoteException when remote simulator cannot be reached
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractGTUGenerator(final String name, final OTSDEVSSimulatorInterface simulator,
            final GTUType<ID> gtuType, final Class<?> gtuClass, final GTUFollowingModel gtuFollowingModel,
            final LaneChangeModel laneChangeModel, final DistContinuousDoubleScalar.Abs<SpeedUnit> initialSpeedDist,
            final DistContinuousDoubleScalar.Rel<TimeUnit> interarrivelTimeDist, final long maxGTUs,
            final DoubleScalar.Abs<TimeUnit> startTime, final DoubleScalar.Abs<TimeUnit> endTime, final Lane lane,
            final RouteGenerator routeGenerator) throws RemoteException, SimRuntimeException
    {
        super();
        this.name = name;
        this.gtuType = gtuType;
        this.gtuClass = gtuClass;
        this.gtuFollowingModel = gtuFollowingModel;
        this.laneChangeModel = laneChangeModel;
        this.initialSpeedDist = initialSpeedDist;
        this.interarrivelTimeDist = interarrivelTimeDist;
        this.maxGTUs = maxGTUs;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lane = lane;
        this.routeGenerator = routeGenerator;

        simulator.scheduleEventAbs(startTime, this, this, "generate", null);
        simulator.scheduleEventAbs(startTime, this, this, "checkCarBuilderList", null);
    }

    /**
     * Generate a GTU.
     * @throws Exception when something in the generation fails.
     */
    @SuppressWarnings("unchecked")
    protected final void generate() throws Exception
    {
        // get the return type of the getID() method of the GTU class
        Class<?> getidtype;
        try
        {
            Method getid = ClassUtil.resolveMethod(this.gtuClass, "getId", new Class<?>[]{});
            getidtype = getid.getReturnType();
        }
        catch (NoSuchMethodException exception)
        {
            throw new GTUException("GTU class " + this.gtuClass.getName() + " does not have getId() method.", exception);
        }

        // create a unique id
        ID id = null;
        this.numberGTUs++;
        if (String.class.isAssignableFrom(getidtype))
        {
            id = (ID) new String(this.name + ":" + this.numberGTUs);
        }
        else if (int.class.isAssignableFrom(getidtype))
        {
            id = (ID) new Integer((int) this.numberGTUs);
        }
        else if (long.class.isAssignableFrom(getidtype))
        {
            id = (ID) new Long(this.numberGTUs);
        }
        else
        {
            // FIXME throw new GTUException("GTU ID class " + getidtype.getName() + ": cannot instantiate.")
            id = (ID) new String(this.name + ":" + this.numberGTUs);
        }

        // create the GTU
        if (LaneBasedIndividualCar.class.isAssignableFrom(getGtuClass()))
        {
            LaneBasedIndividualCarBuilder<ID> carBuilder = new LaneBasedIndividualCarBuilder<ID>();
            carBuilder.setId(id);
            carBuilder.setGtuType(getGtuType());
            carBuilder.setGTUFollowingModel(this.gtuFollowingModel);
            carBuilder.setLaneChangeModel(this.laneChangeModel);
            DoubleScalar.Rel<LengthUnit> carLength = getLengthDist().draw();
            carBuilder.setLength(carLength);
            carBuilder.setWidth(getWidthDist().draw());
            carBuilder.setMaximumVelocity(getMaximumSpeedDist().draw());
            carBuilder.setInitialSpeed(getInitialSpeedDist().draw());
            carBuilder.setSimulator(getSimulator());
            Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions = new LinkedHashMap<>(1);
            initialLongitudinalPositions.put(this.lane, this.lane.getLength().mutable().decrementBy(carLength)
                    .immutable());
            carBuilder.setInitialLongitudinalPositions(initialLongitudinalPositions);
            carBuilder.setRoute(getRouteGenerator().generateRoute());
            carBuilder.setAnimationClass(DefaultCarAnimation.class);

            // put the car in the queue and take it from there -- if the headway is enough, build the car.
            this.carBuilderList.add(carBuilder);
            // carBuilder.build();
        }
        else
        {
            throw new GTUException("GTU class " + getGtuClass().getName() + ": cannot instantiate, no builder.");
        }

        // reschedule next arrival
        OTSSimTimeDouble nextTime = getSimulator().getSimulatorTime().plus(this.interarrivelTimeDist.draw());
        if (nextTime.get().getSI() < this.endTime.getSI())
        {
            getSimulator().scheduleEventAbs(nextTime, this, this, "generate", null);
        }
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in forward direction.
     * @param theLane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the
     *            GTU when we measure in the lane where the original GTU is positioned, and 0.0 for each subsequent lane
     * @param cumDistanceSI the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls
     * @param when the current or future time for which to calculate the headway
     * @param route Route; the route that the GTU intends to follow
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE
     *         meters when no other GTU could not be found within maxDistanceSI meters
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayRecursiveForwardSI(final Lane theLane, final double lanePositionSI,
            final double cumDistanceSI, final double maxDistanceSI, final DoubleScalar.Abs<TimeUnit> when,
            final Route route) throws RemoteException, NetworkException
    {
        LaneBasedGTU<?> otherGTU =
                theLane.getGtuAfter(new DoubleScalar.Rel<LengthUnit>(lanePositionSI, LengthUnit.METER),
                        RelativePosition.REAR, when);
        if (otherGTU != null)
        {
            double distanceM =
                    cumDistanceSI + otherGTU.position(theLane, otherGTU.getRear(), when).getSI() - lanePositionSI;
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return new HeadwayGTU(otherGTU, distanceM);
            }
            return new HeadwayGTU(null, Double.MAX_VALUE);
        }

        // Continue search on successor lanes.
        if (cumDistanceSI + theLane.getLength().getSI() - lanePositionSI < maxDistanceSI)
        {
            // is there a successor link?
            Set<Lane> nextLanes = theLane.nextLanes();
            if (nextLanes.size() > 0)
            {
                HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
                for (Lane nextLane : nextLanes)
                {
                    // Only follow links on the Route if there is a "real" Route
                    if (route == null || route.size() == 0 /* XXXXX STUB dummy route */
                            || route.containsLink(theLane.getParentLink()))
                    {
                        double traveledDistanceSI = cumDistanceSI + theLane.getLength().getSI() - lanePositionSI;
                        HeadwayGTU closest =
                                headwayRecursiveForwardSI(nextLane, 0.0, traveledDistanceSI, maxDistanceSI, when, route);
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
        return new HeadwayGTU(null, Double.MAX_VALUE);
    }

    /**
     * Find the first GTU starting on the specified lane following the specified route.
     * @param maxDistanceSI the maximum distance to look for in SI units
     * @param route Route; the route that the GTU intends to follow
     * @param generatorLane Lane; the lane on which the the search for a leader starts
     * @return the nearest GTU and the net headway to this GTU in SI units when we have found the GTU, or a null GTU
     *         with a distance of Double.MAX_VALUE meters when no other GTU could not be found within maxDistanceSI
     *         meters
     * @throws RemoteException when the simulation time cannot be retrieved
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayGTUSIForward(final double maxDistanceSI, final Route route, final Lane generatorLane)
            throws RemoteException, NetworkException
    {
        DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
        HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
        // search for the closest GTU on all current lanes we are registered on.
        // look forward.
        HeadwayGTU closest =
                headwayRecursiveForwardSI(this.lane, generatorLane.getLength().getSI(), 0.0, maxDistanceSI, when, route);
        if (closest.getDistanceSI() < maxDistanceSI && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
        {
            foundMaxGTUDistanceSI = closest;
        }
        return foundMaxGTUDistanceSI;
    }

    /**
     * Check the available headway for GTU that is about to be constructed.
     * @param maxDistance DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum distance to look for a leader
     * @param route Route; the route that this GTU intends to take
     * @param generatorLane Lane; the lane on which the GTU is generated
     * @return HeadwayGTU; the available headway and the GTU at that headway
     * @throws RemoteException on communications failure
     * @throws NetworkException on network inconsistency
     */
    public final HeadwayGTU headway(final DoubleScalar.Rel<LengthUnit> maxDistance, final Route route,
            final Lane generatorLane) throws RemoteException, NetworkException
    {
        return headwayGTUSIForward(maxDistance.getSI(), route, generatorLane);
    }

    /**
     * Check if car can be generated.
     * @throws Exception on any problem
     */
    protected final void checkCarBuilderList() throws Exception
    {
        if (!this.carBuilderList.isEmpty())
        {
            LaneBasedIndividualCarBuilder<?> carBuilder = this.carBuilderList.get(0);
            Lane generatorLane = carBuilder.getInitialLongitudinalPositions().keySet().iterator().next();
            if (generatorLane.getGtuList().isEmpty())
            {
                GTUFollowingModel followingModel = carBuilder.getGtuFollowingModel();
                HeadwayGTU headwayGTU =
                        headway(new DoubleScalar.Rel<LengthUnit>(250.0, LengthUnit.METER), carBuilder.getRoute(),
                                generatorLane);
                DoubleScalar.Rel<LengthUnit> minimumHeadway = new DoubleScalar.Rel<LengthUnit>(0.0, LengthUnit.METER);
                if (headwayGTU.getOtherGTU() != null)
                {
                    minimumHeadway =
                            followingModel
                                    .minimumHeadway(carBuilder.getInitialSpeed(), headwayGTU.getOtherGTU()
                                            .getLongitudinalVelocity(), new DoubleScalar.Rel<LengthUnit>(1.0,
                                            LengthUnit.CENTIMETER), generatorLane.getSpeedLimit(), carBuilder
                                            .getInitialSpeed());
                }

                if (headwayGTU.getDistance().getSI() >= minimumHeadway.getSI())
                {
                    this.carBuilderList.remove(0);
                    carBuilder.build();
                }
            }
        }

        OTSSimTimeDouble nextTime =
                getSimulator().getSimulatorTime().plus(new DoubleScalar.Rel<TimeUnit>(0.1, TimeUnit.SECOND));
        getSimulator().scheduleEventAbs(nextTime, this, this, "checkCarBuilderList", null);
    }

    /** @return simulator. */
    public abstract OTSDEVSSimulatorInterface getSimulator();

    /** @return lengthDist. */
    public abstract DistContinuousDoubleScalar.Rel<LengthUnit> getLengthDist();

    /** @return widthDist. */
    public abstract DistContinuousDoubleScalar.Rel<LengthUnit> getWidthDist();

    /** @return maximumSpeedDist. */
    public abstract DistContinuousDoubleScalar.Abs<SpeedUnit> getMaximumSpeedDist();

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
    public final GTUType<ID> getGtuType()
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
     * @return gtuFollowingModel.
     */
    public final GTUFollowingModel getGtuFollowingModel()
    {
        return this.gtuFollowingModel;
    }

    /**
     * @return initialSpeedDist.
     */
    public final DistContinuousDoubleScalar.Abs<SpeedUnit> getInitialSpeedDist()
    {
        return this.initialSpeedDist;
    }

    /**
     * @return interarrivelTimeDist.
     */
    public final DistContinuousDoubleScalar.Rel<TimeUnit> getInterarrivelTimeDist()
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
    public final DoubleScalar.Abs<TimeUnit> getStartTime()
    {
        return this.startTime;
    }

    /**
     * @return endTime.
     */
    public final DoubleScalar.Abs<TimeUnit> getEndTime()
    {
        return this.endTime;
    }

    /**
     * @return routeGenerator.
     */
    public final RouteGenerator getRouteGenerator()
    {
        return this.routeGenerator;
    }

    /**
     * @param routeGenerator set routeGenerator.
     */
    public final void setRouteGenerator(final RouteGenerator routeGenerator)
    {
        this.routeGenerator = routeGenerator;
    }

}
