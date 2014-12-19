package org.opentrafficsim.demo.IDMPlus.swing;

import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.lanechanging.AbstractLaneChangeModel;
import org.opentrafficsim.car.lanechanging.Altruistic;
import org.opentrafficsim.car.lanechanging.Egoistic;
import org.opentrafficsim.car.lanechanging.LaneChangeModel;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.AbstractLaneBasedGTU;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.IDM;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.network.Lane;
import org.opentrafficsim.core.network.LaneType;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.LinkLocation;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.demo.IDMPlus.swing.animation.AnimatedCar;
import org.opentrafficsim.demo.IDMPlus.swing.animation.CarAnimation;
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
import org.opentrafficsim.simulationengine.ProbabilityDistributionProperty;
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
        try
        {
            this.properties.add(new ProbabilityDistributionProperty("Traffic composition",
                    "<html>Mix of passenger cars and trucks</html>", new String[]{"passenger car", "truck"},
                    new Double[]{0.8, 0.2}, false));
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
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    new SimulatorFrame("Circular Road animation", new CircularRoad().buildSimulator().getPanel());
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
        RoadSimulationModel model = new RoadSimulationModel();
        final SimpleSimulator result =
                new SimpleSimulator(new OTSSimTimeDouble(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND)),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), model, new Rectangle2D.Double(-1000, -1000, 1000, 1000));
        new ControlPanel(result);

        // Make the tab with the contour plots
        TablePanel charts = new TablePanel(4, 3);
        result.getPanel().getTabbedPane().addTab("statistics", charts);

        // Make the four contour plots
        ContourPlot cp;

        for (int laneIndex = 0; laneIndex <= 1; laneIndex++)
        {
            final String laneName = String.format(" lane %d", laneIndex + 1);
            cp =
                    new DensityContourPlot("DensityPlot " + model.carFollowingModel.getLongName() + " lane "
                            + laneIndex, model.getMinimumDistance(), model.lanes[laneIndex].getLength());
            cp.setTitle("Density Contour Graph");
            cp.setExtendedState(Frame.MAXIMIZED_BOTH);
            model.getContourPlots().get(laneIndex).add(cp);
            charts.setCell(cp.getContentPane(), 2 * laneIndex, 0);

            cp =
                    new SpeedContourPlot("SpeedPlot " + model.carFollowingModel.getLongName() + laneName,
                            model.getMinimumDistance(), model.lanes[laneIndex].getLength());
            cp.setTitle("Speed Contour Graph");
            model.getContourPlots().get(laneIndex).add(cp);
            charts.setCell(cp.getContentPane(), 2 * laneIndex + 1, 0);

            cp =
                    new FlowContourPlot("FlowPlot " + model.carFollowingModel.getLongName() + laneName,
                            model.getMinimumDistance(), model.lanes[laneIndex].getLength());
            cp.setTitle("FLow Contour Graph");
            model.getContourPlots().get(laneIndex).add(cp);
            charts.setCell(cp.getContentPane(), 2 * laneIndex, 1);

            cp =
                    new AccelerationContourPlot("AccelerationPlot " + model.carFollowingModel.getLongName() + laneName,
                            model.getMinimumDistance(), model.lanes[laneIndex].getLength());
            cp.setTitle("Acceleration Contour Graph");
            model.getContourPlots().get(laneIndex).add(cp);
            charts.setCell(cp.getContentPane(), 2 * laneIndex + 1, 1);

            TrajectoryPlot trajectoryPlot =
                    new TrajectoryPlot("TrajectoryPlot " + model.carFollowingModel.getLongName() + laneName,
                            new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), model.getMinimumDistance(),
                            model.lanes[laneIndex].getLength());
            trajectoryPlot.setTitle("Trajectories");
            charts.setCell(trajectoryPlot.getContentPane(), 1 + laneIndex, 2);
            model.getTrajectoryPlots().get(laneIndex).add(trajectoryPlot);
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

    /** The car following model, e.g. IDM Plus. */
    protected GTUFollowingModel carFollowingModel;

    /** The lane change model. */
    protected AbstractLaneChangeModel laneChangeModel;

    /** Cars in each lane. */
    ArrayList<ArrayList<AnimatedCar>> cars;

    /** Minimum distance. */
    private DoubleScalar.Rel<LengthUnit> minimumDistance = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);

    /** The Lanes that contains the simulated Cars. */
    Lane[] lanes;

    /** the speed limit. */
    DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);

    /** the contour plots. */
    private ArrayList<ArrayList<ContourPlot>> contourPlots = new ArrayList<ArrayList<ContourPlot>>();

    /** the trajectory plot. */
    private ArrayList<ArrayList<TrajectoryPlot>> trajectoryPlots = new ArrayList<ArrayList<TrajectoryPlot>>();

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        final int laneCount = 2;
        this.cars = new ArrayList<ArrayList<AnimatedCar>>(laneCount);
        for (int laneIndex = 0; laneIndex < laneCount; laneIndex++)
        {
            this.cars.add(new ArrayList<AnimatedCar>());
            this.trajectoryPlots.add(new ArrayList<TrajectoryPlot>());
            this.contourPlots.add(new ArrayList<ContourPlot>());
        }
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        double radius = 6000 / 2 / Math.PI;
        Node startEnd = new Node("Start/End", new Coordinate(radius, 0, 0));
        Coordinate[] intermediateCoordinates = new Coordinate[255];
        for (int i = 0; i < intermediateCoordinates.length; i++)
        {
            double angle = 2 * Math.PI * (1 + i) / (1 + intermediateCoordinates.length);
            intermediateCoordinates[i] = new Coordinate(radius * Math.cos(angle), radius * Math.sin(angle), 0);
        }
        try
        {
            GTUType<String> gtuType = new GTUType<String>("car");
            LaneType<String> laneType = new LaneType<String>("CarLane");
            laneType.addPermeability(gtuType);
            this.lanes =
                    LaneFactory.makeMultiLane("Circular Link with " + laneCount + " lanes", startEnd, startEnd,
                            intermediateCoordinates, laneCount, laneType, this.simulator);
            this.carFollowingModel =
                    new IDM(new DoubleScalar.Abs<AccelerationUnit>(1, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1,
                                    TimeUnit.SECOND), 1d);
            this.carFollowingModel =
                    new IDMPlus(new DoubleScalar.Abs<AccelerationUnit>(1, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                            new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1,
                                    TimeUnit.SECOND), 1d);
            this.laneChangeModel = new Egoistic();
            this.laneChangeModel = new Altruistic();
            // Put the (not very evenly spaced) cars on the track
            double headway = 40;
            for (int laneIndex = 0; laneIndex < this.lanes.length; laneIndex++)
            {
                double trackLength = this.lanes[laneIndex].getLength().getSI();
                for (double pos = 0; pos <= trackLength - headway; pos += headway)
                {
                    generateCar(new DoubleScalar.Rel<LengthUnit>(pos, LengthUnit.METER), laneIndex, gtuType);
                    if (pos > trackLength / 4 && pos < 3 * trackLength / 4)
                    {
                        generateCar(new DoubleScalar.Rel<LengthUnit>(pos + headway / 2, LengthUnit.METER), laneIndex,
                                gtuType);
                    }
                }
            }
            // Schedule regular updates of the graph
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(9.999, TimeUnit.SECOND), this, this,
                    "drawGraphs", null);
            checkOrdering(RoadSimulationModel.this.cars.get(0));
            checkOrdering(RoadSimulationModel.this.cars.get(1));
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
    protected final void addToPlots(final Car<?> car, int lane) throws RemoteException
    {
        for (ContourPlot contourPlot : this.contourPlots.get(lane))
        {
            contourPlot.addData(car);
        }
        for (TrajectoryPlot trajectoryPlot : this.trajectoryPlots.get(lane))
        {
            trajectoryPlot.addData(car);
        }
    }

    /**
     * Notify the contour plots that the underlying data has changed.
     */
    protected final void drawGraphs()
    {
        for (ArrayList<ContourPlot> laneContourPlots : this.contourPlots)
        {
            for (ContourPlot contourPlot : laneContourPlots)
            {
                contourPlot.reGraph();
            }
        }
        for (ArrayList<TrajectoryPlot> laneTrajectoryPlots : this.trajectoryPlots)
        {
            for (TrajectoryPlot trajectoryPlot : laneTrajectoryPlots)
            {
                trajectoryPlot.reGraph();
            }
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
     * @param gtuType GTUType&lt;String&gt;; the GTU type
     * @throws NamingException on ???
     */
    protected final void generateCar(DoubleScalar.Rel<LengthUnit> initialPosition, int laneIndex,
            GTUType<String> gtuType) throws NamingException
    {
        // System.out.println("GenerateCar " + (this.carsCreated + 1) + " initialPosition is " + initialPosition);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialPositions.put(this.lanes[laneIndex], initialPosition);
        try
        {
            IDMCar car =
                    new IDMCar(++this.carsCreated, gtuType, this.simulator, this.carFollowingModel, this.simulator
                            .getSimulatorTime().get(), initialPositions, initialSpeed);
            this.cars.get(laneIndex).add(car);
            new CarAnimation(car, this.simulator);
            // System.out.print(car + ": ");
            // System.out.println(car.getLocation());
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
     * @return contourPlots
     */
    public final ArrayList<ArrayList<ContourPlot>> getContourPlots()
    {
        return this.contourPlots;
    }

    /**
     * @return trajectoryPlots
     */
    public final ArrayList<ArrayList<TrajectoryPlot>> getTrajectoryPlots()
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

    /**
     * Inner class IDMCar.
     */
    protected class IDMCar extends AnimatedCar
    {
        /** */
        private static final long serialVersionUID = 20141030L;

        /**
         * Create a new IDMCar.
         * @param id integer; the id of the new IDMCar
         * @param gtuType GTUType&lt;String&gt;; the type of the GTU
         * @param simulator OTSDEVSSimulator; the simulator that runs the new IDMCar
         * @param carFollowingModel CarFollowingModel; the car following model of the new IDMCar
         * @param initialTime DoubleScalar.Abs&lt;TimeUnit&gt;; the time of first evaluation of the new IDMCar
         * @param initialLongitudinalPositions Map&lt;Lane, DoubleScalar.Rel&lt;LengthUnit&gt;&gt;; the initial lane
         *            positions of the new IDMCar
         * @param initialSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the initial speed of the new IDMCar
         * @throws NamingException on ???
         * @throws RemoteException on communication failure
         */
        public IDMCar(final int id, GTUType<String> gtuType, final OTSDEVSSimulatorInterface simulator,
                final GTUFollowingModel carFollowingModel, final DoubleScalar.Abs<TimeUnit> initialTime,
                final Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions,
                final DoubleScalar.Abs<SpeedUnit> initialSpeed) throws RemoteException, NamingException
        {
            super(id, gtuType, simulator, carFollowingModel, initialTime, initialLongitudinalPositions, initialSpeed);
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
            wrap();
            // System.out.println("move " + getId());
            if (this.getId() < 0)
            {
                return;
            }
            // FIXME: there should be a much easier way to obtain the leader; we should not have to maintain our own
            // list
            Lane lane = getLongitudinalPositions().keySet().iterator().next();
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
            DoubleScalar.Rel<LengthUnit> longitudinalPosition = positionOfFront(lane, when);
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
                Map<Lane, Rel<LengthUnit>> longitudinalPositions = getLongitudinalPositions();
                DoubleScalar.Rel<LengthUnit> oldPosition = longitudinalPositions.get(lane);
                double oldRatio = oldPosition.getSI() / lane.getLength().getSI();
                // Remove vehicle from it's current lane
                RoadSimulationModel.this.cars.get(laneIndex).remove(this);
                // Figure out where to insert it in the target lane
                laneIndex += lcmr.getLaneChange().equals(LateralDirectionality.LEFT) ? -1 : +1;
                ArrayList<AnimatedCar> carsInLane = RoadSimulationModel.this.cars.get(laneIndex);
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
            getSimulator().scheduleEventRel(new DoubleScalar.Rel<TimeUnit>(0.5, TimeUnit.SECOND), this, this, "move",
                    null);
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
            ArrayList<AnimatedCar> carsInLane = RoadSimulationModel.this.cars.get(laneIndex);
            int vehicleIndex = carsInLane.size() - 1;
            if (vehicleIndex < 0)
            {
                continue;
            }
            while (true)
            {
                AnimatedCar car = carsInLane.get(vehicleIndex);
                LinkLocation ll = null;
                try
                {
                    ll = car.positionOfFront(when);
                }
                catch (RemoteException exception)
                {
                    exception.printStackTrace();
                }
                if (ll.getFractionalLongitudinalPosition() >= 1)
                {
                    // Fix the RelativePositions
                    // It is wrong that we can modify it, but for now we'll make use of that mistake...
                    Map<Lane, Rel<LengthUnit>> relativePositions = car.getLongitudinalPositions();
                    double relativePosition = relativePositions.get(lane).getSI() / lane.getLength().getSI();
                    // System.out.println("Wrapping car " + car.getId() + " in lane " + laneIndex +
                    // " back to position 0");
                    relativePositions.clear();
                    relativePosition -= 1;
                    relativePositions.put(lane, new DoubleScalar.Rel<LengthUnit>(relativePosition, LengthUnit.METER));
                    carsInLane.remove(car);
                    carsInLane.add(0, car);
                    checkOrdering(carsInLane);
                    try
                    {
                        addToPlots(car, laneIndex);
                    }
                    catch (RemoteException exception)
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
    public int pivot(double relativePosition, ArrayList<AnimatedCar> carsInLane)
    {
        if (carsInLane.size() == 0)
        {
            return 0;
        }
        try
        {
            DoubleScalar.Abs<TimeUnit> when = carsInLane.get(0).getSimulator().getSimulatorTime().get();
            Lane lane = carsInLane.get(0).getLongitudinalPositions().keySet().iterator().next();
            double laneLength = lane.getLength().getSI();
            int result;
            for (result = 0; result < carsInLane.size(); result++)
            {
                AnimatedCar pivotCar = carsInLane.get(result);
                double pivotRelativePosition = pivotCar.positionOfFront(lane, when).getSI() / laneLength;
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
            AnimatedCar prototype = (AnimatedCar) result.get(result.size() - 1);
            Map<Lane, DoubleScalar.Rel<LengthUnit>> initialPositions =
                    new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
            DoubleScalar.Abs<TimeUnit> when = RoadSimulationModel.this.simulator.getSimulatorTime().get();
            double position = prototype.positionOfFront(lane, when).getSI();
            if (position > 0)
            {
                position -= laneLength;
            }
            initialPositions.put(lane, new DoubleScalar.Rel<LengthUnit>(position, LengthUnit.METER));
            result.add(
                    0,
                    new IDMCar(-10000 - prototype.getId(), (GTUType<String>) prototype.getGTUType(), prototype
                            .getSimulator(), prototype.getGTUFollowingModel(), when, initialPositions, prototype
                            .getLongitudinalVelocity()));
            // Add a wrapped copy of the first (now second) car at the end
            prototype = (AnimatedCar) result.get(1);
            position = prototype.positionOfFront(lane, when).getSI();
            if (position < laneLength)
            {
                position += laneLength;
            }
            initialPositions = new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
            initialPositions.put(lane, new DoubleScalar.Rel<LengthUnit>(position, LengthUnit.METER));
            result.add(new IDMCar(-20000 - prototype.getId(), (GTUType<String>) prototype.getGTUType(), prototype
                    .getSimulator(), prototype.getGTUFollowingModel(), when, initialPositions, prototype
                    .getLongitudinalVelocity()));
        }
        catch (RemoteException | NetworkException | NamingException exception)
        {
            exception.printStackTrace();
        }
        return result;
    }

    /**
     * Sanity checks.
     * @param list ArrayList&lt;AnimatedCar&gt;; the array of cars to check
     */
    public void checkOrdering(ArrayList<AnimatedCar> list)
    {
        if (list.size() == 0)
        {
            return;
        }
        try
        {
            AnimatedCar first = list.get(0);
            Lane lane = first.getLongitudinalPositions().keySet().iterator().next();
            DoubleScalar.Abs<TimeUnit> when = first.getSimulator().getSimulatorTime().get();
            double position = first.positionOfFront(lane, when).getSI();
            for (int rank = 1; rank < list.size(); rank++)
            {
                AnimatedCar other = list.get(rank);
                Lane otherLane = other.getLongitudinalPositions().keySet().iterator().next();
                if (lane != otherLane)
                {
                    printList(this.cars.indexOf(list));
                    stopSimulator(first.getSimulator(), "cars are not all in the same lane");
                }
                double otherPosition = other.positionOfFront(lane, when).getSI();
                if (otherPosition <= position)
                {
                    printList(this.cars.indexOf(list));
                    stopSimulator(first.getSimulator(), "cars are not correctly ordered: " + first
                            + " should be ahead of " + other);
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
        ArrayList<AnimatedCar> list = this.cars.get(laneIndex);
        for (int rank = 0; rank < list.size(); rank++)
        {
            AnimatedCar car = list.get(rank);
            try
            {
                double relativePosition =
                        car.positionOfFront(this.lanes[laneIndex], this.simulator.getSimulatorTime().get()).getSI()
                                / this.lanes[laneIndex].getLength().getSI();
                System.out.println(String.format("lane %d rank %2d relpos %7.5f: %s", laneIndex, rank,
                        relativePosition, car.toString()));
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
