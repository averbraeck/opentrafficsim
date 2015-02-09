package org.opentrafficsim.demo.carFollowing;

import java.awt.Container;
import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDM;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.lane.changing.AbstractLaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.Altruistic;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
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
import org.opentrafficsim.simulationengine.AbstractProperty;
import org.opentrafficsim.simulationengine.BooleanProperty;
import org.opentrafficsim.simulationengine.CompoundProperty;
import org.opentrafficsim.simulationengine.ContinuousProperty;
import org.opentrafficsim.simulationengine.ControlPanel;
import org.opentrafficsim.simulationengine.IDMPropertySet;
import org.opentrafficsim.simulationengine.IntegerProperty;
import org.opentrafficsim.simulationengine.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.PropertyException;
import org.opentrafficsim.simulationengine.SelectionProperty;
import org.opentrafficsim.simulationengine.SimpleSimulator;
import org.opentrafficsim.simulationengine.SimulatorFrame;
import org.opentrafficsim.simulationengine.WrappableSimulation;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Circular road simulation demo.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 21 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CircularRoad implements WrappableSimulation
{
    /** The properties exhibited by this simulation. */
    private ArrayList<AbstractProperty<?>> properties = new ArrayList<AbstractProperty<?>>();

    /** Create a CircularRoad simulation. */
    public CircularRoad()
    {
        this.properties.add(new SelectionProperty("Lane changing",
                "<html>The lane change strategies vary in politeness.<br />"
                        + "Two types are implemented:<ul><li>Egoistic (looks only at personal gain).</li>"
                        + "<li>Altruistic (assigns effect on new and current follower the same weight as "
                        + "the personal gain).</html>", new String[]{"Egoistic", "Altruistic"}, 0, false, 500));
        this.properties.add(new IntegerProperty("Track length", "Circumference of the track", 2000, 500, 6000,
                "Track length %dm", false, 10));
        this.properties.add(new ContinuousProperty("Mean density", "Number of vehicles per km", 40.0, 5.0, 45.0,
                "Density %.1f veh/km", false, 11));
        this.properties.add(new ContinuousProperty("Density variability",
                "Variability of the number of vehicles per km", 0.0, 0.0, 1.0, "%.1f", false, 12));
        ArrayList<AbstractProperty<?>> outputProperties = new ArrayList<AbstractProperty<?>>();
        for (int lane = 1; lane <= 2; lane++)
        {
            String laneId = String.format("Lane %d ", lane);
            outputProperties.add(new BooleanProperty(laneId + "Density", laneId + "Density contour plot", true, false,
                    0));
            outputProperties.add(new BooleanProperty(laneId + "Flow", laneId + "Flow contour plot", true, false, 1));
            outputProperties.add(new BooleanProperty(laneId + "Speed", laneId + "Speed contour plot", true, false, 2));
            outputProperties.add(new BooleanProperty(laneId + "Acceleration", laneId + "Acceleration contour plot",
                    true, false, 3));
            outputProperties.add(new BooleanProperty(laneId + "Trajectories", laneId
                    + "Trajectory (time/distance) diagram", true, false, 4));
        }
        this.properties
                .add(new CompoundProperty("Output", "Select the graphical output", outputProperties, true, 1000));
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException
     * @throws RemoteException
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
                    CircularRoad circularRoad = new CircularRoad();
                    ArrayList<AbstractProperty<?>> properties = circularRoad.getProperties();
                    try
                    {
                        properties.add(new ProbabilityDistributionProperty("Traffic composition",
                                "<html>Mix of passenger cars and trucks</html>",
                                new String[]{"passenger car", "truck"}, new Double[]{0.8, 0.2}, false, 10));
                    }
                    catch (PropertyException exception)
                    {
                        exception.printStackTrace();
                    }
                    properties.add(new SelectionProperty("Car following model",
                            "<html>The car following model determines "
                                    + "the acceleration that a vehicle will make taking into account "
                                    + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                                    + "curvature of the road) capabilities of the vehicle and personality "
                                    + "of the driver.</html>", new String[]{"IDM", "IDM+"}, 1, false, 1));
                    properties.add(IDMPropertySet.makeIDMPropertySet("Car", new DoubleScalar.Abs<AccelerationUnit>(1.0,
                            AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<AccelerationUnit>(1.5,
                            AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<LengthUnit>(2.0,
                            LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1.0, TimeUnit.SECOND), 2));
                    properties.add(IDMPropertySet.makeIDMPropertySet("Truck", new DoubleScalar.Abs<AccelerationUnit>(
                            0.5, AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<AccelerationUnit>(1.25,
                            AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<LengthUnit>(2.0,
                            LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1.0, TimeUnit.SECOND), 3));
                    new SimulatorFrame("Circular Road animation", circularRoad.buildSimulator(properties).getPanel());
                }
                catch (RemoteException | SimRuntimeException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the simulation.
     * @return SimpleSimulator; the simulation
     * @throws RemoteException on communications failure
     * @throws SimRuntimeException on ???
     */
    public SimpleSimulator buildSimulator(ArrayList<AbstractProperty<?>> userModifiedProperties)
            throws RemoteException, SimRuntimeException
    {
        RoadSimulationModel model = new RoadSimulationModel(userModifiedProperties);
        final SimpleSimulator result =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 2000, 2000));
        new ControlPanel(result);

        // Make the tab with the plots
        AbstractProperty<?> output = new CompoundProperty("", "", this.properties, false, 0).findByShortName("Output");
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
        result.getPanel().getTabbedPane().addTab("statistics", charts);

        for (int i = 0; i < graphCount; i++)
        {
            String graphName = graphs.get(i).getShortName();
            Container container = null;
            LaneBasedGTUSampler graph;
            int pos = graphName.indexOf(' ') + 1;
            String laneNumberText = graphName.substring(pos, pos + 1);
            int lane = Integer.parseInt(laneNumberText) - 1;

            if (graphName.contains("Trajectories"))
            {
                TrajectoryPlot tp =
                        new TrajectoryPlot(graphName, new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND),
                                model.getPath(lane));
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
                    cp = new DensityContourPlot(graphName, model.getPath(lane));
                    cp.setTitle("Density Contour Graph");
                }
                else if (graphName.contains("Speed"))
                {
                    cp = new SpeedContourPlot(graphName, model.getPath(lane));
                    cp.setTitle("Speed Contour Graph");
                }
                else if (graphName.contains("Flow"))
                {
                    cp = new FlowContourPlot(graphName, model.getPath(lane));
                    cp.setTitle("Flow Contour Graph");
                }
                else if (graphName.contains("Acceleration"))
                {
                    cp = new AccelerationContourPlot(graphName, model.getPath(lane));
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
            model.getPlots().add(graph);
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String shortName()
    {
        return "Circular Road simulation";
    }

    /** {@inheritDoc} */
    @Override
    public String description()
    {
        return "<html><h1>Circular Road simulation</h1>"
                + "Vehicles are unequally distributed over a two lane ring road.<br />"
                + "When simulation starts, all vehicles begin driving, some lane changes will occurr and some "
                + "shockwaves should develop.<br />"
                + "Trajectories and contourplots are generated during the simulation for both lanes.</html>";
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<AbstractProperty<?>> getProperties()
    {
        return new ArrayList<AbstractProperty<?>>(this.properties);
    }

}

/**
 * Simulate traffic on a circular, two-lane road.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 21 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class RoadSimulationModel implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20141121L;

    /** the simulator. */
    OTSDEVSSimulatorInterface simulator;

    /** Number of cars created. */
    private int carsCreated = 0;

    /** the car following model, e.g. IDM Plus for cars. */
    protected GTUFollowingModel carFollowingModelCars;

    /** the car following model, e.g. IDM Plus for trucks. */
    protected GTUFollowingModel carFollowingModelTrucks;

    /** The probability that the next generated GTU is a passenger car. */
    double carProbability;

    /** The lane change model. */
    protected AbstractLaneChangeModel laneChangeModel;

    /** Minimum distance. */
    private DoubleScalar.Rel<LengthUnit> minimumDistance = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);

    /** The speed limit. */
    DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

    /** The plots. */
    private ArrayList<LaneBasedGTUSampler> plots = new ArrayList<LaneBasedGTUSampler>();

    /** User settable properties */
    ArrayList<AbstractProperty<?>> properties = null;

    /** The sequence of Lanes that all vehicles will follow. */
    private ArrayList<List<Lane>> paths = new ArrayList<List<Lane>>();

    /** The random number generator used to decide what kind of GTU to generate. */
    Random randomGenerator = new Random(12345);

    /**
     * @param properties
     */
    public RoadSimulationModel(ArrayList<AbstractProperty<?>> properties)
    {
        this.properties = properties;
    }

    /**
     * @param index
     * @return List&lt;Lane*gt;; the set of lanes for the specified index
     */
    public List<Lane> getPath(int index)
    {
        return this.paths.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        final int laneCount = 2;
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            this.paths.add(new ArrayList<Lane>());
        }
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        double radius = 6000 / 2 / Math.PI;
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
                if (ap instanceof SelectionProperty)
                {
                    SelectionProperty sp = (SelectionProperty) ap;
                    if ("Car following model".equals(sp.getShortName()))
                    {
                        carFollowingModelName = sp.getValue();
                    }
                    else if ("Lane changing".equals(sp.getShortName()))
                    {
                        String strategyName = sp.getValue();
                        if ("Egoistic".equals(strategyName))
                        {
                            this.laneChangeModel = new Egoistic();
                        }
                        else if ("Altruistic".equals(strategyName))
                        {
                            this.laneChangeModel = new Altruistic();
                        }
                        else
                        {
                            throw new Error("Lane changing " + strategyName + " not implemented");
                        }
                    }
                }
                else if (ap instanceof ProbabilityDistributionProperty)
                {
                    ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) ap;
                    if (ap.getShortName().equals("Traffic composition"))
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
                    if (ap.getShortName().equals("Output"))
                    {
                        continue; // Output settings are handled elsewhere
                    }
                    if (ap.getShortName().contains("IDM"))
                    {
                        // System.out.println("Car following model name appears to be " + ap.getShortName());
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
                        /*
                         * System.out.println("Created " + carFollowingModelName + " for " + p.getShortName());
                         * System.out.println("a: " + a); System.out.println("b: " + b); System.out.println("s0: " +
                         * s0); System.out.println("tSafe: " + tSafe);
                         */
                    }
                }
            }
            GTUType<String> gtuType = new GTUType<String>("car");
            LaneType<String> laneType = new LaneType<String>("CarLane");
            laneType.addPermeability(gtuType);
            NodeGeotools.STR start = new NodeGeotools.STR("Start", new Coordinate(radius, 0, 0));
            NodeGeotools.STR halfway = new NodeGeotools.STR("Halfway", new Coordinate(-radius, 0, 0));

            Coordinate[] coordsHalf1 = new Coordinate[127];
            for (int i = 0; i < coordsHalf1.length; i++)
            {
                double angle = Math.PI * (1 + i) / (1 + coordsHalf1.length);
                coordsHalf1[i] = new Coordinate(radius * Math.cos(angle), radius * Math.sin(angle), 0);
            }
            Lane[] lanes1 =
                    LaneFactory.makeMultiLane("FirstHalf", start, halfway, coordsHalf1, laneCount, laneType,
                            this.simulator);
            Coordinate[] coordsHalf2 = new Coordinate[127];
            for (int i = 0; i < coordsHalf2.length; i++)
            {
                double angle = Math.PI + Math.PI * (1 + i) / (1 + coordsHalf2.length);
                coordsHalf2[i] = new Coordinate(radius * Math.cos(angle), radius * Math.sin(angle), 0);
            }
            Lane lanes2[] =
                    LaneFactory.makeMultiLane("SecondHalf", halfway, start, coordsHalf2, laneCount, laneType,
                            this.simulator);
            for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
            {
                this.paths.get(laneIndex).add(lanes1[laneIndex]);
                this.paths.get(laneIndex).add(lanes2[laneIndex]);
            }
            // Put the (not very evenly spaced) cars on the track
            double variability = (headway - 20) * headwayVariability;
            System.out.println("headway is " + headway + " variability limit is " + variability);
            Random random = new Random(12345);
            for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
            {
                double lane1Length = lanes1[laneIndex].getLength().getSI();
                double trackLength = lane1Length + lanes2[laneIndex].getLength().getSI();
                for (double pos = 0; pos <= trackLength - headway - variability;)
                {
                    Lane lane = pos >= lane1Length ? lanes2[laneIndex] : lanes1[laneIndex];
                    // Actual headway is uniformly distributed around headway
                    double laneRelativePos = pos > lane1Length ? pos - lane1Length : pos;
                    double actualHeadway = headway + (random.nextDouble() * 2 - 1) * variability;
                    generateCar(new DoubleScalar.Rel<LengthUnit>(laneRelativePos, LengthUnit.METER), lane, gtuType);
                    /*
                     * if (pos > trackLength / 4 && pos < 3 * trackLength / 4) { generateCar(new
                     * DoubleScalar.Rel<LengthUnit>(pos + headway / 2, LengthUnit.METER), laneIndex, gtuType); }
                     */
                    pos += actualHeadway;
                }
            }
            // Schedule regular updates of the graph
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(9.999, TimeUnit.SECOND), this, this,
                    "drawGraphs", null);
        }
        catch (RemoteException | SimRuntimeException | NamingException | NetworkException exception)
        {
            exception.printStackTrace();
        }
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
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(this.simulator.getSimulatorTime().get()
                    .getSI() + 10, TimeUnit.SECOND), this, this, "drawGraphs", null);
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }

    }

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     * @throws NamingException on ???
     * @throws SimRuntimeException
     * @throws NetworkException
     * @throws RemoteException
     */
    protected final void generateCar(DoubleScalar.Rel<LengthUnit> initialPosition, Lane lane, GTUType<String> gtuType)
            throws NamingException, NetworkException, SimRuntimeException, RemoteException
    {
        boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(lane, initialPosition);
        DoubleScalar.Rel<LengthUnit> vehicleLength =
                new DoubleScalar.Rel<LengthUnit>(generateTruck ? 15 : 4, LengthUnit.METER);
        new LaneBasedIndividualCar<>(++this.carsCreated, gtuType, generateTruck ? this.carFollowingModelTrucks
                : this.carFollowingModelCars, initialPositions, initialSpeed, vehicleLength,
                new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(200,
                        SpeedUnit.KM_PER_HOUR), this.simulator);
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return null;
    }

    /**
     * @return plots
     */
    public final ArrayList<LaneBasedGTUSampler> getPlots()
    {
        return this.plots;
    }

    /**
     * @return minimumDistance
     */
    public final DoubleScalar.Rel<LengthUnit> getMinimumDistance()
    {
        return this.minimumDistance;
    }

    /**
     * Stop simulation and throw an Error.
     * @param theSimulator OTSDEVSSimulatorInterface; the simulator
     * @param errorMessage String; the error message
     */
    public void stopSimulator(OTSDEVSSimulatorInterface theSimulator, String errorMessage)
    {
        System.out.println("Error: " + errorMessage);
        try
        {
            if (theSimulator.isRunning())
            {
                theSimulator.stop();
            }
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
        throw new Error(errorMessage);
    }

}
