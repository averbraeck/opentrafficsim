package org.opentrafficsim.demo;

import static org.opentrafficsim.core.gtu.GTUType.CAR;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Simulate four double lane roads with a crossing in the middle.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2016-10-28 16:34:11 +0200 (Fri, 28 Oct 2016) $, @version $Revision: 2429 $, by $Author: pknoppers $,
 * initial version ug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CrossingTrafficLightsModel extends AbstractOTSModel implements UNITS
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /** The network. */
    private final OTSNetwork network = new OTSNetwork("network");

    /** the random stream for this demo. */
    private StreamInterface stream = new MersenneTwister(555);

    /** The headway (inter-vehicle time) distribution. */
    private ContinuousDistDoubleScalar.Rel<Duration, DurationUnit> headwayDistribution =
            new ContinuousDistDoubleScalar.Rel<>(new DistTriangular(this.stream, 7, 9, 15), DurationUnit.SECOND);

    /** The speed distribution. */
    private ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> speedDistribution =
            new ContinuousDistDoubleScalar.Rel<>(new DistTriangular(this.stream, 50, 60, 70), SpeedUnit.KM_PER_HOUR);

    /** Number of cars created. */
    private int carsCreated = 0;

    /** Type of all GTUs. */
    private GTUType gtuType = CAR;

    /** the tactical planner factory for this model. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactory;

    /** Car parameters. */
    private Parameters parametersCar;

    /** The speed limit on all Lanes. */
    private Speed speedLimit = new Speed(80, KM_PER_HOUR);

    /** Fixed green time. */
    private static final Duration TGREEN = new Duration(39.0, DurationUnit.SI);

    /** Fixed yellow time. */
    private static final Duration TYELLOW = new Duration(6.0, DurationUnit.SI);

    /** Fixed red time. */
    private static final Duration TRED = new Duration(45.0, DurationUnit.SI);

    /**
     * @param simulator OTSSimulatorInterface; the simulator for this model
     */
    public CrossingTrafficLightsModel(final OTSSimulatorInterface simulator)
    {
        super(simulator);
        InputParameterHelper.makeInputParameterMapCar(this.inputParameterMap, 1.0);
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel() throws SimRuntimeException
    {
        try
        {
            OTSNode[][] nodes = new OTSNode[4][4];
            nodes[0][0] = new OTSNode(this.network, "sn1", new OTSPoint3D(10, -500));
            nodes[0][1] = new OTSNode(this.network, "sn2", new OTSPoint3D(10, -20));
            nodes[0][2] = new OTSNode(this.network, "sn3", new OTSPoint3D(10, +20));
            nodes[0][3] = new OTSNode(this.network, "sn4", new OTSPoint3D(10, +5000));

            nodes[1][0] = new OTSNode(this.network, "we1", new OTSPoint3D(-500, -10));
            nodes[1][1] = new OTSNode(this.network, "we2", new OTSPoint3D(-20, -10));
            nodes[1][2] = new OTSNode(this.network, "we3", new OTSPoint3D(+20, -10));
            nodes[1][3] = new OTSNode(this.network, "we4", new OTSPoint3D(+5000, -10));

            nodes[2][0] = new OTSNode(this.network, "ns1", new OTSPoint3D(-10, +500));
            nodes[2][1] = new OTSNode(this.network, "ns2", new OTSPoint3D(-10, +20));
            nodes[2][2] = new OTSNode(this.network, "ns3", new OTSPoint3D(-10, -20));
            nodes[2][3] = new OTSNode(this.network, "ns4", new OTSPoint3D(-10, -5000));

            nodes[3][0] = new OTSNode(this.network, "ew1", new OTSPoint3D(+500, 10));
            nodes[3][1] = new OTSNode(this.network, "ew2", new OTSPoint3D(+20, 10));
            nodes[3][2] = new OTSNode(this.network, "ew3", new OTSPoint3D(-20, 10));
            nodes[3][3] = new OTSNode(this.network, "ew4", new OTSPoint3D(-5000, 10));

            LaneType laneType = LaneType.TWO_WAY_LANE;

            Map<Lane, SimpleTrafficLight> trafficLights = new HashMap<>();

            for (int i = 0; i < 4; i++)
            {
                for (int j = 0; j < 3; j++)
                {
                    Lane[] lanes = LaneFactory.makeMultiLane(this.network,
                            "Lane_" + nodes[i][j].getId() + "-" + nodes[i][j + 1].getId(), nodes[i][j], nodes[i][j + 1], null,
                            2, laneType, this.speedLimit, this.simulator);
                    if (j == 0)
                    {
                        for (Lane lane : lanes)
                        {
                            this.simulator.scheduleEventRel(this.headwayDistribution.draw(), this, this, "generateCar",
                                    new Object[] {lane});
                            SimpleTrafficLight tl = new SimpleTrafficLight(lane.getId() + "_TL", lane,
                                    new Length(lane.getLength().minus(new Length(10.0, LengthUnit.METER))), this.simulator);
                            trafficLights.put(lane, tl);

                            try
                            {
                                new TrafficLightAnimation(tl, this.simulator);
                            }
                            catch (RemoteException | NamingException exception)
                            {
                                throw new NetworkException(exception);
                            }

                            if (i == 0 || i == 2)
                            {
                                this.simulator.scheduleEventRel(Duration.ZERO, this, this, "changeTL", new Object[] {tl});
                            }
                            else
                            {
                                this.simulator.scheduleEventRel(TRED, this, this, "changeTL", new Object[] {tl});
                            }
                        }
                    }
                    if (j == 2)
                    {
                        for (Lane lane : lanes)
                        {
                            new SinkSensor(lane, new Length(500.0, METER), this.simulator);
                        }
                    }
                }
            }

            this.parametersCar = InputParameterHelper.getParametersCar(getInputParameterMap());
            this.strategicalPlannerFactory = new LaneBasedStrategicalRoutePlannerFactory(
                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
        }
        catch (SimRuntimeException | NamingException | NetworkException | OTSGeometryException | GTUException
                | ParameterException | InputParameterException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Change the traffic light to a new color.
     * @param tl TrafficLight; the traffic light
     * @throws SimRuntimeException when scheduling fails
     */
    protected final void changeTL(final TrafficLight tl) throws SimRuntimeException
    {
        if (tl.getTrafficLightColor().isRed())
        {
            tl.setTrafficLightColor(TrafficLightColor.GREEN);
            this.simulator.scheduleEventRel(TGREEN, this, this, "changeTL", new Object[] {tl});
        }
        else if (tl.getTrafficLightColor().isGreen())
        {
            tl.setTrafficLightColor(TrafficLightColor.YELLOW);
            this.simulator.scheduleEventRel(TYELLOW, this, this, "changeTL", new Object[] {tl});
        }
        else if (tl.getTrafficLightColor().isYellow())
        {
            tl.setTrafficLightColor(TrafficLightColor.RED);
            this.simulator.scheduleEventRel(TRED, this, this, "changeTL", new Object[] {tl});
        }
    }

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     * @param lane Lane; the lane to generate the car on
     */
    protected final void generateCar(final Lane lane)
    {
        Length initialPosition = new Length(10, METER);
        Speed initialSpeed = new Speed(10.0, KM_PER_HOUR);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        try
        {
            initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
            Length vehicleLength = new Length(4, METER);
            LaneBasedIndividualGTU gtu =
                    new LaneBasedIndividualGTU("" + (++this.carsCreated), this.gtuType, vehicleLength, new Length(1.8, METER),
                            this.speedDistribution.draw(), vehicleLength.multiplyBy(0.5), this.simulator, this.network);
            gtu.setParameters(this.parametersCar);
            gtu.setNoLaneChangeDistance(Length.ZERO);
            gtu.setMaximumAcceleration(Acceleration.createSI(3.0));
            gtu.setMaximumDeceleration(Acceleration.createSI(-8.0));
            Route route = null;
            LaneBasedStrategicalPlanner strategicalPlanner = this.strategicalPlannerFactory.create(gtu, route, null, null);
            gtu.init(strategicalPlanner, initialPositions, initialSpeed);
            this.simulator.scheduleEventRel(this.headwayDistribution.draw(), this, this, "generateCar", new Object[] {lane});
        }
        catch (SimRuntimeException | NetworkException | GTUException | OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public OTSNetwork getNetwork()
    {
        return this.network;
    }
}
