package org.opentrafficsim.demo.carFollowing;

import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.gtu.following.IDM;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LaneType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.demo.geometry.LaneFactory;
import org.opentrafficsim.demo.geometry.Node;
import org.opentrafficsim.graphs.AccelerationContourPlot;
import org.opentrafficsim.graphs.ContourPlot;
import org.opentrafficsim.graphs.DensityContourPlot;
import org.opentrafficsim.graphs.FlowContourPlot;
import org.opentrafficsim.graphs.SpeedContourPlot;
import org.opentrafficsim.graphs.TrajectoryPlot;
import org.opentrafficsim.simulationengine.AbstractProperty;
import org.opentrafficsim.simulationengine.ControlPanel;
import org.opentrafficsim.simulationengine.IncompatiblePropertyException;
import org.opentrafficsim.simulationengine.IntegerProperty;
import org.opentrafficsim.simulationengine.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.SelectionProperty;
import org.opentrafficsim.simulationengine.SimpleSimulator;
import org.opentrafficsim.simulationengine.SimulatorFrame;
import org.opentrafficsim.simulationengine.WrappableSimulation;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Circular lane simulation demo.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 21 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class CircularLane implements WrappableSimulation
{
    /** The properties exhibited by this simulation. */
    private ArrayList<AbstractProperty<?>> properties = new ArrayList<AbstractProperty<?>>();

    /** Create a CircularLane simulation. */
    public CircularLane()
    {
        try
        {
            this.properties
                    .add(new SelectionProperty(
                            "Car following model",
                            "<html>The car following model determines "
                                    + "the acceleration that a vehicle will make taking into account nearby vehicles, infrastructural "
                                    + "restrictions (e.g. speed limit, curvature of the road) capabilities of the vehicle and "
                                    + "personality of the driver.</html>", new String[]{"IDM", "IDM+"}, 1, false));
            this.properties.add(new ProbabilityDistributionProperty("Traffic composition",
                    "<html>Mix of passenger cars and trucks</html>", new String[]{"passenger car", "truck"},
                    new Double[]{0.8, 0.2}, false));
            this.properties.add(new IntegerProperty("Track length", "Circumference of the track", 6000, 1000, 6000,
                    false));
        }
        catch (IncompatiblePropertyException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException
     * @throws RemoteException
     */
    public static void main(final String[] args) throws RemoteException, SimRuntimeException
    {
        // Create the simulation and wrap its panel in a JFrame. It does not get much easier/shorter than this...
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    new SimulatorFrame("Circular Lane animation", new CircularLane().buildSimulator().getPanel());
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
    public SimpleSimulator buildSimulator() throws RemoteException, SimRuntimeException
    {
        LaneSimulationModel model = new LaneSimulationModel(this.properties);
        SimpleSimulator result =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 1000, 1000));
        new ControlPanel(result);

        // Make the tab with the contour plots
        TablePanel charts = new TablePanel(3, 2);
        result.getPanel().getTabbedPane().addTab("statistics", charts);

        // Make the four contour plots
        ContourPlot cp;

        cp =
                new DensityContourPlot("DensityPlot " + model.carFollowingModelCars.getLongName(),
                        model.getMinimumDistance(), model.lane.getLength());
        cp.setTitle("Density Contour Graph");
        cp.setExtendedState(Frame.MAXIMIZED_BOTH);
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 0, 0);

        cp =
                new SpeedContourPlot("SpeedPlot " + model.carFollowingModelCars.getLongName(),
                        model.getMinimumDistance(), model.lane.getLength());
        cp.setTitle("Speed Contour Graph");
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 1, 0);

        cp =
                new FlowContourPlot("FlowPlot " + model.carFollowingModelCars.getLongName(),
                        model.getMinimumDistance(), model.lane.getLength());
        cp.setTitle("FLow Contour Graph");
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 0, 1);

        cp =
                new AccelerationContourPlot("AccelerationPlot " + model.carFollowingModelCars.getLongName(),
                        model.getMinimumDistance(), model.lane.getLength());
        cp.setTitle("Acceleration Contour Graph");
        model.getContourPlots().add(cp);
        charts.setCell(cp.getContentPane(), 1, 1);

        TrajectoryPlot trajectoryPlot =
                new TrajectoryPlot("TrajectoryPlot " + model.carFollowingModelCars.getLongName(),
                        new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), model.getMinimumDistance(),
                        model.lane.getLength());
        trajectoryPlot.setTitle("Trajectories");
        charts.setCell(trajectoryPlot.getContentPane(), 2, 0);
        model.getTrajectoryPlots().add(trajectoryPlot);

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public String shortName()
    {
        return "Circular Lane simulation";
    }

    /** {@inheritDoc} */
    @Override
    public String description()
    {
        return "<html><h1>Circular Lane simulation</h1>"
                + "Vehicles are unequally distributed over a one lane ring road.<br />"
                + "When simulation starts, all vehicles begin driving and some shockwaves should develop.<br />"
                + "Trajectories and contourplots are generated during the simulation.</html>";
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<AbstractProperty<?>> getProperties()
    {
        return new ArrayList<AbstractProperty<?>>(this.properties);
    }

}

/**
 * Simulate traffic on a circular, one-lane road.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 21 nov. 2014 <br>
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

    /** the car following model, e.g. IDM Plus for cars. */
    protected GTUFollowingModel carFollowingModelCars;

    /** the car following model, e.g. IDM Plus for trucks. */
    protected GTUFollowingModel carFollowingModelTrucks;

    /** The probability that the next generated GTU is a passenger car. */
    double carProbability;

    /** cars in the model. */
    ArrayList<Car<Integer>> cars = new ArrayList<Car<Integer>>();

    /** minimum distance. */
    private DoubleScalar.Rel<LengthUnit> minimumDistance = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);

    /** The Lane that contains the simulated Cars. */
    Lane lane;

    /** the speed limit. */
    DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

    /** the contour plots. */
    private ArrayList<ContourPlot> contourPlots = new ArrayList<ContourPlot>();

    /** the trajectory plot. */
    private ArrayList<TrajectoryPlot> trajectoryPlots = new ArrayList<TrajectoryPlot>();

    /** User settable properties */
    ArrayList<AbstractProperty<?>> properties = null;

    /** The random number generator used to decide what kind of GTU to generate. */
    Random randomGenerator = new Random(12345);

    /**
     * @param properties
     */
    public LaneSimulationModel(ArrayList<AbstractProperty<?>> properties)
    {
        this.properties = properties;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        double radius = 6000 / 2 / Math.PI;
        try
        {
            for (AbstractProperty<?> p : this.properties)
            {
                if (p instanceof SelectionProperty)
                {
                    SelectionProperty sp = (SelectionProperty) p;
                    if ("Car following model".equals(sp.getShortName()))
                    {
                        String modelName = sp.getValue();
                        if (modelName.equals("IDM"))
                        {
                            this.carFollowingModelCars =
                                    new IDM(new DoubleScalar.Abs<AccelerationUnit>(1,
                                            AccelerationUnit.METER_PER_SECOND_2),
                                            new DoubleScalar.Abs<AccelerationUnit>(1.5,
                                                    AccelerationUnit.METER_PER_SECOND_2),
                                            new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER),
                                            new DoubleScalar.Rel<TimeUnit>(1, TimeUnit.SECOND), 1d);
                            this.carFollowingModelTrucks =
                                    new IDM(new DoubleScalar.Abs<AccelerationUnit>(0.5,
                                            AccelerationUnit.METER_PER_SECOND_2),
                                            new DoubleScalar.Abs<AccelerationUnit>(1.5,
                                                    AccelerationUnit.METER_PER_SECOND_2),
                                            new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER),
                                            new DoubleScalar.Rel<TimeUnit>(1, TimeUnit.SECOND), 1d);
                        }
                        else if (modelName.equals("IDM+"))
                        {
                            this.carFollowingModelCars =
                                    new IDMPlus(new DoubleScalar.Abs<AccelerationUnit>(1,
                                            AccelerationUnit.METER_PER_SECOND_2),
                                            new DoubleScalar.Abs<AccelerationUnit>(1.5,
                                                    AccelerationUnit.METER_PER_SECOND_2),
                                            new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER),
                                            new DoubleScalar.Rel<TimeUnit>(1, TimeUnit.SECOND), 1d);
                            this.carFollowingModelTrucks =
                                    new IDMPlus(new DoubleScalar.Abs<AccelerationUnit>(0.5,
                                            AccelerationUnit.METER_PER_SECOND_2),
                                            new DoubleScalar.Abs<AccelerationUnit>(1.5,
                                                    AccelerationUnit.METER_PER_SECOND_2),
                                            new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER),
                                            new DoubleScalar.Rel<TimeUnit>(1, TimeUnit.SECOND), 1d);
                        }
                        else
                        {
                            throw new Error("Car following model " + modelName + " not implemented");
                        }
                    }
                    else
                    {
                        throw new Error("Unhandled SelectionProperty " + p.getShortName());
                    }
                }
                else if (p instanceof ProbabilityDistributionProperty)
                {
                    ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) p;
                    String modelName = p.getShortName();
                    if (modelName.equals("Traffic composition"))
                    {
                        this.carProbability = pdp.getValue()[0];
                    }
                    else
                    {
                        throw new Error("Unhandled ProbabilityDistributionProperty " + p.getShortName());
                    }
                }
                else if (p instanceof IntegerProperty)
                {
                    IntegerProperty ip = (IntegerProperty) p;
                    if ("Track length".equals(ip.getShortName()))
                    {
                        radius = ip.getValue() / 2 / Math.PI;
                    }
                    else
                    {
                        throw new Error("Unhandled IntegerProperty " + ip.getShortName());
                    }
                }
                else
                {
                    throw new Error("Unhandled property: " + p);
                }
            }
            Node startEnd = new Node("Start/End", new Coordinate(radius, 0, 0));
            Coordinate[] intermediateCoordinates = new Coordinate[255];
            for (int i = 0; i < intermediateCoordinates.length; i++)
            {
                double angle = 2 * Math.PI * (1 + i) / (1 + intermediateCoordinates.length);
                intermediateCoordinates[i] = new Coordinate(radius * Math.cos(angle), radius * Math.sin(angle), 0);
            }
            LaneType<String> laneType = new LaneType<String>("CarLane");
            // this.lane = LaneFactory.makeLane("Lane", startEnd, startEnd, intermediateCoordinates, laneType,
            // this.simulator);
            this.lane =
                    LaneFactory.makeMultiLane("Lane", startEnd, startEnd, intermediateCoordinates, 1, laneType,
                            this.simulator)[0];
            // Put the (not very evenly spaced) cars on the track
            double trackLength = this.lane.getLength().getSI();
            double headway = 40;
            for (double pos = 0; pos <= trackLength - headway; pos += headway)
            {
                generateCar(new DoubleScalar.Rel<LengthUnit>(pos, LengthUnit.METER));
                if (pos > trackLength / 4 && pos < 3 * trackLength / 4)
                {
                    generateCar(new DoubleScalar.Rel<LengthUnit>(pos + headway / 2, LengthUnit.METER));
                }
            }
            // Schedule regular updates of the graph
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.999, TimeUnit.SECOND), this, this,
                    "drawGraphs", null);
        }
        catch (RemoteException | SimRuntimeException | NamingException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Add one movement step of one Car to all plots.
     * @param car Car
     * @throws RemoteException on communications failure
     */
    protected final void addToPlots(final Car<?> car) throws RemoteException
    {
        for (ContourPlot contourPlot : this.contourPlots)
        {
            contourPlot.addData(car);
        }
        for (TrajectoryPlot trajectoryPlot : this.trajectoryPlots)
        {
            trajectoryPlot.addData(car);
        }
    }

    /**
     * Notify the contour plots that the underlying data has changed.
     */
    protected final void drawGraphs()
    {
        for (ContourPlot contourPlot : this.contourPlots)
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
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(this.simulator.getSimulatorTime().get()
                    .getSI() + 1, TimeUnit.SECOND), this, this, "drawGraphs", null);
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }

    }

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     */
    protected final void generateCar(DoubleScalar.Rel<LengthUnit> initialPosition)
    {
        boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(this.lane, initialPosition);
        try
        {
            DoubleScalar.Rel<LengthUnit> vehicleLength =
                    new DoubleScalar.Rel<LengthUnit>(generateTruck ? 15 : 4, LengthUnit.METER);
            IDMCar car =
                    new IDMCar(++this.carsCreated, null, this.simulator, generateTruck ? this.carFollowingModelTrucks
                            : this.carFollowingModelCars, vehicleLength, this.simulator.getSimulatorTime().get(),
                            initialPositions, initialSpeed);
            this.cars.add(car);
        }
        catch (RemoteException | NamingException exception)
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
    public final ArrayList<ContourPlot> getContourPlots()
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

    /** Inner class IDMCar. */
    protected class IDMCar extends Car<Integer>
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
         * @param initialLongitudinalPositions Map&lt;Lane, DoubleScalar.Rel&lt;LengthUnit&gt;&gt;; the initial lane
         *            positions of the new IDMCar
         * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the new IDMCar
         * @throws NamingException on ???
         * @throws RemoteException on Communications failure
         */
        public IDMCar(final int id, GTUType<String> gtuType, final OTSDEVSSimulatorInterface simulator,
                final GTUFollowingModel carFollowingModel, DoubleScalar.Rel<LengthUnit> vehicleLength,
                final DoubleScalar.Abs<TimeUnit> initialTime,
                final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
                final DoubleScalar.Abs<SpeedUnit> initialSpeed) throws RemoteException, NamingException
        {
            super(id, gtuType, vehicleLength, new DoubleScalar.Rel<LengthUnit>(1.8, LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(200, SpeedUnit.KM_PER_HOUR), carFollowingModel, initialLongitudinalPositions,
                    initialSpeed, simulator);
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
            // System.out.println("move " + getId());
            if (this.getId() < 0)
            {
                return;
            }
            Collection<Car<Integer>> leaders = new ArrayList<Car<Integer>>();
            // FIXME: there should be a much easier way to obtain the leader; we should not have to maintain our own
            // list
            int carIndex = LaneSimulationModel.this.cars.indexOf(this);
            if (carIndex < LaneSimulationModel.this.cars.size() - 1)
            {
                leaders.add(LaneSimulationModel.this.cars.get(carIndex + 1));
            }
            else
            {
                leaders.add(LaneSimulationModel.this.cars.get(0));
            }
            // Horrible hack; wrap the position back to zero when vehicle exceeds length of the circuit
            if (this.positionOfFront().getLongitudinalPosition().getSI() > LaneSimulationModel.this.lane.getLength()
                    .getSI())
            {
                Map<Lane, DoubleScalar.Rel<LengthUnit>> map = this.getLongitudinalPositions();
                for (Lane l : map.keySet())
                {
                    map.put(l, new DoubleScalar.Rel<LengthUnit>(map.get(l).getSI()
                            % LaneSimulationModel.this.lane.getLength().getSI(), LengthUnit.METER));
                }
            }
            // Even more horrible hack; create a fake leader for the vehicle closest to the wrap around point
            Car<Integer> leader = leaders.iterator().next();
            // Figure out the headway
            if (leader.positionOfRear(LaneSimulationModel.this.lane).getSI() < this.positionOfFront(
                    LaneSimulationModel.this.lane).getSI())
            {
                Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions =
                        new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
                initialPositions.put(LaneSimulationModel.this.lane, new DoubleScalar.Rel<LengthUnit>(leader
                        .positionOfFront(LaneSimulationModel.this.lane, getNextEvaluationTime()).getSI()
                        + LaneSimulationModel.this.lane.getLength().getSI(), LengthUnit.METER));
                try
                {
                    IDMCar fakeLeader =
                            new IDMCar(-99999, null, this.getSimulator(), this.getGTUFollowingModel(),
                                    leader.getLength(), this.getSimulator().getSimulatorTime().get(), initialPositions,
                                    leader.getLongitudinalVelocity());
                    leaders.add(fakeLeader);
                    /*-
                    if (getSimulator().getSimulatorTime().get().getSI() > 300)
                    {
                        System.out.println("follower:   " + this);
                        System.out.println("leader:     " + leader);
                        System.out.println("fakeLeader: " + fakeLeader);
                        System.out.println("headway:    "
                                + (fakeLeader.positionOfRear(SimulationModel.this.lane).getSI() - positionOfFront(
                                        SimulationModel.this.lane).getSI()) + "m");
                    }
                     */
                }
                catch (RemoteException exception)
                {
                    exception.printStackTrace();
                }
            }
            GTUFollowingModelResult cfmr =
                    this.getGTUFollowingModel().computeAcceleration(this, leaders, LaneSimulationModel.this.speedLimit);
            setState(cfmr);
            // Add the movement of this Car to the contour plots
            addToPlots(this);
            // Schedule the next evaluation of this car
            getSimulator().scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), this, this, "move",
                    null);
        }
    }

}
