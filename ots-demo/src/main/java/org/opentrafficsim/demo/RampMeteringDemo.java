package org.opentrafficsim.demo;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.control.rampmetering.CycleTimeLightController;
import org.opentrafficsim.road.network.control.rampmetering.RampMetering;
import org.opentrafficsim.road.network.control.rampmetering.RampMeteringLightController;
import org.opentrafficsim.road.network.control.rampmetering.RampMeteringSwitch;
import org.opentrafficsim.road.network.control.rampmetering.RwsSwitch;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.sensor.Detector;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.SimpleTrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.swing.script.AbstractSimulationScript;

import nl.tudelft.simulation.dsol.logger.SimLogger;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 jun. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class RampMeteringDemo extends AbstractSimulationScript
{

    /**
     * @param properties String[] properties
     */
    protected RampMeteringDemo(final String[] properties)
    {
        super("Ramp metering", "Ramp metering", properties);
    }

    /**
     * @param args String[] command line arguments
     * @throws Exception any exception
     */
    public static void main(final String[] args) throws Exception
    {
        new RampMeteringDemo(args).start();
    }

    /** {@inheritDoc} */
    @Override
    protected void setDefaultProperties()
    {
        setProperty("rampMetering", true);
    }

    /** {@inheritDoc} */
    @Override
    protected OTSRoadNetwork setupSimulation(final OTSSimulatorInterface sim) throws Exception
    {
        SimLogger.setSimulator(sim);

        OTSRoadNetwork network = new OTSRoadNetwork("CIE5805", true);
        GTUType car = network.getGtuType(GTUType.DEFAULTS.CAR);

        OTSRoadNode nodeA = new OTSRoadNode(network, "A", new OTSPoint3D(0, 0), Direction.ZERO);
        OTSRoadNode nodeB = new OTSRoadNode(network, "B", new OTSPoint3D(3000, 0), Direction.ZERO);
        OTSRoadNode nodeC = new OTSRoadNode(network, "C", new OTSPoint3D(3250, 0), Direction.ZERO);
        OTSRoadNode nodeD = new OTSRoadNode(network, "D", new OTSPoint3D(6000, 0), Direction.ZERO);
        OTSRoadNode nodeE = new OTSRoadNode(network, "E", new OTSPoint3D(2000, -25), Direction.ZERO);
        OTSRoadNode nodeF = new OTSRoadNode(network, "F", new OTSPoint3D(2750, 0.0), Direction.ZERO);

        LinkType freeway = network.getLinkType(LinkType.DEFAULTS.FREEWAY);
        LaneKeepingPolicy policy = LaneKeepingPolicy.KEEPRIGHT;
        Length laneWidth = Length.createSI(3.6);
        LaneType freewayLane = network.getLaneType(LaneType.DEFAULTS.FREEWAY);
        Speed speedLimit = new Speed(120, SpeedUnit.KM_PER_HOUR);
        Speed rampSpeedLimit = new Speed(70, SpeedUnit.KM_PER_HOUR);
        List<Lane> lanesAB = new LaneFactory(network, nodeA, nodeB, freeway, sim, policy)
                .leftToRight(1.0, laneWidth, freewayLane, speedLimit).addLanes(Permeable.BOTH).getLanes();
        List<Lane> lanesBC = new LaneFactory(network, nodeB, nodeC, freeway, sim, policy)
                .leftToRight(1.0, laneWidth, freewayLane, speedLimit).addLanes(Permeable.BOTH, Permeable.LEFT).getLanes();
        List<Lane> lanesCD = new LaneFactory(network, nodeC, nodeD, freeway, sim, policy)
                .leftToRight(1.0, laneWidth, freewayLane, speedLimit).addLanes(Permeable.BOTH).getLanes();
        List<Lane> lanesEF =
                new LaneFactory(network, nodeE, nodeF, freeway, sim, policy).setOffsetEnd(laneWidth.multiplyBy(1.5).neg())
                        .leftToRight(0.5, laneWidth, freewayLane, rampSpeedLimit).addLanes().getLanes();
        List<Lane> lanesFB = new LaneFactory(network, nodeF, nodeB, freeway, sim, policy)
                .setOffsetStart(laneWidth.multiplyBy(1.5).neg()).setOffsetEnd(laneWidth.multiplyBy(1.5).neg())
                .leftToRight(0.5, laneWidth, freewayLane, speedLimit).addLanes().getLanes();
        for (Lane lane : lanesCD)
        {
            new SinkSensor(lane, lane.getLength().minus(Length.createSI(50)), GTUDirectionality.DIR_PLUS, sim);
        }
        // detectors
        Detector det1 = new Detector("1", lanesAB.get(0), Length.createSI(2900), sim);
        Detector det2 = new Detector("2", lanesAB.get(1), Length.createSI(2900), sim);
        Detector det3 = new Detector("3", lanesCD.get(0), Length.createSI(100), sim);
        Detector det4 = new Detector("4", lanesCD.get(1), Length.createSI(100), sim);
        List<Detector> detectors12 = new ArrayList<>();
        detectors12.add(det1);
        detectors12.add(det2);
        List<Detector> detectors34 = new ArrayList<>();
        detectors34.add(det3);
        detectors34.add(det4);
        if (getBooleanProperty("rampMetering"))
        {
            // traffic light
            TrafficLight light = new SimpleTrafficLight("light", lanesEF.get(0), lanesEF.get(0).getLength(), sim);
            List<TrafficLight> lightList = new ArrayList<>();
            lightList.add(light);
            // ramp metering
            RampMeteringSwitch rampSwitch = new RwsSwitch(detectors12);
            RampMeteringLightController rampLightController =
                    new CycleTimeLightController(sim, lightList, Compatible.EVERYTHING);
            new RampMetering(sim, rampSwitch, rampLightController);
        }

        // OD
        List<OTSRoadNode> origins = new ArrayList<>();
        origins.add(nodeA);
        origins.add(nodeE);
        List<OTSRoadNode> destinations = new ArrayList<>();
        destinations.add(nodeD);
        Categorization categorization = new Categorization("cat", GTUType.class);// , Lane.class);
        TimeVector globalTimeVector = new TimeVector(new double[] { 0, 3600 }, TimeUnit.BASE, StorageType.DENSE);
        Interpolation globalInterpolation = Interpolation.LINEAR;
        ODMatrix od =
                new ODMatrix("rampMetering", origins, destinations, categorization, globalTimeVector, globalInterpolation);
        // Category carCatMainLeft = new Category(categorization, car, lanesAB.get(0));
        // Category carCatMainRight = new Category(categorization, car, lanesAB.get(1));
        Category carCatRamp = new Category(categorization, car);// , lanesEB.get(0));
        FrequencyVector mainDemand =
                new FrequencyVector(new double[] { 2000, 4000 }, FrequencyUnit.PER_HOUR, StorageType.DENSE);
        FrequencyVector rampDemand = new FrequencyVector(new double[] { 250, 750 }, FrequencyUnit.PER_HOUR, StorageType.DENSE);
        double fLeft = 0.6;
        od.putDemandVector(nodeA, nodeD, carCatRamp, mainDemand);
        // od.putDemandVector(nodeA, nodeD, carCatMainLeft, mainDemand, fLeft);
        // od.putDemandVector(nodeA, nodeD, carCatMainRight, mainDemand, 1.0 - fLeft);
        od.putDemandVector(nodeE, nodeD, carCatRamp, rampDemand);
        ODApplier.applyOD(network, od, sim, new ODOptions());

        return network;
    }

}
