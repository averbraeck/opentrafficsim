package org.opentrafficsim.demo.carFollowing;

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

import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
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
import org.opentrafficsim.graphs.TrajectoryPlot;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.perceptionold.LanePerceptionFull;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.AbstractLaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
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
import org.opentrafficsim.simulationengine.OTSSimulationException;
import org.opentrafficsim.simulationengine.properties.AbstractProperty;
import org.opentrafficsim.simulationengine.properties.ProbabilityDistributionProperty;
import org.opentrafficsim.simulationengine.properties.PropertyException;
import org.opentrafficsim.simulationengine.properties.SelectionProperty;

/**
 * Demonstrate the Trajectories plot.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 17 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Trajectories extends AbstractWrappableAnimation implements UNITS
{
    /** */
    private static final long serialVersionUID = 1L;
    
    /** The model. */
    private TrajectoriesModel model;

    /** Create a Trajectories simulation. */
    public Trajectories()
    {
        try
        {
            this.properties.add(new SelectionProperty("CarFollowingModel", "Car following model",
                "<html>The car following model determines "
                    + "the acceleration that a vehicle will make taking into account nearby vehicles, "
                    + "infrastructural restrictions (e.g. speed limit, curvature of the road) "
                    + "capabilities of the vehicle and personality of the driver.</html>", new String[]{"IDM", "IDM+"},
                1, false, 10));
            this.properties.add(new ProbabilityDistributionProperty("TrafficComposition", "Traffic composition",
                "<html>Mix of passenger cars and trucks</html>", new String[]{"passenger car", "truck"}, new Double[]{
                    0.8, 0.2}, false, 9));
        }
        catch (PropertyException exception)
        {
            exception.printStackTrace();
        }
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
     * @throws SimRuntimeException on ???
     */
    public static void main(final String[] args) throws SimRuntimeException
    {
        // Create the simulation and wrap its panel in a JFrame. It does not get much easier/shorter than this...
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    Trajectories trajectories = new Trajectories();
                    trajectories.buildAnimator(new Time(0.0, SECOND), new Duration(0.0, SECOND), new Duration(
                        3600.0, SECOND), trajectories.getProperties(), null, true);
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
    protected final Rectangle2D.Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(0, -100, 5000, 200);
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer)
    {
        this.model = new TrajectoriesModel(this.savedUserModifiedProperties, colorer);
        return this.model;
    }

    /** {@inheritDoc} */
    @Override
    protected final JPanel makeCharts() throws OTSSimulationException
    {
        TablePanel charts = new TablePanel(1, 1);
        Duration sampleInterval = new Duration(0.5, SECOND);
        List<Lane> path = new ArrayList<Lane>();
        path.add(this.model.getLane());
        TrajectoryPlot tp = new TrajectoryPlot("Trajectory Plot", sampleInterval, path);
        tp.setTitle("Density Contour Graph");
        tp.setExtendedState(Frame.MAXIMIZED_BOTH);
        this.model.setTrajectoryPlot(tp);
        charts.setCell(tp.getContentPane(), 0, 0);
        return charts;
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "Trajectory plot";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "<html><H1>Trajectories</H1>"
            + "Simulation of a single lane road of 5 km length. Vechicles are generated at a constant rate of "
            + "1500 veh/hour. At time 300s a blockade is inserted at position 4km; this blockade is removed at time "
            + "420s. This blockade simulates a bridge opening.<br>"
            + "The blockade causes a traffic jam that slowly dissolves after the blockade is removed.<br>"
            + "Output is a Trajectory plots.</html>";
    }

}

/**
 * Simulate a single lane road of 5 km length. Vehicles are generated at a constant rate of 1500 veh/hour. At time 300s a
 * blockade is inserted at position 4 km; this blockade is removed at time 500s. The used car following algorithm is IDM+ <a
 * href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with Relaxation and
 * Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>. <br>
 * Output is a trajectory plot with simulation time along the horizontal axis and distance along the road along the vertical
 * axis.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version ug 1, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class TrajectoriesModel implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20140815L;

    /**The simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /** The network. */
    private OTSNetwork network = new OTSNetwork("network");

    /** The headway (inter-vehicle time). */
    private Duration headway;

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

    /** The lane change model. */
    private AbstractLaneChangeModel laneChangeModel = new Egoistic();

    /** The blocking car. */
    private LaneBasedIndividualGTU block = null;

    /** Minimum distance. */
    private Length minimumDistance = new Length(0, METER);

    /** Maximum distance. */
    private Length maximumDistance = new Length(5000, METER);

    /** The Lane containing the simulated Cars. */
    private Lane lane;

    /** The speed limit. */
    private Speed speedLimit = new Speed(100, KM_PER_HOUR);

    /** The trajectory plot. */
    private TrajectoryPlot trajectoryPlot;

    /** User settable properties. */
    private ArrayList<AbstractProperty<?>> properties = null;

    /** The random number generator used to decide what kind of GTU to generate. */
    private Random randomGenerator = new Random(12345);

    /** The GTUColorer for the generated vehicles. */
    private final GTUColorer gtuColorer;

    /**
     * @param properties ArrayList&lt;AbstractProperty&lt;?&gt;&gt;; the properties
     * @param gtuColorer the default and initial GTUColorer, e.g. a DefaultSwitchableTUColorer.
     */
    public TrajectoriesModel(final ArrayList<AbstractProperty<?>> properties, final GTUColorer gtuColorer)
    {
        this.properties = properties;
        this.gtuColorer = gtuColorer;
    }

    /** {@inheritDoc} */
    @Override
    public final
        void
        constructModel(
            final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> theSimulator)
            throws SimRuntimeException, RemoteException
    {
        this.simulator = (OTSDEVSSimulatorInterface) theSimulator;
        OTSNode from = new OTSNode("From", new OTSPoint3D(getMinimumDistance().getSI(), 0, 0));
        OTSNode to = new OTSNode("To", new OTSPoint3D(getMaximumDistance().getSI(), 0, 0));
        OTSNode end = new OTSNode("End", new OTSPoint3D(getMaximumDistance().getSI() + 50.0, 0, 0));
        Set<GTUType> compatibility = new HashSet<GTUType>();
        compatibility.add(this.gtuType);
        LaneType laneType = new LaneType("CarLane", compatibility);
        try
        {
            this.lane =
                LaneFactory.makeLane("Lane", from, to, null, laneType, this.speedLimit, this.simulator,
                    LongitudinalDirectionality.DIR_PLUS);
            CrossSectionLink endLink =
                LaneFactory.makeLink("endLink", to, end, null, LongitudinalDirectionality.DIR_PLUS);
            // No overtaking, single (sink) lane
            Lane sinkLane =
                new Lane(endLink, "sinkLane", this.lane.getLateralCenterPosition(1.0),
                    this.lane.getLateralCenterPosition(1.0), this.lane.getWidth(1.0), this.lane.getWidth(1.0),
                    laneType, LongitudinalDirectionality.DIR_PLUS, this.speedLimit, new OvertakingConditions.None());
            Sensor sensor = new SinkSensor(sinkLane, new Length(10.0, METER), this.simulator);
            sinkLane.addSensor(sensor, GTUType.ALL);
        }
        catch (NamingException | NetworkException | OTSGeometryException exception1)
        {
            exception1.printStackTrace();
        }

        for (AbstractProperty<?> p : this.properties)
        {
            if (p instanceof SelectionProperty)
            {
                SelectionProperty sp = (SelectionProperty) p;
                if ("CarFollowingModel".equals(sp.getKey()))
                {
                    String modelName = sp.getValue();
                    if (modelName.equals("IDM"))
                    {
                        this.carFollowingModelCars =
                            new IDMOld(new Acceleration(1, METER_PER_SECOND_2), new Acceleration(1.5, METER_PER_SECOND_2),
                                new Length(2, METER), new Duration(1, SECOND), 1d);
                        this.carFollowingModelTrucks =
                            new IDMOld(new Acceleration(0.5, METER_PER_SECOND_2),
                                new Acceleration(1.5, METER_PER_SECOND_2), new Length(2, METER), new Duration(1,
                                    SECOND), 1d);
                    }
                    else if (modelName.equals("IDM+"))
                    {
                        this.carFollowingModelCars =
                            new IDMPlusOld(new Acceleration(1, METER_PER_SECOND_2), new Acceleration(1.5,
                                METER_PER_SECOND_2), new Length(2, METER), new Duration(1, SECOND), 1d);
                        this.carFollowingModelTrucks =
                            new IDMPlusOld(new Acceleration(0.5, METER_PER_SECOND_2), new Acceleration(1.5,
                                METER_PER_SECOND_2), new Length(2, METER), new Duration(1, SECOND), 1d);
                    }
                    else
                    {
                        throw new Error("Car following model " + modelName + " not implemented");
                    }
                }
                else
                {
                    throw new Error("Unhandled SelectionProperty " + p.getKey());
                }
            }
            else if (p instanceof ProbabilityDistributionProperty)
            {
                ProbabilityDistributionProperty pdp = (ProbabilityDistributionProperty) p;
                String modelName = p.getKey();
                if (modelName.equals("TrafficComposition"))
                {
                    this.carProbability = pdp.getValue()[0];
                }
                else
                {
                    throw new Error("Unhandled ProbabilityDistributionProperty " + p.getKey());
                }
            }
            else
            {
                throw new Error("Unhandled property: " + p);
            }
        }

        // 1500 [vehicles / hour] == 2.4s headway
        this.headway = new Duration(3600.0 / 1500.0, SECOND);

        try
        {
            // Schedule creation of the first car (this will re-schedule itself one headway later, etc.).
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, SECOND), this, this, "generateCar",
                null);
            // Create a block at t = 5 minutes
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(300, SECOND), this, this, "createBlock",
                null);
            // Remove the block at t = 7 minutes
            this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(420, SECOND), this, this, "removeBlock",
                null);
            // Schedule regular updates of the graph
            for (int t = 1; t <= 1800; t++)
            {
                this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(t - 0.001, SECOND), this, this,
                    "drawGraph", null);
            }
        }
        catch (SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Set up the block.
     * @throws NamingException on error during adding of animation handler
     * @throws SimRuntimeException on ???
     * @throws NetworkException on network inconsistency
     * @throws GTUException if creation of the GTU fails
     * @throws OTSGeometryException when the initial position is not on the cecnter line of the lane
     */
    protected final void createBlock() throws NamingException, SimRuntimeException, NetworkException, GTUException,
        OTSGeometryException
    {
        Length initialPosition = new Length(4000, METER);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        initialPositions.add(new DirectedLanePosition(this.getLane(), initialPosition, GTUDirectionality.DIR_PLUS));
        BehavioralCharacteristics behavioralCharacteristics = DefaultsFactory.getDefaultBehavioralCharacteristics();
        //LaneBasedBehavioralCharacteristics drivingCharacteristics =
        //    new LaneBasedBehavioralCharacteristics(this.carFollowingModelCars, this.laneChangeModel);
        LaneBasedStrategicalPlanner strategicalPlanner =
            new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics,
                new LaneBasedGTUFollowingTacticalPlanner(this.carFollowingModelCars));
        this.block =
            new LaneBasedIndividualGTU("999999", this.gtuType, initialPositions, new Speed(0.0, KM_PER_HOUR),
                new Length(4, METER), new Length(1.8, METER), new Speed(0.0, KM_PER_HOUR), this.simulator,
                strategicalPlanner, new LanePerceptionFull(), DefaultCarAnimation.class, this.gtuColorer, this.network);
    }

    /**
     * Remove the block.
     */
    protected final void removeBlock()
    {
        this.block.destroy();
        this.block = null;
    }

    /**
     * Generate cars at a fixed rate (implemented by re-scheduling this method).
     */
    protected final void generateCar()
    {
        boolean generateTruck = this.randomGenerator.nextDouble() > this.carProbability;
        Length initialPosition = new Length(0, METER);
        Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
        try
        {
            initialPositions.add(new DirectedLanePosition(this.getLane(), initialPosition, GTUDirectionality.DIR_PLUS));
            Speed initialSpeed = new Speed(100, KM_PER_HOUR);
            Length vehicleLength = new Length(generateTruck ? 15 : 4, METER);
            GTUFollowingModelOld gtuFollowingModel =
                generateTruck ? this.carFollowingModelTrucks : this.carFollowingModelCars;
            if (null == gtuFollowingModel)
            {
                throw new Error("gtuFollowingModel is null");
            }
            BehavioralCharacteristics behavioralCharacteristics = DefaultsFactory.getDefaultBehavioralCharacteristics();
            //LaneBasedBehavioralCharacteristics drivingCharacteristics =
            //    new LaneBasedBehavioralCharacteristics(gtuFollowingModel, this.laneChangeModel);
            LaneBasedStrategicalPlanner strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics, 
                    new LaneBasedGTUFollowingTacticalPlanner(gtuFollowingModel));
            new LaneBasedIndividualGTU("" + (++this.carsCreated), this.gtuType, initialPositions, initialSpeed,
                vehicleLength, new Length(1.8, METER), new Speed(200, KM_PER_HOUR), this.simulator,
                strategicalPlanner, new LanePerceptionFull(), DefaultCarAnimation.class, this.gtuColorer, this.network);
            // Re-schedule this method after headway seconds
            this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
        }
        catch (SimRuntimeException | NamingException | NetworkException | GTUException | OTSGeometryException exception)
        {
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<DoubleScalar.Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble>
        getSimulator() throws RemoteException
    {
        return this.simulator;
    }

    /**
     * Notify the contour plots that the underlying data has changed.
     */
    protected final void drawGraph()
    {
        this.trajectoryPlot.reGraph();
    }

    /**
     * @return minimum distance of the simulation
     */
    public final Length getMinimumDistance()
    {
        return this.minimumDistance;
    }

    /**
     * @return maximum distance of the simulation
     */
    public final Length getMaximumDistance()
    {
        return this.maximumDistance;
    }

    /**
     * @param trajectoryPlot TrajectoryPlot
     */
    public final void setTrajectoryPlot(final TrajectoryPlot trajectoryPlot)
    {
        this.trajectoryPlot = trajectoryPlot;
    }

    /**
     * @return lane.
     */
    public Lane getLane()
    {
        return this.lane;
    }

}
