package org.opentrafficsim.demo.carFollowing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.CompoundProperty;
import org.opentrafficsim.base.modelproperties.ContinuousProperty;
import org.opentrafficsim.base.modelproperties.ProbabilityDistributionProperty;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.base.modelproperties.SelectionProperty;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.network.route.RouteGenerator;
import org.opentrafficsim.core.units.distributions.ContinuousDistDoubleScalar;
import org.opentrafficsim.graphs.LaneBasedGTUSampler;
import org.opentrafficsim.graphs.TrajectoryPlot;
import org.opentrafficsim.road.animation.AnimationToggles;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedTemplateGTUType;
import org.opentrafficsim.road.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.AbstractLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Altruistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.DefaultLMRSPerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.lane.tactical.toledo.ToledoFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.modelproperties.IDMPropertySet;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.SimpleSimulatorInterface;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.distributions.DistErlang;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-12-13 02:02:22 +0100 (Tue, 13 Dec 2016) $, @version $Revision: 2930 $, by $Author: wjschakel $,
 * initial version 4 mrt. 2015 <br>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class XMLNetworks2 extends AbstractWrappableAnimation implements UNITS
{
    /** */
    private static final long serialVersionUID = 20160422L;

    /** The model. */
    private XMLNetwork2Model model;

    /**
     * Define the XMLNetworks.
     */
    public XMLNetworks2()
    {
        this.properties.add(new SelectionProperty(
                "Network", "Network", "Network", new String[] { "Merge 1 plus 1 into 1", "Merge 2 plus 1 into 2",
                        "Merge 2 plus 2 into 4", "Split 1 into 1 plus 1", "Split 2 into 1 plus 2", "Split 4 into 2 plus 2" },
                0, false, 0));
        this.properties.add(new SelectionProperty("TacticalPlanner", "Tactical planner",
                "<html>The tactical planner determines if a lane change is desired and possible.</html>",
                new String[] { "MOBIL/IDM", "DIRECTED/IDM", "LMRS", "Toledo" }, 0, false, 600));
        this.properties.add(new SelectionProperty("LaneChanging", "Lane changing",
                "<html>The lane change friendliness (if used -- eg just for MOBIL.</html>",
                new String[] { "Egoistic", "Altruistic" }, 0, false, 600));
        this.properties.add(new ContinuousProperty("FlowPerInputLane", "Flow per input lane", "Traffic flow per input lane",
                500d, 0d, 3000d, "%.0f veh/h", false, 1));
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
    protected final void addAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesStandard(this);
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel()
    {
        this.model = new XMLNetwork2Model(this.savedUserModifiedProperties);
        return this.model;
    }

    /** {@inheritDoc} */
    @Override
    protected final void addTabs(final SimpleSimulatorInterface simulator)
    {
        int graphCount = this.model.pathCount();
        int columns = 1;
        int rows = 0 == columns ? 0 : (int) Math.ceil(graphCount * 1.0 / columns);
        TablePanel charts = new TablePanel(columns, rows);
        for (int graphIndex = 0; graphIndex < graphCount; graphIndex++)
        {
            TrajectoryPlot tp = new TrajectoryPlot("Trajectories on lane " + (graphIndex + 1), new Duration(0.5, SECOND),
                    this.model.getPath(graphIndex), simulator);
            tp.setTitle("Trajectory Graph");
            tp.setExtendedState(Frame.MAXIMIZED_BOTH);
            LaneBasedGTUSampler graph = tp;
            Container container = tp.getContentPane();
            charts.setCell(container, graphIndex % columns, graphIndex / columns);
            this.model.getPlots().add(graph);
        }
        addTab(getTabCount(), "statistics", charts);
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "Test networks - XML version";
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
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2016-12-13 02:02:22 +0100 (Tue, 13 Dec 2016) $, @version $Revision: 2930 $, by $Author: wjschakel $,
 * initial version mrt. 2015 <br>
 * @author <a href="http://Hansvanlint.weblog.tudelft.nl">Hans van Lint</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class XMLNetwork2Model implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20150304L;

    /** The simulator. */
    private DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** The network. */
    private final OTSNetwork network = new OTSNetwork("network");

    /** The plots. */
    private List<LaneBasedGTUSampler> plots = new ArrayList<>();

    /** User settable properties. */
    private List<Property<?>> properties = null;

    /** The sequence of Lanes that all vehicles will follow. */
    private List<List<Lane>> paths = new ArrayList<>();

    /** The average headway (inter-vehicle time). */
    private Duration averageHeadway;

    /** The minimum headway. */
    private Duration minimumHeadway;

    /** The probability distribution for the variable part of the headway. */
    DistContinuous headwayGenerator;

    /** The speed limit. */
    private Speed speedLimit = new Speed(60, KM_PER_HOUR);

    /** Number of cars created. */
    // private int carsCreated = 0;

    /** Type of all GTUs (required to permit lane changing). */
    GTUType gtuType = GTUType.CAR;

    /** The car following model, e.g. IDM Plus for cars. */
    private GTUFollowingModelOld carFollowingModelCars;

    /** The car following model, e.g. IDM Plus for trucks. */
    private GTUFollowingModelOld carFollowingModelTrucks;

    /** The lane change model. */
    AbstractLaneChangeModel laneChangeModel = new Egoistic();

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** Random stream. */
    private StreamInterface stream = new MersenneTwister(12346);

    /** The random number generator used to decide what kind of GTU to generate. */
    // private Random randomGenerator = new Random(12346);

    /** Probability distribution disttria(70,80,100). */
    // private DistContinuous disttria = new DistTriangular(new MersenneTwister(), 70, 80, 100);

    /** The route generator. */
    private RouteGenerator routeGenerator;

    /** Strategical planner generator for cars. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorCars = null;

    /** Strategical planner generator for cars. */
    private LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerGeneratorTrucks = null;

    /**
     * @param userModifiedProperties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the (possibly user modified) properties
     */
    XMLNetwork2Model(final List<Property<?>> userModifiedProperties)
    {
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
    public final List<LaneBasedGTUSampler> getPlots()
    {
        return this.plots;
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel(final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> theSimulator)
            throws SimRuntimeException
    {
        this.simulator = (DEVSSimulatorInterface.TimeDoubleUnit) theSimulator;
        try
        {
            CompoundProperty cp = new CompoundProperty("", "", "", this.properties, false, 0);
            String networkType = (String) cp.findByKey("Network").getValue();
            boolean merge = networkType.startsWith("M");
            int lanesOnMain = Integer.parseInt(networkType.split(" ")[merge ? 1 : 5]);
            int lanesOnBranch = Integer.parseInt(networkType.split(" ")[3]);
            int lanesOnCommon = lanesOnMain + lanesOnBranch;
            int lanesOnCommonCompressed = Integer.parseInt(networkType.split(" ")[merge ? 5 : 1]);

            LaneType laneType = LaneType.TWO_WAY_LANE;
            // Get car-following model name
            String carFollowingModelName = null;
            CompoundProperty propertyContainer = new CompoundProperty("", "", "", this.properties, false, 0);
            Property<?> cfmp = propertyContainer.findByKey("CarFollowingModel");
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

            // Get car-following model parameter
            for (Property<?> ap : new CompoundProperty("", "", "", this.properties, false, 0))
            {
                if (ap instanceof CompoundProperty)
                {
                    cp = (CompoundProperty) ap;
                    if (ap.getKey().contains("IDM"))
                    {
                        // System.out.println("Car following model name appears to be " + ap.getKey());
                        Acceleration a = IDMPropertySet.getA(cp);
                        Acceleration b = IDMPropertySet.getB(cp);
                        Length s0 = IDMPropertySet.getS0(cp);
                        Duration tSafe = IDMPropertySet.getTSafe(cp);
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
                        if (ap.getKey().contains("Car"))
                        {
                            this.carFollowingModelCars = gtuFollowingModel;
                        }
                        else if (ap.getKey().contains("Truck"))
                        {
                            this.carFollowingModelTrucks = gtuFollowingModel;
                        }
                        else
                        {
                            throw new Error("Cannot determine gtu type for " + ap.getKey());
                        }
                    }
                }
            }

            // Get lane change model
            cfmp = propertyContainer.findByKey("LaneChanging");
            if (null == cfmp)
            {
                throw new Error("Cannot find \"Lane changing\" property");
            }
            if (cfmp instanceof SelectionProperty)
            {
                String laneChangeModelName = ((SelectionProperty) cfmp).getValue();
                if ("Egoistic".equals(laneChangeModelName))
                {
                    this.laneChangeModel = new Egoistic();
                }
                else if ("Altruistic".equals(laneChangeModelName))
                {
                    this.laneChangeModel = new Altruistic();
                }
                else
                {
                    throw new Error("Lane changing " + laneChangeModelName + " not implemented");
                }
            }
            else
            {
                throw new Error("\"Lane changing\" property has wrong type");
            }

            StringBuilder xmlCode = new StringBuilder();
            xmlCode.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE xml>\n");
            xmlCode.append("<NETWORK xmlns=\"http://www.opentrafficsim.org/ots\" "
                    + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
            xmlCode.append("\txsi:schemaLocation=\"http://www.opentrafficsim.org/ots "
                    + "http://www.opentrafficsim.org/docs/current/ots-network.xsd\">\n\n");
            xmlCode.append("\t<DEFINITIONS>\n\n");
            xmlCode.append("\t<GLOBAL />\n\n");
            xmlCode.append("\t\t<GTUTYPE NAME=\"CAR\" />\n");
            xmlCode.append("\t\t<GTUTYPE NAME=\"TRUCK\" />\n\n");
            xmlCode.append("\t\t<GTU NAME=\"CAR\" GTUTYPE=\"CAR\" LENGTH=\"UNIF(4,7) m\" WIDTH=\"UNIF(1.7, 2) m\" "
                    + "MAXSPEED=\"CONST(120) km/h\" />\n");
            xmlCode.append("\t\t<GTU NAME=\"TRUCK\" GTUTYPE=\"TRUCK\" LENGTH=\"UNIF(16,24) m\" WIDTH=\"UNIF(2.2, 2.7) m\" "
                    + "MAXSPEED=\"CONST(100) km/h\" />\n\n");
            xmlCode.append("\t\t<GTUMIX NAME=\"gtumix\">\n");
            xmlCode.append("\t\t\t<GTU NAME=\"CAR\" WEIGHT=\"" + (100 * this.carProbability) + "\"></GTU>\n");
            xmlCode.append("\t\t\t<GTU NAME=\"TRUCK\" WEIGHT=\"" + (100 - 100 * this.carProbability) + "\"></GTU>\n");
            xmlCode.append("\t\t</GTUMIX>\n\n");

            xmlCode.append("\t\t<ROADTYPE NAME=\"NORMALROAD\" DEFAULTLANEWIDTH=\"3.5m\" DEFAULTLANEKEEPING=\"KEEPRIGHT\"\n"
                    + "\t\t\tDEFAULTOVERTAKING=\"LEFTSET([CAR, TRUCK] OVERTAKE [ALL]) RIGHTSPEED(40 km/h)\">\n");
            xmlCode.append("\t\t\t<SPEEDLIMIT GTUTYPE=\"CAR\" LEGALSPEEDLIMIT=\"60km/h\" />\n");
            xmlCode.append("\t\t</ROADTYPE>\n");

            xmlCode.append(makeRoadLayout("BRANCH", lanesOnBranch));
            xmlCode.append(makeRoadLayout("MAIN", lanesOnMain));
            xmlCode.append(makeRoadLayout("COMMON", lanesOnCommon));
            xmlCode.append(makeRoadLayout("COMMONCOMPRESSED", lanesOnCommonCompressed));

            xmlCode.append("\t</DEFINITIONS>\n");
            xmlCode.append("\t<NODE NAME=\"From\" COORDINATE=\"(0,0,0)\" ANGLE=\"0 deg\" />\n");
            xmlCode.append("\t<NODE NAME=\"End\" COORDINATE=\"(2000,0,0)\" ANGLE=\"0 deg\" />\n");
            xmlCode.append("\t<NODE NAME=\"From2\" COORDINATE=\"(0,-50,0)\" ANGLE=\"0 deg\" />\n");
            xmlCode.append("\t<NODE NAME=\"FirstVia\" COORDINATE=\"(500,0,0)\" ANGLE=\"0 deg\" />\n");
            xmlCode.append("\t<NODE NAME=\"SecondVia\" COORDINATE=\"(1000,0,0)\" ANGLE=\"0 deg\" />\n");
            xmlCode.append("\t<NODE NAME=\"End2\" COORDINATE=\"(2000,-50,0)\" ANGLE=\"0 deg\" />\n");

            // OTSNode from = new OTSNode(this.network, "From", new OTSPoint3D(0, 0, 0));
            // OTSNode end = new OTSNode(this.network, "End", new OTSPoint3D(2000, 0, 0));
            // OTSNode from2a = new OTSNode(this.network, "From2a", new OTSPoint3D(0, -50, 0));
            // OTSNode from2b = new OTSNode(this.network, "From2b", new OTSPoint3D(490, -2, 0));
            // OTSNode firstVia = new OTSNode(this.network, "FirstVia", new OTSPoint3D(500, 0, 0));
            // OTSNode end2a = new OTSNode(this.network, "End2a", new OTSPoint3D(1020, -2, 0));
            // OTSNode end2b = new OTSNode(this.network, "End2b", new OTSPoint3D(2000, -50, 0));
            // OTSNode secondVia = new OTSNode(this.network, "SecondVia", new OTSPoint3D(1000, 0, 0));
            if (merge)
            {
                // provide a route -- at the merge point, the GTU can otherwise decide to "go back"
                xmlCode.append("\t<ROUTE NAME=\"ALL\" NODELIST=\"FirstVia SecondVia End\" />");
                xmlCode.append("\t<ROUTEMIX NAME=\"routemix\" >\n");
                xmlCode.append("\t\t<ROUTE WEIGHT=\"1\" NAME=\"ALL\" />\n");
                xmlCode.append("\t</ROUTEMIX>\n");
                // ArrayList<Node> mainRouteNodes = new ArrayList<>();
                // mainRouteNodes.add(firstVia);
                // mainRouteNodes.add(secondVia);
                // mainRouteNodes.add(end);
                // Route mainRoute = new Route("main", mainRouteNodes);
                // this.routeGenerator = new FixedRouteGenerator(mainRoute);
            }
            else
            {
                // determine the routes
                xmlCode.append("\t<ROUTE NAME=\"From_End\" NODELIST=\"FirstVia SecondVia End\" />\n");
                xmlCode.append("\t<ROUTE NAME=\"From_End2\" NODELIST=\"FirstVia SecondVia End2\" />\n");
                xmlCode.append("\t<ROUTEMIX NAME=\"routemix\" >\n");
                xmlCode.append("\t\t<ROUTE WEIGHT=\"" + lanesOnMain + "\" NAME=\"toEnd\" />\n");
                xmlCode.append("\t\t<ROUTE WEIGHT=\"" + lanesOnBranch + "\" NAME=\"toEnd2\" />\n");
                xmlCode.append("\t</ROUTEMIX>\n");

                // List<FrequencyAndObject<Route>> routeProbabilities = new ArrayList<>();
                //
                // ArrayList<Node> mainRouteNodes = new ArrayList<>();
                // mainRouteNodes.add(firstVia);
                // mainRouteNodes.add(secondVia);
                // mainRouteNodes.add(end);
                // Route mainRoute = new Route("main", mainRouteNodes);
                // routeProbabilities.add(new FrequencyAndObject<>(lanesOnMain, mainRoute));
                //
                // ArrayList<Node> sideRouteNodes = new ArrayList<>();
                // sideRouteNodes.add(firstVia);
                // sideRouteNodes.add(secondVia);
                // sideRouteNodes.add(end2a);
                // sideRouteNodes.add(end2b);
                // Route sideRoute = new Route("side", sideRouteNodes);
                // routeProbabilities.add(new FrequencyAndObject<>(lanesOnBranch, sideRoute));
                // try
                // {
                // this.routeGenerator = new ProbabilisticRouteGenerator(routeProbabilities, new MersenneTwister(1234));
                // }
                // catch (ProbabilityException exception)
                // {
                // exception.printStackTrace();
                // }
            }

            // Get remaining properties
            for (Property<?> ap : new CompoundProperty("", "", "", this.properties, false, 0))
            {
                if (ap instanceof SelectionProperty)
                {
                    SelectionProperty sp = (SelectionProperty) ap;
                    if ("TacticalPlanner".equals(sp.getKey()))
                    {
                        String tacticalPlannerName = sp.getValue();
                        if ("IDM".equals(tacticalPlannerName))
                        {
                            new LaneBasedStrategicalRoutePlannerFactory(
                                    new LaneBasedGTUFollowingTacticalPlannerFactory(this.carFollowingModelCars));
                            new LaneBasedStrategicalRoutePlannerFactory(
                                    new LaneBasedGTUFollowingTacticalPlannerFactory(this.carFollowingModelTrucks));
                        }
                        else if ("MOBIL/IDM".equals(tacticalPlannerName))
                        {
                            new LaneBasedStrategicalRoutePlannerFactory(
                                    new LaneBasedCFLCTacticalPlannerFactory(this.carFollowingModelCars, this.laneChangeModel));
                            new LaneBasedStrategicalRoutePlannerFactory(new LaneBasedCFLCTacticalPlannerFactory(
                                    this.carFollowingModelTrucks, this.laneChangeModel));
                        }
                        else if ("DIRECTED/IDM".equals(tacticalPlannerName))
                        {
                            new LaneBasedStrategicalRoutePlannerFactory(
                                    new LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory(this.carFollowingModelCars));
                            new LaneBasedStrategicalRoutePlannerFactory(
                                    new LaneBasedGTUFollowingDirectedChangeTacticalPlannerFactory(
                                            this.carFollowingModelTrucks));
                        }
                        else if ("LMRS".equals(tacticalPlannerName))
                        {
                            // provide default parameters with the car-following model
                            this.strategicalPlannerGeneratorCars = new LaneBasedStrategicalRoutePlannerFactory(
                                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
                            this.strategicalPlannerGeneratorTrucks = new LaneBasedStrategicalRoutePlannerFactory(
                                    new LMRSFactory(new IDMPlusFactory(this.stream), new DefaultLMRSPerceptionFactory()));
                        }
                        else if ("Toledo".equals(tacticalPlannerName))
                        {
                            new LaneBasedStrategicalRoutePlannerFactory(new ToledoFactory());
                            new LaneBasedStrategicalRoutePlannerFactory(new ToledoFactory());
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
                    String modelName = ap.getKey();
                    if (modelName.equals("TrafficComposition"))
                    {
                        this.carProbability = pdp.getValue()[0];
                    }
                }
                else if (ap instanceof ContinuousProperty)
                {
                    ContinuousProperty contP = (ContinuousProperty) ap;
                    if (contP.getKey().startsWith("Flow"))
                    {
                        this.averageHeadway = new Duration(3600.0 / contP.getValue(), SECOND);
                        this.minimumHeadway = new Duration(3, SECOND);
                        this.headwayGenerator = new DistErlang(new MersenneTwister(1234), 4,
                                DoubleScalar.minus(this.averageHeadway, this.minimumHeadway).getSI());
                    }
                }
                else if (ap instanceof CompoundProperty)
                {
                    CompoundProperty compoundProperty = (CompoundProperty) ap;
                    if (ap.getKey().equals("Output"))
                    {
                        continue; // Output settings are handled elsewhere
                    }
                    if (ap.getKey().contains("IDM"))
                    {
                        Acceleration a = IDMPropertySet.getA(compoundProperty);
                        Acceleration b = IDMPropertySet.getB(compoundProperty);
                        Length s0 = IDMPropertySet.getS0(compoundProperty);
                        Duration tSafe = IDMPropertySet.getTSafe(compoundProperty);
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
                        if (ap.getKey().contains("Car"))
                        {
                            this.carFollowingModelCars = gtuFollowingModel;
                        }
                        else if (ap.getKey().contains("Truck"))
                        {
                            this.carFollowingModelTrucks = gtuFollowingModel;
                        }
                        else
                        {
                            throw new Error("Cannot determine gtu type for " + ap.getKey());
                        }
                    }
                }
            }

            if (merge)
            {
                xmlCode.append("\t<LINK NAME=\"From2 to FirstVia\" NODESTART=\"From2\" NODEEND=\"FirstVia\" "
                        + "ROADLAYOUT=\"BRANCH\">\n");
                xmlCode.append("\t\t<STRAIGHT />\n");
                for (int lane = 1; lane <= lanesOnBranch; lane++)
                {
                    xmlCode.append("\t\t<GENERATOR LANE=\"Lane" + lane + "\" POSITION=\"16m\" IAT=\"ERLANG(4,"
                            + this.averageHeadway.minus(this.minimumHeadway).si + ") s\" "
                            + "INITIALSPEED=\"UNIFORM(80,90) km/h\" GTUMIX=\"gtumix\" ROUTEMIX=\"routemix\"\n"
                            + "\t\t\tGTUCOLORER=\"SWITCHABLE\" />\n");
                    // Initial speed is not GTUType dependent.
                }
                xmlCode.append("\t</LINK>\n");
            }
            else
            {
                xmlCode.append("\t<LINK NAME=\"SecondVia to End2\" NODESTART=\"SecondVia\" NODEEND=\"End2\" "
                        + "ROADLAYOUT=\"BRANCH\">\n");
                xmlCode.append("\t\t<STRAIGHT />\n");
                // In the original simulation; the sinks were on a separate (invisible) lane
                for (int lane = 1; lane <= lanesOnBranch; lane++)
                {
                    xmlCode.append("\t\t<SINK POSITION=\"999m\" LANE=\"Lane" + lane + "\" />\n");
                }
                xmlCode.append("\t</LINK>\n");
                // LaneFactory.makeMultiLaneBezier(this.network, "SecondVia to end2a", firstVia, secondVia, end2a, end2b,
                // lanesOnBranch, lanesOnCommon - lanesOnBranch, lanesOnCommon - lanesOnBranch, laneType, this.speedLimit,
                // this.simulator, LongitudinalDirectionality.DIR_PLUS);
                // setupSink(LaneFactory.makeMultiLane(this.network, "end2a to end2b", end2a, end2b, null, lanesOnBranch,
                // lanesOnCommon - lanesOnBranch, 0, laneType, this.speedLimit, this.simulator,
                // LongitudinalDirectionality.DIR_PLUS), laneType);
            }
            xmlCode.append(
                    "\t<LINK NAME=\"From to FirstVia\" NODESTART=\"From2\" NODEEND=\"FirstVia\" " + "ROADLAYOUT=\"MAIN\">\n");
            xmlCode.append("\t\t<STRAIGHT />\n");
            for (int lane = 1; lane <= (merge ? lanesOnMain : lanesOnCommonCompressed); lane++)
            {
                xmlCode.append("\t\t<GENERATOR LANE=\"Lane" + lane + "\" POSITION=\"16m\" IAT=\"ERLANG(4,"
                        + this.averageHeadway.minus(this.minimumHeadway).si + ") s\" "
                        + "INITIALSPEED=\"UNIFORM(80,90) km/h\" GTUMIX=\"gtumix\" ROUTEMIX=\"routemix\"\n"
                        + "\t\t\tGTUCOLORER=\"SWITCHABLE\" />\n");
                // Initial speed is not GTUType dependent.
            }
            xmlCode.append("\t</LINK>\n");

            // Lane[] startLanes =
            // LaneFactory.makeMultiLane(this.network, "From to FirstVia", from, firstVia, null, merge ? lanesOnMain
            // : lanesOnCommonCompressed, laneType, this.speedLimit, this.simulator,
            // LongitudinalDirectionality.DIR_PLUS);
            // setupGenerator(startLanes);

            xmlCode.append("\t<LINK NAME=\"FirstVia to SecondVia\" NODESTART=\"FirstVia\" NODEEND=\"SecondVia\" "
                    + "ROADLAYOUT=\"COMMON\">\n");
            xmlCode.append("\t\t<STRAIGHT />\n");
            for (int lane = lanesOnCommonCompressed + 1; lane <= lanesOnCommon; lane++)
            {
                xmlCode.append("\t\t<BLOCK LANE=\"Lane" + lane + "\" POSITION=\"END-1mm\" />\n");
            }
            xmlCode.append("\t</LINK>\n");
            // Lane[] common =
            // LaneFactory.makeMultiLane(this.network, "FirstVia to SecondVia", firstVia, secondVia, null, lanesOnCommon,
            // laneType, this.speedLimit, this.simulator, LongitudinalDirectionality.DIR_PLUS);
            // if (merge)
            // {
            // for (int i = lanesOnCommonCompressed; i < lanesOnCommon; i++)
            // {
            // setupBlock(common[i]);
            // }
            xmlCode.append("\t<LINK NAME=\"SecondVia to End2\" NODESTART=\"SecondVia\" NODEEND=\"End2\" "
                    + "ROADLAYOUT=\"BRANCH\">\n");
            xmlCode.append("\t\t<STRAIGHT />\n");
            // In the original simulation; the sinks were on a separate (invisible) lane
            for (int lane = 1; lane <= lanesOnBranch; lane++)
            {
                xmlCode.append("\t\t<SINK POSITION=\"999m\" LANE=\"Lane" + lane + "\" />\n");
            }
            xmlCode.append("\t</LINK>\n");

            // }
            xmlCode.append(
                    "\t<LINK NAME=\"SecondVia to End\" NODESTART=\"SecondVia\" NODEEND=\"End\" " + "ROADLAYOUT=\"BRANCH\">");
            xmlCode.append("\t\t<STRAIGHT />\n");
            // In the original simulation; the sinks were on a separate (invisible) lane
            for (int lane = 1; lane <= lanesOnBranch; lane++)
            {
                xmlCode.append("\t\t<SINK POSITION=\"999m\" LANE=\"Lane" + lane + "\" />\n");
            }
            xmlCode.append("\t</LINK>\n");
            // setupSink(LaneFactory.makeMultiLane(this.network, "SecondVia to end", secondVia, end, null, merge
            // ? lanesOnCommonCompressed : lanesOnMain, laneType, this.speedLimit, this.simulator,
            // LongitudinalDirectionality.DIR_PLUS), laneType);

            // for (int index = 0; index < lanesOnCommon; index++)
            // {
            // this.paths.add(new ArrayList<Lane>());
            // Lane lane = common[index];
            // // Follow back
            // while (lane.prevLanes(this.gtuType).size() > 0)
            // {
            // if (lane.prevLanes(this.gtuType).size() > 1)
            // {
            // throw new NetworkException("This network should not have lane merge points");
            // }
            // lane = lane.prevLanes(this.gtuType).keySet().iterator().next();
            // }
            // // Follow forward
            // while (true)
            // {
            // this.paths.get(index).add(lane);
            // int branching = lane.nextLanes(this.gtuType).size();
            // if (branching == 0)
            // {
            // break;
            // }
            // if (branching > 1)
            // {
            // throw new NetworkException("This network should not have lane split points");
            // }
            // lane = lane.nextLanes(this.gtuType).keySet().iterator().next();
            // }
            // }
            xmlCode.append("</NETWORK>\n");
            XmlNetworkLaneParser nlp = new XmlNetworkLaneParser((DEVSSimulatorInterface.TimeDoubleUnit) theSimulator);

            System.out.println("Building network from XML description\n" + xmlCode.toString());
            nlp.build(new ByteArrayInputStream(xmlCode.toString().getBytes()), this.network, true);
            this.simulator.scheduleEventAbs(new Time(0.999, TimeUnit.BASE_SECOND), this, this, "drawGraphs", null);
        }
        catch (NamingException | NetworkException | GTUException | OTSGeometryException | PropertyException
                | ParserConfigurationException | SAXException | IOException | ValueException | ParameterException exception1)
        {
            exception1.printStackTrace();
        }
    }

    /** Width of a lane. */
    static final double LANE_WIDTH = 3.5;

    /**
     * @param name String; name of the road layout
     * @param lanes int; number of lanes in the road layout
     * @return String; XML code that represents the road layout
     */
    private String makeRoadLayout(final String name, final int lanes)
    {
        StringBuilder xmlCode = new StringBuilder();
        xmlCode.append("\t\t<ROADLAYOUT NAME=\"" + name + "\" ROADTYPE=\"NORMALROAD\">\n");
        xmlCode.append("\t\t\t<SHOULDER WIDTH=\"2m\" OFFSET=\"-1m\" COLOR=\"GREEN\" />\n");
        double cumulativeOffset = 0;
        for (int lane = 0; lane < lanes; lane++)
        {
            xmlCode.append("\t\t\t<STRIPE TYPE=\"" + (0 == lane ? "SOLID" : "DASHED") + "\" OFFSET=\"1m\" WIDTH=\"20cm\" />\n");
            xmlCode.append("\t\t\t<LANE NAME=\"Lane" + (lane + 1) + "\" OFFSET=\"" + (cumulativeOffset + LANE_WIDTH / 2)
                    + "m\" DIRECTION=\"FORWARD\" />\n");
            cumulativeOffset += LANE_WIDTH;
        }
        xmlCode.append("\t\t\t<STRIPE TYPE=\"SOLID\" OFFSET=\"" + cumulativeOffset + "m\" WIDTH=\"20cm\" />\n");
        xmlCode.append("\t\t\t<SHOULDER WIDTH=\"2m\" OFFSET=\"" + (cumulativeOffset + 1) + "m\" COLOR=\"GREEN\" />\n");
        xmlCode.append("\t\t</ROADLAYOUT>\n");
        return xmlCode.toString();
    }

    /**
     * @param stream the random stream to use
     * @param lane reference lane to generate GTUs on
     * @param lengthDistribution distribution of the GTU length
     * @param widthDistribution distribution of the GTU width
     * @param maximumSpeedDistribution distribution of the GTU's maximum speed
     * @param initialPositions initial position(s) of the GTU on the Lane(s)
     * @param strategicalPlannerFactory factory to generate the strategical planner for the GTU
     * @return template for a GTU
     * @throws GTUException when characteristics cannot be initialized
     */
    LaneBasedTemplateGTUType makeTemplate(final StreamInterface stream, final Lane lane,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> lengthDistribution,
            final ContinuousDistDoubleScalar.Rel<Length, LengthUnit> widthDistribution,
            final ContinuousDistDoubleScalar.Rel<Speed, SpeedUnit> maximumSpeedDistribution,
            final Set<DirectedLanePosition> initialPositions,
            final LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> strategicalPlannerFactory) throws GTUException
    {
        return new LaneBasedTemplateGTUType(this.gtuType, new Generator<Length>()
        {
            @Override
            public Length draw()
            {
                return lengthDistribution.draw();
            }
        }, new Generator<Length>()
        {
            @Override
            public Length draw()
            {
                return widthDistribution.draw();
            }
        }, new Generator<Speed>()
        {
            @Override
            public Speed draw()
            {
                return maximumSpeedDistribution.draw();
            }
        },
                /*-new Generator<LaneBasedStrategicalPlanner>()
                {
                    public LaneBasedStrategicalPlanner draw() throws ProbabilityException, ParameterException
                    {
                        BehavioralCharacteristics behavioralCharacteristics = DefaultsFactory.getDefaultBehavioralCharacteristics();
                        behavioralCharacteristics.setParameter(ParameterTypes.LOOKAHEAD, new Length(450.0, LengthUnit.METER));
                        try
                        {
                            return new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics, tacticalPlanner,
                                XMLNetworkModel.this.routeGenerator.draw());
                        }
                        catch (GTUException exception)
                        {
                            throw new ParameterException(exception);
                        }
                    }
                }*/
                strategicalPlannerFactory, this.routeGenerator);

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
            this.simulator.scheduleEventAbs(new Time(this.simulator.getSimulatorTime().getSI() + 1, TimeUnit.BASE_SECOND),
                    this, this, "drawGraphs", null);
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
    // Length initialPosition = new Length(16, METER);
    // Speed initialSpeed = new Speed(50, KM_PER_HOUR);
    // boolean generate = true;
    // // Check if there is sufficient room
    // // Find the first vehicle on the lane
    // LaneBasedGTU leader = null;
    // Time when = new Time(this.simulator.getSimulatorTime().si, TimeUnit.SI);
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
    // double leaderSpeed = leader.getSpeed().si;
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
    // Length vehicleLength = new Length(generateTruck ? 15 : 4, METER);
    // GTUFollowingModel gtuFollowingModel = generateTruck ? this.carFollowingModelTrucks : this.carFollowingModelCars;
    // double speed = this.disttria.draw();
    //
    // LaneBasedDrivingCharacteristics drivingCharacteristics =
    // new LaneBasedDrivingCharacteristics(gtuFollowingModel, this.laneChangeModel);
    // drivingCharacteristics.setForwardHeadwayDistance(new Length(450.0, LengthUnit.METER));
    // LaneBasedStrategicalPlanner strategicalPlanner =
    // new LaneBasedStrategicalRoutePlanner(drivingCharacteristics, this.tacticalPlanner,
    // this.routeGenerator.draw());
    // new LaneBasedIndividualGTU("" + (++this.carsCreated), this.gtuType, initialPositions, initialSpeed,
    // vehicleLength, new Length(1.8, METER), new Speed(speed, KM_PER_HOUR), this.simulator,
    // strategicalPlanner, new LanePerceptionFull(), DefaultCarAnimation.class, this.gtuColorer, this.network);
    // }
    // Object[] arguments = new Object[1];
    // arguments[0] = lane;
    // this.simulator.scheduleEventRel(new Duration(this.headwayGenerator.draw(), SECOND), this, this, "generateCar",
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
    public SimulatorInterface<Time, Duration, SimTimeDoubleUnit> getSimulator()
    {
        return this.simulator;
    }

    /** {@inheritDoc} */
    @Override
    public OTSNetwork getNetwork()
    {
        return this.network;
    }

    /**
     * The route colorer to show whether GTUs stay on the main route or go right at the split.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * $LastChangedDate: 2016-12-13 02:02:22 +0100 (Tue, 13 Dec 2016) $, @version $Revision: 2930 $, by $Author: wjschakel $,
     * initial version Jan 3, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    private class DirectionGTUColorer implements GTUColorer
    {
        /** The legend. */
        private List<LegendEntry> legend = new ArrayList<>();

        /** ... */
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
