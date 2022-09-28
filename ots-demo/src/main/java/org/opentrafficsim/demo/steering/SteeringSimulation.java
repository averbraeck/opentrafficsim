package org.opentrafficsim.demo.steering;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.MassUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Mass;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djutils.cli.CliUtil;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.Bezier;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.units.distributions.ContinuousDistMass;
import org.opentrafficsim.road.gtu.generator.od.DefaultGTUCharacteristicsGeneratorOD;
import org.opentrafficsim.road.gtu.generator.od.ODApplier;
import org.opentrafficsim.road.gtu.generator.od.ODOptions;
import org.opentrafficsim.road.gtu.generator.od.StrategicalPlannerFactorySupplierOD;
import org.opentrafficsim.road.gtu.generator.od.StrategicalPlannerFactorySupplierOD.TacticalPlannerFactorySupplierOD;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.VehicleModel;
import org.opentrafficsim.road.gtu.lane.VehicleModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.steering.SteeringLmrs;
import org.opentrafficsim.road.gtu.lane.tactical.util.Steering;
import org.opentrafficsim.road.gtu.lane.tactical.util.Steering.FeedbackTable;
import org.opentrafficsim.road.gtu.lane.tactical.util.Steering.FeedbackTable.FeedbackVector;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.GapAcceptance;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;
import org.opentrafficsim.road.gtu.strategical.od.Categorization;
import org.opentrafficsim.road.gtu.strategical.od.Category;
import org.opentrafficsim.road.gtu.strategical.od.Interpolation;
import org.opentrafficsim.road.gtu.strategical.od.ODMatrix;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.Stripe.Permeable;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;
import org.opentrafficsim.road.network.lane.object.sensor.Detector;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.swing.script.AbstractSimulationScript;

import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import picocli.CommandLine.Option;

/**
 * Simulation script for steering functionality.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 jan. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class SteeringSimulation extends AbstractSimulationScript
{

    /** Feedback table. */
    static final FeedbackTable FEEDBACK_CAR;

    /** Number of lanes. */
    @Option(names = "--numberOfLanes", description = "Number of lanes", defaultValue = "2")
    private int numberOfLanes;

    static
    {
        // TODO: define tables
        List<FeedbackVector> list = new ArrayList<>();
        list.add(new FeedbackVector(new Speed(25.0, SpeedUnit.KM_PER_HOUR), 0.0, 0.0, 0.0, 0.0));
        list.add(new FeedbackVector(new Speed(75.0, SpeedUnit.KM_PER_HOUR), 0.0, 0.0, 0.0, 0.0));
        FEEDBACK_CAR = new FeedbackTable(list);
    }

    /**
     * Start a simulation.
     * @param args String...; command line arguments
     */
    public static void main(final String... args)
    {
        try
        {
            SteeringSimulation sim = new SteeringSimulation();
            CliUtil.execute(sim, args);
            sim.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Constructor.
     */
    protected SteeringSimulation()
    {
        super("Steering simulation", "Steering simulation");
    }

    /**
     * Sets up the simulation based on provided properties. Properties can be obtained with {@code getProperty()}. Setting up a
     * simulation should at least create a network and some demand. Additionally this may setup traffic control, sampling, etc.
     * @param sim OTSSimulatorInterface; simulator
     * @return OTSRoadNetwork; network
     * @throws Exception on any exception
     */
    @Override
    protected OTSRoadNetwork setupSimulation(final OTSSimulatorInterface sim) throws Exception
    {
        OTSRoadNetwork network = new OTSRoadNetwork("Steering network", true, getSimulator());
        Length laneWidth = Length.instantiateSI(3.5);
        Length stripeWidth = Length.instantiateSI(0.2);

        // points
        OTSPoint3D pointA = new OTSPoint3D(0, 0);
        OTSPoint3D pointB = new OTSPoint3D(2000, 0);
        OTSPoint3D pointC = new OTSPoint3D(2250, 0);
        OTSPoint3D pointD = new OTSPoint3D(3250, 0);
        OTSPoint3D pointE = new OTSPoint3D(1500, -30);

        // nodes
        OTSRoadNode nodeA = new OTSRoadNode(network, "A", pointA, Direction.ZERO);
        OTSRoadNode nodeB = new OTSRoadNode(network, "B", pointB, Direction.ZERO);
        OTSRoadNode nodeC = new OTSRoadNode(network, "C", pointC, Direction.ZERO);
        OTSRoadNode nodeD = new OTSRoadNode(network, "D", pointD, Direction.ZERO);
        OTSRoadNode nodeE = new OTSRoadNode(network, "E", pointE, Direction.ZERO);

        // links
        CrossSectionLink linkAB = new CrossSectionLink(network, "AB", nodeA, nodeB,
                network.getLinkType(LinkType.DEFAULTS.FREEWAY), new OTSLine3D(pointA, pointB), LaneKeepingPolicy.KEEPRIGHT);
        CrossSectionLink linkBC = new CrossSectionLink(network, "BC", nodeB, nodeC,
                network.getLinkType(LinkType.DEFAULTS.FREEWAY), new OTSLine3D(pointB, pointC), LaneKeepingPolicy.KEEPRIGHT);
        CrossSectionLink linkCD = new CrossSectionLink(network, "CD", nodeC, nodeD,
                network.getLinkType(LinkType.DEFAULTS.FREEWAY), new OTSLine3D(pointC, pointD), LaneKeepingPolicy.KEEPRIGHT);
        CrossSectionLink linkEB =
                new CrossSectionLink(network, "EB", nodeE, nodeB, network.getLinkType(LinkType.DEFAULTS.FREEWAY),
                        Bezier.cubic(nodeE.getLocation(), nodeB.getLocation()), LaneKeepingPolicy.KEEPRIGHT);

        // lanes and stripes
        List<Lane> originLanes = new ArrayList<>();
        for (int i = 0; i < this.numberOfLanes; i++)
        {
            for (CrossSectionLink link : new CrossSectionLink[] {linkAB, linkBC, linkCD})
            {
                Lane lane = new Lane(link, "Lane " + (i + 1), laneWidth.times((0.5 + i)), laneWidth,
                        network.getLaneType(LaneType.DEFAULTS.FREEWAY), new Speed(120, SpeedUnit.KM_PER_HOUR));
                Length offset = laneWidth.times(i + 1.0);
                Stripe stripe = new Stripe(link, offset, offset, stripeWidth);
                if (i < this.numberOfLanes - 1)
                {
                    stripe.addPermeability(network.getGtuType(GTUType.DEFAULTS.VEHICLE), Permeable.BOTH);
                }
                // sink sensors
                if (lane.getParentLink().getId().equals("CD"))
                {
                    new SinkSensor(lane, lane.getLength().minus(Length.instantiateSI(100.0)), Compatible.EVERYTHING, sim);
                    // detectors 100m after on ramp
                    new Detector(lane.getFullId(), lane, Length.instantiateSI(100.0), sim); // id equal to lane, may be
                                                                                            // different
                }
                if (lane.getParentLink().getId().equals("AB"))
                {
                    originLanes.add(lane);
                }
            }
        }
        new Stripe(linkAB, Length.ZERO, Length.ZERO, stripeWidth);
        Stripe stripe = new Stripe(linkBC, Length.ZERO, Length.ZERO, stripeWidth);
        stripe.addPermeability(network.getGtuType(GTUType.DEFAULTS.VEHICLE), Permeable.LEFT);
        new Stripe(linkCD, Length.ZERO, Length.ZERO, stripeWidth);
        new Lane(linkBC, "Acceleration lane", laneWidth.times(-0.5), laneWidth, network.getLaneType(LaneType.DEFAULTS.FREEWAY),
                new Speed(120, SpeedUnit.KM_PER_HOUR));
        new Lane(linkEB, "Onramp", laneWidth.times(-0.5), laneWidth, network.getLaneType(LaneType.DEFAULTS.FREEWAY),
                new Speed(120, SpeedUnit.KM_PER_HOUR));
        new Stripe(linkEB, Length.ZERO, Length.ZERO, stripeWidth);
        new Stripe(linkEB, laneWidth.neg(), laneWidth.neg(), stripeWidth);
        new Stripe(linkBC, laneWidth.neg(), laneWidth.neg(), stripeWidth);

        // OD
        List<OTSNode> origins = new ArrayList<>();
        origins.add(nodeA);
        origins.add(nodeE);
        List<OTSNode> destinations = new ArrayList<>();
        destinations.add(nodeD);
        TimeVector timeVector = DoubleVector.instantiate(new double[] {0.0, 0.5, 1.0}, TimeUnit.BASE_HOUR, StorageType.DENSE);
        Interpolation interpolation = Interpolation.LINEAR; // or STEPWISE
        Categorization categorization = new Categorization("GTU type", GTUType.class);
        Category carCategory = new Category(categorization, network.getGtuType(GTUType.DEFAULTS.CAR));
        Category truCategory = new Category(categorization, network.getGtuType(GTUType.DEFAULTS.TRUCK));
        ODMatrix odMatrix = new ODMatrix("Steering OD", origins, destinations, categorization, timeVector, interpolation);

        odMatrix.putDemandVector(nodeA, nodeD, carCategory, freq(new double[] {1000.0, 2000.0, 0.0}));
        odMatrix.putDemandVector(nodeA, nodeD, truCategory, freq(new double[] {100.0, 200.0, 0.0}));
        odMatrix.putDemandVector(nodeE, nodeD, carCategory, freq(new double[] {500.0, 1000.0, 0.0}));

        // anonymous tactical-planner-factory supplier
        AbstractLaneBasedTacticalPlannerFactory<SteeringLmrs> car = new AbstractLaneBasedTacticalPlannerFactory<SteeringLmrs>(
                new IDMPlusFactory(sim.getModel().getStream("generation")), new DefaultLMRSPerceptionFactory())
        {
            @Override
            public SteeringLmrs create(final LaneBasedGTU gtu) throws GTUException
            {
                return new SteeringLmrs(nextCarFollowingModel(gtu), gtu, getPerceptionFactory().generatePerception(gtu),
                        Synchronization.PASSIVE, Cooperation.PASSIVE, GapAcceptance.INFORMED, FEEDBACK_CAR);
            }

            @Override
            public Parameters getParameters() throws ParameterException
            {
                // TODO: add parameters if required (run and wait for ParameterException to find missing parameters)
                ParameterSet parameters = new ParameterSet();
                getCarFollowingParameters().setAllIn(parameters);
                parameters.setDefaultParameters(Steering.class);
                return parameters;
            }
        };
        TacticalPlannerFactorySupplierOD tacticalPlannerFactorySupplierOD = new TacticalPlannerFactorySupplierOD()
        {
            @Override
            public LaneBasedTacticalPlannerFactory<SteeringLmrs> getFactory(final Node origin, final Node destination,
                    final Category category, final StreamInterface randomStream)
            {
                GTUType gtuType = category.get(GTUType.class);
                if (gtuType.equals(network.getGtuType(GTUType.DEFAULTS.CAR)))
                {
                    return car;
                }
                else
                {
                    // TODO: other GTU types
                    return null;
                }
            };
        };
        // anonymous vehicle model factory
        // TODO: supply mass and inertia values, possibly randomized, correlated?
        ContinuousDistMass massDistCar =
                new ContinuousDistMass(new DistUniform(sim.getModel().getStream("generation"), 600, 1200), MassUnit.SI);
        ContinuousDistMass massDistTruck =
                new ContinuousDistMass(new DistUniform(sim.getModel().getStream("generation"), 2000, 10000), MassUnit.SI);
        double momentOfInertiaAboutZ = 100; // no idea...
        VehicleModelFactory vehicleModelGenerator = new VehicleModelFactory()
        {
            @Override
            public VehicleModel create(final GTUType gtuType)
            {
                Mass mass =
                        gtuType.isOfType(network.getGtuType(GTUType.DEFAULTS.CAR)) ? massDistCar.draw() : massDistTruck.draw();
                return new VehicleModel.MassBased(mass, momentOfInertiaAboutZ);
            }
        };
        // characteristics generator using OD info and default route based strategical level
        DefaultGTUCharacteristicsGeneratorOD characteristicsGenerator = new DefaultGTUCharacteristicsGeneratorOD.Factory()
                .setFactorySupplier(StrategicalPlannerFactorySupplierOD.route(tacticalPlannerFactorySupplierOD))
                .setVehicleModelGenerator(vehicleModelGenerator).create();

        // od options
        ODOptions odOptions = new ODOptions().set(ODOptions.NO_LC_DIST, Length.instantiateSI(300.0)).set(ODOptions.GTU_TYPE,
                characteristicsGenerator);
        ODApplier.applyOD(network, odMatrix, odOptions);

        return network;
    }

    /**
     * Creates a frequency vector.
     * @param array double[]; array in veh/h
     * @return FrequencyVector; frequency vector
     * @throws ValueRuntimeException on problem
     */
    private FrequencyVector freq(final double[] array) throws ValueRuntimeException
    {
        return DoubleVector.instantiate(array, FrequencyUnit.PER_HOUR, StorageType.DENSE);
    }

}
