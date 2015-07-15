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

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDM;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.lane.changing.AbstractLaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.graphs.AccelerationContourPlot;
import org.opentrafficsim.graphs.ContourPlot;
import org.opentrafficsim.graphs.DensityContourPlot;
import org.opentrafficsim.graphs.FlowContourPlot;
import org.opentrafficsim.graphs.LaneBasedGTUSampler;
import org.opentrafficsim.graphs.SpeedContourPlot;
import org.opentrafficsim.graphs.TrajectoryPlot;
import org.opentrafficsim.simulationengine.AbstractWrappableSimulation;
import org.opentrafficsim.simulationengine.WrappableSimulation;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.BooleanProperty;
import org.opentrafficsim.simulationengine.properties.CompoundProperty;
import org.opentrafficsim.simulationengine.properties.ContinuousProperty;
import org.opentrafficsim.simulationengine.properties.IDMPropertySet;
import org.opentrafficsim.simulationengine.properties.IntegerProperty;
import org.opentrafficsim.simulationengine.properties.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.properties.PropertyException;
import org.opentrafficsim.simulationengine.properties.SelectionProperty;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Circular lane simulation demo.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version21 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CircularLane extends AbstractWrappableSimulation implements WrappableSimulation
{
    /** the model. */
    private LaneSimulationModel model;

    /** Create a CircularLane simulation. */
    public CircularLane()
    {
        this.properties.add(new IntegerProperty("Track length", "Circumference of the track", 2000, 500, 6000,
            "Track length %dm", false, 10));
        this.properties.add(new ContinuousProperty("Mean density", "Number of vehicles per km", 40.0, 5.0, 45.0,
            "Density %.1f veh/km", false, 11));
        this.properties.add(new ContinuousProperty("Density variability", "Variability of the number of vehicles per km",
            0.0, 0.0, 1.0, "%.1f", false, 12));
        ArrayList<AbstractProperty<?>> outputProperties = new ArrayList<AbstractProperty<?>>();
        outputProperties.add(new BooleanProperty("Density", "Density contour plot", true, false, 0));
        outputProperties.add(new BooleanProperty("Flow", "Flow contour plot", true, false, 1));
        outputProperties.add(new BooleanProperty("Speed", "Speed contour plot", true, false, 2));
        outputProperties.add(new BooleanProperty("Acceleration", "Acceleration contour plot", true, false, 3));
        outputProperties.add(new BooleanProperty("Trajectories", "Trajectory (time/distance) diagram", true, false, 4));
        this.properties.add(new CompoundProperty("Output graphs", "Select the graphical output", outputProperties, true,
            1000));
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
     * @throws RemoteException on communications failure
     */
    public static void main(final String[] args) throws RemoteException, SimRuntimeException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    CircularLane circularLane = new CircularLane();
                    ArrayList<AbstractProperty<?>> propertyList = circularLane.getProperties();
                    try
                    {
                        propertyList.add(new ProbabilityDistributionProperty("Traffic composition",
                            "<html>Mix of passenger cars and trucks</html>", new String[] {"passenger car", "truck"},
                            new Double[] {0.8, 0.2}, false, 10));
                    }
                    catch (PropertyException exception)
                    {
                        exception.printStackTrace();
                    }
                    propertyList.add(new SelectionProperty("Car following model",
                        "<html>The car following model determines "
                            + "the acceleration that a vehicle will make taking into account "
                            + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                            + "curvature of the road) capabilities of the vehicle and personality "
                            + "of the driver.</html>", new String[] {"IDM", "IDM+"}, 1, false, 1));
                    propertyList.add(IDMPropertySet.makeIDMPropertySet("Car", new DoubleScalar.Abs<AccelerationUnit>(1.0,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<AccelerationUnit>(1.5,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER),
                        new DoubleScalar.Rel<TimeUnit>(1.0, TimeUnit.SECOND), 2));
                    propertyList.add(IDMPropertySet.makeIDMPropertySet("Truck", new DoubleScalar.Abs<AccelerationUnit>(0.5,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<AccelerationUnit>(1.25,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER),
                        new DoubleScalar.Rel<TimeUnit>(1.0, TimeUnit.SECOND), 3));
                    circularLane.buildSimulator(propertyList, null, true);
                }
                catch (RemoteException | SimRuntimeException | NamingException exception)
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
        return new Rectangle2D.Double(-1000, -1000, 2000, 2000);
    }

    /** {@inheritDoc} */
    @Override
    protected final JPanel makeCharts()
    {
        // Make the tab with the plots
        AbstractProperty<?> output =
            new CompoundProperty("", "", this.properties, false, 0).findByShortName("Output graphs");
        if (null == output)
        {
            throw new Error("Cannot find output properties");
        }
        ArrayList<BooleanProperty> graphs = new ArrayList<BooleanProperty>();
        if (output instanceof CompoundProperty)
        {
            CompoundProperty outputProperties = (CompoundProperty) output;
            for (AbstractProperty<?> ap : outputProperties.getValue())
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
            throw new Error("output properties should be compound");
        }
        int graphCount = graphs.size();
        int columns = (int) Math.ceil(Math.sqrt(graphCount));
        int rows = 0 == columns ? 0 : (int) Math.ceil(graphCount * 1.0 / columns);
        TablePanel charts = new TablePanel(columns, rows);

        for (int i = 0; i < graphCount; i++)
        {
            String graphName = graphs.get(i).getShortName();
            Container container = null;
            LaneBasedGTUSampler graph;
            if (graphName.contains("Trajectories"))
            {
                TrajectoryPlot tp =
                    new TrajectoryPlot("TrajectoryPlot", new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), this.model
                        .getPath());
                tp.setTitle("Trajectory Graph");
                tp.setExtendedState(Frame.MAXIMIZED_BOTH);
                graph = tp;
                container = tp.getContentPane();
            }
            else
            {
                ContourPlot cp;
                if (graphName.contains("Density"))
                {
                    cp = new DensityContourPlot("DensityPlot", this.model.getPath());
                    cp.setTitle("Density Contour Graph");
                }
                else if (graphName.contains("Speed"))
                {
                    cp = new SpeedContourPlot("SpeedPlot", this.model.getPath());
                    cp.setTitle("Speed Contour Graph");
                }
                else if (graphName.contains("Flow"))
                {
                    cp = new FlowContourPlot("FlowPlot", this.model.getPath());
                    cp.setTitle("Flow Contour Graph");
                }
                else if (graphName.contains("Acceleration"))
                {
                    cp = new AccelerationContourPlot("AccelerationPlot", this.model.getPath());
                    cp.setTitle("Acceleration Contour Graph");
                }
                else
                {
                    throw new Error("Unhandled type of contourplot: " + graphName);
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
        return "<html><h1>Circular Lane simulation</h1>"
            + "Vehicles are unequally distributed over a one lane ring road.<br />"
            + "When simulation starts, all vehicles begin driving and some shockwaves may develop (depending on "
            + "the selected track length and car following parameters).<br />"
            + "Selected trajectory and contour plots are generated during the simulation.</html>";
    }

}

/**
 * Simulate traffic on a circular, one-lane road.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version21 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class LaneSimulationModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20141121L;

    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** number of cars created. */
    private int carsCreated = 0;

    /** Type of all GTUs. */
    private GTUType<String> gtuType = GTUType.makeGTUType("Car");

    /** the car following model, e.g. IDM Plus for cars. */
    private GTUFollowingModel carFollowingModelCars;

    /** the car following model, e.g. IDM Plus for trucks. */
    private GTUFollowingModel carFollowingModelTrucks;

    /** The probability that the next generated GTU is a passenger car. */
    private double carProbability;

    /** The lane change model. */
    private AbstractLaneChangeModel laneChangeModel = new Egoistic();

    /** minimum distance. */
    private DoubleScalar.Rel<LengthUnit> minimumDistance = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);

    /** The left Lane that contains simulated Cars. */
    private Lane lane1;

    /** The right Lane that contains simulated Cars. */
    private Lane lane2;

    /** the speed limit. */
    private DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

    /** the contour plots. */
    private ArrayList<LaneBasedGTUSampler> contourPlots = new ArrayList<LaneBasedGTUSampler>();

    /** the trajectory plot. */
    private ArrayList<TrajectoryPlot> trajectoryPlots = new ArrayList<TrajectoryPlot>();

    /** User settable properties. */
    private ArrayList<AbstractProperty<?>> properties = null;

    /** The random number generator used to decide what kind of GTU to generate. */
    private Random randomGenerator = new Random(12345);

    /** The sequence of Lanes that all vehicles will follow. */
    private List<Lane> path = new ArrayList<Lane>();

    /** The GTUColorer for the generated vehicles. */
    private final GTUColorer gtuColorer;

    /**
     * @return a newly created path (which all GTUs in this simulation will follow).
     */
    public List<Lane> getPath()
    {
        return new ArrayList<Lane>(this.path);
    }

    /**
     * @param properties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the user modified properties for the model
     * @param gtuColorer the default and initial GTUColorer, e.g. a DefaultSwitchableTUColorer.
     */
    public LaneSimulationModel(final ArrayList<AbstractProperty<?>> properties, final GTUColorer gtuColorer)
    {
        this.properties = properties;
        this.gtuColorer = gtuColorer;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
        throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        double radius = 2000 / 2 / Math.PI;
        double headway = 40;
        double headwayVariability = 0;
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
                // System.out.println("Handling property " + ap.getShortName());
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
                else if (ap instanceof IntegerProperty)
                {
                    IntegerProperty ip = (IntegerProperty) ap;
                    if ("Track length".equals(ip.getShortName()))
                    {
                        radius = ip.getValue() / 2 / Math.PI;
                    }
                }
                else if (ap instanceof ContinuousProperty)
                {
                    ContinuousProperty cp = (ContinuousProperty) ap;
                    if (cp.getShortName().equals("Mean density"))
                    {
                        headway = 1000 / cp.getValue();
                    }
                    if (cp.getShortName().equals("Density variability"))
                    {
                        headwayVariability = cp.getValue();
                    }
                }
                else if (ap instanceof CompoundProperty)
                {
                    CompoundProperty cp = (CompoundProperty) ap;
                    if (ap.getShortName().equals("Output graphs"))
                    {
                        continue; // Output settings are handled elsewhere
                    }
                    if (ap.getShortName().contains("IDM"))
                    {
                        DoubleScalar.Abs<AccelerationUnit> a = IDMPropertySet.getA(cp);
                        DoubleScalar.Abs<AccelerationUnit> b = IDMPropertySet.getB(cp);
                        DoubleScalar.Rel<LengthUnit> s0 = IDMPropertySet.getS0(cp);
                        DoubleScalar.Rel<TimeUnit> tSafe = IDMPropertySet.getTSafe(cp);
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

            NodeGeotools.STR start = new NodeGeotools.STR("Start", new Coordinate(radius, 0, 0));
            NodeGeotools.STR halfway = new NodeGeotools.STR("Halfway", new Coordinate(-radius, 0, 0));
            LaneType<String> laneType = new LaneType<String>("CarLane");
            laneType.addCompatibility(this.gtuType);

            Coordinate[] coordsHalf1 = new Coordinate[127];
            for (int i = 0; i < coordsHalf1.length; i++)
            {
                double angle = Math.PI * (1 + i) / (1 + coordsHalf1.length);
                coordsHalf1[i] = new Coordinate(radius * Math.cos(angle), radius * Math.sin(angle), 0);
            }
            this.lane1 =
                LaneFactory
                    .makeMultiLane("Lane1", start, halfway, coordsHalf1, 1, laneType, this.speedLimit, this.simulator)[0];
            this.path.add(this.lane1);

            Coordinate[] coordsHalf2 = new Coordinate[127];
            for (int i = 0; i < coordsHalf2.length; i++)
            {
                double angle = Math.PI + Math.PI * (1 + i) / (1 + coordsHalf2.length);
                coordsHalf2[i] = new Coordinate(radius * Math.cos(angle), radius * Math.sin(angle), 0);
            }
            this.lane2 =
                LaneFactory
                    .makeMultiLane("Lane2", halfway, start, coordsHalf2, 1, laneType, this.speedLimit, this.simulator)[0];
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
                generateCar(this.lane1, new DoubleScalar.Rel<LengthUnit>(pos, LengthUnit.METER));
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
                generateCar(this.lane2, new DoubleScalar.Rel<LengthUnit>(pos, LengthUnit.METER));
                pos += actualHeadway;
            }
            // Schedule regular updates of the graph
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.999, TimeUnit.SECOND), this, this,
                "drawGraphs", null);
        }
        catch (RemoteException | SimRuntimeException | NamingException | NetworkException | GTUException exception)
        {
            exception.printStackTrace();
        }
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
     * @param lane Lane; the lane on which the new cars are placed
     * @param initialPosition DoubleScalar.Rel&lt;LengthUnit&gt;; the initial longitudinal position of the new cars on the lane
     * @throws GTUException should not happen
     */
    protected final void generateCar(final Lane lane, final DoubleScalar.Rel<LengthUnit> initialPosition)
        throws GTUException
    {
        boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new LinkedHashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(lane, initialPosition);
        try
        {
            DoubleScalar.Rel<LengthUnit> vehicleLength =
                new DoubleScalar.Rel<LengthUnit>(generateTruck ? 15 : 4, LengthUnit.METER);
            GTUFollowingModel gtuFollowingModel = generateTruck ? this.carFollowingModelTrucks : this.carFollowingModelCars;
            if (null == gtuFollowingModel)
            {
                throw new Error("gtuFollowingModel is null");
            }
            new LaneBasedIndividualCar<>(++this.carsCreated, this.gtuType, generateTruck ? this.carFollowingModelTrucks
                : this.carFollowingModelCars, this.laneChangeModel, initialPositions, initialSpeed, vehicleLength,
                new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(200,
                    SpeedUnit.KM_PER_HOUR), new Route(new ArrayList<Node<?, ?>>()), this.simulator,
                DefaultCarAnimation.class, this.gtuColorer);
        }
        catch (RemoteException | NamingException | SimRuntimeException | NetworkException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return null;
    }

    /**
     * @return contourPlots
     */
    public final ArrayList<LaneBasedGTUSampler> getPlots()
    {
        return this.contourPlots;
    }

    /**
     * @return trajectoryPlots
     */
    public final ArrayList<TrajectoryPlot> getTrajectoryPlots()
    {
        return this.trajectoryPlots;
    }

    /**
     * @return minimumDistance
     */
    public final DoubleScalar.Rel<LengthUnit> getMinimumDistance()
    {
        return this.minimumDistance;
    }

}
