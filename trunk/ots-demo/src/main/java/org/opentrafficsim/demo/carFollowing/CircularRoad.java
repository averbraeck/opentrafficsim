package org.opentrafficsim.demo.carFollowing;

import java.awt.Container;
import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.opentrafficsim.core.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.core.gtu.lane.changing.AbstractLaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.Altruistic;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.factory.Node;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneLocation;
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
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
                + "the personal gain).</html>", new String[] {"Egoistic", "Altruistic"}, 0, false, 500));
        this.properties.add(new IntegerProperty("Track length", "Circumference of the track", 2000, 500, 6000,
            "Track length %dm", false, 10));
        this.properties.add(new ContinuousProperty("Mean density", "Number of vehicles per km", 40.0, 5.0, 45.0,
            "Density %.1f veh/km", false, 11));
        this.properties.add(new ContinuousProperty("Density variability", "Variability of the number of vehicles per km",
            0.0, 0.0, 1.0, "%.1f", false, 12));
        ArrayList<AbstractProperty<?>> outputProperties = new ArrayList<AbstractProperty<?>>();
        for (int lane = 1; lane <= 2; lane++)
        {
            String laneId = String.format("Lane %d ", lane);
            outputProperties.add(new BooleanProperty(laneId + "Density", laneId + "Density contour plot", true, false, 0));
            outputProperties.add(new BooleanProperty(laneId + "Flow", laneId + "Flow contour plot", true, false, 1));
            outputProperties.add(new BooleanProperty(laneId + "Speed", laneId + "Speed contour plot", true, false, 2));
            outputProperties.add(new BooleanProperty(laneId + "Acceleration", laneId + "Acceleration contour plot", true,
                false, 3));
            outputProperties.add(new BooleanProperty(laneId + "Trajectories", laneId + "Trajectory (time/distance) diagram",
                true, false, 4));
        }
        this.properties.add(new CompoundProperty("Output", "Select the graphical output", outputProperties, true, 1000));
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
                            "<html>Mix of passenger cars and trucks</html>", new String[] {"passenger car", "truck"},
                            new Double[] {0.8, 0.2}, false, 10));
                    }
                    catch (PropertyException exception)
                    {
                        exception.printStackTrace();
                    }
                    properties.add(new SelectionProperty("Car following model", "<html>The car following model determines "
                        + "the acceleration that a vehicle will make taking into account "
                        + "nearby vehicles, infrastructural restrictions (e.g. speed limit, "
                        + "curvature of the road) capabilities of the vehicle and personality " + "of the driver.</html>",
                        new String[] {"IDM", "IDM+"}, 1, false, 1));
                    properties.add(IDMPropertySet.makeIDMPropertySet("Car", new DoubleScalar.Abs<AccelerationUnit>(1.0,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<AccelerationUnit>(1.5,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER),
                        new DoubleScalar.Rel<TimeUnit>(1.0, TimeUnit.SECOND), 2));
                    properties.add(IDMPropertySet.makeIDMPropertySet("Truck", new DoubleScalar.Abs<AccelerationUnit>(0.5,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<AccelerationUnit>(1.25,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER),
                        new DoubleScalar.Rel<TimeUnit>(1.0, TimeUnit.SECOND), 3));
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
    public SimpleSimulator buildSimulator(ArrayList<AbstractProperty<?>> userModifiedProperties) throws RemoteException,
        SimRuntimeException
    {
        RoadSimulationModel model = new RoadSimulationModel(userModifiedProperties);
        final SimpleSimulator result =
            new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND),
                new DoubleScalar.Rel<TimeUnit>(3600.0, TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 2000,
                    2000));
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
                List<Lane> path = new ArrayList<Lane>();
                path.add(model.lanes[lane]);
                TrajectoryPlot tp =
                    new TrajectoryPlot(graphName, new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), path);
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
                    cp = new DensityContourPlot(graphName, model.getMinimumDistance(), model.lanes[lane].getLength());
                    cp.setTitle("Density Contour Graph");
                }
                else if (graphName.contains("Speed"))
                {
                    cp = new SpeedContourPlot(graphName, model.getMinimumDistance(), model.lanes[lane].getLength());
                    cp.setTitle("Speed Contour Graph");
                }
                else if (graphName.contains("Flow"))
                {
                    cp = new FlowContourPlot(graphName, model.getMinimumDistance(), model.lanes[lane].getLength());
                    cp.setTitle("Flow Contour Graph");
                }
                else if (graphName.contains("Acceleration"))
                {
                    cp = new AccelerationContourPlot(graphName, model.getMinimumDistance(), model.lanes[lane].getLength());
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
            model.getPlots().get(lane).add(graph);
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
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** Cars in each lane. */
    ArrayList<ArrayList<LaneBasedIndividualCar<Integer>>> cars;

    /** Minimum distance. */
    private DoubleScalar.Rel<LengthUnit> minimumDistance = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);

    /** The Lanes that contains the simulated Cars. */
    Lane[] lanes;

    /** The speed limit. */
    DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

    /** The plots. */
    private ArrayList<ArrayList<LaneBasedGTUSampler>> plots = new ArrayList<ArrayList<LaneBasedGTUSampler>>();

    /** User settable properties */
    ArrayList<AbstractProperty<?>> properties = null;

    /** The random number generator used to decide what kind of GTU to generate. */
    Random randomGenerator = new Random(12345);

    /**
     * @param properties
     */
    public RoadSimulationModel(ArrayList<AbstractProperty<?>> properties)
    {
        this.properties = properties;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
        throws SimRuntimeException, RemoteException
    {
        final int laneCount = 2;
        this.cars = new ArrayList<ArrayList<LaneBasedIndividualCar<Integer>>>(laneCount);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            this.cars.add(new ArrayList<LaneBasedIndividualCar<Integer>>());
            this.plots.add(new ArrayList<LaneBasedGTUSampler>());
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
                         * System.out.println("a: " + a); System.out.println("b: " + b); System.out.println("s0: " + s0);
                         * System.out.println("tSafe: " + tSafe);
                         */
                    }
                }
            }
            GTUType<String> gtuType = new GTUType<String>("car");
            LaneType<String> laneType = new LaneType<String>("CarLane");
            laneType.addPermeability(gtuType);
            Node startEnd = new Node("Start/End", new Coordinate(radius, 0, 0));
            Coordinate[] intermediateCoordinates = new Coordinate[255];
            for (int i = 0; i < intermediateCoordinates.length; i++)
            {
                double angle = 2 * Math.PI * (1 + i) / (1 + intermediateCoordinates.length);
                intermediateCoordinates[i] = new Coordinate(radius * Math.cos(angle), radius * Math.sin(angle), 0);
            }
            this.lanes =
                LaneFactory.makeMultiLane("Circular Link with " + laneCount + " lanes", startEnd, startEnd,
                    intermediateCoordinates, laneCount, laneType, this.simulator);
            // Put the (not very evenly spaced) cars on the track
            double variability = (headway - 20) * headwayVariability;
            System.out.println("headway is " + headway + " variability limit is " + variability);
            Random random = new Random(12345);
            for (int laneIndex = 0; laneIndex < this.lanes.length; laneIndex++)
            {
                double trackLength = this.lanes[laneIndex].getLength().getSI();
                for (double pos = 0; pos <= trackLength - headway - variability;)
                {
                    // Actual headway is uniformly distributed around headway
                    double actualHeadway = headway + (random.nextDouble() * 2 - 1) * variability;
                    generateCar(new DoubleScalar.Rel<LengthUnit>(pos, LengthUnit.METER), laneIndex, gtuType);
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
            checkOrdering(RoadSimulationModel.this.cars.get(0));
            checkOrdering(RoadSimulationModel.this.cars.get(1));
        }
        catch (RemoteException | SimRuntimeException | NamingException | NetworkException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add one movement step of one Car to all plots.
     * @param car Car
     * @throws NetworkException when car not in lane
     * @throws RemoteException on communications failure
     */
    protected final void addToPlots(final LaneBasedIndividualCar<?> car, int lane) throws NetworkException, RemoteException
    {
        for (LaneBasedGTUSampler plot : this.plots.get(lane))
        {
            plot.addData(car);
        }
    }

    /**
     * Notify the contour plots that the underlying data has changed.
     */
    protected final void drawGraphs()
    {
        for (ArrayList<LaneBasedGTUSampler> lanePlots : this.plots)
        {
            for (LaneBasedGTUSampler plot : lanePlots)
            {
                plot.reGraph();
            }
        }
        // Re schedule this method
        try
        {
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(
                this.simulator.getSimulatorTime().get().getSI() + 10, TimeUnit.SECOND), this, this, "drawGraphs", null);
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
     */
    protected final void generateCar(DoubleScalar.Rel<LengthUnit> initialPosition, int laneIndex, GTUType<String> gtuType)
        throws NamingException, NetworkException, SimRuntimeException
    {
        boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(this.lanes[laneIndex], initialPosition);
        try
        {
            DoubleScalar.Rel<LengthUnit> vehicleLength =
                new DoubleScalar.Rel<LengthUnit>(generateTruck ? 15 : 4, LengthUnit.METER);
            IDMCar car =
                new IDMCar(++this.carsCreated, gtuType, this.simulator, generateTruck ? this.carFollowingModelTrucks
                    : this.carFollowingModelCars, vehicleLength, this.simulator.getSimulatorTime().get(), initialPositions,
                    initialSpeed);
            this.cars.get(laneIndex).add(car);
        }
        catch (RemoteException exception)
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
     * @return plots
     */
    public final ArrayList<ArrayList<LaneBasedGTUSampler>> getPlots()
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
     * Inner class IDMCar.
     */
    protected class IDMCar extends LaneBasedIndividualCar<Integer>
    {
        /** */
        private static final long serialVersionUID = 20141030L;

        /**
         * Create a new IDMCar.
         * @param id integer; the id of the new IDMCar
         * @param gtuType GTUType&lt;String&gt;; the type of the GTU
         * @param simulator OTSDEVSSimulator; the simulator that runs the new IDMCar
         * @param carFollowingModel CarFollowingModel; the car following model of the new IDMCar
         * @param vehicleLength DoubleScalar.Rel&lt;LengthUnit&gt;; the length of the new IDMCar
         * @param initialTime DoubleScalar.Abs&lt;TimeUnit&gt;; the time of first evaluation of the new IDMCar
         * @param initialLongitudinalPositions Map&lt;Lane, DoubleScalar.Rel&lt;LengthUnit&gt;&gt;; the initial lane positions
         *            of the new IDMCar
         * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the new IDMCar
         * @throws NamingException on ???
         * @throws RemoteException on communication failure
         * @throws SimRuntimeException
         * @throws NetworkException
         */
        public IDMCar(final int id, GTUType<String> gtuType, final OTSDEVSSimulatorInterface simulator,
            final GTUFollowingModel carFollowingModel, DoubleScalar.Rel<LengthUnit> vehicleLength,
            final DoubleScalar.Abs<TimeUnit> initialTime,
            final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
            final DoubleScalar.Abs<SpeedUnit> initialSpeed) throws RemoteException, NamingException, NetworkException,
            SimRuntimeException
        {
            super(id, gtuType, carFollowingModel, initialLongitudinalPositions, initialSpeed, vehicleLength,
                new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(200,
                    SpeedUnit.KM_PER_HOUR), simulator);
            try
            {
                if (id >= 0)
                {
                    simulator.scheduleEventAbs(simulator.getSimulatorTime(), this, this, "move", null);
                }
            }
            catch (SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
            // System.out.println("Created IDMCar " + id);
        }

        /**
         * Determine the movement of this car.
         * @throws RemoteException RemoteException
         * @throws NamingException on ???
         * @throws NetworkException on network inconsistency
         * @throws SimRuntimeException on ???
         */
        protected final void move() throws RemoteException, NamingException, NetworkException, SimRuntimeException
        {
            wrap();
            // System.out.println("move " + getId());
            if (this.getId() < 0)
            {
                return;
            }
            // FIXME: there should be a much easier way to obtain the leader; we should not have to maintain our own
            // list
            Lane lane = positions(getFront()).keySet().iterator().next();
            int laneIndex = -1;
            for (int i = 0; i < RoadSimulationModel.this.lanes.length; i++)
            {
                if (lane == RoadSimulationModel.this.lanes[i])
                {
                    laneIndex = i;
                }
            }
            if (laneIndex < 0)
            {
                throw new Error("Cannot find lane of vehicle " + this);
            }
            DoubleScalar.Abs<TimeUnit> when = getSimulator().getSimulatorTime().get();
            DoubleScalar.Rel<LengthUnit> longitudinalPosition = position(lane, getFront(), when);
            double relativePosition = longitudinalPosition.getSI() / lane.getLength().getSI();
            Collection<AbstractLaneBasedGTU<?>> sameLaneTraffic = carsInSpecifiedLane(laneIndex);
            Collection<AbstractLaneBasedGTU<?>> leftLaneTraffic = carsInSpecifiedLane(laneIndex - 1);
            Collection<AbstractLaneBasedGTU<?>> rightLaneTraffic = carsInSpecifiedLane(laneIndex + 1);
            LaneChangeModel.LaneChangeModelResult lcmr =
                RoadSimulationModel.this.laneChangeModel.computeLaneChangeAndAcceleration(this, sameLaneTraffic,
                    rightLaneTraffic, leftLaneTraffic, RoadSimulationModel.this.speedLimit,
                    new DoubleScalar.Rel<AccelerationUnit>(0.3, AccelerationUnit.METER_PER_SECOND_2),
                    new DoubleScalar.Rel<AccelerationUnit>(0.1, AccelerationUnit.METER_PER_SECOND_2),
                    new DoubleScalar.Rel<AccelerationUnit>(-0.3, AccelerationUnit.METER_PER_SECOND_2));
            // System.out.println("lane change result of " + this + ": " + lcmr);
            if (lcmr.getLaneChange() != null)
            {
                // Remember at what ratio on the old lane the vehicle was at the PREVIOUS time step
                // FIXME: the longitudinal positions map should not be editable from the outside...
                Map<Lane, Rel<LengthUnit>> longitudinalPositions = positions(getFront());
                DoubleScalar.Rel<LengthUnit> oldPosition = longitudinalPositions.get(lane);
                double oldRatio = oldPosition.getSI() / lane.getLength().getSI();
                // Remove vehicle from it's current lane
                RoadSimulationModel.this.cars.get(laneIndex).remove(this);
                // Figure out where to insert it in the target lane
                laneIndex += lcmr.getLaneChange().equals(LateralDirectionality.LEFT) ? -1 : +1;
                ArrayList<LaneBasedIndividualCar<Integer>> carsInLane = RoadSimulationModel.this.cars.get(laneIndex);
                lane = RoadSimulationModel.this.lanes[laneIndex];
                int pivot = pivot(relativePosition, carsInLane);
                // Insert vehicle
                // System.out.println("Inserting car " + this.getId() + " at position " + pivot);
                carsInLane.add(pivot, this);
                longitudinalPositions.clear();
                // Put the vehicle in the new lane at the ratio that it was at the PREVIOUS time step
                // The reason for this is that the vehicle is moved forward in setState below and setState requires
                // that the location has not yet been updated.
                longitudinalPositions.put(lane, new DoubleScalar.Rel<LengthUnit>(oldRatio * lane.getLength().getSI(),
                    LengthUnit.METER));
                checkOrdering(carsInLane);
            }
            setState(lcmr.getGfmr());
            checkOrdering(RoadSimulationModel.this.cars.get(0));
            checkOrdering(RoadSimulationModel.this.cars.get(1));
            // Add the movement of this Car to the contour plots
            addToPlots(this, laneIndex);
            // System.out.println("Moved " + this);
            // Schedule the next evaluation of this car
            getSimulator().scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), this, this, "move", null);
            // printList(0);
            // printList(1);
        }
    }

    /**
     * Wrap cars that are now beyond the length of their circular lane.
     */
    public void wrap()
    {
        DoubleScalar.Abs<TimeUnit> when = null;
        try
        {
            when = RoadSimulationModel.this.simulator.getSimulatorTime().get();
        }
        catch (RemoteException exception1)
        {
            exception1.printStackTrace();
        }
        for (int laneIndex = 0; laneIndex < RoadSimulationModel.this.cars.size(); laneIndex++)
        {
            Lane lane = RoadSimulationModel.this.lanes[laneIndex];
            ArrayList<LaneBasedIndividualCar<Integer>> carsInLane = RoadSimulationModel.this.cars.get(laneIndex);
            int vehicleIndex = carsInLane.size() - 1;
            if (vehicleIndex < 0)
            {
                continue;
            }
            while (true)
            {
                LaneBasedIndividualCar<Integer> car = carsInLane.get(vehicleIndex);
                LaneLocation ll = null;
                try
                {
                    ll = new LaneLocation(lane, car.position(lane, car.getFront(), when));
                }
                catch (NetworkException exception)
                {
                    exception.printStackTrace();
                }
                if (ll.getFractionalLongitudinalPosition() >= 1)
                {
                    // Fix the RelativePositions
                    // It is wrong that we can modify it, but for now we'll make use of that mistake...
                    try
                    {
                        // FIXME: the longitudinal positions map should not be editable from the outside...
                        Map<Lane, Rel<LengthUnit>> relativePositions = car.positions(car.getFront());
                        double relativePosition = relativePositions.get(lane).getSI() / lane.getLength().getSI();
                        // System.out.println("Wrapping car " + car.getId() + " in lane " + laneIndex +
                        // " back to position 0");
                        relativePositions.clear();
                        relativePosition -= 1;
                        relativePositions.put(lane, new DoubleScalar.Rel<LengthUnit>(relativePosition, LengthUnit.METER));
                        carsInLane.remove(car);
                        carsInLane.add(0, car);
                        checkOrdering(carsInLane);
                        addToPlots(car, laneIndex);
                    }
                    catch (RemoteException | NetworkException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                else
                {
                    break;
                }
            }
        }
    }

    /**
     * Return the index of the car in the lane where a GTU at relativePosition should be inserted.
     * @param relativePosition double; between 0 (begin of the lane) and 1 (end of the lane)
     * @param carsInLane ArrayList&lt;AnimatedCar&gt;; the cars in the lane
     * @return int
     */
    public int pivot(double relativePosition, ArrayList<LaneBasedIndividualCar<Integer>> carsInLane)
    {
        if (carsInLane.size() == 0)
        {
            return 0;
        }
        try
        {
            DoubleScalar.Abs<TimeUnit> when = carsInLane.get(0).getSimulator().getSimulatorTime().get();
            Lane lane = carsInLane.get(0).positions(carsInLane.get(0).getFront()).keySet().iterator().next();
            double laneLength = lane.getLength().getSI();
            int result;
            for (result = 0; result < carsInLane.size(); result++)
            {
                LaneBasedIndividualCar<Integer> pivotCar = carsInLane.get(result);
                double pivotRelativePosition = pivotCar.position(lane, pivotCar.getFront(), when).getSI() / laneLength;
                if (pivotRelativePosition > relativePosition)
                {
                    break;
                }
            }
            // System.out.println("pivot is " + result + " carsInLane.size is " + carsInLane.size());
            return result;
        }
        catch (RemoteException | NetworkException exception)
        {
            exception.printStackTrace();
        }
        throw new Error("Oops");
    }

    /**
     * Find the leader and follower for a given relative position in the indicated lane.
     * @param laneIndex int; the index of the lane
     * @return ArrayList<AnimatedCar> containing the immediate leader and follower near relativePosition
     */
    @SuppressWarnings("unchecked")
    Collection<AbstractLaneBasedGTU<?>> carsInSpecifiedLane(int laneIndex)
    {
        ArrayList<AbstractLaneBasedGTU<?>> result = new ArrayList<AbstractLaneBasedGTU<?>>();
        if (laneIndex < 0 || laneIndex >= RoadSimulationModel.this.lanes.length)
        {
            return result;
        }
        Lane lane = RoadSimulationModel.this.lanes[laneIndex];
        result.addAll(RoadSimulationModel.this.cars.get(laneIndex));
        if (0 == result.size())
        {
            return result;
        }
        try
        {
            final double laneLength = lane.getLength().getSI();
            // Add a wrapped copy of the last car at the beginning
            LaneBasedIndividualCar<Integer> prototype = (LaneBasedIndividualCar<Integer>) result.get(result.size() - 1);
            Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
            DoubleScalar.Abs<TimeUnit> when = RoadSimulationModel.this.simulator.getSimulatorTime().get();
            double position = prototype.position(lane, prototype.getFront(), when).getSI();
            if (position > 0)
            {
                position -= laneLength;
            }
            initialPositions.put(lane, new DoubleScalar.Rel<LengthUnit>(position, LengthUnit.METER));
            // HACK FIXME (negative length trick)
            IDMCar fakeFollower =
                new IDMCar(-10000 - prototype.getId(), (GTUType<String>) prototype.getGTUType(), prototype.getSimulator(),
                    prototype.getGTUFollowingModel(), new DoubleScalar.Rel<LengthUnit>(-prototype.getLength().getSI(),
                        LengthUnit.METER), when, initialPositions, prototype.getLongitudinalVelocity());
            result.add(0, fakeFollower);
            // Add a wrapped copy of the first (now second) car at the end
            prototype = (LaneBasedIndividualCar<Integer>) result.get(1);
            position = prototype.position(lane, prototype.getFront(), when).getSI();
            if (position < laneLength)
            {
                position += laneLength;
            }
            initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
            initialPositions.put(lane, new DoubleScalar.Rel<LengthUnit>(position, LengthUnit.METER));
            // HACK FIXME (negative length trick)
            IDMCar fakeLeader =
                new IDMCar(-20000 - prototype.getId(), (GTUType<String>) prototype.getGTUType(), prototype.getSimulator(),
                    prototype.getGTUFollowingModel(), new DoubleScalar.Rel<LengthUnit>(-prototype.getLength().getSI(),
                        LengthUnit.METER), when, initialPositions, prototype.getLongitudinalVelocity());
            result.add(fakeLeader);
        }
        catch (RemoteException | NetworkException | NamingException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
        return result;
    }

    /**
     * Sanity checks.
     * @param list ArrayList&lt;AnimatedCar&gt;; the array of cars to check
     */
    public void checkOrdering(ArrayList<LaneBasedIndividualCar<Integer>> list)
    {
        if (list.size() == 0)
        {
            return;
        }
        try
        {
            LaneBasedIndividualCar<Integer> first = list.get(0);
            Lane lane = first.positions(first.getFront()).keySet().iterator().next();
            DoubleScalar.Abs<TimeUnit> when = first.getSimulator().getSimulatorTime().get();
            double position = first.position(lane, first.getFront(), when).getSI();
            for (int rank = 1; rank < list.size(); rank++)
            {
                LaneBasedIndividualCar<Integer> other = list.get(rank);
                Lane otherLane = other.positions(other.getFront()).keySet().iterator().next();
                if (lane != otherLane)
                {
                    printList(this.cars.indexOf(list));
                    stopSimulator(first.getSimulator(), "cars are not all in the same lane");
                }
                double otherPosition = other.position(lane, other.getFront(), when).getSI();
                if (otherPosition <= position)
                {
                    printList(this.cars.indexOf(list));
                    stopSimulator(first.getSimulator(), "cars are not correctly ordered: " + first + " should be ahead of "
                        + other);
                }
                first = other;
                position = otherPosition;
            }
        }
        catch (RemoteException | NetworkException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Print a list of cars on the console.
     * @param laneIndex int; index of the lane whereof the cars must be printed
     */
    public void printList(int laneIndex)
    {
        ArrayList<LaneBasedIndividualCar<Integer>> list = this.cars.get(laneIndex);
        for (int rank = 0; rank < list.size(); rank++)
        {
            LaneBasedIndividualCar<Integer> car = list.get(rank);
            try
            {
                double relativePosition =
                    car.position(this.lanes[laneIndex], car.getFront(), this.simulator.getSimulatorTime().get()).getSI()
                        / this.lanes[laneIndex].getLength().getSI();
                System.out.println(String.format("lane %d rank %2d relpos %7.5f: %s", laneIndex, rank, relativePosition, car
                    .toString()));
            }
            catch (RemoteException | NetworkException exception)
            {
                exception.printStackTrace();
            }
        }
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
