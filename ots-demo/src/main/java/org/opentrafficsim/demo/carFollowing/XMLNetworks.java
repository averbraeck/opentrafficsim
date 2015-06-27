package org.opentrafficsim.demo.carFollowing;

import java.awt.Container;
import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.naming.NamingException;
import javax.swing.JPanel;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistErlang;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.following.FixedAccelerationModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDM;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.lane.changing.AbstractLaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.FixedLaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.CrossSectionLink;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.lane.SinkLane;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.ProbabilisticFixedRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.graphs.LaneBasedGTUSampler;
import org.opentrafficsim.graphs.TrajectoryPlot;
import org.opentrafficsim.simulationengine.AbstractWrappableSimulation;
import org.opentrafficsim.simulationengine.WrappableSimulation;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.CompoundProperty;
import org.opentrafficsim.simulationengine.properties.ContinuousProperty;
import org.opentrafficsim.simulationengine.properties.IDMPropertySet;
import org.opentrafficsim.simulationengine.properties.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.properties.SelectionProperty;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 4 mrt. 2015 <br>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class XMLNetworks extends AbstractWrappableSimulation implements WrappableSimulation
{
    /** the model. */
    private XMLNetworkModel model;

    /**
     * Define the XMLNetworks.
     */
    public XMLNetworks()
    {
        this.properties.add(new SelectionProperty("Network", "Network", new String[] {"Merge 1 plus 1 into 1",
            "Merge 2 plus 1 into 2", "Merge 2 plus 2 into 4", "Split 1 into 1 plus 1", "Split 2 into 1 plus 2",
            "Split 4 into 2 plus 2"}, 0, false, 0));
        this.properties.add(new ContinuousProperty("Flow per input lane", "Traffic flow per input lane", 500d, 0d, 3000d,
            "%.0f veh/h", false, 1));
    }

    /** {@inheritDoc} */
    @Override
    public final void stopTimersThreads()
    {
        super.stopTimersThreads();
        this.model = null;
    }

    /** {@inheritDoc} */
    @Override
    protected final Rectangle2D.Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(-50, -300, 1300, 600);
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer)
    {
        this.model = new XMLNetworkModel(this.savedUserModifiedProperties, colorer);
        return this.model;
    }

    /** {@inheritDoc} */
    @Override
    protected final JPanel makeCharts()
    {
        int graphCount = this.model.pathCount();
        int columns = 1;
        int rows = 0 == columns ? 0 : (int) Math.ceil(graphCount * 1.0 / columns);
        TablePanel charts = new TablePanel(columns, rows);
        for (int graphIndex = 0; graphIndex < graphCount; graphIndex++)
        {
            TrajectoryPlot tp =
                new TrajectoryPlot("Trajectories on lane " + (graphIndex + 1), new DoubleScalar.Rel<TimeUnit>(0.5,
                    TimeUnit.SECOND), this.model.getPath(graphIndex));
            tp.setTitle("Trajectory Graph");
            tp.setExtendedState(Frame.MAXIMIZED_BOTH);
            LaneBasedGTUSampler graph = tp;
            Container container = tp.getContentPane();
            charts.setCell(container, graphIndex % columns, graphIndex / columns);
            this.model.getPlots().add(graph);
        }
        return charts;
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "Test networks";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "<html><h1>Test Networks</h1>Prove that the test networks can be constructed and rendered on screen "
            + "and that a mix of cars and trucks can run on them.<br/>On the statistics tab, a trajectory plot "
            + "is generated for each lane.</html>";
    }

}

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 4 mrt. 2015 <br>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class XMLNetworkModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20150304L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** The plots. */
    private ArrayList<LaneBasedGTUSampler> plots = new ArrayList<LaneBasedGTUSampler>();

    /** User settable properties. */
    private ArrayList<AbstractProperty<?>> properties = null;

    /** The sequence of Lanes that all vehicles will follow. */
    private ArrayList<List<Lane>> paths = new ArrayList<List<Lane>>();

    /** The average headway (inter-vehicle time). */
    private DoubleScalar.Rel<TimeUnit> averageHeadway;

    /** The minimum headway. */
    private DoubleScalar.Rel<TimeUnit> minimumHeadway;

    /** The probability distribution for the variable part of the headway. */
    private DistContinuous headwayGenerator;

    /** The speed limit. */
    private DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(60, SpeedUnit.KM_PER_HOUR);

    /** number of cars created. */
    private int carsCreated = 0;

    /** type of all GTUs (required to permit lane changing). */
    private GTUType<String> gtuType = GTUType.makeGTUType("Car");

    /** the car following model, e.g. IDM Plus for cars. */
    private GTUFollowingModel carFollowingModelCars;

    /** the car following model, e.g. IDM Plus for trucks. */
    private GTUFollowingModel carFollowingModelTrucks;

    /** The lane change model. */
    private AbstractLaneChangeModel laneChangeModel = new Egoistic();

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** The random number generator used to decide what kind of GTU to generate. */
    private Random randomGenerator = new Random(12346);

    /** The route generator. */
    private RouteGenerator routeGenerator;

    /** The GTUColorer for the generated vehicles. */
    private final GTUColorer gtuColorer;

    /**
     * @param userModifiedProperties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the (possibly user modified) properties
     * @param gtuColorer the default and initial GTUColorer, e.g. a DefaultSwitchableTUColorer.
     */
    public XMLNetworkModel(final ArrayList<AbstractProperty<?>> userModifiedProperties, final GTUColorer gtuColorer)
    {
        this.properties = userModifiedProperties;
        this.gtuColorer = gtuColorer;
    }

    /**
     * @param index int; the rank number of the path
     * @return List&lt;Lane&gt;; the set of lanes for the specified index
     */
    public final List<Lane> getPath(final int index)
    {
        return this.paths.get(index);
    }

    /**
     * Return the number of paths that can be used to show graphs.
     * @return int; the number of paths that can be used to show graphs
     */
    public final int pathCount()
    {
        return this.paths.size();
    }

    /**
     * @return plots
     */
    public final ArrayList<LaneBasedGTUSampler> getPlots()
    {
        return this.plots;
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel(
        final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
        throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        NodeGeotools.STR from = new NodeGeotools.STR("From", new Coordinate(0, 0, 0));
        NodeGeotools.STR end = new NodeGeotools.STR("End", new Coordinate(1200, 0, 0));
        NodeGeotools.STR from2 = new NodeGeotools.STR("From2", new Coordinate(0, -50, 0));
        NodeGeotools.STR firstVia = new NodeGeotools.STR("Via1", new Coordinate(800, 0, 0));
        NodeGeotools.STR end2 = new NodeGeotools.STR("End2", new Coordinate(1200, -50, 0));
        NodeGeotools.STR secondVia = new NodeGeotools.STR("Via2", new Coordinate(1000, 0, 0));
        CompoundProperty cp = new CompoundProperty("", "", this.properties, false, 0);
        String networkType = (String) cp.findByShortName("Network").getValue();
        boolean merge = networkType.startsWith("M");
        int lanesOnMain = Integer.parseInt(networkType.split(" ")[merge ? 1 : 5]);
        int lanesOnBranch = Integer.parseInt(networkType.split(" ")[3]);
        int lanesOnCommon = lanesOnMain + lanesOnBranch;
        int lanesOnCommonCompressed = Integer.parseInt(networkType.split(" ")[merge ? 5 : 1]);

        LaneType<String> laneType = new LaneType<String>("CarLane");
        laneType.addCompatibility(this.gtuType);
        try
        {
            String carFollowingModelName = null;
            CompoundProperty propertyContainer = new CompoundProperty("", "", this.properties, false, 0);
            AbstractProperty<?> cfmp = propertyContainer.findByShortName("Car following model");
            if (null == cfmp)
            {
                throw new Error("Cannot find \"Car following model\" property");
            }
            if (cfmp instanceof SelectionProperty)
            {
                carFollowingModelName = ((SelectionProperty) cfmp).getValue();
            }
            else
            {
                throw new Error("\"Car following model\" property has wrong type");
            }
            Iterator<AbstractProperty<ArrayList<AbstractProperty<?>>>> iterator =
                new CompoundProperty("", "", this.properties, false, 0).iterator();
            while (iterator.hasNext())
            {
                AbstractProperty<?> ap = iterator.next();
                if (ap instanceof SelectionProperty)
                {
                    SelectionProperty sp = (SelectionProperty) ap;
                    if ("Car following model".equals(sp.getShortName()))
                    {
                        carFollowingModelName = sp.getValue();
                    }
                }
                else if (ap instanceof ProbabilityDistributionProperty)
                {
                    ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) ap;
                    String modelName = ap.getShortName();
                    if (modelName.equals("Traffic composition"))
                    {
                        this.carProbability = pdp.getValue()[0];
                    }
                }
                else if (ap instanceof ContinuousProperty)
                {
                    ContinuousProperty contP = (ContinuousProperty) ap;
                    if (contP.getShortName().startsWith("Flow "))
                    {
                        this.averageHeadway = new DoubleScalar.Rel<TimeUnit>(3600.0 / contP.getValue(), TimeUnit.SECOND);
                        this.minimumHeadway = new DoubleScalar.Rel<TimeUnit>(3, TimeUnit.SECOND);
                        this.headwayGenerator =
                            new DistErlang(new MersenneTwister(1234), 4, DoubleScalar.minus(this.averageHeadway,
                                this.minimumHeadway).getSI());
                    }
                }
                else if (ap instanceof CompoundProperty)
                {
                    CompoundProperty compoundProperty = (CompoundProperty) ap;
                    if (ap.getShortName().equals("Output"))
                    {
                        continue; // Output settings are handled elsewhere
                    }
                    if (ap.getShortName().contains("IDM"))
                    {
                        DoubleScalar.Abs<AccelerationUnit> a = IDMPropertySet.getA(compoundProperty);
                        DoubleScalar.Abs<AccelerationUnit> b = IDMPropertySet.getB(compoundProperty);
                        DoubleScalar.Rel<LengthUnit> s0 = IDMPropertySet.getS0(compoundProperty);
                        DoubleScalar.Rel<TimeUnit> tSafe = IDMPropertySet.getTSafe(compoundProperty);
                        GTUFollowingModel gtuFollowingModel = null;
                        if (carFollowingModelName.equals("IDM"))
                        {
                            gtuFollowingModel = new IDM(a, b, s0, tSafe, 1.0);
                        }
                        else if (carFollowingModelName.equals("IDM+"))
                        {
                            gtuFollowingModel = new IDMPlus(a, b, s0, tSafe, 1.0);
                        }
                        else
                        {
                            throw new Error("Unknown gtu following model: " + carFollowingModelName);
                        }
                        if (ap.getShortName().contains(" Car "))
                        {
                            this.carFollowingModelCars = gtuFollowingModel;
                        }
                        else if (ap.getShortName().contains(" Truck "))
                        {
                            this.carFollowingModelTrucks = gtuFollowingModel;
                        }
                        else
                        {
                            throw new Error("Cannot determine gtu type for " + ap.getShortName());
                        }
                    }
                }
            }

            setupGenerator(LaneFactory.makeMultiLane("From to FirstVia", from, firstVia, null, merge ? lanesOnMain
                : lanesOnCommonCompressed, laneType, this.speedLimit, this.simulator));
            Lane[] common =
                LaneFactory.makeMultiLane("FirstVia to SecondVia", firstVia, secondVia, null, lanesOnCommon, laneType,
                    this.speedLimit, this.simulator);
            if (merge)
            {
                for (int i = lanesOnCommonCompressed; i < lanesOnCommon; i++)
                {
                    setupBlock(common[i]);
                }
            }
            setupSink(LaneFactory.makeMultiLane("SecondVia to end", secondVia, end, null, merge ? lanesOnCommonCompressed
                : lanesOnMain, laneType, this.speedLimit, this.simulator));
            if (merge)
            {
                setupGenerator(LaneFactory.makeMultiLane("From2 to FirstVia", from2, firstVia, null, lanesOnBranch, 0,
                    lanesOnCommon - lanesOnBranch, laneType, this.speedLimit, this.simulator));
                this.routeGenerator = new FixedRouteGenerator(new ArrayList<Node<?, ?>>());
            }
            else
            {
                setupSink(LaneFactory.makeMultiLane("SecondVia to end2", secondVia, end2, null, lanesOnBranch, lanesOnCommon
                    - lanesOnBranch, 0, laneType, this.speedLimit, this.simulator));
                SortedMap<RouteGenerator, java.lang.Double> routeProbabilities =
                    new TreeMap<RouteGenerator, java.lang.Double>();
                ArrayList<Node<?, ?>> mainRoute = new ArrayList<Node<?, ?>>();
                mainRoute.add(end);
                routeProbabilities.put(new FixedRouteGenerator(mainRoute), new java.lang.Double(lanesOnMain));
                ArrayList<Node<?, ?>> sideRoute = new ArrayList<Node<?, ?>>();
                sideRoute.add(end2);
                routeProbabilities.put(new FixedRouteGenerator(sideRoute), new java.lang.Double(lanesOnBranch));
                this.routeGenerator = new ProbabilisticFixedRouteGenerator(routeProbabilities, 1234);
            }
            for (int index = 0; index < lanesOnCommon; index++)
            {
                this.paths.add(new ArrayList<Lane>());
                Lane lane = common[index];
                // Follow back
                while (lane.prevLanes().size() > 0)
                {
                    if (lane.prevLanes().size() > 1)
                    {
                        throw new NetworkException("This network should not have lane merge points");
                    }
                    lane = lane.prevLanes().iterator().next();
                }
                // Follow forward
                while (true)
                {
                    this.paths.get(index).add(lane);
                    int branching = lane.nextLanes().size();
                    if (branching == 0)
                    {
                        break;
                    }
                    if (branching > 1)
                    {
                        throw new NetworkException("Thisnetwork should not have lane split points");
                    }
                    lane = lane.nextLanes().iterator().next();
                }
            }
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.999, TimeUnit.SECOND), this, this,
                "drawGraphs", null);
        }
        catch (NamingException | NetworkException | GTUException exception1)
        {
            exception1.printStackTrace();
        }
    }

    /**
     * Add a generator to an array of Lane.
     * @param lanes Lane[]; the lanes that must get a generator at the start
     * @return Lane[]; the lanes
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException on ???
     */
    private Lane[] setupGenerator(final Lane[] lanes) throws RemoteException, SimRuntimeException
    {
        for (Lane lane : lanes)
        {
            Object[] arguments = new Object[1];
            arguments[0] = lane;
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND), this, this, "generateCar",
                arguments);
        }
        return lanes;
    }

    /**
     * Append a sink to each lane of an array of Lanes.
     * @param lanes Lane[]; the array of lanes
     * @return Lane[]; the lanes
     * @throws NetworkException on network inconsistency
     */
    private Lane[] setupSink(final Lane[] lanes) throws NetworkException
    {
        CrossSectionLink<?, ?> link = lanes[0].getParentLink();
        NodeGeotools.STR to = (NodeGeotools.STR) link.getEndNode();
        NodeGeotools.STR from = (NodeGeotools.STR) link.getStartNode();
        double endLinkLength = 50; // [m]
        double endX = to.getX() + (endLinkLength / link.getLength().getSI()) * (to.getX() - from.getX());
        double endY = to.getY() + (endLinkLength / link.getLength().getSI()) * (to.getY() - from.getY());
        NodeGeotools.STR end = new NodeGeotools.STR("END", new Coordinate(endX, endY, to.getZ()));
        CrossSectionLink<?, ?> endLink = LaneFactory.makeLink("endLink", to, end, null);
        for (Lane lane : lanes)
        {
            new SinkLane(endLink, lane.getLateralCenterPosition(1.0), lane.getWidth(1.0), lane.getLaneType(),
                LongitudinalDirectionality.FORWARD, this.speedLimit);
        }
        return lanes;
    }

    /**
     * Put a block at the end of a Lane.
     * @param lane Lane; the lane on which the block is placed
     * @return Lane; the lane
     * @throws RemoteException on communications failure
     * @throws NamingException on ???
     * @throws NetworkException on network inconsistency
     * @throws SimRuntimeException on ???
     * @throws GTUException when construction of the GTU (the block is a GTU) fails
     */
    private Lane setupBlock(final Lane lane) throws RemoteException, NamingException, NetworkException, SimRuntimeException,
        GTUException
    {
        DoubleScalar.Rel<LengthUnit> initialPosition = lane.getLength();
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new LinkedHashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(lane, initialPosition);
        GTUFollowingModel gfm =
            new FixedAccelerationModel(new DoubleScalar.Abs<AccelerationUnit>(0, AccelerationUnit.SI),
                new DoubleScalar.Rel<TimeUnit>(java.lang.Double.MAX_VALUE, TimeUnit.SI));
        LaneChangeModel lcm = new FixedLaneChangeModel(null);
        new LaneBasedIndividualCar<Integer>(999999, this.gtuType, gfm, lcm, initialPositions,
            new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR),
            new DoubleScalar.Rel<LengthUnit>(1, LengthUnit.METER), lane.getWidth(1), new DoubleScalar.Abs<SpeedUnit>(0,
                SpeedUnit.KM_PER_HOUR), new Route(new ArrayList<Node<?, ?>>()), this.simulator);
        return lane;
    }

    /**
     * Notify the contour plots that the underlying data has changed.
     */
    protected final void drawGraphs()
    {
        for (LaneBasedGTUSampler plot : this.plots)
        {
            plot.reGraph();
        }
        // Re schedule this method
        try
        {
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(
                this.simulator.getSimulatorTime().get().getSI() + 1, TimeUnit.SECOND), this, this, "drawGraphs", null);
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }

    }

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     * @param lane Lane; the lane on which the generated cars are placed
     */
    protected final void generateCar(final Lane lane)
    {
        boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(50, SpeedUnit.KM_PER_HOUR);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new LinkedHashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(lane, initialPosition);
        try
        {
            DoubleScalar.Rel<LengthUnit> vehicleLength =
                new DoubleScalar.Rel<LengthUnit>(generateTruck ? 15 : 4, LengthUnit.METER);
            GTUFollowingModel gtuFollowingModel = generateTruck ? this.carFollowingModelTrucks : this.carFollowingModelCars;
            new LaneBasedIndividualCar<Integer>(++this.carsCreated, this.gtuType, gtuFollowingModel, this.laneChangeModel,
                initialPositions, initialSpeed, vehicleLength, new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER),
                new DoubleScalar.Abs<SpeedUnit>(200, SpeedUnit.KM_PER_HOUR), this.routeGenerator.generateRoute(),
                this.simulator, DefaultCarAnimation.class, this.gtuColorer);
            Object[] arguments = new Object[1];
            arguments[0] = lane;
            this.simulator.scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(this.headwayGenerator.draw(), TimeUnit.SECOND),
                this, this, "generateCar", arguments);
        }
        catch (RemoteException | SimRuntimeException | NamingException | NetworkException | GTUException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return this.simulator;
    }

}
