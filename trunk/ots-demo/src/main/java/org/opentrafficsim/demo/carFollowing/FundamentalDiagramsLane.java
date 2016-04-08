package org.opentrafficsim.demo.carFollowing;

import java.awt.Frame;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.ArrayList;
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
import org.opentrafficsim.core.gtu.drivercharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.graphs.FundamentalDiagramLane;
import org.opentrafficsim.road.gtu.animation.DefaultCarAnimation;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
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
 * Demonstrate the FundamentalDiagram plot.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version 17 dec. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FundamentalDiagramsLane extends AbstractWrappableAnimation implements UNITS
{
    /** The model. */
    private FundamentalDiagramLanePlotsModel model;

    /** Create a FundamentalDiagrams simulation. */
    public FundamentalDiagramsLane()
    {
        try
        {
            this.properties.add(new SelectionProperty("Car following model",
                "<html>The car following model determines "
                    + "the acceleration that a vehicle will make taking into account nearby vehicles, "
                    + "infrastructural restrictions (e.g. speed limit, curvature of the road) "
                    + "capabilities of the vehicle and personality of the driver.</html>", new String[]{"IDM", "IDM+"},
                1, false, 500));
            this.properties.add(new ProbabilityDistributionProperty("Traffic composition",
                "<html>Mix of passenger cars and trucks</html>", new String[]{"passenger car", "truck"}, new Double[]{
                    0.8, 0.2}, false, 10));
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
                    FundamentalDiagramsLane fundamentalDiagramsLane = new FundamentalDiagramsLane();
                    fundamentalDiagramsLane.buildAnimator(new Time.Abs(0.0, SECOND), new Time.Rel(0.0, SECOND),
                        new Time.Rel(3600.0, SECOND), fundamentalDiagramsLane.getProperties(), null, true);
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException exception)
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
        this.model = new FundamentalDiagramLanePlotsModel(this.savedUserModifiedProperties, colorer);
        return this.model;
    }

    /** {@inheritDoc} */
    @Override
    protected final Rectangle2D.Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(0, -100, 5000, 200);
    }

    /** {@inheritDoc} */
    @Override
    protected final JPanel makeCharts() throws OTSSimulationException
    {
        final int panelsPerRow = 3;
        TablePanel charts = new TablePanel(3, panelsPerRow);
        for (int plotNumber = 0; plotNumber < 9; plotNumber++)
        {
            FundamentalDiagramLane fd;
            try
            {
                Lane lane = this.model.getLane(plotNumber);
                int xs = (int) lane.getParentLink().getStartNode().getPoint().x;
                int xe = (int) lane.getParentLink().getEndNode().getPoint().x;
                fd =
                    new FundamentalDiagramLane("Fundamental Diagram for [" + xs + ", " + xe + "] m", new Time.Rel(1.0,
                        SECOND), lane, (OTSDEVSSimulatorInterface) this.model.getSimulator());
                fd.setTitle("Fundamental Diagram Graph");
                fd.setExtendedState(Frame.MAXIMIZED_BOTH);
                this.model.getFundamentalDiagrams().add(fd);
                charts.setCell(fd.getContentPane(), plotNumber / panelsPerRow, plotNumber % panelsPerRow);
            }
            catch (NetworkException | RemoteException | SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
        }
        return charts;
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "Fundamental Diagrams";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "<html><h1>Fundamental Diagram Plots</H1>"
            + "Simulation of a single lane road of 5 km length. Vechicles are generated at a constant rate of "
            + "1500 veh/hour. At time 300s a blockade is inserted at position 4km; this blockade is removed at time "
            + "500s. This blockade simulates a bridge opening.<br>"
            + "The blockade causes a traffic jam that slowly dissolves after the blockade is removed.<br>"
            + "Output is a set of Diagrams that plot observed density, flow and speed plots against each other.</html>";
    }

    /**
     * Simulate a single lane road of 5 km length. Vehicles are generated at a constant rate of 1500 veh/hour. At time 300s a
     * blockade is inserted at position 4 km; this blockade is removed at time 500s. The used car following algorithm is IDM+ <a
     * href="http://opentrafficsim.org/downloads/MOTUS%20reference.pdf"><i>Integrated Lane Change Model with Relaxation and
     * Synchronization</i>, by Wouter J. Schakel, Victor L. Knoop and Bart van Arem, 2012</a>. <br>
     * Output is a set of FundamentalDiagram plots for various point along the lane.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
     * initial version ug 1, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class FundamentalDiagramLanePlotsModel implements OTSModelInterface, UNITS
    {
        /** */
        private static final long serialVersionUID = 20140820L;

        /** The network. */
        private OTSNetwork network = new OTSNetwork("network");

        /** The simulator. */
        private OTSDEVSSimulatorInterface simulator;

        /** The headway (inter-vehicle time). */
        private Time.Rel headway;

        /** Number of cars created. */
        private int carsCreated = 0;

        /** Type of all GTUs. */
        private GTUType gtuType = GTUType.makeGTUType("Car");

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

        /** Starting x-position. */
        private Length.Rel startX = new Length.Rel(0, METER);

        /** Length per lane. */
        private Length.Rel laneLength = new Length.Rel(500, METER);

        /** The Lanes containing the simulated Cars. */
        private List<Lane> lanes = new ArrayList<>();

        /** The speed limit. */
        private Speed speedLimit = new Speed(100, KM_PER_HOUR);

        /** The fundamental diagram plots. */
        private ArrayList<FundamentalDiagramLane> fundamentalDiagramsLane = new ArrayList<>();

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
        public FundamentalDiagramLanePlotsModel(final ArrayList<AbstractProperty<?>> properties,
            final GTUColorer gtuColorer)
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
            try
            {
                LaneType laneType = new LaneType("CarLane");
                laneType.addCompatibility(this.gtuType);
                OTSNode node = new OTSNode("Node 0", new OTSPoint3D(this.startX.getSI(), 0, 0));
                for (int laneNr = 0; laneNr < 10; laneNr++)
                {
                    OTSNode next =
                        new OTSNode("Node " + (laneNr + 1),
                            new OTSPoint3D(node.getPoint().x + this.laneLength.si, 0, 0));
                    Lane lane =
                        LaneFactory.makeLane("Lane", node, next, null, laneType, this.speedLimit, this.simulator,
                            LongitudinalDirectionality.DIR_PLUS);
                    this.lanes.add(lane);
                    node = next;
                }
                // create SinkLane
                OTSNode end = new OTSNode("End", new OTSPoint3D(node.getPoint().x + 50.0, 0, 0));
                CrossSectionLink endLink =
                    LaneFactory.makeLink("endLink", node, end, null, LongitudinalDirectionality.DIR_PLUS);
                int last = this.lanes.size() - 1;
                Lane sinkLane =
                    new Lane(endLink, "sinkLane", this.lanes.get(last).getLateralCenterPosition(1.0), this.lanes.get(
                        last).getLateralCenterPosition(1.0), this.lanes.get(last).getWidth(1.0), this.lanes.get(last)
                        .getWidth(1.0), laneType, LongitudinalDirectionality.DIR_PLUS, this.speedLimit,
                        new OvertakingConditions.None());
                Sensor sensor = new SinkSensor(sinkLane, new Length.Rel(10.0, METER), this.simulator);
                sinkLane.addSensor(sensor, GTUType.ALL);
            }
            catch (NamingException | NetworkException | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }

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
                                new IDMOld(new Acceleration(1, METER_PER_SECOND_2), new Acceleration(1.5,
                                    METER_PER_SECOND_2), new Length.Rel(2, METER), new Time.Rel(1, SECOND), 1d);
                            this.carFollowingModelTrucks =
                                new IDMOld(new Acceleration(0.5, METER_PER_SECOND_2), new Acceleration(1.5,
                                    METER_PER_SECOND_2), new Length.Rel(2, METER), new Time.Rel(1, SECOND), 1d);
                        }
                        else if (modelName.equals("IDM+"))
                        {
                            this.carFollowingModelCars =
                                new IDMPlusOld(new Acceleration(1, METER_PER_SECOND_2), new Acceleration(1.5,
                                    METER_PER_SECOND_2), new Length.Rel(2, METER), new Time.Rel(1, SECOND), 1d);
                            this.carFollowingModelTrucks =
                                new IDMPlusOld(new Acceleration(0.5, METER_PER_SECOND_2), new Acceleration(1.5,
                                    METER_PER_SECOND_2), new Length.Rel(2, METER), new Time.Rel(1, SECOND), 1d);
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
                else
                {
                    throw new Error("Unhandled property: " + p);
                }
            }

            // 1500 [veh / hour] == 2.4s headway
            this.headway = new Time.Rel(3600.0 / 1500.0, SECOND);

            try
            {
                // Schedule creation of the first car (this will re-schedule itself one headway later, etc.).
                this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(0.0, SECOND), this, this, "generateCar",
                    null);
                // Create a block at t = 5 minutes
                this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(1000, SECOND), this, this,
                    "createBlock", null);
                // Remove the block at t = 7 minutes
                this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(1200, SECOND), this, this,
                    "removeBlock", null);
                // Schedule regular updates of the graph
                for (int t = 1; t <= this.simulator.getReplication().getTreatment().getRunLength().si / 25; t++)
                {
                    this.simulator.scheduleEventAbs(new DoubleScalar.Abs<TimeUnit>(25 * t - 0.001, SECOND), this, this,
                        "drawGraphs", null);
                }
            }
            catch (SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
        }

        /**
         * Set up the block in the last lane of the list.
         * @throws RemoteException on communications failure
         */
        protected final void createBlock() throws RemoteException
        {
            Length.Rel initialPosition = new Length.Rel(200, METER);
            Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
            try
            {
                initialPositions.add(new DirectedLanePosition(this.lanes.get(this.lanes.size() - 1), initialPosition,
                    GTUDirectionality.DIR_PLUS));
                BehavioralCharacteristics behavioralCharacteristics = new BehavioralCharacteristics();
                //LaneBasedBehavioralCharacteristics drivingCharacteristics =
                //    new LaneBasedBehavioralCharacteristics(this.carFollowingModelCars, this.laneChangeModel);
                LaneBasedStrategicalPlanner strategicalPlanner =
                    new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics,
                        new LaneBasedGTUFollowingTacticalPlanner(this.carFollowingModelCars));
                this.block =
                    new LaneBasedIndividualGTU("999999", this.gtuType, initialPositions, new Speed(0.0, KM_PER_HOUR),
                        new Length.Rel(4, METER), new Length.Rel(1.8, METER), new Speed(0.0, KM_PER_HOUR),
                        this.simulator, strategicalPlanner, new LanePerceptionFull(), DefaultCarAnimation.class,
                        this.gtuColorer, this.network);
            }
            catch (SimRuntimeException | NamingException | NetworkException | GTUException | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }
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
            Length.Rel initialPosition = new Length.Rel(0, METER);
            Speed initialSpeed = new Speed(100, KM_PER_HOUR);
            Set<DirectedLanePosition> initialPositions = new LinkedHashSet<>(1);
            try
            {
                initialPositions.add(new DirectedLanePosition(this.lanes.get(0), initialPosition,
                    GTUDirectionality.DIR_PLUS));
                Length.Rel vehicleLength = new Length.Rel(generateTruck ? 15 : 4, METER);
                GTUFollowingModelOld gtuFollowingModel =
                    generateTruck ? this.carFollowingModelTrucks : this.carFollowingModelCars;
                if (null == gtuFollowingModel)
                {
                    throw new Error("gtuFollowingModel is null");
                }
                BehavioralCharacteristics behavioralCharacteristics = new BehavioralCharacteristics();
                //LaneBasedBehavioralCharacteristics drivingCharacteristics =
                //    new LaneBasedBehavioralCharacteristics(gtuFollowingModel, this.laneChangeModel);
                LaneBasedStrategicalPlanner strategicalPlanner =
                    new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics,
                        new LaneBasedGTUFollowingTacticalPlanner(gtuFollowingModel));
                new LaneBasedIndividualGTU("" + (++this.carsCreated), this.gtuType, initialPositions, initialSpeed,
                    vehicleLength, new Length.Rel(1.8, METER), new Speed(200, KM_PER_HOUR), this.simulator,
                    strategicalPlanner, new LanePerceptionFull(), DefaultCarAnimation.class, this.gtuColorer,
                    this.network);
                this.simulator.scheduleEventRel(this.headway, this, this, "generateCar", null);
            }
            catch (SimRuntimeException | NamingException | NetworkException | GTUException | OTSGeometryException exception)
            {
                exception.printStackTrace();
            }
        }

        /**
     * 
     */
        protected final void drawGraphs()
        {
            // Notify the Fundamental Diagram plots that the underlying data has changed
            for (FundamentalDiagramLane fd : this.fundamentalDiagramsLane)
            {
                fd.reGraph();
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
         * @return fundamentalDiagramPlots
         */
        public final ArrayList<FundamentalDiagramLane> getFundamentalDiagrams()
        {
            return this.fundamentalDiagramsLane;
        }

        /**
         * @param laneNr the lane in the list.
         * @return lane.
         */
        public Lane getLane(final int laneNr)
        {
            return this.lanes.get(laneNr);
        }
    }
}
