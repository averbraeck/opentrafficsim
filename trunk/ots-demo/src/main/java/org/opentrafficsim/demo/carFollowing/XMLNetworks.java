package org.opentrafficsim.demo.carFollowing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JPanel;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistErlang;
import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.animation.SwitchableGTUColorer;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.route.FixedRouteGenerator;
import org.opentrafficsim.core.network.route.ProbabilisticRouteGenerator;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.graphs.LaneBasedGTUSampler;
import org.opentrafficsim.graphs.TrajectoryPlot;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.generator.LaneBasedGTUGenerator;
import org.opentrafficsim.road.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.lane.LaneBasedTemplateGTUTypeDistribution;
import org.opentrafficsim.road.gtu.lane.driver.LaneBasedBehavioralCharacteristics;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingChange0TacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingLaneChangeTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.AbstractLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.FixedLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.Sensor;
import org.opentrafficsim.road.network.lane.SinkSensor;
import org.opentrafficsim.road.network.lane.changing.OvertakingConditions;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.CompoundProperty;
import org.opentrafficsim.simulationengine.properties.ContinuousProperty;
import org.opentrafficsim.simulationengine.properties.IDMPropertySet;
import org.opentrafficsim.simulationengine.properties.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.properties.SelectionProperty;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 4 mrt. 2015 <br>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class XMLNetworks extends AbstractWrappableAnimation implements UNITS
{
    /** the model. */
    private XMLNetworkModel model;

    /**
     * Define the XMLNetworks.
     */
    public XMLNetworks()
    {
        this.properties.add(new SelectionProperty("Network", "Network", new String[] { "Merge 1 plus 1 into 1",
                "Merge 2 plus 1 into 2", "Merge 2 plus 2 into 4", "Split 1 into 1 plus 1", "Split 2 into 1 plus 2",
                "Split 4 into 2 plus 2" }, 0, false, 0));
        this.properties.add(new SelectionProperty("Tactical planner",
                "<html>The tactical planner determines if a lane change is desired and possible.</html>", new String[] {
                        "MOBIL", "Verbraeck", "Verbraeck0" }, 0, false, 600));
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
                    new TrajectoryPlot("Trajectories on lane " + (graphIndex + 1), new Time.Rel(0.5, SECOND),
                            this.model.getPath(graphIndex));
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
                + "and that a mix of cars and trucks can run on them.<br>On the statistics tab, a trajectory plot "
                + "is generated for each lane.</html>";
    }

}

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version mrt. 2015 <br>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class XMLNetworkModel implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20150304L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** network. */
    private OTSNetwork network = new OTSNetwork("network");

    /** The plots. */
    private ArrayList<LaneBasedGTUSampler> plots = new ArrayList<LaneBasedGTUSampler>();

    /** User settable properties. */
    private ArrayList<AbstractProperty<?>> properties = null;

    /** The sequence of Lanes that all vehicles will follow. */
    private ArrayList<List<Lane>> paths = new ArrayList<List<Lane>>();

    /** The average headway (inter-vehicle time). */
    private Time.Rel averageHeadway;

    /** The minimum headway. */
    private Time.Rel minimumHeadway;

    /** The probability distribution for the variable part of the headway. */
    DistContinuous headwayGenerator;

    /** The speed limit. */
    private Speed speedLimit = new Speed(60, KM_PER_HOUR);

    /** number of cars created. */
    // private int carsCreated = 0;

    /** type of all GTUs (required to permit lane changing). */
    GTUType gtuType = GTUType.makeGTUType("Car");

    /** the car following model, e.g. IDM Plus for cars. */
    private GTUFollowingModelOld carFollowingModelCars;

    /** the car following model, e.g. IDM Plus for trucks. */
    private GTUFollowingModelOld carFollowingModelTrucks;

    /** The lane change model. */
    AbstractLaneChangeModel laneChangeModel = new Egoistic();

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** The random number generator used to decide what kind of GTU to generate. */
    // private Random randomGenerator = new Random(12346);

    /** disttria(70,80,100). */
    // private DistContinuous disttria = new DistTriangular(new MersenneTwister(), 70, 80, 100);

    /** The route generator. */
    RouteGenerator routeGenerator;

    /** The GTUColorer for the generated vehicles. */
    private final GTUColorer gtuColorer;

    /** The tactical planner that will be used by all GTUs. */
    TacticalPlanner tacticalPlanner = null;

    /** Id generator (used by all generators). */
    IdGenerator idGenerator = new IdGenerator("");

    /**
     * @param userModifiedProperties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the (possibly user modified) properties
     * @param gtuColorer the default and initial GTUColorer, e.g. a DefaultSwitchableTUColorer.
     */
    XMLNetworkModel(final ArrayList<AbstractProperty<?>> userModifiedProperties, final GTUColorer gtuColorer)
    {
        this.gtuColorer = gtuColorer;
        if (this.gtuColorer instanceof SwitchableGTUColorer)
        {
            // FIXME: How the hell can we get at the colorControlPanel?
            // It has not even been fully constructed yet; so we need a later opportunity to patch the gtuColorer
            // colorControlPanel.addItem(new DirectionGTUColorer());
        }

        this.properties = userModifiedProperties;
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
        OTSNode from = new OTSNode("From", new OTSPoint3D(0, 0, 0));
        OTSNode end = new OTSNode("End", new OTSPoint3D(2000, 0, 0));
        OTSNode from2a = new OTSNode("From2a", new OTSPoint3D(0, -50, 0));
        OTSNode from2b = new OTSNode("From2b", new OTSPoint3D(490, -2, 0));
        OTSNode firstVia = new OTSNode("Via1", new OTSPoint3D(500, 0, 0));
        OTSNode end2a = new OTSNode("End2a", new OTSPoint3D(1020, -2, 0));
        OTSNode end2b = new OTSNode("End2b", new OTSPoint3D(2000, -50, 0));
        OTSNode secondVia = new OTSNode("Via2", new OTSPoint3D(1000, 0, 0));
        CompoundProperty cp = new CompoundProperty("", "", this.properties, false, 0);
        String networkType = (String) cp.findByShortName("Network").getValue();
        boolean merge = networkType.startsWith("M");
        int lanesOnMain = Integer.parseInt(networkType.split(" ")[merge ? 1 : 5]);
        int lanesOnBranch = Integer.parseInt(networkType.split(" ")[3]);
        int lanesOnCommon = lanesOnMain + lanesOnBranch;
        int lanesOnCommonCompressed = Integer.parseInt(networkType.split(" ")[merge ? 5 : 1]);

        LaneType laneType = new LaneType("CarLane");
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
                    else if ("Tactical planner".equals(sp.getShortName()))
                    {
                        String tacticalPlannerName = sp.getValue();
                        if ("MOBIL".equals(tacticalPlannerName))
                        {
                            this.tacticalPlanner = new LaneBasedCFLCTacticalPlanner();
                        }
                        else if ("Verbraeck".equals(tacticalPlannerName))
                        {
                            this.tacticalPlanner = new LaneBasedGTUFollowingLaneChangeTacticalPlanner();
                        }
                        else if ("Verbraeck0".equals(tacticalPlannerName))
                        {
                            this.tacticalPlanner = new LaneBasedGTUFollowingChange0TacticalPlanner();
                        }
                        else
                        {
                            throw new Error("Don't know how to create a " + tacticalPlannerName + " tactical planner");
                        }
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
                        this.averageHeadway = new Time.Rel(3600.0 / contP.getValue(), SECOND);
                        this.minimumHeadway = new Time.Rel(3, SECOND);
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
                        Acceleration a = IDMPropertySet.getA(compoundProperty);
                        Acceleration b = IDMPropertySet.getB(compoundProperty);
                        Length.Rel s0 = IDMPropertySet.getS0(compoundProperty);
                        Time.Rel tSafe = IDMPropertySet.getTSafe(compoundProperty);
                        GTUFollowingModelOld gtuFollowingModel = null;
                        if (carFollowingModelName.equals("IDM"))
                        {
                            gtuFollowingModel = new IDMOld(a, b, s0, tSafe, 1.0);
                        }
                        else if (carFollowingModelName.equals("IDM+"))
                        {
                            gtuFollowingModel = new IDMPlusOld(a, b, s0, tSafe, 1.0);
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

            Lane[] startLanes =
                    LaneFactory.makeMultiLane("From to FirstVia", from, firstVia, null, merge ? lanesOnMain
                            : lanesOnCommonCompressed, laneType, this.speedLimit, this.simulator,
                            LongitudinalDirectionality.DIR_PLUS);
            setupGenerator(startLanes);
            Lane[] common =
                    LaneFactory.makeMultiLane("FirstVia to SecondVia", firstVia, secondVia, null, lanesOnCommon, laneType,
                            this.speedLimit, this.simulator, LongitudinalDirectionality.DIR_PLUS);
            if (merge)
            {
                for (int i = lanesOnCommonCompressed; i < lanesOnCommon; i++)
                {
                    setupBlock(common[i]);
                }
            }
            setupSink(LaneFactory.makeMultiLane("SecondVia to end", secondVia, end, null, merge ? lanesOnCommonCompressed
                    : lanesOnMain, laneType, this.speedLimit, this.simulator, LongitudinalDirectionality.DIR_PLUS), laneType);
            if (merge)
            {
                setupGenerator(LaneFactory.makeMultiLane("From2a to From2b", from2a, from2b, null, lanesOnBranch, 0,
                        lanesOnCommon - lanesOnBranch, laneType, this.speedLimit, this.simulator,
                        LongitudinalDirectionality.DIR_PLUS));
                LaneFactory.makeMultiLaneBezier("From2b to FirstVia", from2a, from2b, firstVia, secondVia, lanesOnBranch,
                        lanesOnCommon - lanesOnBranch, lanesOnCommon - lanesOnBranch, laneType, this.speedLimit,
                        this.simulator, LongitudinalDirectionality.DIR_PLUS);

                // provide a route -- at the merge point, the GTU can otherwise decide to "go back"
                ArrayList<Node> mainRouteNodes = new ArrayList<Node>();
                mainRouteNodes.add(firstVia);
                mainRouteNodes.add(secondVia);
                mainRouteNodes.add(end);
                Route mainRoute = new Route("main", mainRouteNodes);
                this.routeGenerator = new FixedRouteGenerator(mainRoute);
            }
            else
            {
                LaneFactory.makeMultiLaneBezier("SecondVia to end2a", firstVia, secondVia, end2a, end2b, lanesOnBranch,
                        lanesOnCommon - lanesOnBranch, lanesOnCommon - lanesOnBranch, laneType, this.speedLimit,
                        this.simulator, LongitudinalDirectionality.DIR_PLUS);
                setupSink(LaneFactory.makeMultiLane("end2a to end2b", end2a, end2b, null, lanesOnBranch, lanesOnCommon
                        - lanesOnBranch, 0, laneType, this.speedLimit, this.simulator, LongitudinalDirectionality.DIR_PLUS),
                        laneType);

                // determine the routes
                List<FrequencyAndObject<Route>> routeProbabilities = new ArrayList<>();

                ArrayList<Node> mainRouteNodes = new ArrayList<Node>();
                mainRouteNodes.add(firstVia);
                mainRouteNodes.add(secondVia);
                mainRouteNodes.add(end);
                Route mainRoute = new Route("main", mainRouteNodes);
                routeProbabilities.add(new FrequencyAndObject<Route>(lanesOnMain, mainRoute));

                ArrayList<Node> sideRouteNodes = new ArrayList<Node>();
                sideRouteNodes.add(firstVia);
                sideRouteNodes.add(secondVia);
                sideRouteNodes.add(end2a);
                sideRouteNodes.add(end2b);
                Route sideRoute = new Route("side", sideRouteNodes);
                routeProbabilities.add(new FrequencyAndObject<Route>(lanesOnBranch, sideRoute));
                try
                {
                    this.routeGenerator = new ProbabilisticRouteGenerator(routeProbabilities, new MersenneTwister(1234));
                }
                catch (ProbabilityException exception)
                {
                    exception.printStackTrace();
                }
            }

            for (int index = 0; index < lanesOnCommon; index++)
            {
                this.paths.add(new ArrayList<Lane>());
                Lane lane = common[index];
                // Follow back
                while (lane.prevLanes(this.gtuType).size() > 0)
                {
                    if (lane.prevLanes(this.gtuType).size() > 1)
                    {
                        throw new NetworkException("This network should not have lane merge points");
                    }
                    lane = lane.prevLanes(this.gtuType).keySet().iterator().next();
                }
                // Follow forward
                while (true)
                {
                    this.paths.get(index).add(lane);
                    int branching = lane.nextLanes(this.gtuType).size();
                    if (branching == 0)
                    {
                        break;
                    }
                    if (branching > 1)
                    {
                        throw new NetworkException("This network should not have lane split points");
                    }
                    lane = lane.nextLanes(this.gtuType).keySet().iterator().next();
                }
            }
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.999, SECOND), this, this, "drawGraphs", null);
        }
        catch (NamingException | NetworkException | GTUException | OTSGeometryException | ProbabilityException exception1)
        {
            exception1.printStackTrace();
        }
    }

    /**
     * Add a generator to an array of Lane.
     * @param lanes Lane[]; the lanes that must get a generator at the start
     * @return Lane[]; the lanes
     * @throws SimRuntimeException on ???
     * @throws GTUException
     * @throws ProbabilityException
     */
    private Lane[] setupGenerator(final Lane[] lanes) throws SimRuntimeException, GTUException, ProbabilityException
    {
        for (Lane lane : lanes)
        {
            makeGenerator(lane);

            // Object[] arguments = new Object[1];
            // arguments[0] = lane;
            // this.simulator.scheduleEventAbs(new Time.Abs(0.0, SECOND), this, this, "generateCar", arguments);
        }
        return lanes;
    }

    /**
     * Build a generator.
     * @param lane Lane; the lane on which the generated GTUs are placed
     * @return LaneBasedGTUGenerator
     * @throws GTUException
     * @throws SimRuntimeException
     * @throws ProbabilityException
     */
    private LaneBasedGTUGenerator makeGenerator(final Lane lane) throws GTUException, SimRuntimeException, ProbabilityException
    {
        StreamInterface stream = new MersenneTwister(1234); // Use a fixed seed for the demos
        Distribution<LaneBasedTemplateGTUType> distribution = new Distribution<LaneBasedTemplateGTUType>(stream);
        Length.Rel initialPosition = new Length.Rel(16, METER);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));

        LaneBasedTemplateGTUType template =
                makeTemplate(stream, lane, new ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit>(new DistUniform(stream,
                        3, 6), METER), new ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit>(new DistUniform(stream, 1.6,
                        2.0), METER), new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistUniform(stream, 140, 180),
                        KM_PER_HOUR), new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistUniform(stream, 100, 125),
                        KM_PER_HOUR), initialPositions, this.carFollowingModelCars);
        // System.out.println("Constructed template " + template);
        distribution.add(new FrequencyAndObject<LaneBasedTemplateGTUType>(this.carProbability, template));
        template =
                makeTemplate(stream, lane, new ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit>(new DistUniform(stream,
                        8, 14), METER), new ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit>(new DistUniform(stream, 2.0,
                        2.5), METER), new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistUniform(stream, 100, 140),
                        KM_PER_HOUR), new ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit>(new DistUniform(stream, 80, 90),
                        KM_PER_HOUR), initialPositions, this.carFollowingModelTrucks);
        // System.out.println("Constructed template " + template);
        distribution.add(new FrequencyAndObject<LaneBasedTemplateGTUType>(1.0 - this.carProbability, template));
        LaneBasedTemplateGTUTypeDistribution templateDistribution = new LaneBasedTemplateGTUTypeDistribution(distribution);
        LaneBasedGTUGenerator.RoomChecker roomChecker = new CanPlaceDemoCode();
        return new LaneBasedGTUGenerator(lane.getId(), new Generator<Time.Rel>()
        {
            public Time.Rel draw()
            {
                return new Time.Rel(XMLNetworkModel.this.headwayGenerator.draw(), TimeUnit.SECOND);
            }
        }, Long.MAX_VALUE, new Time.Abs(0, TimeUnit.SI), new Time.Abs(Double.MAX_VALUE, TimeUnit.SI), this.gtuColorer,
                templateDistribution, initialPositions, this.network,
                /*-
                new LaneBasedGTUGenerator.RoomChecker()
                {
                    @Override
                    public Speed canPlace(Speed leaderSpeed, org.djunits.value.vdouble.scalar.Length.Rel headway,
                            LaneBasedGTUCharacteristics laneBasedGTUCharacteristics) throws NetworkException
                    {
                        // This implementation simply returns null if the headway is less than the headway wanted for driving at
                        // the current speed of the leader
                        if (headway.lt(laneBasedGTUCharacteristics
                                .getStrategicalPlanner()
                                .getDrivingCharacteristics()
                                .getGTUFollowingModel()
                                .minimumHeadway(leaderSpeed, leaderSpeed, new Length.Rel(0.1, LengthUnit.METER),
                                        new Length.Rel(Double.MAX_VALUE, LengthUnit.SI),
                                        lane.getSpeedLimit(XMLNetworkModel.this.gtuType),
                                        laneBasedGTUCharacteristics.getMaximumVelocity())))
                        {
                            return null;
                        }
                        return leaderSpeed;
                    }
                }
                 */
                roomChecker);
    }

    /**
     * @param stream
     * @param lane
     * @param lengthDistribution 
     * @param widthDistribution 
     * @param maximumVelocityDistribution 
     * @param initialSpeedDistribution 
     * @param initialPositions 
     * @param gtuFollowingModel 
     * @return x
     * @throws GTUException
     */
    LaneBasedTemplateGTUType makeTemplate(final StreamInterface stream, final Lane lane,
            final ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> lengthDistribution,
            final ContinuousDistDoubleScalar.Rel<Length.Rel, LengthUnit> widthDistribution,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumVelocityDistribution,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> initialSpeedDistribution,
            Set<DirectedLanePosition> initialPositions, final GTUFollowingModelOld gtuFollowingModel) throws GTUException
    {
        return new LaneBasedTemplateGTUType(this.gtuType.getId(), this.idGenerator, new Generator<Length.Rel>()
        {
            public Length.Rel draw()
            {
                return lengthDistribution.draw();
            }
        }, new Generator<Length.Rel>()
        {
            public Length.Rel draw()
            {
                return widthDistribution.draw();
            }
        }, new Generator<Speed>()
        {
            public Speed draw()
            {
                return maximumVelocityDistribution.draw();
            }
        }, this.simulator, new Generator<LaneBasedStrategicalPlanner>()
        {
            public LaneBasedStrategicalPlanner draw() throws ProbabilityException
            {
                LaneBasedBehavioralCharacteristics drivingCharacteristics =
                        new LaneBasedBehavioralCharacteristics(gtuFollowingModel, XMLNetworkModel.this.laneChangeModel);
                drivingCharacteristics.setForwardHeadwayDistance(new Length.Rel(450.0, LengthUnit.METER));

                return new LaneBasedStrategicalRoutePlanner(drivingCharacteristics, XMLNetworkModel.this.tacticalPlanner,
                        XMLNetworkModel.this.routeGenerator.draw());
            }
        }, new Generator<LanePerceptionFull>()
        {
            public LanePerceptionFull draw()
            {
                return new LanePerceptionFull();
            }
        }, initialPositions, new Generator<Speed>()
        {
            public Speed draw()
            {
                return initialSpeedDistribution.draw();
            }
        }, this.network);

    }

    /**
     * Append a sink to each lane of an array of Lanes.
     * @param lanes Lane[]; the array of lanes
     * @param laneType the LaneType for cars
     * @return Lane[]; the lanes
     * @throws NetworkException on network inconsistency
     * @throws OTSGeometryException on problem making the path for a link
     */
    private Lane[] setupSink(final Lane[] lanes, final LaneType laneType) throws NetworkException, OTSGeometryException
    {
        CrossSectionLink link = lanes[0].getParentLink();
        OTSNode to = link.getEndNode();
        OTSNode from = link.getStartNode();
        double endLinkLength = 50; // [m]
        double endX = to.getPoint().x + (endLinkLength / link.getLength().getSI()) * (to.getPoint().x - from.getPoint().x);
        double endY = to.getPoint().y + (endLinkLength / link.getLength().getSI()) * (to.getPoint().y - from.getPoint().y);
        OTSNode end = new OTSNode("END", new OTSPoint3D(endX, endY, to.getPoint().z));
        CrossSectionLink endLink = LaneFactory.makeLink("endLink", to, end, null, LongitudinalDirectionality.DIR_PLUS);
        for (Lane lane : lanes)
        {
            // Overtaking left and right allowed on the sinkLane
            Lane sinkLane =
                    new Lane(endLink, lane.getId() + "." + "sinkLane", lane.getLateralCenterPosition(1.0),
                            lane.getLateralCenterPosition(1.0), lane.getWidth(1.0), lane.getWidth(1.0), laneType,
                            LongitudinalDirectionality.DIR_PLUS, this.speedLimit, new OvertakingConditions.LeftAndRight());
            Sensor sensor = new SinkSensor(sinkLane, new Length.Rel(10.0, METER), this.simulator);
            sinkLane.addSensor(sensor, GTUType.ALL);
        }
        return lanes;
    }

    /**
     * Put a block at the end of a Lane.
     * @param lane Lane; the lane on which the block is placed
     * @return Lane; the lane
     * @throws NamingException on ???
     * @throws NetworkException on network inconsistency
     * @throws SimRuntimeException on ???
     * @throws GTUException when construction of the GTU (the block is a GTU) fails
     * @throws OTSGeometryException when the initial path is wrong
     */
    private Lane setupBlock(final Lane lane) throws NamingException, NetworkException, SimRuntimeException, GTUException,
            OTSGeometryException
    {
        Length.Rel initialPosition = lane.getLength();
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
        GTUFollowingModelOld gfm =
                new FixedAccelerationModel(new Acceleration(0, AccelerationUnit.SI), new Time.Rel(java.lang.Double.MAX_VALUE,
                        TimeUnit.SI));
        LaneChangeModel lcm = new FixedLaneChangeModel(null);
        LaneBasedBehavioralCharacteristics drivingCharacteristics = new LaneBasedBehavioralCharacteristics(gfm, lcm);
        LaneBasedStrategicalPlanner strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(drivingCharacteristics, this.tacticalPlanner);
        // new LaneBasedCFLCTacticalPlanner());
        new LaneBasedIndividualGTU("999999", this.gtuType, initialPositions, new Speed(0.0, KM_PER_HOUR), new Length.Rel(1,
                METER), lane.getWidth(1), new Speed(0.0, KM_PER_HOUR), this.simulator, strategicalPlanner,
                new LanePerceptionFull(), DefaultCarAnimation.class, this.gtuColorer, this.network);
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
            this.simulator.scheduleEventAbs(new Time.Abs(this.simulator.getSimulatorTime().get().getSI() + 1, SECOND), this,
                    this, "drawGraphs", null);
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }

    }

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     * @param lane Lane; the lane on which the generated cars are placed
     */
    // protected final void generateCar(final Lane lane)
    // {
    // Length.Rel initialPosition = new Length.Rel(16, METER);
    // Speed initialSpeed = new Speed(50, KM_PER_HOUR);
    // boolean generate = true;
    // // Check if there is sufficient room
    // // Find the first vehicle on the lane
    // LaneBasedGTU leader = null;
    // Time.Abs when = new Time.Abs(this.simulator.getSimulatorTime().get().si, TimeUnit.SI);
    // try
    // {
    // leader = lane.getGtuAhead(initialPosition, GTUDirectionality.DIR_PLUS, RelativePosition.REAR, when);
    // if (null != leader)
    // {
    // double headway =
    // leader.fractionalPosition(lane, leader.getRear()) * lane.getLength().si - initialPosition.si - 15.0 / 2;
    // if (headway < 0.1)
    // {
    // System.out.println("Not generating GTU due to insufficient room");
    // generate = false;
    // }
    // double leaderSpeed = leader.getVelocity().si;
    // if (leaderSpeed < initialSpeed.si)
    // {
    // // What distance will it take to reduce speed to 0 with a decent deceleration?
    // double decentDeceleration = 5; // [m/s/s]
    // double deltaT = initialSpeed.si / decentDeceleration;
    // double distance = 0.5 * decentDeceleration * deltaT * deltaT;
    // if (distance > headway)
    // {
    // System.out.println("Not generating GTU due to slow driving GTU within emergency stop range");
    // generate = false;
    // }
    // }
    // }
    // }
    // catch (GTUException exception1)
    // {
    // exception1.printStackTrace();
    // }
    // try
    // {
    // if (generate)
    // {
    // boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
    // Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
    // initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
    // Length.Rel vehicleLength = new Length.Rel(generateTruck ? 15 : 4, METER);
    // GTUFollowingModel gtuFollowingModel = generateTruck ? this.carFollowingModelTrucks : this.carFollowingModelCars;
    // double speed = this.disttria.draw();
    //
    // LaneBasedDrivingCharacteristics drivingCharacteristics =
    // new LaneBasedDrivingCharacteristics(gtuFollowingModel, this.laneChangeModel);
    // drivingCharacteristics.setForwardHeadwayDistance(new Length.Rel(450.0, LengthUnit.METER));
    // LaneBasedStrategicalPlanner strategicalPlanner =
    // new LaneBasedStrategicalRoutePlanner(drivingCharacteristics, this.tacticalPlanner,
    // this.routeGenerator.draw());
    // new LaneBasedIndividualGTU("" + (++this.carsCreated), this.gtuType, initialPositions, initialSpeed,
    // vehicleLength, new Length.Rel(1.8, METER), new Speed(speed, KM_PER_HOUR), this.simulator,
    // strategicalPlanner, new LanePerceptionFull(), DefaultCarAnimation.class, this.gtuColorer, this.network);
    // }
    // Object[] arguments = new Object[1];
    // arguments[0] = lane;
    // this.simulator.scheduleEventRel(new Time.Rel(this.headwayGenerator.draw(), SECOND), this, this, "generateCar",
    // arguments);
    // }
    // catch (SimRuntimeException | NamingException | NetworkException | GTUException | OTSGeometryException
    // | ProbabilityException exception)
    // {
    // exception.printStackTrace();
    // }
    // }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
            throws RemoteException
    {
        return this.simulator;
    }

    /**
     * The route colorer to show whether GTUs stay on the main route or go right at the split.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version Jan 3, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    private class DirectionGTUColorer implements GTUColorer
    {
        /** the legend. */
        private List<LegendEntry> legend = new ArrayList<>();

        /** */
        DirectionGTUColorer()
        {
            super();
            this.legend.add(new LegendEntry(Color.RED, "Right", "Go right"));
            this.legend.add(new LegendEntry(Color.BLUE, "Main", "Main route"));
        }

        /** {@inheritDoc} */
        @Override
        public Color getColor(final GTU gtu)
        {
            AbstractLaneBasedGTU laneBasedGTU = (AbstractLaneBasedGTU) gtu;
            Route route = ((LaneBasedStrategicalRoutePlanner) laneBasedGTU.getStrategicalPlanner()).getRoute();
            if (route == null)
            {
                return Color.black;
            }
            if (route.toString().toLowerCase().contains("end2"))
            {
                return Color.red;
            }
            if (route.toString().toLowerCase().contains("end"))
            {
                return Color.blue;
            }
            return Color.black;
        }

        /** {@inheritDoc} */
        @Override
        public List<LegendEntry> getLegend()
        {
            return this.legend;
        }
    }
}
