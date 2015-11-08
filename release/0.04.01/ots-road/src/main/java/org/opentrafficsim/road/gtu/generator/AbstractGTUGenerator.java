package org.opentrafficsim.road.gtu.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.tudelft.simulation.dsol.SimRuntimeException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.road.car.LaneBasedIndividualCar;
import org.opentrafficsim.road.car.LaneBasedIndividualCar.LaneBasedIndividualCarBuilder;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.following.GTUFollowingModel;
import org.opentrafficsim.road.gtu.following.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.route.LaneBasedRouteGenerator;
import org.opentrafficsim.road.network.route.LaneBasedRouteNavigator;

/**
 * Common code for LaneBasedGTU generators that may have to postpone putting a GTU on the road due to congestion growing into
 * the generator. <br>
 * Generally, these generators will discover that there is not enough room AFTER having decided what kind (particular length) of
 * GTU will be constructed next. When this happens, the generator must remember the properties of the GTU, but postpone actual
 * generation until there is enough room.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version Feb 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractGTUGenerator
{
    /** The generator name. Will be used for generated GTUs as Name:# where # is the id of the GTU when ID is a String. */
    private final String name;

    /** The type of GTU to generate. */
    private final GTUType gtuType;

    /** The GTU class to instantiate. */
    private final Class<?> gtuClass;

    /** The GTU following model to use. */
    private final GTUFollowingModel gtuFollowingModel;

    /** The lane change model to use. */
    private final LaneChangeModel laneChangeModel;

    /** Distribution of the initial speed of the GTU. */
    private final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist;

    /** Distribution of the interarrival time. */
    private final ContinuousDistDoubleScalar.Rel<Time.Rel, TimeUnit> interarrivelTimeDist;

    /** Generated number of GTUs. */
    private long generatedGTUs = 0;

    /** Maximum number of GTUs to generate. */
    private final long maxGTUs;

    /** Start time of generation (delayed start). */
    private final Time.Abs startTime;

    /** End time of generation. */
    private final Time.Abs endTime;

    /** Lane to generate the GTU on -- at the end for now. */
    private final Lane lane;

    /** position on the lane, relative to the design line of the link. */
    private final Length.Rel position;

    /** Route generator used to create a route for each generated GTU. */
    private LaneBasedRouteGenerator routeGenerator;

    /** GTUColorer to use. */
    private final GTUColorer gtuColorer;

    /** Car builder list. */
    private List<LaneBasedIndividualCarBuilder> carBuilderList = new ArrayList<>();

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
     * @param lane the lane to generate the GTU on
     * @param position position on the lane, relative to the design line of the link
     * @param routeGenerator RouteGenerator; the route generator that will create a route for each generated GTU
     * @param gtuColorer the GTUColorer to use
     * @throws SimRuntimeException when simulation scheduling fails
     */
    @SuppressWarnings("checkstyle:parameternumber")
    public AbstractGTUGenerator(final String name, final OTSDEVSSimulatorInterface simulator, final GTUType gtuType,
        final Class<?> gtuClass, final GTUFollowingModel gtuFollowingModel, final LaneChangeModel laneChangeModel,
        final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDist,
        final ContinuousDistDoubleScalar.Rel<Time.Rel, TimeUnit> interarrivelTimeDist, final long maxGTUs,
        final Time.Abs startTime, final Time.Abs endTime, final Lane lane, final Length.Rel position,
        final LaneBasedRouteGenerator routeGenerator, final GTUColorer gtuColorer) throws SimRuntimeException
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
        this.position = position;
        this.routeGenerator = routeGenerator;
        this.gtuColorer = gtuColorer;

        simulator.scheduleEventAbs(startTime, this, this, "generate", null);
    }

    /**
     * Generate a GTU.
     * @throws Exception when something in the generation fails.
     */
    protected final void generate() throws Exception
    {
        // check if we are after the end time
        if (getSimulator().getSimulatorTime().getTime().gt(this.endTime))
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
        if (LaneBasedIndividualCar.class.isAssignableFrom(getGtuClass()))
        {
            LaneBasedIndividualCarBuilder carBuilder = new LaneBasedIndividualCarBuilder();
            carBuilder.setId(id);
            carBuilder.setGtuType(getGtuType());
            carBuilder.setGTUFollowingModel(this.gtuFollowingModel);
            carBuilder.setLaneChangeModel(this.laneChangeModel);
            Length.Rel carLength = getLengthDist().draw();
            carBuilder.setLength(carLength);
            carBuilder.setWidth(getWidthDist().draw());
            carBuilder.setMaximumVelocity(getMaximumSpeedDist().draw());
            carBuilder.setInitialSpeed(getInitialSpeedDist().draw());
            carBuilder.setSimulator(getSimulator());
            Map<Lane, Length.Rel> initialLongitudinalPositions = new LinkedHashMap<>(1);
            initialLongitudinalPositions.put(this.lane, this.position);
            carBuilder.setInitialLongitudinalPositions(initialLongitudinalPositions);
            carBuilder.setRouteGenerator(this.routeGenerator);
            carBuilder.setAnimationClass(DefaultCarAnimation.class);
            carBuilder.setGtuColorer(this.gtuColorer);
            this.generatedGTUs++;

            if (enoughSpace(carBuilder))
            {
                carBuilder.build();
            }
            else
            {
                // put the car in the queue and take it from there -- if the headway is enough, build the car.
                this.carBuilderList.add(carBuilder);
                // System.out.println("GTUGenerator - backlog = " + this.carBuilderList.size());
                if (this.carBuilderList.size() == 1)
                {
                    // first entry in list - start the watch thread
                    getSimulator().scheduleEventRel(new Time.Rel(0.1, TimeUnit.SECOND), this, this,
                        "checkCarBuilderList", null);
                }
            }
        }
        else
        {
            throw new GTUException("GTU class " + getGtuClass().getName() + ": cannot instantiate, no builder.");
        }

        // reschedule next arrival
        OTSSimTimeDouble nextTime = getSimulator().getSimulatorTime().plus(this.interarrivelTimeDist.draw());
        if (nextTime.get().le(this.endTime))
        {
            getSimulator().scheduleEventAbs(nextTime, this, this, "generate", null);
        }
    }

    /**
     * Check if the car to be built is not overlapping with another GTU on the same lane, and if it has enough headway to be
     * generated safely.
     * @param carBuilder the car to be generated
     * @return true if car can be safely built, false otherwise.
     * @throws NetworkException if GTU does not have a position on the lane where it is registered
     */
    protected final boolean enoughSpace(final LaneBasedIndividualCarBuilder carBuilder) throws NetworkException
    {
        Lane generatorLane = carBuilder.getInitialLongitudinalPositions().keySet().iterator().next();
        double genPosSI = carBuilder.getInitialLongitudinalPositions().get(generatorLane).getSI();
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
        GTUFollowingModel followingModel = carBuilder.getGtuFollowingModel();

        HeadwayGTU headwayGTU =
            headway(new Length.Rel(250.0, LengthUnit.METER), carBuilder.getRouteGenerator().generateRouteNavigator(),
                generatorLane);
        Length.Rel minimumHeadway = new Length.Rel(0.0, LengthUnit.METER);
        if (headwayGTU.getOtherGTU() != null)
        {
            minimumHeadway =
                followingModel.minimumHeadway(carBuilder.getInitialSpeed(), headwayGTU.getOtherGTU()
                    .getLongitudinalVelocity(), new Length.Rel(1.0, LengthUnit.CENTIMETER), generatorLane
                    .getSpeedLimit(carBuilder.getGtuType()), carBuilder.getMaximumVelocity());
            double acc =
                followingModel
                    .computeAcceleration(carBuilder.getInitialSpeed(), carBuilder.getMaximumVelocity(),
                        headwayGTU.getOtherGTU().getLongitudinalVelocity(), minimumHeadway,
                        carBuilder.getMaximumVelocity()).getSI();
            if (acc < 0)
            {
                System.err.println(getSimulator().getSimulatorTime() + ", generator headway for GTU "
                    + headwayGTU.getOtherGTU() + ", distance " + headwayGTU.getDistanceSI() + " m, max "
                    + minimumHeadway + ", has to brake with a=" + acc + " m/s^2");
                return false;
            }
        }

        // System.out.println(getSimulator().getSimulatorTime() + ", generator headway for GTU " + headwayGTU.getOtherGTU()
        // + ", distance " + headwayGTU.getDistanceSI() + " m, max " + minimumHeadway);
        return headwayGTU.getDistance().ge(minimumHeadway);
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in forward direction.
     * @param theLane the lane where we are looking right now
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the GTU
     *            when we measure in the lane where the original GTU is positioned, and 0.0 for each subsequent lane
     * @param cumDistanceSI the distance we have already covered searching on previous lanes
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls
     * @param when the current or future time for which to calculate the headway
     * @param routeNavigator Route; the route that the GTU intends to follow
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE meters when
     *         no other GTU could not be found within maxDistanceSI meters
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayRecursiveForwardSI(final Lane theLane, final double lanePositionSI,
        final double cumDistanceSI, final double maxDistanceSI, final Time.Abs when,
        final LaneBasedRouteNavigator routeNavigator) throws NetworkException
    {
        LaneBasedGTU otherGTU =
            theLane.getGtuAfter(new Length.Rel(lanePositionSI, LengthUnit.METER), RelativePosition.REAR, when);
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
            if (theLane.nextLanes(this.gtuType).size() > 0)
            {
                HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
                for (Lane nextLane : theLane.nextLanes(this.gtuType))
                {
                    // TODO Only follow links on the Route if there is a "real" Route
                    // if (routeNavigator.getRoute() == null || routeNavigator.getRoute().size() == 0 /* XXXXX STUB dummy route
                    // */
                    // || routeNavigator.getRoute().containsLink((Link) theLane.getParentLink()))
                    {
                        double traveledDistanceSI = cumDistanceSI + theLane.getLength().getSI() - lanePositionSI;
                        HeadwayGTU closest =
                            headwayRecursiveForwardSI(nextLane, 0.0, traveledDistanceSI, maxDistanceSI, when,
                                routeNavigator);
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
     * @param routeNavigator Route; the route that the GTU intends to follow
     * @param generatorLane Lane; the lane on which the the search for a leader starts
     * @return the nearest GTU and the net headway to this GTU in SI units when we have found the GTU, or a null GTU with a
     *         distance of Double.MAX_VALUE meters when no other GTU could not be found within maxDistanceSI meters
     * @throws NetworkException when there is a problem with the geometry of the network
     */
    private HeadwayGTU headwayGTUSIForward(final double maxDistanceSI, final LaneBasedRouteNavigator routeNavigator,
        final Lane generatorLane) throws NetworkException
    {
        Time.Abs when = getSimulator().getSimulatorTime().getTime();
        HeadwayGTU foundMaxGTUDistanceSI = new HeadwayGTU(null, Double.MAX_VALUE);
        // search for the closest GTU on all current lanes we are registered on.
        // look forward.
        HeadwayGTU closest =
            headwayRecursiveForwardSI(this.lane, generatorLane.getLength().getSI(), 0.0, maxDistanceSI, when,
                routeNavigator);
        if (closest.getDistanceSI() < maxDistanceSI && closest.getDistanceSI() < foundMaxGTUDistanceSI.getDistanceSI())
        {
            foundMaxGTUDistanceSI = closest;
        }
        return foundMaxGTUDistanceSI;
    }

    /**
     * Check the available headway for GTU that is about to be constructed.
     * @param maxDistance DoubleScalar.Rel&lt;LengthUnit&gt;; the maximum distance to look for a leader
     * @param routeNavigator RouteNavigator; the route that this GTU intends to take
     * @param generatorLane Lane; the lane on which the GTU is generated
     * @return HeadwayGTU; the available headway and the GTU at that headway
     * @throws NetworkException on network inconsistency
     */
    public final HeadwayGTU headway(final Length.Rel maxDistance, final LaneBasedRouteNavigator routeNavigator,
        final Lane generatorLane) throws NetworkException
    {
        return headwayGTUSIForward(maxDistance.getSI(), routeNavigator, generatorLane);
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
                carBuilder.build();
            }
        }

        // only reschedule if list not empty
        if (!this.carBuilderList.isEmpty())
        {
            getSimulator()
                .scheduleEventRel(new Time.Rel(0.1, TimeUnit.SECOND), this, this, "checkCarBuilderList", null);
        }
    }

    /** @return simulator. */
    public abstract OTSDEVSSimulatorInterface getSimulator();

    /** @return lengthDist. */
    public abstract ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> getLengthDist();

    /** @return widthDist. */
    public abstract ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> getWidthDist();

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
     * @return gtuFollowingModel.
     */
    public final GTUFollowingModel getGtuFollowingModel()
    {
        return this.gtuFollowingModel;
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
    public final ContinuousDistDoubleScalar.Rel<Time.Rel, TimeUnit> getInterarrivelTimeDist()
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
    public final Time.Abs getStartTime()
    {
        return this.startTime;
    }

    /**
     * @return endTime.
     */
    public final Time.Abs getEndTime()
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
    public final void setRouteGenerator(final LaneBasedRouteGenerator routeGenerator)
    {
        this.routeGenerator = routeGenerator;
    }

    /**
     * @return laneChangeModel.
     */
    public final LaneChangeModel getLaneChangeModel()
    {
        return this.laneChangeModel;
    }

    /**
     * @return gtuColorer.
     */
    public final GTUColorer getGtuColorer()
    {
        return this.gtuColorer;
    }

}
