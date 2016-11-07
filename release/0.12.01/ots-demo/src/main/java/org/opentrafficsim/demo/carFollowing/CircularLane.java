package org.opentrafficsim.demo.carFollowing;

import java.awt.Container;
import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.BooleanProperty;
import org.opentrafficsim.base.modelproperties.CompoundProperty;
import org.opentrafficsim.base.modelproperties.ContinuousProperty;
import org.opentrafficsim.base.modelproperties.IntegerProperty;
import org.opentrafficsim.base.modelproperties.ProbabilityDistributionProperty;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.base.modelproperties.SelectionProperty;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.graphs.AccelerationContourPlot;
import org.opentrafficsim.graphs.ContourPlot;
import org.opentrafficsim.graphs.DensityContourPlot;
import org.opentrafficsim.graphs.FlowContourPlot;
import org.opentrafficsim.graphs.LaneBasedGTUSampler;
import org.opentrafficsim.graphs.SpeedContourPlot;
import org.opentrafficsim.graphs.TrajectoryPlot;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.modelproperties.IDMPropertySet;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.SimpleSimulatorInterface;

/**
 * Circular lane simulation demo.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 21 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CircularLane extends AbstractWrappableAnimation implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;

    /** The model. */
    private LaneSimulationModel model;

    /**
     * Create a CircularLane simulation.
     * @throws PropertyException when one of the properties has a non-unique tag
     */
    public CircularLane() throws PropertyException
    {
        this.properties.add(new IntegerProperty("TrackLength", "Track length", "Circumference of the track", 2000, 500, 6000,
                "Track length %dm", false, 10));
        this.properties.add(new ContinuousProperty("MeanDensity", "Mean density", "Number of vehicles per km", 40.0, 5.0, 45.0,
                "Density %.1f veh/km", false, 11));
        this.properties.add(new ContinuousProperty("DensityVariability", "Density variability",
                "Variability of the number of vehicles per km", 0.0, 0.0, 1.0, "%.1f", false, 12));
        List<Property<?>> outputProperties = new ArrayList<>();
        outputProperties.add(new BooleanProperty("DensityPlot", "Density", "Density contour plot", true, false, 0));
        outputProperties.add(new BooleanProperty("FlowPlot", "Flow", "Flow contour plot", true, false, 1));
        outputProperties.add(new BooleanProperty("SpeedPlot", "Speed", "Speed contour plot", true, false, 2));
        outputProperties.add(new BooleanProperty("AccelerationPlot", "Acceleration", "Acceleration contour plot", true, false,
                3));
        outputProperties.add(new BooleanProperty("TrajectoryPlot", "Trajectories", "Trajectory (time/distance) diagram", true,
                false, 4));
        this.properties.add(new CompoundProperty("OutputGraphs", "Output graphs", "Select the graphical output",
                outputProperties, true, 1000));
    }

    /** {@inheritDoc} */
    @Override
    public final void stopTimersThreads()
    {
        super.stopTimersThreads();
        this.model = null;
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException should never happen
     */
    public static void main(final String[] args) throws SimRuntimeException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    CircularLane circularLane = new CircularLane();
                    List<Property<?>> propertyList = circularLane.getProperties();
                    try
                    {
                        propertyList.add(new ProbabilityDistributionProperty("TrafficComposition", "Traffic composition",
                                "<html>Mix of passenger cars and trucks</html>", new String[] { "passenger car", "truck" },
                                new Double[] { 0.8, 0.2 }, false, 10));
                    }
                    catch (PropertyException exception)
                    {
                        exception.printStackTrace();
                    }
                    propertyList.add(new SelectionProperty("CarFollowingModel", "Car following model",
                            "<html>The car following model determines "
                                    + "the acceleration that a vehicle will make taking into account "
                                    + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                                    + "curvature of the road) capabilities of the vehicle and personality "
                                    + "of the driver.</html>", new String[] { "IDM", "IDM+" }, 1, false, 1));
                    propertyList.add(IDMPropertySet.makeIDMPropertySet("IDMCar", "Car", new Acceleration(1.0,
                            METER_PER_SECOND_2), new Acceleration(1.5, METER_PER_SECOND_2), new Length(2.0, METER),
                            new Duration(1.0, SECOND), 2));
                    propertyList.add(IDMPropertySet.makeIDMPropertySet("IDMTruck", "Truck", new Acceleration(0.5,
                            METER_PER_SECOND_2), new Acceleration(1.25, METER_PER_SECOND_2), new Length(2.0, METER),
                            new Duration(1.0, SECOND), 3));
                    circularLane.buildAnimator(new Time(0.0, SECOND), new Duration(0.0, SECOND), new Duration(3600.0, SECOND),
                            propertyList, null, true);
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | PropertyException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer)
    {
        this.model = new LaneSimulationModel(this.savedUserModifiedProperties, colorer);
        return this.model;
    }

    /** {@inheritDoc} */
    @Override
    protected final Rectangle2D.Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(-350, -350, 700, 700);
    }

    /** {@inheritDoc} */
    @Override
    protected final JPanel makeCharts(final SimpleSimulatorInterface simulator) throws OTSSimulationException,
            PropertyException
    {
        // Make the tab with the plots
        Property<?> output = new CompoundProperty("", "", "", this.properties, false, 0).findSubPropertyByKey("OutputGraphs");
        if (null == output)
        {
            throw new OTSSimulationException("Cannot find output properties");
        }
        ArrayList<BooleanProperty> graphs = new ArrayList<>();
        if (output instanceof CompoundProperty)
        {
            CompoundProperty outputProperties = (CompoundProperty) output;
            for (Property<?> ap : outputProperties.getValue())
            {
                if (ap instanceof BooleanProperty)
                {
                    BooleanProperty bp = (BooleanProperty) ap;
                    if (bp.getValue())
                    {
                        graphs.add(bp);
                    }
                }
            }
        }
        else
        {
            throw new OTSSimulationException("output properties should be compound");
        }
        int graphCount = graphs.size();
        int columns = (int) Math.ceil(Math.sqrt(graphCount));
        int rows = 0 == columns ? 0 : (int) Math.ceil(graphCount * 1.0 / columns);
        TablePanel charts = new TablePanel(columns, rows);

        for (int i = 0; i < graphCount; i++)
        {
            String graphName = graphs.get(i).getKey();
            Container container = null;
            LaneBasedGTUSampler graph;
            if (graphName.equals("TrajectoryPlot"))
            {
                TrajectoryPlot tp =
                        new TrajectoryPlot("TrajectoryPlot", new Duration(0.5, SECOND), this.model.getPath(), simulator);
                tp.setTitle("Trajectory Graph");
                tp.setExtendedState(Frame.MAXIMIZED_BOTH);
                graph = tp;
                container = tp.getContentPane();
            }
            else
            {
                ContourPlot cp;
                if (graphName.equals("DensityPlot"))
                {
                    cp = new DensityContourPlot("DensityPlot", this.model.getPath());
                    cp.setTitle("Density Contour Graph");
                }
                else if (graphName.equals("SpeedPlot"))
                {
                    cp = new SpeedContourPlot("SpeedPlot", this.model.getPath());
                    cp.setTitle("Speed Contour Graph");
                }
                else if (graphName.equals("FlowPlot"))
                {
                    cp = new FlowContourPlot("FlowPlot", this.model.getPath());
                    cp.setTitle("Flow Contour Graph");
                }
                else if (graphName.equals("AccelerationPlot"))
                {
                    cp = new AccelerationContourPlot("AccelerationPlot", this.model.getPath());
                    cp.setTitle("Acceleration Contour Graph");
                }
                else
                {
                    throw new OTSSimulationException("Unhandled type of contourplot: " + graphName);
                }
                graph = cp;
                container = cp.getContentPane();
            }
            // Add the container to the matrix
            charts.setCell(container, i % columns, i / columns);
            this.model.getPlots().add(graph);
        }
        return charts;
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "Circular Lane simulation";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "<html><h1>Circular Lane simulation</h1>" + "Vehicles are unequally distributed over a one lane ring road.<br>"
                + "When simulation starts, all vehicles begin driving and some shockwaves may develop (depending on "
                + "the selected track length and car following parameters).<br>"
                + "Selected trajectory and contour plots are generated during the simulation.</html>";
    }

}

/**
 * Simulate traffic on a circular, one-lane road.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 1 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class LaneSimulationModel implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20141121L;

    /** The network. */
    private final OTSNetwork network = new OTSNetwork("network");

    /** The simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** Number of cars created. */
    private int carsCreated = 0;

    /** Type of all GTUs. */
    private GTUType gtuType = new GTUType("Car");

    /** The car following model, e.g. IDM Plus for cars. */
    private GTUFollowingModelOld carFollowingModelCars;

    /** The car following model, e.g. IDM Plus for trucks. */
    private GTUFollowingModelOld carFollowingModelTrucks;

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** Minimum distance. */
    private Length minimumDistance = new Length(0, METER);

    /** The left Lane that contains simulated Cars. */
    private Lane lane1;

    /** The right Lane that contains simulated Cars. */
    private Lane lane2;

    /** The speed limit. */
    private Speed speedLimit = new Speed(100, KM_PER_HOUR);

    /** The contour plots. */
    private List<LaneBasedGTUSampler> contourPlots = new ArrayList<>();

    /** The trajectory plot. */
    private List<TrajectoryPlot> trajectoryPlots = new ArrayList<>();

    /** User settable properties. */
    private List<Property<?>> properties = null;

    /** The random number generator used to decide what kind of GTU to generate. */
    private Random randomGenerator = new Random(12345);

    /** The sequence of Lanes that all vehicles will follow. */
    private List<Lane> path = new ArrayList<>();

    /** The GTUColorer for the generated vehicles. */
    private final GTUColorer gtuColorer;

    /**
     * @param properties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the user modified properties for the model
     * @param gtuColorer the default and initial GTUColorer, e.g. a DefaultSwitchableTUColorer.
     */
    LaneSimulationModel(final List<Property<?>> properties, final GTUColorer gtuColorer)
    {
        this.properties = properties;
        this.gtuColorer = gtuColorer;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        double radius = 2000 / 2 / Math.PI;
        double headway = 40;
        double headwayVariability = 0;
        try
        {
            String carFollowingModelName = null;
            CompoundProperty propertyContainer = new CompoundProperty("", "", "", this.properties, false, 0);
            Property<?> cfmp = propertyContainer.findByKey("CarFollowingModel");
            if (null == cfmp)
            {
                throw new SimRuntimeException("Cannot find \"Car following model\" property");
            }
            if (cfmp instanceof SelectionProperty)
            {
                carFollowingModelName = ((SelectionProperty) cfmp).getValue();
            }
            else
            {
                throw new SimRuntimeException("\"Car following model\" property has wrong type");
            }
            for (Property<?> ap : new CompoundProperty("", "", "", this.properties, false, 0))
            {
                // System.out.println("Handling property " + ap.getKey());
                if (ap instanceof SelectionProperty)
                {
                    SelectionProperty sp = (SelectionProperty) ap;
                    if ("CarFollowingModel".equals(sp.getKey()))
                    {
                        carFollowingModelName = sp.getValue();
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
                else if (ap instanceof IntegerProperty)
                {
                    IntegerProperty ip = (IntegerProperty) ap;
                    if ("Track length".equals(ip.getKey()))
                    {
                        radius = ip.getValue() / 2 / Math.PI;
                    }
                }
                else if (ap instanceof ContinuousProperty)
                {
                    ContinuousProperty cp = (ContinuousProperty) ap;
                    if (cp.getKey().equals("MeanDensity"))
                    {
                        headway = 1000 / cp.getValue();
                    }
                    if (cp.getKey().equals("DensityVariability"))
                    {
                        headwayVariability = cp.getValue();
                    }
                }
                else if (ap instanceof CompoundProperty)
                {
                    CompoundProperty cp = (CompoundProperty) ap;
                    if (ap.getKey().equals("OutputGraphs"))
                    {
                        continue; // Output settings are handled elsewhere
                    }
                    if (ap.getKey().contains("IDM"))
                    {
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
                            throw new SimRuntimeException("Unknown gtu following model: " + carFollowingModelName);
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
                            throw new SimRuntimeException("Cannot determine gtu type for " + ap.getKey());
                        }
                    }
                }
            }

            OTSNode start = new OTSNode(this.network, "Start", new OTSPoint3D(radius, 0, 0));
            OTSNode halfway = new OTSNode(this.network, "Halfway", new OTSPoint3D(-radius, 0, 0));
            Set<GTUType> compatibility = new HashSet<>();
            compatibility.add(this.gtuType);
            LaneType laneType = new LaneType("CarLane", compatibility);

            OTSPoint3D[] coordsHalf1 = new OTSPoint3D[127];
            for (int i = 0; i < coordsHalf1.length; i++)
            {
                double angle = Math.PI * (1 + i) / (1 + coordsHalf1.length);
                coordsHalf1[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle), 0);
            }
            this.lane1 =
                    LaneFactory.makeMultiLane(this.network, "Lane1", start, halfway, coordsHalf1, 1, laneType, this.speedLimit,
                            this.simulator, LongitudinalDirectionality.DIR_PLUS)[0];
            this.path.add(this.lane1);

            OTSPoint3D[] coordsHalf2 = new OTSPoint3D[127];
            for (int i = 0; i < coordsHalf2.length; i++)
            {
                double angle = Math.PI + Math.PI * (1 + i) / (1 + coordsHalf2.length);
                coordsHalf2[i] = new OTSPoint3D(radius * Math.cos(angle), radius * Math.sin(angle), 0);
            }
            this.lane2 =
                    LaneFactory.makeMultiLane(this.network, "Lane2", halfway, start, coordsHalf2, 1, laneType, this.speedLimit,
                            this.simulator, LongitudinalDirectionality.DIR_PLUS)[0];
            this.path.add(this.lane2);

            // Put the (not very evenly spaced) cars on track1
            double trackLength = this.lane1.getLength().getSI();
            double variability = (headway - 20) * headwayVariability;
            System.out.println("headway is " + headway + " variability limit is " + variability);
            Random random = new Random(12345);
            for (double pos = 0; pos <= trackLength - headway - variability;)
            {
                // Actual headway is uniformly distributed around headway
                double actualHeadway = headway + (random.nextDouble() * 2 - 1) * variability;
                generateCar(this.lane1, new Length(pos, METER));
                pos += actualHeadway;
            }
            // Put the (not very evenly spaced) cars on track2
            trackLength = this.lane2.getLength().getSI();
            variability = (headway - 20) * headwayVariability;
            System.out.println("headway is " + headway + " variability limit is " + variability);
            random = new Random(54321);
            for (double pos = 0; pos <= trackLength - headway - variability;)
            {
                // Actual headway is uniformly distributed around headway
                double actualHeadway = headway + (random.nextDouble() * 2 - 1) * variability;
                generateCar(this.lane2, new Length(pos, METER));
                pos += actualHeadway;
            }
            // Schedule regular updates of the graph
            this.simulator.scheduleEventAbs(new Time(0.999, SECOND), this, this, "drawGraphs", null);
        }
        catch (SimRuntimeException | NamingException | NetworkException | GTUException | OTSGeometryException
                | PropertyException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * @return a newly created path (which all GTUs in this simulation will follow).
     */
    public List<Lane> getPath()
    {
        return new ArrayList<>(this.path);
    }

    /**
     * Notify the contour plots that the underlying data has changed.
     */
    protected final void drawGraphs()
    {
        for (LaneBasedGTUSampler contourPlot : this.contourPlots)
        {
            contourPlot.reGraph();
        }
        for (TrajectoryPlot trajectoryPlot : this.trajectoryPlots)
        {
            trajectoryPlot.reGraph();
        }
        // Re schedule this method
        try
        {
            this.simulator.scheduleEventAbs(new Time(this.simulator.getSimulatorTime().get().getSI() + 1, SECOND), this, this,
                    "drawGraphs", null);
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }

    }

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     * @param lane Lane; the lane on which the new cars are placed
     * @param initialPosition Length; the initial longitudinal position of the new cars on the lane
     * @throws GTUException should not happen
     */
    protected final void generateCar(final Lane lane, final Length initialPosition) throws GTUException
    {
        boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
        Speed initialSpeed = new Speed(0, KM_PER_HOUR);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        initialPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
        try
        {
            Length vehicleLength = new Length(generateTruck ? 15 : 4, METER);
            GTUFollowingModelOld gtuFollowingModel = generateTruck ? this.carFollowingModelTrucks : this.carFollowingModelCars;
            if (null == gtuFollowingModel)
            {
                throw new GTUException("gtuFollowingModel is null");
            }
            BehavioralCharacteristics behavioralCharacteristics = DefaultsFactory.getDefaultBehavioralCharacteristics();
            LaneBasedIndividualGTU gtu =
                    new LaneBasedIndividualGTU("" + (++this.carsCreated), this.gtuType, vehicleLength, new Length(1.8, METER),
                            new Speed(200, KM_PER_HOUR), this.simulator, this.network);
            LaneBasedStrategicalPlanner strategicalPlanner =
                    new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics, new LaneBasedGTUFollowingTacticalPlanner(
                            gtuFollowingModel, gtu), gtu);
            gtu.initWithAnimation(strategicalPlanner, initialPositions, initialSpeed, DefaultCarAnimation.class,
                    this.gtuColorer);
        }
        catch (NamingException | SimRuntimeException | NetworkException | OTSGeometryException exception)
        {
            throw new GTUException(exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return null;
    }

    /**
     * @return contourPlots
     */
    public final List<LaneBasedGTUSampler> getPlots()
    {
        return this.contourPlots;
    }

    /**
     * @return trajectoryPlots
     */
    public final List<TrajectoryPlot> getTrajectoryPlots()
    {
        return this.trajectoryPlots;
    }

    /**
     * @return minimumDistance
     */
    public final Length getMinimumDistance()
    {
        return this.minimumDistance;
    }

}
